package com.maklja.timesheet.bot.action;

import com.maklja.timesheet.bot.model.EmailConfig;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SendEmailAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailAction.class);

    private final EmailConfig config;

    public SendEmailAction(final EmailConfig emailConfig) {
        this.config = emailConfig;
    }

    public void execute(final String subject, final String text) {
        try {
            LOGGER.info("Sending email {} from {} to {}.", subject, config.from(), String.join(", ", config.to()));
            final var request = Unirest.post(config.url())
                    .basicAuth("api", config.apiKey())
                    .queryString("from", config.from())
                    .queryString("to", config.to())
                    .queryString("subject", subject)
                    .queryString("text", text)
                    .asJson();
            final var resp = request.getBody();
            final var msg = resp.getObject().get("message");
            LOGGER.info("Email sent: {}", msg);
        } catch (final UnirestException e) {
            LOGGER.error("Failed to send email", e);
            throw new RuntimeException(e);
        }
    }
}
