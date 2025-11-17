package guru.nicks.commons.cucumber.statemachine;

import guru.nicks.commons.cucumber.statemachine.domain.TestOrderEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static guru.nicks.commons.validation.dsl.ValiDsl.checkNotNull;

/**
 * Dummy repository which resembles Spring Data and stores entities in memory.
 */
public class TestOrderRepository {

    private static final Map<UUID, TestOrderEntity> orderById = new ConcurrentHashMap<>();

    public Optional<TestOrderEntity> findById(UUID id) {
        return Optional.ofNullable(orderById.get(id));
    }

    public TestOrderEntity getById(UUID id) {
        return findById(id).orElseThrow();
    }

    public boolean existsById(UUID id) {
        return orderById.containsKey(id);
    }

    public TestOrderEntity save(TestOrderEntity orderEntity) {
        checkNotNull(orderEntity, "orderEntity");

        // auto-assign ID
        if (orderEntity.getId() == null) {
            orderEntity.setId(UUID.randomUUID());
        }

        orderById.put(orderEntity.getId(), orderEntity);
        return orderEntity;
    }

}
