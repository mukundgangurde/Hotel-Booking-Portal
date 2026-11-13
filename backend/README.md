# Hotel Booking Portal - Backend

For complete project setup instructions—including running the backend in IntelliJ IDEA,
creating and configuring `backend\.env`, Windows Command Prompt commands, MySQL setup, and
starting the frontend—see the [project README](../README.md).

## Run backend tests from Command Prompt

From the repository root:

```bat
backend\mvnw.cmd -f backend\pom.xml test
```

Tests use an in-memory H2 database and do not require MySQL.
