package praktikum;

import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import com.github.javafaker.Faker;

public class OrderApiTests {

    private final ApiClient apiClient = new ApiClient();
    private final Faker faker = new Faker();
    private String initialAccessToken; // Токен для удаления пользователя
    private String orderAccessToken; // Токен для создания заказа
    private String userEmail;
    private String userPassword;
    private String userName;

    @Before
    public void setUp() {
        // Генерация уникальных данных пользователя
        userEmail = faker.internet().emailAddress();
        userPassword = faker.internet().password();
        userName = faker.name().fullName();

        // Создание пользователя
        Response createResponse = apiClient.createUser(userEmail, userPassword, userName);
        createResponse.then().statusCode(200);
        initialAccessToken = createResponse.jsonPath().getString("accessToken");
    }

    @Test
    public void createOrderWithAuthorization() {
        // Логин под существующим пользователем
        Response loginResponse = apiClient.loginUser(userEmail, userPassword);
        loginResponse.then().statusCode(200);
        orderAccessToken = loginResponse.jsonPath().getString("accessToken");

        // Идентификаторы ингредиентов
        String[] ingredients = {"61c0c5a71d1f82001bdaaa6d"};

        // Создание заказа с авторизацией
        Response orderResponse = apiClient.createOrder(orderAccessToken, ingredients);
        orderResponse.then().statusCode(200);
        orderResponse.then().body("success", equalTo(true));
        orderResponse.then().body("name", equalTo("Флюоресцентный бургер"));
        orderResponse.then().body("order.ingredients[0]._id", equalTo(ingredients[0]));
    }

    @Test // !!!не валидный тест по логике создать заказ можно только аутентифицированным пользователем, по факту создается без Authorization!!!
    public void createOrderWithoutAuthorization() {
        // Идентификаторы ингредиентов
        String[] ingredients = {"61c0c5a71d1f82001bdaaa6d"};

        // Создание заказа без авторизации
        Response orderResponse = apiClient.createOrder(null, ingredients);
        orderResponse.then().statusCode(401); // Ожидаем статус 401 Unauthorized (предположил что должен быть такой же как при Изменение данных пользователя без авторизации)
        orderResponse.then().body("success", equalTo(false));
        orderResponse.then().body("message", equalTo("You should be authorized"));
    }

    @Test
    public void createOrderWithoutIngredients() {
        // Создание заказа без ингредиентов
        Response orderResponse = apiClient.createOrder(initialAccessToken, new String[]{});
        orderResponse.then().statusCode(400); // Ожидаем статус 400 Bad Request
        orderResponse.then().body("success", equalTo(false));
        orderResponse.then().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithInvalidIngredientHash() {
        // Идентификаторы ингредиентов с неверным хешем
        String[] ingredients = {"invalid_hash_1", "invalid_hash_2"};

        // Создание заказа с неверным хешем ингредиентов
        Response orderResponse = apiClient.createOrder(orderAccessToken, ingredients);
        orderResponse.then().statusCode(500); // Ожидаем статус 500 Internal Server Error
    }

    @After
    public void tearDown() {
        // Удаление пользователя после тестов
        if (initialAccessToken != null) {
            Response deleteResponse = apiClient.deleteUser(initialAccessToken);
            deleteResponse.then().statusCode(202);
            deleteResponse.then().body("success", equalTo(true));
            deleteResponse.then().body("message", equalTo("User successfully removed"));
        }
    }
}

