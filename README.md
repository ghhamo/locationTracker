# Build and Run

## Requirements
- [Gradle](https://gradle.org/install/)
- [Docker](https://www.docker.com/get-started/) with Docker Compose

## Steps to Build and Run

1. Build the `userService` submodule:
   ```sh
   gradle :userService:bootJar
   ```

2. Build the `producer` and `consumer` modules:
   ```sh
   gradle :producer:installDist
   gradle :consumer:installDist
   ```

3. Run the application with Docker Compose:
   ```sh
   docker compose up --build
   ```

That's it! The application should now be running.
Producer is sending location updates for 10 users as fast as it can.
Consumer is pulling those update in batches every 10 seconds.
You can see consumer working by looking at logs in consumers docker container.

You can stop the application with Docker Compose:
```sh
docker compose down
```

