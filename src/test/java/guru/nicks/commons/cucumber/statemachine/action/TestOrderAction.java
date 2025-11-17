package guru.nicks.commons.cucumber.statemachine.action;

import guru.nicks.commons.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.commons.cucumber.statemachine.domain.TestOrderState;

import org.springframework.statemachine.action.Action;

/**
 * Base interface for Order State Machine actions.
 */
public interface TestOrderAction extends Action<TestOrderState, TestOrderEvent> {
}
