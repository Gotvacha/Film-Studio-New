package com.function;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class InsertReviewFunction {
    @FunctionName("InsertReviewFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req", 
                methods = {HttpMethod.POST}, 
                authLevel = AuthorizationLevel.ANONYMOUS) 
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to insert a review.");

        String requestBody = "";

        try {
            requestBody = URLDecoder.decode(request.getBody().orElse(""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        Map<String,String> headers = request.getHeaders();
        headers.forEach((key, value) -> {
            context.getLogger().info(String.format("%s: %s", key, value));
        });

        context.getLogger().info("Printing request body:");
        context.getLogger().info(requestBody);

       String[] formData = requestBody.split("&");

        String filmIdStr = formData[0].split("=")[1];
        String title = formData[1].split("=")[1];
        String opinion = formData[2].split("=")[1];
        String ratingStr = formData[3].split("=")[1];
        String author = formData[4].split("=")[1];

        if (filmIdStr.isEmpty() || title.isEmpty() || opinion.isEmpty() || ratingStr.isEmpty() || author.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Missing required parameters.").build();
        }

        int filmId;
        int rating;
        try {
            filmId = Integer.parseInt(filmIdStr.trim());
            rating = Integer.parseInt(ratingStr.trim());
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid request format.").build();
        }

        LocalDate currentDate = LocalDate.now();

        String connectionString = "jdbc:postgresql://filmstudioserver.postgres.database.azure.com:5432/postgres?user=postgresqladmin&password=mrazqazureBS69&sslmode=require";

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String sql = "INSERT INTO Reviews (film_id, title, opinion, rating, datetime, author) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, filmId);
                statement.setString(2, title);
                statement.setString(3, opinion);
                statement.setInt(4, rating);
                statement.setObject(5, currentDate);
                statement.setString(6, author);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    return request.createResponseBuilder(HttpStatus.OK).body("Review inserted successfully.").build();
                } else {
                    return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert review.").build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert review.").build();
        }
    }
}
