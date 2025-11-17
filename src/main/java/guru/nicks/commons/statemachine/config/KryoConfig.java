package guru.nicks.commons.statemachine.config;

import com.esotericsoftware.kryo.Kryo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.statemachine.support.DefaultStateMachineContext;

/**
 * Fixes Kryo serialization error 'Class not registered' for {@link DefaultStateMachineContext}, arrays, etc.
 */
@Configuration
public class KryoConfig {

    @Bean
    @Primary
    public Kryo kryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.register(DefaultStateMachineContext.class);
        return kryo;
    }

}
