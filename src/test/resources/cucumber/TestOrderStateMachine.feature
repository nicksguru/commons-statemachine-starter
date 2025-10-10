@orders #@disabled
Feature: Order State Machine

  Scenario: create order
    When create order
    Then order state is NEW

  Scenario: Cancel order
    When create order
    And cancel order
    Then order state is CANCELLED
    And no exception should be thrown

  Scenario: Cancel already cancelled order
    When create order
    And cancel order
    And cancel order
    Then order state is CANCELLED
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Pay order
    When create order
    And pay order
    Then order state is PAID
    And no exception should be thrown

  Scenario: Pay already paid order
    When create order
    And pay order
    And pay order
    Then order state is PAID
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Pay cancelled order
    When create order
    And cancel order
    And pay order
    Then order state is CANCELLED
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Deliver order
    When create order
    And pay order
    And deliver order
    Then order state is DELIVERED
    And no exception should be thrown

  Scenario: Deliver already delivered order
    When create order
    And pay order
    And deliver order
    And deliver order
    Then order state is DELIVERED
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Deliver unpaid order
    When create order
    And deliver order
    Then order state is NEW
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Request refund before delivery
    When create order
    And pay order
    And request refund
    Then order state is REFUND_REQUESTED
    And no exception should be thrown

  Scenario: Request refund after delivery
    When create order
    And pay order
    And deliver order
    And request refund
    Then order state is REFUND_REQUESTED
    And no exception should be thrown

  Scenario: Request refund of unpaid order
    When create order
    And request refund
    Then order state is NEW
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Request refund more than once
    When create order
    And pay order
    And request refund
    And request refund
    Then order state is REFUND_REQUESTED
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Approve refund
    When create order
    And pay order
    And request refund
    And approve refund request
    Then order state is REFUNDED
    And no exception should be thrown

  Scenario: Approve already approved refund (not allowed unless multiple approvals are required)
    When create order
    And pay order
    And request refund
    And approve refund request
    And approve refund request
    Then order state is REFUNDED
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Reject refund (exception expected from transition-bound action)
    When create order
    And pay order
    And request refund
    And reject refund request
    Then order state is REFUND_REQUESTED
    And exception from transition-bound action or event doesn't match transition graph

  Scenario: Render state machine graph in SVG format
    When state machine graph is rendered in SVG format
    Then rendered state machine graph contains "<svg "
    # state name (as a comment)
    And rendered state machine graph contains ">NEW<"
    # state name (as a comment)
    And rendered state machine graph contains ">PAID<"
    # transition from NEW to PAID (as a comment)
    And rendered state machine graph contains "<!-- NEW&#45;&gt;PAID -->"
