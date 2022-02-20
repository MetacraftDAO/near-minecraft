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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.example.examplemod.utils.DatabaseDataWrapper.PlayTime;
import com.example.examplemod.utils.DatabaseDataWrapper.PlayTimeRows;
import com.example.examplemod.utils.DatabaseDataWrapper.VerifiedUser;
import com.example.examplemod.utils.DatabaseDataWrapper.VerifiedUserRows;

// This class is a singleton, use DatabaseConnector.getInstance() to get the instance.
public final class DatabaseConnector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String VERIFIED_USER_DATABASE_URL = "https://parseapi.back4app.com/classes/VerifiedUser";
    private static final String PLAY_TIME_DATABASE_URL = "https://parseapi.back4app.com/classes/PlayTime";
    private static DatabaseConnector instance;

    private HashMap<String, String> credsHeaders;

    private DatabaseConnector() {
        try {
            credsHeaders = getDatabaseHeaders();
        } catch (IOException err) {
            LOGGER.atError().log(err);
        }
    }

    public VerifiedUser getVerifiedUser(String uuid) {
        try {
            HttpRequest request = buildUserQueryRequest(uuid);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            LOGGER.info("get verified user status: " + String.valueOf(response.statusCode()) + " response: "
                    + response.body());

            if (response.statusCode() != 200) {
                LOGGER.info("Error in connecting database, user is not verified.");
                return null;
            }

            Gson gson = new Gson();
            VerifiedUserRows users = gson.fromJson(response.body(), VerifiedUserRows.class);
            return users.getFirst();
        } catch (IOException | URISyntaxException | InterruptedException err) {
            LOGGER.info("Error:", err.toString());
            return null;
        }
    }

    public Boolean isUserVerified(String uuid) {
        VerifiedUser user = getVerifiedUser(uuid);
        if (user == null)
            return false;

        LOGGER.info(user.isVerified ? "user is verified." : "user is not verified.");
        return user.isVerified;
    }

    public boolean updatePlayTimeTikTime(String objectId, String tikTime) {
        try {
            String body = String.format("{\"tikTime\":\"%s\"}", tikTime);
            HttpRequest request = buildPlayTimeUpdateRequest(objectId, body);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("update play time tiktime status: " + response.statusCode() + " post response is: "
                    + response.body());
            return response.statusCode() == 200;
        } catch (NullPointerException | URISyntaxException | IOException | InterruptedException err) {
            LOGGER.atError().log(err);
            return false;
        }
    }

    public boolean writePlayTime(String nearAccountId) {
        try {
            String body = String.format("{\"nearAccountId\":\"%s\"}", nearAccountId);
            HttpRequest request = buildPlayTimePostRequest(body);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("write play time status: " + response.statusCode() + " post response is: " + response.body());
            return response.statusCode() == 201;
        } catch (NullPointerException | URISyntaxException | IOException | InterruptedException err) {
            LOGGER.atError().log(err);
            return false;
        }
    }

    public boolean updatePlayTime(String objectId, String tikTime, String tokTime, long accumulatedPlayTime) {
        try {
            LOGGER.info("update object " + objectId);
            String body = String.format("{\"tikTime\":\"%s\", \"tokTime\":\"%s\", \"accumulatedPlayTime\":%s}",
                    tikTime, tokTime, accumulatedPlayTime);
            HttpRequest request = buildPlayTimeUpdateRequest(objectId, body);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("update play time status: " + response.statusCode() + " post response is: " + response.body());
            return response.statusCode() == 200;
        } catch (NullPointerException | URISyntaxException | IOException | InterruptedException err) {
            LOGGER.atError().log(err);
            return false;
        }
    }

    public PlayTime getPlayTime(String nearAccountId) {
        try {
            HttpRequest request = buildPlayTimeQueryRequest(nearAccountId);
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("get play time status: " + response.statusCode() + " post response is: " + response.body());

            if (response.statusCode() != 200) {
                LOGGER.info("Error in connecting database, user is not verified.");
                return null;
            }

            Gson gson = new Gson();
            PlayTimeRows times = gson.fromJson(response.body(), PlayTimeRows.class);
            return times.getFirst();
        } catch (NullPointerException | URISyntaxException | IOException | InterruptedException err) {
            LOGGER.atError().log(err);
            return null;
        }
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
        String query = String.format("where={\"uuid\":\"%s\"}", uuid);
        uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        HashMap<String, String> headers = getDatabaseHeaders();
        headers.forEach((key, value) -> builder.header(key, value));
        return builder.GET().build();
    }

    private HttpRequest buildPlayTimeQueryRequest(String nearAccountId) throws URISyntaxException, IOException {
        URI uri = new URI(PLAY_TIME_DATABASE_URL);
        String query = String.format("where={\"nearAccountId\":\"%s\"}", nearAccountId);
        uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        HashMap<String, String> headers = getDatabaseHeaders();
        headers.forEach((key, value) -> builder.header(key, value));
        return builder.GET().build();
    }

    private HttpRequest buildPlayTimeUpdateRequest(String objectId, String body)
            throws NullPointerException, URISyntaxException, IOException {
        URI uri = new URI(PLAY_TIME_DATABASE_URL + "/" + objectId);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .PUT(BodyPublishers.ofString(body))
                .header("Content-Type", "application/json");

        HashMap<String, String> headers = getDatabaseHeaders();
        headers.forEach((key, value) -> builder.header(key, value));

        return builder.build();
    }

    private HttpRequest buildPlayTimePostRequest(String body)
            throws NullPointerException, URISyntaxException, IOException {
        URI uri = new URI(PLAY_TIME_DATABASE_URL);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .POST(BodyPublishers.ofString(body))
                .header("Content-Type", "application/json");

        HashMap<String, String> headers = getDatabaseHeaders();
        headers.forEach((key, value) -> builder.header(key, value));

        return builder.build();
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