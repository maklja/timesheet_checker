package com.maklja.timesheet.bot.model;

public record Project(String clientId, String projectId, String categoryId, int hours) {
}
