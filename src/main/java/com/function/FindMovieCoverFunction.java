package com.function;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class FindMovieCoverFunction {
    @FunctionName("FindMovieCoverFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to search for a movie cover.");
        String requestBody = "";

        try {
            requestBody = URLDecoder.decode(request.getBody().orElse(""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        context.getLogger().info("Printing request body:" + requestBody);

        if (requestBody.split("=").length <= 1) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Missing required parameters.").build();
        }

        String movieName = requestBody.split("=")[1];

        String finalQuery = movieName + " [movie cover]";

        String searchAPIKey = "AIzaSyC29c4rkpWwZh1dDmWRoAB7Eqpf-dkgxaI";

        String searchEngineId = "872f80fe824a14b5c";

        try {
            Customsearch customsearch = new Customsearch.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("FindMovieCoverFunction")
                    .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(searchAPIKey))
                    .build();

            Customsearch.Cse.List list = customsearch.cse().list(finalQuery);
            list.setCx(searchEngineId);
            list.setSearchType("image");

            Search results = list.execute();
            if (results.getItems() != null && !results.getItems().isEmpty()) {
                List<Result> searchResults = results.getItems();
                for(Result result : searchResults) {
                    if (isImage(result.getLink())) {
                        String movieCoverUrl = "<html><title>" + movieName + "</title><body><p>" + movieName + "'s cover: "
                        + result.getTitle() + "</p><img src=\"" + result.getLink() + "\" alt=\"" + result.getLink() + "\" width=\"500\" height=\"700\"></body></html>";
                        return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "text/html").body(movieCoverUrl).build();
                    }
                }
            }

            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Movie cover not found.").build();
        
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while searching for movie cover.").build();
        }
    }

    private boolean isImage(String url) {
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp"};
        String fileExtension = url.substring(url.lastIndexOf('.') + 1).toLowerCase();
        return Arrays.asList(imageExtensions).contains(fileExtension);
    }
}
