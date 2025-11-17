package guru.nicks.commons.cucumber.statemachine.world;

import io.cucumber.spring.ScenarioScope;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Domain-specific (not feature-specific) state shared between scenario steps. Thanks to
 * {@link ScenarioScope @ScenarioScope}, each scenario gets a fresh copy.
 */
@Component
@ScenarioScope
@Data
public class TestOrderWorld {

    private UUID orderId;

}
