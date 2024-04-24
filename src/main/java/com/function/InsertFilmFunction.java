package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.util.Optional;

public class InsertFilmFunction {
    @FunctionName("InsertFilmFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to insert a film.");

        String requestBody = "";

        try {
            requestBody = URLDecoder.decode(request.getBody().orElse(""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        context.getLogger().info("Printing request body:" + requestBody);
        String[] formData = requestBody.split("&");

        String title = formData[0].split("=")[1];
        String yearStr = formData[1].split("=")[1];
        String genre = formData[2].split("=")[1];
        String description = formData[3].split("=")[1];
        String director = formData[4].split("=")[1];
        String actors = formData[5].split("=")[1];

        if (title.isEmpty() || yearStr.isEmpty() || genre.isEmpty() || description.isEmpty() || director.isEmpty() || actors.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Missing required parameters.").build();
        }

        int year;
        try {
            year = Integer.parseInt(yearStr.trim());
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid year format.").build();
        }

        String connectionString = "jdbc:postgresql://filmstudioserver.postgres.database.azure.com/filmbase?user=filmsadmin&password=123456-Aa&sslmode=require";

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String sql = "INSERT INTO Films (title, year, genre, description, director, actors) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, title);
                statement.setInt(2, year);
                statement.setString(3, genre);
                statement.setString(4, description);
                statement.setString(5, director);
                statement.setString(6, actors);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    return request.createResponseBuilder(HttpStatus.OK).body("Film inserted successfully.").build();
                } else {
                    return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert film.").build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert film.").build();
        }
    }
}

// import com.google.common.collect.Multimap;
// import com.google.common.collect.MultimapBuilder;
        // Multimap<String, String> queryParams = MultimapBuilder.hashKeys().arrayListValues().build();
        // request.getQueryParameters().forEach(queryParams::put);
        // String title = queryParams.get("title").stream().findFirst().orElse("");
        // String yearStr = queryParams.get("year").stream().findFirst().orElse("");
        // String genre = queryParams.get("genre").stream().findFirst().orElse("");
        // String description = queryParams.get("description").stream().findFirst().orElse("");
        // String director = queryParams.get("director").stream().findFirst().orElse("");
        // String actors = queryParams.get("actors").stream().findFirst().orElse("");

// import java.util.Map;
// Map<String, String> formData = request.getBody();

        // String title = formData.get("title");
        // int year = Integer.parseInt(formData.get("year"));
        // String genre = formData.get("genre");
        // String description = formData.get("description");
        // String director = formData.get("director");
        // String actors = formData.get("actors");

// to host.json: ,
// "http": {
//   "routePrefix": "",
//   "maxConcurrentRequests": 100,
//   "maxOutstandingRequests": 100,
//   "dynamicThrottlesEnabled": false,
//   "staticRoutes": {
//     "routePrefix": "",
//     "directory": "wwwroot"
//   }
// }