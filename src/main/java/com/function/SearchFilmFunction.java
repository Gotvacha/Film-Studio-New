package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.util.Optional;

public class SearchFilmFunction {
    @FunctionName("SearchFilmFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to search for a film.");
        String requestBody = "";

        try {
            requestBody = URLDecoder.decode(request.getBody().orElse(""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        context.getLogger().info("Printing request body:");
        context.getLogger().info(requestBody);

        String selectFilmSql = "SELECT * FROM Films";
        String searchString = null;

        if (requestBody.split("=").length > 1) {
            searchString = requestBody.split("=")[1];
            selectFilmSql = "SELECT * FROM Films WHERE title LIKE ?";
        }

        String connectionString = "jdbc:postgresql://filmstudioserver.postgres.database.azure.com:5432/postgres?user=postgresqladmin&password=mrazqazureBS69&sslmode=require";

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            try (PreparedStatement selectFilmsStatement = connection.prepareStatement(selectFilmSql)) {
                if (searchString != null) {
                    selectFilmsStatement.setString(1, ("%" + searchString + "%"));
                }

                ResultSet filmsResultSet = selectFilmsStatement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();

                while (filmsResultSet.next()) {
                    int film_id = filmsResultSet.getInt("id");
                    String title = filmsResultSet.getString("title");
                    int year = filmsResultSet.getInt("year");
                    String genre = filmsResultSet.getString("genre");
                    String description = filmsResultSet.getString("description");
                    String director = filmsResultSet.getString("director");
                    String actors = filmsResultSet.getString("actors");
                    int average_rating = filmsResultSet.getInt("average_rating");

                    String selectOpinionSql = "SELECT * FROM Reviews WHERE film_id = ?";
                    
                    responseBuilder.append("Title: ").append(title).append("-\t").append(year).append(", ")
                        .append(genre).append(", ").append(description).append(", ").append(director).append(", ")
                        .append(actors).append(", ").append(average_rating).append("\n");

                    try (PreparedStatement selectReviewStatement = connection.prepareStatement(selectOpinionSql)) {
                        selectReviewStatement.setInt(1, film_id);
                        ResultSet reviewsResultSet = selectReviewStatement.executeQuery();
                        
                        if(reviewsResultSet.next()){
                            responseBuilder.append("Opinions:\n");
                            String reviewTitle = reviewsResultSet.getString("title");
                            String opinion = reviewsResultSet.getString("opinion");
                            int rating = reviewsResultSet.getInt("rating");
                            String authorName = reviewsResultSet.getString("author");
                            
                            responseBuilder.append("\t").append(reviewTitle).append(", ").append(opinion).append(", ").append(rating)
                                .append(", ").append(authorName).append(";\n");
                        }

                        while (reviewsResultSet.next()) {
                            String reviewTitle = reviewsResultSet.getString("title");
                            String opinion = reviewsResultSet.getString("opinion");
                            int rating = reviewsResultSet.getInt("rating");
                            String authorName = reviewsResultSet.getString("author");
                            
                            responseBuilder.append("\t").append(reviewTitle).append(", ").append(opinion).append(", ").append(rating)
                                .append(", ").append(authorName).append(";\n");
                        }
                    }
                }
                return request.createResponseBuilder(HttpStatus.OK).body(responseBuilder.toString()).build();
            }
        } catch (SQLException e) {
            context.getLogger().severe("SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to search for films.").build();
        }
    }
}
