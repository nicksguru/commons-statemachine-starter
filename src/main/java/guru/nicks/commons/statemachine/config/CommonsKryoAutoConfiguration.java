package guru.nicks.commons.statemachine.config;

import com.esotericsoftware.kryo.Kryo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.statemachine.support.DefaultStateMachineContext;

/**
 * Autoconfiguration that fixes Kryo serialization error 'Class not registered' for {@link DefaultStateMachineContext},
 * arrays, etc. The {@lin Kryo} bean is declared as primary to override the default one. To disblae this behavior, use:
 * <pre>{@code
 * @SpringBootApplication(exclude = CommonsKryoAutoConfiguration.class)
 * }</pre>
 *
 */
@AutoConfiguration
@ConditionalOnClass(Kryo.class)
@Slf4j
public class CommonsKryoAutoConfiguration {

    @Bean
    @Primary
    public Kryo kryo() {
        log.debug("Building (overriding!) {} bean - setting registrationRequired=false to fix serialization errors",
                Kryo.class.getSimpleName());

        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.register(DefaultStateMachineContext.class);
        return kryo;
    }

}
