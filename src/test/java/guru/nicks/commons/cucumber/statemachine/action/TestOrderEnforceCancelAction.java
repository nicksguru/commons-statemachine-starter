package guru.nicks.commons.cucumber.statemachine.action;

import guru.nicks.commons.cucumber.statemachine.domain.TestOrderEvent;
import guru.nicks.commons.cucumber.statemachine.domain.TestOrderState;

import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

/**
 * Sets state to {@link TestOrderState#CANCELLED}. DOESN'T WORK because of the following exception:
 * <pre>
 * org.hibernate.id.IdentifierGenerationException: Identifier of entity
 * 'org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine' must be manually assigned before calling
 * 'persist()'
 * </pre>
 */
@Component
public class TestOrderEnforceCancelAction implements TestOrderAction {

    @Override
    public void execute(StateContext<TestOrderState, TestOrderEvent> context) {
        /*context.getStateMachine()
                .getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachineReactively(
                                    new DefaultStateMachineContext<>(OrderState.CANCELLED, context.getEvent(), null,
                                            context.getExtendedState(), null))
                            .block();
                });*/
    }

}
