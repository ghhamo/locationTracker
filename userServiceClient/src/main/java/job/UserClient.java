package job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.List;

public class UserClient {
    private final OkHttpClient httpClient; // OkHttp client instance
    private final ObjectMapper objectMapper; // Jackson ObjectMapper for JSON parsing
    private final String baseUrl = "http://userservice:8080/users/"; // Base URL for the User Service API

    public UserClient() {
        this.httpClient = new OkHttpClient(); // Initialize OkHttp client
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper
    }

    // Method to create a user
    public UserDto createUser(String username) {
        String requestBody = "{\"username\": \"" + username + "\"}";

        Request request = new Request.Builder()
                .url(baseUrl + "create")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response: " + response);
            }
            return parseUserDto(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    // Method to get the distance for a user
    public double getDistance(Long userId) {
        Request request = new Request.Builder()
                .url(baseUrl + userId + "/distance")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response: " + response);
            }
            String responseBody = response.body().string();
            return Double.parseDouble(responseBody);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch distance for user ID: " + userId, e);
        }
    }

    // Method to get all users
    public List<UserDto> getAllUsers() {
        Request request = new Request.Builder()
                .url(baseUrl)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response: " + response);
            }
            String jsonResponse = response.body().string();
            return parseUserDtoList(jsonResponse);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch all users", e);
        }
    }

    // Method to update the distance for a user
    public UserDto updateDistance(Long userId, double distance) {
        String requestBody = "{\"distance\": " + distance + "}";

        Request request = new Request.Builder()
                .url(baseUrl + userId + "/update-distance")
                .addHeader("Content-Type", "application/json")
                .put(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response: " + response);
            }
            String responseBody = response.body().string();
            return parseUserDto(responseBody);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update distance for user ID: " + userId, e);
        }
    }

    // Helper method to parse JSON string into UserDto object
    private UserDto parseUserDto(String json) {
        try {
            return objectMapper.readValue(json, UserDto.class); // Parse JSON into UserDto
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse UserDto from JSON", e);
        }
    }

    // Helper method to parse JSON string into a list of UserDto objects
    private List<UserDto> parseUserDtoList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<UserDto>>() {}); // Parse JSON into List<UserDto>
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse List<UserDto> from JSON", e);
        }
    }
}