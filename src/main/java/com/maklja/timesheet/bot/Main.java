package com.maklja.timesheet.bot;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Main {
    public static void main(String[] args) {
        final var options = new BrowserType.LaunchOptions();
        options.setHeadless(false);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(options)) {
            Page page = browser.newPage();
            page.navigate("https://timesheet.vegait.rs");
            System.out.println(page.title());
        }
    }
}