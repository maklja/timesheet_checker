package com.maklja.timesheet.bot.config;

import com.maklja.timesheet.bot.model.EmailConfig;
import com.maklja.timesheet.bot.model.Project;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.PollingStrategies;
import io.helidon.config.yaml.YamlConfigParser;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigWrapper {
    private static final String CONFIG_PATH = "/etc/timesheet/config.yaml";
    private static final Duration REFRESH_DURATION = Duration.ofMinutes(15);
    private Config config;

    public ConfigWrapper() {
        config = Config.builder()
                .addParser(YamlConfigParser.create())
                .addMapper(new ProjectConfigMapper())
                .addMapper(new EmailConfigMapper())
                .addSource(ConfigSources.file(CONFIG_PATH)
                        .pollingStrategy(PollingStrategies.regular(REFRESH_DURATION))
                        .optional()
                )
                .addSource(ConfigSources.classpath("application.yaml"))
                .build();

        config.onChange(newConfig -> config = newConfig);
    }

    public String getUrl() {
        return config.get("timesheet.url").asString().orElseThrow();
    }

    public String getUsername() {
        return config.get("timesheet.username").asString().orElseThrow();
    }

    public String getPassword() {
        return config.get("timesheet.password").asString().orElseThrow();
    }

    public EmailConfig getEmailConfig() {
        return config.get("email").as(EmailConfig.class).orElseThrow();
    }

    public Map<Integer, List<Project>> getProjectPerDay() {
        return config.get("timesheet.projects").asNodeList()
                .map(configs -> configs.stream()
                        .reduce(new HashMap<Integer, List<Project>>(),
                                (daysConfig, dayConfig) -> {
                                    final var key = dayConfig.key();
                                    final int dayId = Integer.parseInt(key.name());
                                    final var project = dayConfig.asList(Project.class).orElseThrow();
                                    daysConfig.put(dayId, project);
                                    return daysConfig;
                                },
                                (daysConfig1, daysConfig2) -> daysConfig1))
                .orElseThrow();
    }
}
