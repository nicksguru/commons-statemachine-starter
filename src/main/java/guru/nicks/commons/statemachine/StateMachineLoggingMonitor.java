package guru.nicks.commons.statemachine;

import guru.nicks.commons.utils.TimeUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

/**
 * Logs how long it took to perform state transitions and actions.
 */
@Slf4j
public class StateMachineLoggingMonitor<S, E> extends AbstractStateMachineMonitor<S, E> {

    @Override
    public void transition(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration) {
        // null during initialization
        S from = Optional.ofNullable(transition.getSource())
                .map(State::getId)
                .orElse(null);

        S to = Optional.ofNullable(transition.getTarget())
                .map(State::getId)
                .orElse(null);

        log.debug("[{}] Transition from {} to {} took {}", stateMachine.getId(), from, to,
                TimeUtils.humanFormatDuration(Duration.ofMillis(duration)));
    }

    @Override
    public void action(StateMachine<S, E> stateMachine,
            Function<StateContext<S, E>, Mono<Void>> action, long duration) {
        log.debug("[{}] Last action took {}", stateMachine.getId(),
                TimeUtils.humanFormatDuration(Duration.ofMillis(duration)));
    }

}
