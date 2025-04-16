package praktikum;

import com.google.gson.Gson;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.qameta.allure.Step; // Import the Step annotation
import static io.restassured.RestAssured.given;

public class ApiClient {

    @Step("Регистрация пользователя")
    public Response createUser(String email, String password, String name) {
        return given()
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}", email, password, name))
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .post("/api/auth/register");
    }

    @Step("Логин пользователя")
    public Response loginUser(String email, String password) {
        return given()
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password))
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .post("/api/auth/login");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String accessToken) {
        return given()
                .contentType("application/json")
                .header("Authorization", accessToken)
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .delete("/api/auth/user");
    }

    @Step("Изменения данных пользователя")
    public Response updateUser(String accessToken, String email, String name) {
        RequestSpecification request = given()
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"name\": \"%s\"}", email, name))
                .baseUri(EnvConfig.BASE_URL);

        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .patch("/api/auth/user");
    }

    @Step("Создание заказа")
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

    private static class OrderRequest {
        private String[] ingredients;

        public OrderRequest(String[] ingredients) {
            this.ingredients = ingredients;
        }
    }

    @Step("Получение списка заказов")
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
