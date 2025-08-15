Social Media App Backend (Java/Spring Boot Version)
🚀 Overview
This is a backend service built with Spring Boot that provides user registration and login functionality for a social media application. It features:

User signup with email verification.

Secure password storage using BCrypt.

JWT-based authentication for secure API access.

MongoDB for data persistence.

🛠️ Setup Instructions
Prerequisites
Java Development Kit (JDK) 17 or later.

Apache Maven.

MongoDB instance (running locally or on a cloud service).

Steps
Create Project Structure: Create the directory structure as outlined by the file paths in the document titles.

Save the Files: Save each document below into its corresponding file path.

Configure Properties: Open src/main/resources/application.properties and update the values for your environment:

spring.data.mongodb.uri: Your MongoDB connection string.

app.jwtSecret: A strong, secret key for signing JWTs.

app.jwtExpirationInMs: Token expiration time.

app.url: The base URL of your server (for email links).

spring.mail.*: Your email server (SMTP) credentials. For testing, you can use a service like Mailtrap.io which provides you with SMTP credentials for a development inbox.

Build the Project: Open a terminal in the root directory of the project and run:

mvn clean install

Run the Application: Once the build is successful, run the application with:

mvn spring-boot:run

The server will start, typically on port 8080.

📖 API Endpoints
POST /api/auth/signup: Register a new user.

GET /api/auth/confirm/{token}: Confirm a user's email address via the link sent.

POST /api/auth/login: Log in a user and receive a JWT.