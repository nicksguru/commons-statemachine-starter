package guru.nicks.commons.statemachine;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.StateMachineInterceptor;

/**
 * Interceptor performing in-memory state machine context persistence. To be used for testing purposes only.
 */
public class StateMachineInMemoryRuntimePersister<S, E, T>
        extends AbstractPersistingStateMachineInterceptor<S, E, T>
        implements StateMachineRuntimePersister<S, E, T> {

    private final StateMachinePersist<S, E, Object> persist = new StateMachineInMemoryPersister<>();

    @Override
    public StateMachineInterceptor<S, E> getInterceptor() {
        return this;
    }

    @Override
    public void write(StateMachineContext<S, E> context, T contextObj) throws Exception {
        persist.write(context, contextObj);
    }

    @Override
    public StateMachineContext<S, E> read(T contextObj) throws Exception {
        return persist.read(contextObj);
    }

}
