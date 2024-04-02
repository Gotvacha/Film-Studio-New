package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.sql.*;
import java.util.Optional;

public class BaseFunction {
    @FunctionName("BaseFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to create table.");

        String connectionString = "jdbc:postgresql://filmstudioserver.postgres.database.azure.com:5432/postgres?user=postgresqladmin&password=mrazqazureBS69&sslmode=require";
        boolean tableCreated = createFilmsTable(connectionString, context) && createReviewsTable(connectionString, context);

        if (tableCreated) {
            return request.createResponseBuilder(HttpStatus.OK).body("Tables created successfully.").build();
        } else {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create tables.").build();
        }
    }

    private boolean createFilmsTable(String connectionString, ExecutionContext context) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String sql = "CREATE TABLE IF NOT EXISTS Films ("
                       + "id SERIAL PRIMARY KEY,"
                       + "title VARCHAR(255),"
                       + "year INTEGER,"
                       + "genre VARCHAR(100),"
                       + "description TEXT,"
                       + "director VARCHAR(255),"
                       + "actors TEXT,"
                       + "average_rating INTEGER"
                       + ")";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
                context.getLogger().info("Films table created or already exists.");
                return true;
            }
        } catch (Exception e) {
            context.getLogger().severe("Error creating Films table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean createReviewsTable(String connectionString, ExecutionContext context) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String sql = "CREATE TABLE IF NOT EXISTS Reviews ("
                       + "id SERIAL PRIMARY KEY,"
                       + "film_id INTEGER REFERENCES Films(id),"
                       + "title VARCHAR(255),"
                       + "opinion TEXT,"
                       + "rating INTEGER,"
                       + "datetime VARCHAR(255),"
                       + "author VARCHAR(255)"
                       + ")";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
                context.getLogger().info("Reviews table created or already exists.");
                return true;
            }
        } catch (Exception e) {
            context.getLogger().severe("Error creating Reviews table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
