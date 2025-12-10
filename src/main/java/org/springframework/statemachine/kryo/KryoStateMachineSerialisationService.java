package org.springframework.statemachine.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultStateContext;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This is a copy of the original class, with a patch to allow for Kryo to not require registration. The package is the
 * same too, to this (patched) version overrides the original one in JVM.
 * <p>
 * Without the patch, State Machine states can't be persisted: Kryo complains of {@link DefaultStateContext} class not
 * registered. If this class is registered manually, Kryo then complains of {@link ArrayList} class not registered.
 * Consequently, state transitions do not take place.
 * <p>
 * Solution: call {@link Kryo#setRegistrationRequired(boolean)}, passing {@code false} to it.
 * <p>
 * See {@link #configureKryoInstance(Kryo)} - the code between '// PATCH START' and '// PATCH END' is the patch.
 */
public class KryoStateMachineSerialisationService<S, E> extends AbstractKryoStateMachineSerialisationService<S, E> {

    @Override
    protected void doEncode(Kryo kryo, Object object, Output output) {
        kryo.writeObject(output, object);
    }

    @Override
    protected <T> T doDecode(Kryo kryo, Input input, Class<T> type) {
        return kryo.readObject(input, type);
    }

    @Override
    protected void configureKryoInstance(Kryo kryo) {
        // PATCH START
        kryo.setRegistrationRequired(false);
        // PATCH END
        kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer<S, E>());
        kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
        kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
    }

}
