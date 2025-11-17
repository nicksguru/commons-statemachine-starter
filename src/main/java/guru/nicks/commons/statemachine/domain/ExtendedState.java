package guru.nicks.commons.statemachine.domain;

import jakarta.annotation.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;

import java.lang.invoke.MethodHandles;

/**
 * State machine context variables that can exist in {@link org.springframework.statemachine.ExtendedState}. This
 * interface is supposed to be implemented by {@link Enum}s.
 *
 * @see #getValueClass()
 */
public interface ExtendedState {

    /**
     * Must be managed by a listener as follows: cleared before entering a new state, set on state machine error (caused
     * by a transition-bound action).
     */
    String LAST_EXCEPTION_KEY = "_lastException";

    /**
     * Reads last exception ({@value #LAST_EXCEPTION_KEY}) from the given state machine's extended state.
     *
     * @param stateMachine state machine
     * @return last exception; can be {@code null}
     */
    @Nullable
    static Exception readLastExceptionFromStateMachine(StateMachine<?, ?> stateMachine) {
        return stateMachine
                .getExtendedState()
                .get(LAST_EXCEPTION_KEY, Exception.class);
    }

    /**
     * Saves last exception ({@value #LAST_EXCEPTION_KEY}) in the given state machine's extended state.
     *
     * @param stateMachine state machine
     * @param e            exception to save; can be {@code null}, in which case the last exception is cleared
     */
    static void saveLastExceptionInStateMachine(StateMachine<?, ?> stateMachine, @Nullable Exception e) {
        if (e == null) {
            stateMachine.getExtendedState()
                    .getVariables()
                    .remove(LAST_EXCEPTION_KEY);
        } else {
            stateMachine.getExtendedState()
                    .getVariables()
                    .put(LAST_EXCEPTION_KEY, e);
            LoggerFactory
                    .getLogger(MethodHandles.lookup().lookupClass())
                    .error("Saved last exception in state machine '{}': {}", stateMachine.getId(), e.getMessage(), e);
        }
    }

    Class<?> getValueClass();

    /**
     * Reads variable from the given state machine's {@link org.springframework.statemachine.ExtendedState}.
     *
     * @param stateMachine state machine
     * @param clazz        value class
     * @param <T>          value type
     * @return value - never {@code null} (see class-level comment for details)
     */
    @Nullable
    default <T> T readFromStateMachine(StateMachine<?, ?> stateMachine, Class<T> clazz) {
        return stateMachine
                .getExtendedState()
                .get(this, clazz);
    }

    /**
     * Saves variable in the given state machine's {@link org.springframework.statemachine.ExtendedState}.
     *
     * @param stateMachine state machine
     * @param value        if {@code null}, {@link #removeFromStateMachine(StateMachine)} is called instead
     */
    default void saveInStateMachine(StateMachine<?, ?> stateMachine, @Nullable Object value) {
        // in case nulls aren't permitted by the underlying map, remove the value explicitly
        if (value == null) {
            removeFromStateMachine(stateMachine);
            return;
        }

        if (!getValueClass().isInstance(value)) {
            throw new IllegalArgumentException("Value must be of class [" + getValueClass().getName() + "]");
        }

        stateMachine.getExtendedState()
                .getVariables()
                .put(this, value);
    }

    /**
     * Removes variable from the given state machine's {@link org.springframework.statemachine.ExtendedState}.
     *
     * @param stateMachine state machine
     */
    default void removeFromStateMachine(StateMachine<?, ?> stateMachine) {
        stateMachine.getExtendedState()
                .getVariables()
                .remove(this);
    }

}
