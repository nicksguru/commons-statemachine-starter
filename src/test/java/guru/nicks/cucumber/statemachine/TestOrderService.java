package guru.nicks.cucumber.statemachine;

import guru.nicks.cucumber.statemachine.domain.TestOrderEntity;
import guru.nicks.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.cucumber.statemachine.domain.TestOrderExtendedState;
import guru.nicks.cucumber.statemachine.domain.TestOrderState;
import guru.nicks.exception.http.NotFoundException;
import guru.nicks.statemachine.StateMachineAware;
import guru.nicks.statemachine.StateMachineGraphVisualizer;
import guru.nicks.statemachine.StateMachineStartCompletionListener;
import guru.nicks.utils.TransformUtils;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestOrderService implements
        StateMachineAware<TestOrderState, TestOrderEvent, TestOrderExtendedState, UUID>,
        StateMachineGraphVisualizer<TestOrderState, TestOrderEvent> {

    // DI
    private final StateMachineService<TestOrderState, TestOrderEvent> orderStateMachineService;
    @Getter(onMethod_ = @Override)
    private final StateMachineFactory<TestOrderState, TestOrderEvent> stateMachineFactory;
    private final TestOrderRepository repository;

    public TestOrderEntity getById(UUID id) {
        return repository.getById(id);
    }

    public TestOrderEntity save(TestOrderEntity orderEntity) {
        return repository.save(orderEntity);
    }

    /**
     * Because of the order existence check, the method is transactional.
     *
     * @param orderId order ID
     * @throws NotFoundException order not found
     */
    @Transactional
    @Nullable
    @Override
    public <T> T getWithStateMachine(UUID orderId,
            Function<StateMachine<TestOrderState, TestOrderEvent>, T> function) {
        if (!repository.existsById(orderId)) {
            throw new NotFoundException();
        }

        var stateMachine = waitForStateMachineStart(orderId);
        T result = function.apply(stateMachine);
        waitForStateMachineStop(orderId);
        return result;
    }

    @Transactional
    @Override
    public void runWithStateMachine(UUID orderId, Consumer<StateMachine<TestOrderState, TestOrderEvent>> consumer) {
        getWithStateMachine(orderId, TransformUtils.toFunction(consumer));
    }

    @Override
    public StateMachine<TestOrderState, TestOrderEvent> waitForStateMachineStart(UUID orderId) {
        var stateMachine = orderStateMachineService.acquireStateMachine(convertToStateMachineId(orderId), false);
        // ensure order ID is always there
        TestOrderExtendedState.ORDER_ID.saveInStateMachine(stateMachine, orderId);

        // Don't use resetStateMachineReactively() to initialize machine context - such action is not intercepted by the
        // persistence interceptor; context will not be persisted. Instead, set context first (above), then start.
        StateMachineStartCompletionListener.waitForStateMachineStart(stateMachine);
        return stateMachine;
    }

    @Override
    public void waitForStateMachineStop(UUID orderId) {
        orderStateMachineService.releaseStateMachine(convertToStateMachineId(orderId));
    }

    private String convertToStateMachineId(UUID orderId) {
        return "orderId:" + orderId;
    }

}
