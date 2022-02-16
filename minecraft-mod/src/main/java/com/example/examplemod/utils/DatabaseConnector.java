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
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public Boolean isUserVerified(String username, String uuid) {
        try {
            HttpRequest request = buildUserQueryRequest(username);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            LOGGER.info("Status: " + String.valueOf(response.statusCode()));
            LOGGER.info("Response body: " + response.body());
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

    private HttpRequest buildUserQueryRequest(String username) throws URISyntaxException, IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(VERIFIED_USER_DATABASE_URL));
        HashMap<String, String> headers = getDatabaseHeaders();
        headers.forEach((key, value) -> builder.header(key, value));
        BodyPublisher publisher = BodyPublishers.ofString(String.format("where={\"username\":\"%s\"}", username));
        builder.method("GET", publisher);
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