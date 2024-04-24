package com.function;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class SearchSoundtrackFunction {
    @FunctionName("SearchSoundtrackFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to search for a movie soundtrack.");
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

        String finalQuery = movieName + " [movie soundtrack site:youtube.com]";

        String searchAPIKey = "AIzaSyC29c4rkpWwZh1dDmWRoAB7Eqpf-dkgxaI";

        String searchEngineId = "872f80fe824a14b5c";

        try {
            Customsearch customsearch = new Customsearch.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("SearchSoundtrackFunction")
                    .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(searchAPIKey))
                    .build();

            Customsearch.Cse.List list = customsearch.cse().list(finalQuery);
            list.setCx(searchEngineId);

            Search results = list.execute();
            if (results.getItems() != null && !results.getItems().isEmpty()) {
                for (Result searchResult : results.getItems()) {
                    if(searchResult.getLink().contains("/playlist?")) continue;
                    String movieSoundtrackURL = "<html><title>" + movieName + "</title><body><p>" + movieName + "'s soundtrack: "
                        + searchResult.getTitle() + "</p><iframe width='560' height='315' src='"
                        + embedVideoLink(searchResult.getLink()) + "' autoplay; picture-in-picture' allowfullscreen></iframe></body></html>";
                    return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "text/html").body(movieSoundtrackURL).build();
                }
            }

            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Movie soundtrack not found.").build();
        
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while searching for movie soundtrack.").build();
        }
    }

    private String embedVideoLink(String videoLink) {
        if (videoLink != null && videoLink.contains("watch?v=")) {
            return videoLink.replace("watch?v=", "embed/");
        } else {
            return videoLink;
        }
    }
}
