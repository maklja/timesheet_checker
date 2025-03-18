package com.maklja.timesheet.bot.action;

import com.maklja.timesheet.bot.model.Project;

import java.time.Instant;
import java.util.List;

public record ChargeHoursActionPayload(String timesheetUrl,
                                       String username,
                                       String password,
                                       Instant timestamp,
                                       List<Project> projects) {
}
