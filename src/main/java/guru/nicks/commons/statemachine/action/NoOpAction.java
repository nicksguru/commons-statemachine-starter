package guru.nicks.commons.statemachine.action;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * Does nothing. Needed to fill in the action placeholder in order to specify the 2nd action (error handler) in
 * {@link org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer#action(Action, Action)}.
 */
public class NoOpAction<S, E> implements Action<S, E> {

    @Override
    public void execute(StateContext<S, E> context) {
        // do nothing
    }

}
