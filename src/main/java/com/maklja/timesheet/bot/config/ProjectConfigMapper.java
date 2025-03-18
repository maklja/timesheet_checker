package com.maklja.timesheet.bot.config;

import com.maklja.timesheet.bot.model.Project;
import io.helidon.config.Config;
import io.helidon.config.spi.ConfigMapperProvider;

import java.util.Map;
import java.util.function.Function;

public class ProjectConfigMapper implements ConfigMapperProvider {
    @Override
    public Map<Class<?>, Function<Config, ?>> mappers() {
        return Map.of(Project.class, config ->
                new Project(
                        config.get("clientId").asString().orElseThrow(),
                        config.get("projectId").asString().orElseThrow(),
                        config.get("categoryId").asString().orElseThrow(),
                        config.get("hours").asInt().orElseThrow()
                ));
    }
}