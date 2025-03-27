package com.maklja.timesheet.bot.model;

public record ChargeHoursActionResult(
        String clientId,
        String clientName,
        String projectId,
        String projectName,
        String categoryId,
        String categoryName,
        int hours
) {
}
