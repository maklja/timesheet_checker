package com.maklja.timesheet.bot.config;

import com.maklja.timesheet.bot.model.EmailConfig;
import io.helidon.config.Config;
import io.helidon.config.spi.ConfigMapperProvider;

import java.util.Map;
import java.util.function.Function;

public class EmailConfigMapper implements ConfigMapperProvider {
    @Override
    public Map<Class<?>, Function<Config, ?>> mappers() {
        return Map.of(EmailConfig.class, config -> new EmailConfig(
                config.get("url").asString().orElseThrow(),
                config.get("from").asString().orElseThrow(),
                config.get("apiKey").asString().orElseThrow(),
                config.get("to").asList(String.class).orElseThrow()
        ));
    }
}
