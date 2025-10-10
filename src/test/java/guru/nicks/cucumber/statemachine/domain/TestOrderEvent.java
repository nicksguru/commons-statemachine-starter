package guru.nicks.cucumber.statemachine.domain;

/**
 * Events that cause transitions between order states.
 *
 * @see TestOrderState
 */
public enum TestOrderEvent {

    CANCEL,
    REQUEST_REFUND,
    APPROVE_REFUND,
    REJECT_REFUND,
    PAY,
    DELIVER

}
