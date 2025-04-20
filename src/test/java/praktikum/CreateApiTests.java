package praktikum;

import io.restassured.response.Response;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import com.github.javafaker.Faker;


public class CreateApiTests {

    private final ApiClient apiClient = new ApiClient();
    private final Faker faker = new Faker();

    @Test
    public void createUniqueUser() {
        // Генерация уникального email и других данных с помощью Faker
        String uniqueEmail = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();

        // Тест на создание уникального пользователя
        Response response = apiClient.createUser(uniqueEmail, password, name);

        // Проверка статуса ответа
        response.then().statusCode(200);
        response.then().body("success", equalTo(true));

        // Проверка наличия данных пользователя
        response.then().body("user.email", equalTo(uniqueEmail));
        response.then().body("user.name", equalTo(name));

        // Проверка наличия токенов
        String accessToken = response.jsonPath().getString("accessToken");
        String refreshToken = response.jsonPath().getString("refreshToken");

        assertNotNull("Access token should not be null", accessToken);
        assertNotNull("Refresh token should not be null", refreshToken);

        // Удаление пользователя после теста
        Response deleteResponse = apiClient.deleteUser(accessToken);

        // Проверка статуса удаления
        deleteResponse.then().statusCode(202);
        deleteResponse.then().body("message", equalTo("User successfully removed"));
    }

    @Test
    public void createExistingUser() {
        String existingEmail = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();

        // Сначала создаем пользователя
        Response createResponse = apiClient.createUser(existingEmail, password, name);

        // Проверка, что пользователь был успешно создан
        createResponse.then().statusCode(200);
        createResponse.then().body("success", equalTo(true));

        // Теперь пытаемся создать того же пользователя снова
        Response response = apiClient.createUser(existingEmail, password, name);

        response.then().statusCode(403);
        response.then().body("success", equalTo(false));
        response.then().body("message", equalTo("User already exists"));

        // Удаление пользователя после теста
        Response deleteResponse = apiClient.deleteUser(createResponse.jsonPath().getString("accessToken"));
        deleteResponse.then().statusCode(202);
        deleteResponse.then().body("message", equalTo("User successfully removed"));
    }

    @Test
    public void createUserWithoutRequiredField() {
        // Тест на создание пользователя без обязательного поля
        Response response = apiClient.createUser("", "password", "Username");

        response.then().statusCode(403);
        response.then().body("success", equalTo(false));
        response.then().body("message", equalTo("Email, password and name are required fields"));
    }
}
