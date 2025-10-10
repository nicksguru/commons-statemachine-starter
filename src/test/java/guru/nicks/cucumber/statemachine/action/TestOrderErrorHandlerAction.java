package guru.nicks.cucumber.statemachine.action;

import guru.nicks.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.cucumber.statemachine.domain.TestOrderState;
import guru.nicks.statemachine.domain.ExtendedState;
import guru.nicks.statemachine.domain.StateMachineException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Stores exceptions raised by transition-bound actions in {@link org.springframework.statemachine.ExtendedState} under
 * the {@link ExtendedState#LAST_EXCEPTION_KEY} key (wrapped in {@link StateMachineException}). To do so, this action
 * <b>must be specified as error action for every transition</b>.
 */
@Component
@Slf4j
public class TestOrderErrorHandlerAction implements TestOrderAction {

    @Override
    public void execute(StateContext<TestOrderState, TestOrderEvent> context) {
        TestOrderState from = Optional.ofNullable(context.getTransition().getSource())
                .map(State::getId)
                .orElse(null);

        TestOrderState to = Optional.ofNullable(context.getTransition().getTarget())
                .map(State::getId)
                .orElse(null);

        var e = new RuntimeException(String.format(Locale.US,
                "Transition from state '%s' to state '%s' failed", from, to));
        ExtendedState.saveLastExceptionInStateMachine(context.getStateMachine(), e);
    }

}
