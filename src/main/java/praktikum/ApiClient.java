package praktikum;

import com.google.gson.Gson;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.qameta.allure.Step;
import static io.restassured.RestAssured.given;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    private Gson gson = new Gson();

    @Step("Регистрация пользователя")
    public Response createUser(String email, String password, String name) {
        Map<String, String> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        user.put("name", name);

        return given()
                .contentType("application/json")
                .body(gson.toJson(user))
                .baseUri(EnvConfig.BASE_URL)
                .when()
                .post("/api/auth/register");
    }

    @Step("Логин пользователя")
    public Response loginUser(String email, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        return given()
                .contentType("application/json")
                .body(gson.toJson(credentials))
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
        Map<String, String> userUpdates = new HashMap<>();
        userUpdates.put("email", email);
        userUpdates.put("name", name);

        RequestSpecification request = given()
                .contentType("application/json")
                .body(gson.toJson(userUpdates))
                .baseUri(EnvConfig.BASE_URL);

        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .patch("/api/auth/user");
    }

    @Step("Создание заказа")
    public Response createOrder(String accessToken, String[] ingredients) {
        Map<String, String[]> orderRequest = new HashMap<>();
        orderRequest.put("ingredients", ingredients);

        RequestSpecification request = given()
                .contentType("application/json")
                .body(gson.toJson(orderRequest))
                .baseUri(EnvConfig.BASE_URL);

        if (accessToken != null) {
            request.header("Authorization", accessToken);
        }

        return request.when()
                .post("/api/orders");
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

    @Step("Выбор ингридиента")
    public String createIngredient() {
        return "61c0c5a71d1f82001bdaaa6d"; // Замените на реальный идентификатор
    }
}
