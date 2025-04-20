package praktikum;

import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import com.github.javafaker.Faker;

public class UserOrderApiTests {

    private final ApiClient apiClient = new ApiClient();
    private final Faker faker = new Faker();
    private String initialAccessToken; // Токен для удаления пользователя
    private String orderAccessToken; // Токен для создания заказа
    private String userEmail;
    private String userPassword;
    private String userName;
    private String orderId; // ID заказа для проверки

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
    public void getOrdersWithAuthorization() {
        // Логин под существующим пользователем
        Response loginResponse = apiClient.loginUser(userEmail, userPassword);
        loginResponse.then().statusCode(200);
        orderAccessToken = loginResponse.jsonPath().getString("accessToken");

        // Создание заказа с авторизацией
        String[] ingredients = {"61c0c5a71d1f82001bdaaa6d"}; // Пример валидного хеша ингредиента
        Response orderResponse = apiClient.createOrder(orderAccessToken, ingredients);
        orderResponse.then().statusCode(200);
        orderId = orderResponse.jsonPath().getString("order._id"); // Сохраняем ID заказа для проверки

        // Получение заказов с авторизацией
        Response ordersResponse = apiClient.getOrders(orderAccessToken);
        ordersResponse.then().statusCode(200);
        ordersResponse.then().body("success", equalTo(true));
        ordersResponse.then().body("orders[0]._id", equalTo(orderId)); // Проверяем, что заказ есть в списке
    }

    @Test
    public void getOrdersWithoutAuthorization() {
        // Получение заказов без авторизации
        Response ordersResponse = apiClient.getOrders(null);
        ordersResponse.then().statusCode(401); // Ожидаем статус 401 Unauthorized
        ordersResponse.then().body("success", equalTo(false));
        ordersResponse.then().body("message", equalTo("You should be authorised"));
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
