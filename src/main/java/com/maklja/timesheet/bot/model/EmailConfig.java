package com.maklja.timesheet.bot.model;

import java.util.List;

public record EmailConfig(String url, String from, String apiKey, List<String> to) {
}
