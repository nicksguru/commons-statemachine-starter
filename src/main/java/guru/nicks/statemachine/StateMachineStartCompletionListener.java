package guru.nicks.statemachine;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

import java.util.concurrent.CountDownLatch;

import static guru.nicks.validation.dsl.ValiDsl.checkNotNull;

/**
 * Lets wait until state machine's asynchronous start completes, i.e. machine state becomes non-null.
 *
 * @see #waitForStateMachineStart(StateMachine)
 */
@Slf4j
public class StateMachineStartCompletionListener<S, E> extends StateMachineListenerAdapter<S, E> {

    @Getter
    private final CountDownLatch startLock = new CountDownLatch(1);

    /**
     * Lets state machine start up and initialize its state. This process is asynchronous from the state machine
     * perspective, and this method makes is synchronous.
     *
     * @throws NullPointerException current state machine state is {@code null}, which means the listener was not
     *                              installed or worked as expected
     */
    public static <S, E> void waitForStateMachineStart(StateMachine<S, E> stateMachine) {
        var listener = new StateMachineStartCompletionListener<S, E>();
        stateMachine.addStateListener(listener);
        stateMachine.startReactively().block();

        try {
            listener.getStartLock().await();
        } catch (InterruptedException e) {
            log.error("[{}] Thread interrupted while waiting for state machine to start", stateMachine.getId());
            Thread.currentThread().interrupt();
        }

        checkNotNull(stateMachine.getState().getId(), "state machine state");
    }

    @Override
    public void stateMachineStarted(StateMachine<S, E> stateMachine) {
        stateMachine.removeStateListener(this);
        startLock.countDown();
    }

}
