package guru.nicks.commons.statemachine;

import guru.nicks.commons.exception.http.ConflictException;
import guru.nicks.commons.exception.http.NotFoundException;
import guru.nicks.commons.statemachine.domain.ExtendedState;
import guru.nicks.commons.statemachine.domain.StateMachineException;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.service.StateMachineService;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static guru.nicks.commons.validation.dsl.ValiDsl.checkNotNull;

/**
 * Interacts with a state machine. For example, consider a CRUD service which also manages entity states.
 *
 * @param <S>  state type
 * @param <E>  event type
 * @param <P>  extended state property type - often {@link Enum} to avoid using raw {@link String}
 * @param <ID> entity ID type
 */
@SuppressWarnings("java:S119") // allow non-single-letter type names in generics
public interface StateMachineAware<S, E, P extends ExtendedState, ID> {

    default Logger getLog() {
        return LoggerFactory.getLogger(AopUtils.getTargetClass(this));
    }

    /**
     * Sends event to state machine (calls {@link #withStateMachine(Object, Function)} internally). Returns after the
     * event has been accepted/rejected (i.e. processed successfully, or rejected by the transition-bound action, or
     * rejected because there's no such path in the transition graph).
     * <p>
     * Event processing is, internally, asynchronous and non-transactional (transition-bound actions must start own
     * transactions if needed), but <b>this method is synchronous</b> - it waits for the event processing to complete.
     *
     * @param entityId ID of entity whose state is being managed
     * @param event    event related to the object being managed
     * @throws StateMachineException its cause is the exception thrown by the transition-bound action, or by the state
     *                               machine itself, or {@link ConflictException} if rejected by the transition graph
     */
    default void processEventInStateMachine(ID entityId, E event) {
        checkNotNull(entityId, "entityId");

        Exception e = withStateMachine(entityId, stateMachine -> {
            getLog().debug("[{}] Sending event {} to state machine (current state machine state: {})",
                    entityId, event, stateMachine.getState().getId());

            boolean eventDenied = stateMachine
                    .sendEvent(Mono.just(
                            MessageBuilder.withPayload(event).build()))
                    .blockLast()
                    .getResultType() == StateMachineEventResult.ResultType.DENIED;

            Exception lastException = ExtendedState.readLastExceptionFromStateMachine(stateMachine);

            // event denied, but there's no exception from a transition-bound action - this means no action was invoked
            // at all because the event didn't match the transition graph
            if (eventDenied && (lastException == null)) {
                lastException = new ConflictException("Event '" + event + "' not accepted in state '"
                        + stateMachine.getState().getId() + "'");
            }

            return lastException;
        });

        if (e != null) {
            throw new StateMachineException("Event processing failed in state machine: " + e.getMessage(), e);
        }
    }

    /**
     * Starts state machine for the given entity ID, calls the given function, and waits until the state machine is
     * stopped (i.e. all asynchronous listeners have completed - for example, updated the entity state in DB).
     * Transaction demarcation is up to subclasses.
     *
     * @param entityId ID of entity whose state is being managed (will be used to identify the state machine)
     * @param mapper   function to apply to the state machine, the flow is usually as follows:
     *                 <ul>
     *                  <li>if the entity does not exist in DB, throw a subclass of {@link NotFoundException}</li>
     *                  <li>call {@link #waitForStateMachineStart(Object)} (which succeeds for any entity ID, hence the
     *                      check above)</li>
     *                  <li>perform the business logic, such as {@link #processEventInStateMachine(Object, Object)}</li>
     *                  <li>call {@link #waitForStateMachineStop(Object)}</li>
     *                 </ul>
     * @param <T>      function result type
     * @return what {@code function} has returned
     */
    @Nullable
    <T> T withStateMachine(ID entityId, Function<StateMachine<S, E>, T> mapper);

    /**
     * Retrieves state from state machine.
     *
     * @param entityId ID of entity whose state is being managed
     * @return entity state
     */
    default S getStateFromStateMachine(ID entityId) {
        S state = withStateMachine(entityId, stateMachine -> stateMachine.getState().getId());
        return checkNotNull(state, "state");
    }

    /**
     * Retrieves property from state machine's extended state. This method is mainly for testing/debugging - it
     * retrieves state machine internals. Everything that needs to be exposed on the business level should be stored in
     * DB by transition-bound actions.
     *
     * @param entityId ID of entity whose state is being managed
     * @param property property to retrieve
     * @param clazz    property value class
     * @param <T>      property value type
     * @return property value
     */
    @Nullable
    default <T> T getExtendedStateFromStateMachine(ID entityId, P property, Class<T> clazz) {
        return withStateMachine(entityId, stateMachine -> property.readFromStateMachine(stateMachine, clazz));
    }

    /**
     * Calls {@link StateMachineService#acquireStateMachine(String, boolean)} which either returns the machine from its
     * in-memory cache, or restores it from a persistent state, or creates a new machine. Then, implementations MUST
     * wait until the (asynchronous) state machine is started.
     *
     * @param entityId ID of entity whose state is being managed
     * @return state machine
     */
    StateMachine<S, E> waitForStateMachineStart(ID entityId);

    /**
     * Calls {@link StateMachineService#releaseStateMachine(String)} which waits until the state machine is stopped.
     *
     * @param entityId ID of entity whose state is being managed
     */
    void waitForStateMachineStop(ID entityId);

}
