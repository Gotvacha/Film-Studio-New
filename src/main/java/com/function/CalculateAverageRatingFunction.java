package com.function;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.sql.*;

public class CalculateAverageRatingFunction {
    @FunctionName("CalculateAverageRatingFunction")
    public void run(
            @TimerTrigger(name = "timerTrigger", schedule = "0 30 11 * * *")
                String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("Java Timer trigger processed a request to calculate average rating.");

        String connectionString = "jdbc:postgresql://filmstudioserver.postgres.database.azure.com/filmbase?user=filmsadmin&password=123456-Aa&sslmode=require";

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String selectSql = "SELECT film_id, AVG(rating) AS average_rating FROM Reviews GROUP BY film_id";

            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                String updateSql = "UPDATE Films SET average_rating = ? WHERE id = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    while (resultSet.next()) {
                        int filmId = resultSet.getInt("film_id");
                        double averageRating = resultSet.getDouble("average_rating");

                        updateStatement.setInt(1, ((int)averageRating));
                        updateStatement.setInt(2, filmId);
                        updateStatement.executeUpdate();
                    }
                }
            }
            context.getLogger().info("Average ratings calculated and updated successfully.");
        } catch (SQLException e) {
            context.getLogger().severe("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
