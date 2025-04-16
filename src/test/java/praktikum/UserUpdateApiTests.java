package praktikum;

import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import com.github.javafaker.Faker;

public class UserUpdateApiTests {

    private final ApiClient apiClient = new ApiClient();
    private final Faker faker = new Faker();
    private String initialAccessToken; // Токен для удаления пользователя
    private String loginAccessToken; // Токен для изменения данных пользователя
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
    public void updateUserDataWithAuthorization() {
        // Логин под существующим пользователем
        Response loginResponse = apiClient.loginUser(userEmail, userPassword);
        loginResponse.then().statusCode(200);
        loginAccessToken = loginResponse.jsonPath().getString("accessToken");

        // Новые данные для обновления
        String newEmail = "updated-email@yandex.ru";
        String newName = "Дмитрий";

        // Изменение данных пользователя с токеном из логина
        Response updateResponse = apiClient.updateUser(loginAccessToken, newEmail, newName);
        updateResponse.then().statusCode(200);
        updateResponse.then().body("success", equalTo(true));
        updateResponse.then().body("user.email", equalTo(newEmail));
        updateResponse.then().body("user.name", equalTo(newName));
    }

    @Test
    public void updateUserDataWithoutAuthorization() {
        // Новые данные для обновления
        String newEmail = "updated-email@yandex.ru";
        String newName = "Дмитрий";

        // Изменение данных пользователя без авторизации
        Response updateResponse = apiClient.updateUser(null, newEmail, newName);
        updateResponse.then().statusCode(401);
        updateResponse.then().body("success", equalTo(false));
        updateResponse.then().body("message", equalTo("You should be authorised"));
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

