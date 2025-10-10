package guru.nicks.cucumber;

import guru.nicks.cucumber.statemachine.TestOrderRepository;
import guru.nicks.cucumber.statemachine.TestOrderService;
import guru.nicks.cucumber.statemachine.action.TestOrderErrorHandlerAction;
import guru.nicks.cucumber.statemachine.action.TestOrderRejectRefundAction;
import guru.nicks.cucumber.statemachine.config.TestOrderStateMachineConfig;
import guru.nicks.cucumber.statemachine.world.TestOrderWorld;
import guru.nicks.cucumber.world.TextWorld;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Initializes Spring Context shared by all scenarios. Mocking is done inside step definition classes to let each
 * scenario program a different behavior. However, purely default mocks can be declared here (using annotations), but
 * remember to not alter their behavior in step classes.
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = {
        // scenario-scoped states
        TestOrderWorld.class, TextWorld.class,

        TestOrderStateMachineConfig.class, TestOrderErrorHandlerAction.class, TestOrderRejectRefundAction.class,
        TestOrderService.class, TestOrderRepository.class
})
public class CucumberBootstrap {
}
