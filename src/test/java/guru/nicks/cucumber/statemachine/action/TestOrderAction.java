package guru.nicks.cucumber.statemachine.action;

import guru.nicks.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.cucumber.statemachine.domain.TestOrderState;

import org.springframework.statemachine.action.Action;

/**
 * Base interface for Order State Machine actions.
 */
public interface TestOrderAction extends Action<TestOrderState, TestOrderEvent> {
}
