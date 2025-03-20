package com.maklja.timesheet.bot.action;

import com.maklja.timesheet.bot.model.ChargeHoursActionPayload;
import com.maklja.timesheet.bot.model.ChargeHoursActionResult;
import com.microsoft.playwright.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChargeHoursAction {
    private static final Logger LOGGER = Logger.getLogger(ChargeHoursAction.class.getName());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-M-d")
            .withZone(ZoneId.systemDefault());
    private static final String USERNAME_INPUT_SELECTOR = "#UserName";
    private static final String PASSWORD_INPUT_SELECTOR = "#password";
    private static final String PROJECT_INPUTS_SELECTOR = "#js-timesheet > tr:nth-child(%s) > td:nth-child(%s) > select";
    private static final String SPENT_HOURS_INPUT_SELECTOR = "#js-timesheet > tr:nth-child(%s) > td:nth-child(5) > input";
    private static final String SAVE_BUTTON_SELECTOR = "#saveButton";

    public List<ChargeHoursActionResult> execute(final ChargeHoursActionPayload payload) {
        final var options = new BrowserType.LaunchOptions();
        options.setHeadless(false);

        LOGGER.log(Level.INFO, "Starting hours charge for date {0}", DATE_FORMATTER.format(payload.timestamp()));
        try (final var playwright = Playwright.create();
             final var browser = playwright.chromium().launch(options)) {
            final var page = browser.newPage();
            loginAction(payload, page);
            LOGGER.info("Login completed successfully");
            final var actionResults = chargeHoursAction(payload, page);
            LOGGER.info("Hours charge completed successfully");
            return actionResults;
        }
    }

    private static List<ChargeHoursActionResult> chargeHoursAction(final ChargeHoursActionPayload payload, final Page page) {
        final var chargeDate = DATE_FORMATTER.format(payload.timestamp());
        page.navigate("%s/Timesheet?date=%s".formatted(payload.timesheetUrl(), chargeDate));

        final var projects = payload.projects();
        final var actionResults = new ArrayList<ChargeHoursActionResult>(projects.size());
        for (int i = 0; i < projects.size(); ++i) {
            final var project = projects.get(i);
            LOGGER.log(Level.INFO, "Charging project: {0}", project);

            final var selectorIdx = i + 1;
            final var clientSelect = page.locator(PROJECT_INPUTS_SELECTOR.formatted(selectorIdx, 1));
            final var clientSelectValues = String.join(", ", clientSelect.allInnerTexts());
            LOGGER.log(Level.INFO, "Available client: {0}", clientSelectValues);
            clientSelect.selectOption(project.clientId());
            final var clientName = clientSelect.locator("option:checked").textContent();

            final var projectSelect = page.locator(PROJECT_INPUTS_SELECTOR.formatted(selectorIdx, 2));
            final var projectSelectValues = String.join(", ", projectSelect.allInnerTexts());
            LOGGER.log(Level.INFO, "Available projects: {0}", projectSelectValues);
            projectSelect.selectOption(project.projectId());
            final var projectName = projectSelect.locator("option:checked").textContent();

            final var categorySelect = page.locator(PROJECT_INPUTS_SELECTOR.formatted(selectorIdx, 3));
            final var categorySelectValues = String.join(", ", categorySelect.allInnerTexts());
            LOGGER.log(Level.INFO, "Available categories: {0}", categorySelectValues);
            categorySelect.selectOption(project.categoryId());
            final var categoryName = categorySelect.locator("option:checked").textContent();

            final var spentHoursInput = page.locator(SPENT_HOURS_INPUT_SELECTOR.formatted(selectorIdx));
            spentHoursInput.fill(Integer.toString(project.hours()));

            actionResults.add(new ChargeHoursActionResult(
                    project.clientId(),
                    projectName,
                    project.clientId(),
                    clientName,
                    project.categoryId(),
                    categoryName,
                    project.hours()
            ));
        }

        final var saveButton = page.locator(SAVE_BUTTON_SELECTOR);
        saveButton.click();
        page.waitForURL(payload.timesheetUrl());

        return actionResults;
    }

    private static void loginAction(final ChargeHoursActionPayload payload, final Page page) {
        page.navigate(payload.timesheetUrl());

        final var usernameField = page.locator(USERNAME_INPUT_SELECTOR);
        usernameField.fill(payload.username());

        final var passwordField = page.locator(PASSWORD_INPUT_SELECTOR);
        passwordField.fill(payload.password());
        passwordField.press("Enter");
    }
}
