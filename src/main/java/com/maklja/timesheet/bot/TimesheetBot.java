package com.maklja.timesheet.bot;


import com.maklja.timesheet.bot.action.SendEmailAction;
import com.maklja.timesheet.bot.model.ChargeHoursActionPayload;
import com.maklja.timesheet.bot.config.ConfigWrapper;
import com.maklja.timesheet.bot.action.ChargeHoursAction;
import io.helidon.scheduling.Scheduling;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TimesheetBot {
    private static final Logger LOGGER = Logger.getLogger(TimesheetBot.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd-MM-yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final Object lock = new Object();

    public static void main(final String[] args) {
        LOGGER.log(Level.INFO, "Timesheet bot started");
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
        LOGGER.log(Level.INFO, "Executing charge hours at {0}", formatedTimestamp);
        final var dayInWeek = timestamp.atZone(ZoneId.systemDefault()).getDayOfWeek();
        final var projectPerDays = config.getProjectPerDay();
        final var projects = projectPerDays.get(dayInWeek.getValue());
        if (projects == null) {
            LOGGER.log(Level.WARNING, "Projects not found for data {0}", formatedTimestamp);
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
        LOGGER.log(Level.INFO, "Hours successfully charged at {0}", formatedTimestamp);
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
        LOGGER.log(Level.INFO, "Email sent at {0}", formatedTimestamp);
    }
}