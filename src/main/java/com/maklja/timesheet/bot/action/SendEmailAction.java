package com.maklja.timesheet.bot.action;

import com.maklja.timesheet.bot.model.EmailConfig;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SendEmailAction {
    private static final Logger LOGGER = Logger.getLogger(SendEmailAction.class.getName());
    private final EmailConfig config;

    public SendEmailAction(final EmailConfig emailConfig) {
        this.config = emailConfig;
    }

    public void execute(final String subject, final String text) {
        try {
            final var request = Unirest.post(config.url())
                    .basicAuth("api", config.apiKey())
                    .queryString("from", config.from())
                    .queryString("to", config.to())
                    .queryString("subject", subject)
                    .queryString("text", text)
                    .asJson();
            final var resp = request.getBody();
            final var msg = resp.getObject().get("message");
            LOGGER.log(Level.INFO, "Email sent: {0}", msg);
        } catch (final UnirestException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email", e);
            throw new RuntimeException(e);
        }
    }
}
