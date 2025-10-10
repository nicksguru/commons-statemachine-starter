package guru.nicks.cucumber.statemachine.config;

import guru.nicks.cucumber.statemachine.TestOrderRepository;
import guru.nicks.cucumber.statemachine.TestOrderStateMachineListener;
import guru.nicks.cucumber.statemachine.action.TestOrderErrorHandlerAction;
import guru.nicks.cucumber.statemachine.action.TestOrderRejectRefundAction;
import guru.nicks.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.cucumber.statemachine.domain.TestOrderState;
import guru.nicks.statemachine.StateMachineInMemoryRuntimePersister;
import guru.nicks.statemachine.StateMachineLoggingMonitor;
import guru.nicks.statemachine.action.NoOpAction;
import guru.nicks.statemachine.domain.ExtendedState;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory(name = "orderStateMachineFactory")
@RequiredArgsConstructor
public class TestOrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<TestOrderState, TestOrderEvent> {

    // DI
    private final TestOrderErrorHandlerAction errorHandlerAction;
    private final TestOrderRejectRefundAction rejectRefundAction;
    private final TestOrderRepository orderRepository;

    private final Action<TestOrderState, TestOrderEvent> noOpAction = new NoOpAction<>();

    /**
     * Configures listeners, persistence, and monitoring.
     *
     * @param config configurer
     * @throws Exception error from state machine
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<TestOrderState, TestOrderEvent> config) throws Exception {
        // @formatter:off
        config.withConfiguration()
                .listener(new TestOrderStateMachineListener(orderRepository))
                .and()
                .withVerifier()
                    .enabled(true)
                .and()
                .withPersistence()
                    .runtimePersister(orderStateMachineRuntimePersister())
                .and()
                .withMonitoring()
                    .monitor(new StateMachineLoggingMonitor<>());
        // @formatter:on
    }

    /**
     * Configures states,
     * <p>
     * WARNING: if end states are specified, state machine stops itself there and then fails to start. Under such
     * circumstances, it becomes impossible to find out its final state ({@link StateMachine#getState()} loops
     * infinitely). This isn't a problem if states are persisted in DB and can be read from there directly. Also, the
     * transition graph forbids transitions from such states anyway.
     *
     * @param states configurer
     * @throws Exception error from state machine
     * @see #configure(StateMachineTransitionConfigurer) transitions
     */
    @Override
    public void configure(StateMachineStateConfigurer<TestOrderState, TestOrderEvent> states) throws Exception {
        states.withStates()
                .states(EnumSet.allOf(TestOrderState.class))
                .initial(TestOrderState.NEW);
        //.end(TestOrderState.CANCELLED)
    }

    /**
     * Configures transitions between states. Passes {@link TestOrderErrorHandlerAction} as <b>every</b> action's error
     * handler in order to store exceptions in {@link ExtendedState#LAST_EXCEPTION_KEY}.
     *
     * @param transitions configurer
     * @throws Exception error from state machine
     * @see #configure(StateMachineStateConfigurer) states
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<TestOrderState, TestOrderEvent> transitions)
            throws Exception {
        // @formatter:off
        transitions
                .withExternal()
                    .source(TestOrderState.NEW)
                    .event(TestOrderEvent.CANCEL)
                    .target(TestOrderState.CANCELLED)
                    .action(noOpAction, errorHandlerAction)
                .and()
                .withExternal()
                    .source(TestOrderState.NEW)
                    .event(TestOrderEvent.PAY)
                    .target(TestOrderState.PAID)
                .and()
                .withExternal()
                    .source(TestOrderState.PAID)
                    .event(TestOrderEvent.DELIVER)
                    .target(TestOrderState.DELIVERED)
                    .action(noOpAction, errorHandlerAction)
                .and()
                .withExternal()
                    .source(TestOrderState.PAID)
                    .event(TestOrderEvent.REQUEST_REFUND)
                    .target(TestOrderState.REFUND_REQUESTED)
                    .action(noOpAction, errorHandlerAction)
                .and()
                .withExternal()
                    .source(TestOrderState.DELIVERED)
                    .event(TestOrderEvent.REQUEST_REFUND)
                    .target(TestOrderState.REFUND_REQUESTED)
                    .action(noOpAction, errorHandlerAction)
                .and()
                .withExternal()
                    .source(TestOrderState.REFUND_REQUESTED)
                    .event(TestOrderEvent.APPROVE_REFUND)
                    .target(TestOrderState.REFUNDED)
                    .action(noOpAction, errorHandlerAction)
                .and()
                .withExternal()
                    .source(TestOrderState.REFUND_REQUESTED)
                    .event(TestOrderEvent.REJECT_REFUND)
                    .target(TestOrderState.REFUND_REJECTED)
                    .action(rejectRefundAction, errorHandlerAction);
        // @formatter:on
    }

    /**
     * Creates a service which should be used to recover persistent state machine states in an abstract way (usually
     * with {@link StateMachineService#acquireStateMachine(String)}).
     * <p>
     * WARNING: state machines are <b>created on the fly</b> (in their initial state) if their persistent state was not
     * found.
     *
     * @param stateMachineFactory          state machine factory
     * @param stateMachineRuntimePersister context persister
     * @return service bean
     */
    @Bean
    public StateMachineService<TestOrderState, TestOrderEvent> orderStateMachineService(
            StateMachineFactory<TestOrderState, TestOrderEvent> stateMachineFactory,
            StateMachineRuntimePersister<TestOrderState, TestOrderEvent, String> stateMachineRuntimePersister) {
        return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
    }

    @Bean
    public StateMachineRuntimePersister<TestOrderState, TestOrderEvent, String> orderStateMachineRuntimePersister() {
        return new StateMachineInMemoryRuntimePersister<>();
    }

}
