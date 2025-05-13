package dk.patientassist.test.api;

import dk.patientassist.test.auth.AuthManager;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.*;

public class DishApiClient {

    private static final String BASE_URI = "http://localhost:7070/api";

    static {
        baseURI = BASE_URI;
    }

    public static Response createDish(Map<String, String> dishDetails) {
        return withAuthRetry(() ->
                given()
                        .header("Authorization", "Bearer " + AuthManager.getAccessToken())
                        .contentType("application/json")
                        .body(dishDetails)
                        .post("/dishes")
        );
    }

    public static Response getDish(String dishName) {
        return withAuthRetry(() ->
                given()
                        .header("Authorization", "Bearer " + AuthManager.getAccessToken())
                        .get("/dishes/{name}", dishName)
        );
    }

    public static Response deleteDish(String dishName) {
        return withAuthRetry(() ->
                given()
                        .header("Authorization", "Bearer " + AuthManager.getAccessToken())
                        .delete("/dishes/{name}", dishName)
        );
    }

    public static Response updateDish(String dishName, Map<String, String> updates) {
        return withAuthRetry(() ->
                given()
                        .header("Authorization", "Bearer " + AuthManager.getAccessToken())
                        .contentType("application/json")
                        .body(updates)
                        .put("/dishes/{name}", dishName)
        );
    }

    public static Response patchDish(String dishName, Map<String, String> patch) {
        return withAuthRetry(() ->
                given()
                        .header("Authorization", "Bearer " + AuthManager.getAccessToken())
                        .contentType("application/json")
                        .body(patch)
                        .patch("/dishes/{name}", dishName)
        );
    }

    @FunctionalInterface
    interface ApiCall {
        Response execute();
    }

    private static Response withAuthRetry(ApiCall call) {
        Response response = call.execute();
        if (response.getStatusCode() == 401 && AuthManager.getRefreshToken() != null) {
            refreshAccessToken();
            response = call.execute(); // Retry once
        }
        return response;
    }

    private static void refreshAccessToken() {
        Response refreshResponse = given()
                .contentType("application/json")
                .body(Map.of("refreshToken", AuthManager.getRefreshToken()))
                .post("/auth/refresh");

        if (refreshResponse.statusCode() == 200) {
            AuthManager.setAccessToken(refreshResponse.jsonPath().getString("accessToken"));
        } else {
            AuthManager.clear();
            throw new RuntimeException("Session expired. Re-login required.");
        }
    }
}
