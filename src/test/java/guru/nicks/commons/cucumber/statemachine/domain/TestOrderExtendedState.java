package guru.nicks.commons.cucumber.statemachine.domain;

import guru.nicks.commons.statemachine.domain.ExtendedState;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;

import java.util.UUID;

/**
 * State machine context variables that can exist in {@link org.springframework.statemachine.ExtendedState} for orders.
 *
 * @see #readFromStateMachine(StateMachine, Class)
 */
@Getter
@RequiredArgsConstructor
public enum TestOrderExtendedState implements ExtendedState {

    ORDER_ID(UUID.class);

    private final Class<?> valueClass;

}
