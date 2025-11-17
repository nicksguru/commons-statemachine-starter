package guru.nicks.commons.cucumber.statemachine.domain;

/**
 * Order states.
 *
 * @see TestOrderEvent
 */
public enum TestOrderState {

    NEW,
    CANCELLED,
    DELIVERED,
    PAID,
    REFUND_REQUESTED,
    REFUNDED,
    REFUND_REJECTED

}
