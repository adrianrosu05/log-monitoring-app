# Log Processor

## Project Description

A simple Java-based log processing application that reads log files, processes job durations and generates reports.

##  Project Structure

```
/log-processor-project
│── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── models/       # Data models (Log, LogStatus)
│   │   │   ├── services/     # Business logic (LogProcessor)
│   │   │   ├── utils/        # Constants and helper classes
│   │   │   ├── Main.java     # Entry point of the application
│   │   ├── resources/        # Configuration files (if needed)
│   │── test/                 # Unit tests
│── logs.log                  # Sample log file
│── report.log                  # Sample report file
│── pom.xml                   # Maven configuration
│── README.md                 # Project documentation
│── .gitignore                 # Git ignored files
```

## Features

- Reads and parses a log file.
- Processes job start and end times, calculating durations.
- Categorizes jobs as OK, WARNING, or ERROR based on execution time.
- Generates a report and writes it to a file.
- Logs errors and warnings for debugging.

## Installation & Setup

### Prerequisites

- Java 11+
- Maven (if using Maven)

### Clone the repository

```sh
git clone https://github.com/adrianrosu05/log-monitoring-app.git
cd log-processor-project
```

### Build the project (Maven)

```sh
mvn clean install
```

## 🔧 How to Run the Application

```sh
java -jar target/log-processor.jar
```

Or, if using an IDE, run the `Main.java` file.

## Log File Format

The log file (`logs.log`) should be formatted as follows:

```
12:00:00,Job1,START,1001
12:10:00,Job1,END,1001
12:05:00,Job2,START,1002
12:07:00,Job2,END,1002
```

Each line contains:

- **Timestamp** (HH:mm:ss)
- **Job Name**
- **Status** (`START` or `END`)
- **Process ID (PID)**

## Expected Output

The generated report will be saved to `report.log` with the following format:

```
Job Job1 (PID 1001) Start: 12:00:00, End: 12:10:00, Duration: 10 min 0 sec - OK
Job Job2 (PID 1002) Start: 12:05:00, End: 12:07:00, Duration: 2 min 0 sec - OK
```

## Running Tests

To run unit tests, use:

```sh
mvn test
```

## Technologies Used

- **Java 11+**
- **Maven** (for dependency management)
- **SLF4J + Logback** (for logging)
- **JUnit 5** (for testing)
- **Lombok** (to reduce boilerplate code)


