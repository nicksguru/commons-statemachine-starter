package guru.nicks.commons.statemachine;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor performing in-memory state machine context persistence. To be used for testing purposes only.
 */
public class StateMachineInMemoryPersister<S, E> implements StateMachinePersist<S, E, Object> {

    private final Map<String, StateMachineContext<S, E>> storage = new ConcurrentHashMap<>();

    @Override
    public void write(StateMachineContext<S, E> context, Object machineId) {
        storage.put(machineId.toString(), context);
    }

    @Override
    public StateMachineContext<S, E> read(Object machineId) {
        return storage.get(machineId.toString());
    }

}
