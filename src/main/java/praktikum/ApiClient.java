package praktikum;

import com.google.gson.Gson;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

public class ApiClient {

    public Response createUser(String email, String password, String name) {
        return given()
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}", email, password, name))
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .post("/api/auth/register");
    }

    public Response loginUser(String email, String password) {
        return given()
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password))
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .post("/api/auth/login");
    }

    public Response deleteUser(String accessToken) {
        return given()
                .contentType("application/json")
                .header("Authorization", accessToken)
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .delete("/api/auth/user");
    }
    public Response updateUser(String accessToken, String email, String name) {
        // Создаем запрос с заголовком авторизации только если токен не null
        RequestSpecification request = given()
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"name\": \"%s\"}", email, name))
                .baseUri(EnvConfig.BASE_URL);

        // Добавляем заголовок авторизации, если токен не null
        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .patch("/api/auth/user");
    }

    public Response createOrder(String accessToken, String[] ingredients) {
        Gson gson = new Gson();
        String jsonBody = gson.toJson(new OrderRequest(ingredients));

        RequestSpecification request = given()
                .contentType("application/json")
                .body(jsonBody)
                .baseUri(EnvConfig.BASE_URL);

        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .post("/api/orders");
    }

    // Вспомогательный класс для сериализации
    private static class OrderRequest {
        private String[] ingredients;

        public OrderRequest(String[] ingredients) {
            this.ingredients = ingredients;
        }
    }
        public Response getOrders(String accessToken) {
        RequestSpecification request = given()
                .contentType("application/json")
                .baseUri(EnvConfig.BASE_URL);

        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .get("/api/orders");
    }
}
