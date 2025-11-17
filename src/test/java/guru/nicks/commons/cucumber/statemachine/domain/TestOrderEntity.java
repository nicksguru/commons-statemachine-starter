package guru.nicks.commons.cucumber.statemachine.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TestOrderEntity {

    private UUID id;
    private TestOrderState state;

}
