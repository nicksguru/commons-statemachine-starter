package guru.nicks.commons.cucumber.statemachine.action;

import guru.nicks.commons.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.commons.cucumber.statemachine.domain.TestOrderState;

import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

/**
 * Throws exception on any call.
 */
@Component
public class TestOrderRejectRefundAction implements TestOrderAction {

    @Override
    public void execute(StateContext<TestOrderState, TestOrderEvent> context) {
        throw new IllegalArgumentException("Artificial error (to test refund rejection) during transition to " +
                context.getTransition().getTarget().getId());
    }

}
