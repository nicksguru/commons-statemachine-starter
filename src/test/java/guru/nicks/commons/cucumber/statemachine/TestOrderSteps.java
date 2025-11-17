package guru.nicks.commons.cucumber.statemachine;

import guru.nicks.commons.cucumber.statemachine.domain.TestOrderEntity;
import guru.nicks.commons.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.commons.cucumber.statemachine.domain.TestOrderState;
import guru.nicks.commons.cucumber.statemachine.world.TestOrderWorld;
import guru.nicks.commons.cucumber.world.TextWorld;
import guru.nicks.commons.statemachine.domain.StateMachineException;

import guru.nidi.graphviz.engine.Format;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RequiredArgsConstructor
@Slf4j
public class TestOrderSteps {

    // DI
    private final TestOrderService testOrderService;
    private final TestOrderWorld testOrderWorld;
    private final TextWorld textWorld;

    @Before
    public void setup() {
        textWorld.setLastException(null);
    }

    // in tests: 'Then {orderState} is ...'
    @ParameterType(name = "orderState", value = ".+")
    public TestOrderState createOrderStateFromString(String str) {
        return TestOrderState.valueOf(str);
    }

    @When("create order")
    // for fetching lazy-loaded properties
    @Transactional
    public void create_order() {
        var orderEntity = TestOrderEntity.builder()
                .state(TestOrderState.NEW)
                .build();
        testOrderService.save(orderEntity);
        testOrderWorld.setOrderId(orderEntity.getId());
    }

    @When("cancel order")
    public void cancel_order() {
        sendOrderEvent(TestOrderEvent.CANCEL);
    }

    @When("pay order")
    public void pay_order() {
        sendOrderEvent(TestOrderEvent.PAY);
    }

    @When("deliver order")
    public void deliver_order() {
        sendOrderEvent(TestOrderEvent.DELIVER);
    }

    @When("request refund")
    public void request_refund() {
        sendOrderEvent(TestOrderEvent.REQUEST_REFUND);
    }

    @When("approve refund request")
    public void approve_refund() {
        sendOrderEvent(TestOrderEvent.APPROVE_REFUND);
    }

    @When("reject refund request")
    public void reject_refund() {
        sendOrderEvent(TestOrderEvent.REJECT_REFUND);
    }

    @Then("order state is {orderState}")
    public void order_state_is(TestOrderState expectedOrderState) {
        // check entity property set by TestOrderStateMachineListener
        TestOrderState orderState = testOrderService.getById(testOrderWorld.getOrderId()).getState();
        assertThat(orderState).isEqualTo(expectedOrderState);
    }

    @Then("exception from transition-bound action (or event doesn't match transition graph)")
    public void exception_from_state_machine() {
        assertThat(textWorld.getLastException()).isInstanceOf(StateMachineException.class);
    }

    // to be used with exception_from_transition_bound_action / no_exception_from_transition_bound_action
    private void sendOrderEvent(TestOrderEvent event) {
        textWorld.setLastException(catchThrowable(() ->
                testOrderService.processEventInStateMachine(testOrderWorld.getOrderId(), event)));
    }

    @When("state machine graph is rendered in {word} format")
    public void stateMachineGraphIsRenderedInFormat(String formatStr) {
        Format format = Format.valueOf(formatStr);
        Pair<ContentType, byte[]> graph = testOrderService.renderStateMachineGraph(format);

        assertThat(graph)
                .as("state machine graph")
                .isNotNull();

        assertThat(graph.getKey())
                .as("state machine graph content type")
                .isNotNull();

        assertThat(graph.getValue())
                .as("state machine graph content")
                .hasSizeGreaterThanOrEqualTo(1);
        textWorld.setOutput(new String(graph.getValue(), StandardCharsets.UTF_8));
    }

    @Then("rendered state machine graph contains {string}")
    public void renderedStateMachineGraphContains(String expected) {
        assertThat(textWorld.getOutput().getFirst())
                .as("rendered state machine graph")
                .contains(expected);
    }

}
