package com.example.examplemod.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

// This class is a singleton, use DatabaseConnector.getInstance() to get the instance.
public final class DatabaseConnector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String VERIFIED_USER_DATABASE_URL = "https://parseapi.back4app.com/classes/VerifiedUser";
    private static DatabaseConnector instance;

    private HashMap<String, String> credsHeaders;

    private DatabaseConnector() {
        try {
            credsHeaders = getDatabaseHeaders();
        } catch (IOException err) {
            LOGGER.atError().log(err);
        }
    }

    // Represents a row in database for a user.
    private class User {
        class Row {
            public String objectId;
            public String createdAt;
            public String updatedAt;
            public Boolean isVerified;
            public String username;
        }

        public ArrayList<Row> results;

        Boolean isVerified() {
            if (results == null || results.isEmpty()) {
                return false;
            }
            return results.get(0).isVerified;
        }
    }

    public Boolean isUserVerified(String uuid) {
        try {
            HttpRequest request = buildUserQueryRequest(uuid);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            LOGGER.info("Status: " + String.valueOf(response.statusCode()) + "response: " + response.body());

            Gson gson = new Gson();
            User user = gson.fromJson(response.body(), User.class);
            return user.isVerified();
        } catch (URISyntaxException | IOException | InterruptedException err) {
            LOGGER.info("Error:", err.toString());
        }
        return true;
    }

    // Singleton factory.
    public static DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    private HttpRequest buildUserQueryRequest(String uuid) throws URISyntaxException, IOException {
        URI uri = new URI(VERIFIED_USER_DATABASE_URL);
        String query = String.format("where={\"uuid\":\"%s\"&keys=isVerified,username}", uuid);
        uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        HashMap<String, String> headers = getDatabaseHeaders();
        headers.forEach((key, value) -> builder.header(key, value));
        return builder.GET().build();
    }

    // Set credsHeaders and return.
    private HashMap<String, String> getDatabaseHeaders() throws IOException {
        if (credsHeaders != null) {
            return credsHeaders;
        }
        credsHeaders = new HashMap<String, String>();
        Path homePath = Paths.get(System.getProperty("user.home"));

        Path keyPath = Paths.get(homePath.toString(), ".back4app_key");
        String key = ReadFileContent(keyPath.toString());
        credsHeaders.put("X-Parse-Master-Key", key);

        Path appIdPath = Paths.get(homePath.toString(), ".back4app_app_id");
        String appId = ReadFileContent(appIdPath.toString());
        credsHeaders.put("X-Parse-Application-Id", appId);
        return credsHeaders;
    }

    private String ReadFileContent(String path) throws IOException {
        File file = new File(path);
        BufferedReader buffer = new BufferedReader(new FileReader(file));
        String content = buffer.lines().collect(Collectors.joining());
        buffer.close();
        return content;
    }
}