package guru.nicks.cucumber.statemachine;

import guru.nicks.cucumber.statemachine.action.TestOrderErrorHandlerAction;
import guru.nicks.cucumber.statemachine.domain.TestOrderEntity;
import guru.nicks.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.cucumber.statemachine.domain.TestOrderExtendedState;
import guru.nicks.cucumber.statemachine.domain.TestOrderState;
import guru.nicks.statemachine.domain.ExtendedState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.Optional;
import java.util.UUID;

/**
 * Logs various events. Also updates order state in DB (via {@link TestOrderRepository}) upon each successful state
 * change.
 * <p>
 * NOTE: listener exceptions (no connection to DB, missing beans, etc.) do not affect the state machine behavior and
 * are not event logged anywhere.
 */
@RequiredArgsConstructor
@Slf4j
public class TestOrderStateMachineListener extends StateMachineListenerAdapter<TestOrderState, TestOrderEvent> {

    private final TestOrderRepository orderRepository;

    /**
     * This method is called both on errors in transition-bound actions and also when events just do not match the
     * current state (there's no transition specified). It's impossible to distinguish the first case from the second
     * one. For this reason, the only way to catch transition errors seems to specify the 2nd action - error handler
     * action.
     *
     * @param event event
     */
    @Override
    public void eventNotAccepted(Message<TestOrderEvent> event) {
        log.warn("Event not accepted (no matching transition / error in transition's action): {}", event);
    }

    @Override
    public void stateMachineStarted(StateMachine<TestOrderState, TestOrderEvent> stateMachine) {
        log.info("[{}] State machine started", stateMachine.getId());
    }

    @Override
    public void stateMachineStopped(StateMachine<TestOrderState, TestOrderEvent> stateMachine) {
        log.info("[{}] State machine stopped", stateMachine.getId());
    }

    /**
     * Resets {@link ExtendedState#LAST_EXCEPTION_KEY} at each transition start. To save transition errors in
     * {@link ExtendedState#LAST_EXCEPTION_KEY}, always specify the 2nd action - error handler - as
     * {@link TestOrderErrorHandlerAction}.
     */
    @Override
    public void stateContext(StateContext<TestOrderState, TestOrderEvent> stateContext) {
        switch (stateContext.getStage()) {
            case TRANSITION_START:
                ExtendedState.saveLastExceptionInStateMachine(stateContext.getStateMachine(), null);
                break;

            case EXTENDED_STATE_CHANGED:
                var e = ExtendedState.readLastExceptionFromStateMachine(stateContext.getStateMachine());

                if (e != null) {
                    log.error("[{}] Extended state has exception: {}", stateContext.getStateMachine().getId(),
                            e.getMessage(), e);
                }

                break;

            case STATE_CHANGED:
                // 'from' is null during state machine initialization
                TestOrderState from = Optional.ofNullable(stateContext.getSource())
                        .map(State::getId)
                        .orElse(null);

                TestOrderState to = Optional.ofNullable(stateContext.getTarget())
                        .map(State::getId)
                        .orElse(null);

                UUID orderId = stateContext.getExtendedState().get(TestOrderExtendedState.ORDER_ID, UUID.class);
                log.info("[{}] State changed: {} -> {}", stateContext.getStateMachine().getId(), from, to);

                // throws exception if no such order, but listener exceptions are not propagated or logged
                TestOrderEntity orderEntity = orderRepository.getById(orderId);
                orderEntity.setState(to);
                orderRepository.save(orderEntity);

                break;

            default:
                break;
        }
    }

}
