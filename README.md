# Introduction

This is the solution to the Backend Developer assignment for Natlex Group Oy, implemented by Mohammad Pandi.

## How to Build, Test, and Run

```sh
# To build the project (including running tests)
./gradlew build

# To test the project
./gradlew test

# To format the files
./gradlew spotlessApply

# To run the application with the development profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## API Documentation

While the project is running (assuming it is on port `8080`), the API documentation will be available at `http://localhost:8080/swagger-ui/index.html`.


## Considerations
* **File Storage:** In this implementation, server file storage is used to store files. However, in a real production environment, files should be stored on an external object store.
* **Export in Progress:** While the export is in progress, any request to download the file will throw a custom exception (`ExportInProgressException`) and return an HTTP 503 status to the client. We may consider using a different HTTP status.
* **File Import Validation:** No validation is enforced on the headers of imported files. In more restricted situations, we may consider enforcing validation on the headers.
* **User and Role Setup:** The setup for users and roles is limited to development, with usernames and passwords configured in `application-dev.properties`. In production, a more sophisticated user management solution should be used.
