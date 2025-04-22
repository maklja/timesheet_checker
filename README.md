# Timesheet Auto Bot

A Java-based automation tool for filling Vega IT timesheets automatically. This project is designed to be run as a scheduled task using cron on Linux systems.

## Features

- Automated timesheet submission
- Configurable through YAML configuration
- Headless browser automation using Playwright
- Logging support with SLF4J and Logback
- Runtime configuration override support

## Prerequisites

- Java 21 or higher
- Maven 3.x
- Linux system with cron
- Playwright browser dependencies

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/timesheet_auto_bot.git
cd timesheet_auto_bot
```

2. Build the project:
```bash
mvn clean package
```

3. Install Playwright dependencies:
```bash
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
```

## Configuration

The application supports two levels of configuration:

1. Default configuration in the project root:
```yaml
timesheet:
  url: "https://your-timesheet-url.com"
  username: "your-username"
  password: "your-password"
  # Add other configuration parameters as needed
```

2. External configuration override (recommended for production):
   - Place your configuration file at `/etc/timesheet/config.yaml`
   - The application will automatically detect and use this configuration
   - Changes to this file take effect immediately without requiring a restart
   - This configuration takes precedence over the default configuration

Example external configuration structure:
```yaml
timesheet:
  url: "https://production-timesheet-url.com"
  username: "production-username"
  password: "production-password"
  # Add other production-specific parameters
```

## Usage

### Manual Execution

```bash
java -jar target/timesheet_auto_bot-1.0.0.jar
```

### Setting up with Cron

1. Make the JAR file executable:
```bash
chmod +x target/timesheet_auto_bot-1.0.0.jar
```

2. Create a shell script wrapper (e.g., `run_timesheet_bot.sh`):
```bash
#!/bin/bash
cd /path/to/timesheet_auto_bot
java -jar target/timesheet_auto_bot-1.0.0.jar
```

3. Make the script executable:
```bash
chmod +x run_timesheet_bot.sh
```

4. Add to crontab (run every Friday at 4:30 PM):
```bash
30 16 * * 5 /path/to/run_timesheet_bot.sh >> /path/to/timesheet_auto_bot/logs/cron.log 2>&1
```

## Logging

Logs are written to the `logs` directory. The application uses SLF4J with Logback for logging.

## Troubleshooting

- Check the logs in the `logs` directory for any errors
- Ensure all dependencies are properly installed
- Verify your configuration file is correctly formatted
- Make sure the cron job has proper permissions to execute the script
- If using external configuration, verify the file permissions at `/etc/timesheet/config.yaml`

## License

This project is licensed under the MIT License - see the LICENSE file for details. 