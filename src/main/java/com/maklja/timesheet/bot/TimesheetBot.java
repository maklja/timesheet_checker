package com.maklja.timesheet.bot;


import com.maklja.timesheet.bot.action.SendEmailAction;
import com.maklja.timesheet.bot.model.ChargeHoursActionPayload;
import com.maklja.timesheet.bot.config.ConfigWrapper;
import com.maklja.timesheet.bot.action.ChargeHoursAction;
import io.helidon.scheduling.Scheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class TimesheetBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetBot.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd-MM-yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final Object lock = new Object();

    public static void main(final String[] args) {
        LOGGER.info("Timesheet bot started");
        synchronized (lock) {
            final var config = new ConfigWrapper();
            Scheduling.cron()
                    .expression(config.getScheduledCron())
                    .task(inv -> chargeHours(config))
                    .build();

            try {
                lock.wait();
            } catch (final InterruptedException ignored) {
            }
        }
    }

    private static void chargeHours(final ConfigWrapper config) {
        final var timestamp = Instant.now();
        final var formatedTimestamp = DATE_FORMATTER.format(timestamp);
        LOGGER.info("Executing charge hours at {}", formatedTimestamp);
        final var dayInWeek = timestamp.atZone(ZoneId.systemDefault()).getDayOfWeek();
        final var projectPerDays = config.getProjectPerDay();
        final var projects = projectPerDays.get(dayInWeek.getValue());
        if (projects == null) {
            LOGGER.warn("Projects not found for data {}", formatedTimestamp);
            return;
        }
        final var actionPayload = new ChargeHoursActionPayload(
                config.getUrl(),
                config.getUsername(),
                config.getPassword(),
                timestamp,
                projects
        );
        final var chargeHoursAction = new ChargeHoursAction();
        final var actionResult = chargeHoursAction.execute(actionPayload);
        LOGGER.info("Hours successfully charged at {}", formatedTimestamp);
        final var sendEmailAction = new SendEmailAction(config.getEmailConfig());
        final var projectsEmail = actionResult.stream()
                .map(project -> "%s\t%s\t%s\t%s".formatted(
                        project.clientName(),
                        project.projectName(),
                        project.categoryName(),
                        project.hours())
                )
                .collect(Collectors.joining("\n"));
        sendEmailAction.execute("Hours charged: %s".formatted(formatedTimestamp), projectsEmail);
        LOGGER.info("Email sent at {}", formatedTimestamp);
    }
}