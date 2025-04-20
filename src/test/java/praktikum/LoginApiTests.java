package praktikum;
import io.restassured.response.Response;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import com.github.javafaker.Faker;

public class LoginApiTests {

    private final ApiClient apiClient = new ApiClient();
    private final Faker faker = new Faker();

    @Test
    public void loginExistingUser() {
        String existingEmail = faker.internet().emailAddress();
        String password = faker.internet().password();
        String name = faker.name().fullName();

        // Сначала создаем пользователя
        Response createResponse = apiClient.createUser(existingEmail, password, name);
        createResponse.then().statusCode(200);

        // Логин под существующим пользователем
        Response loginResponse = apiClient.loginUser(existingEmail, password);
        loginResponse.then().statusCode(200);
        loginResponse.then().body("success", equalTo(true));
        loginResponse.then().body("user.email", equalTo(existingEmail));
        loginResponse.then().body("user.name", equalTo(name));

        // Удаление пользователя после теста
        Response deleteResponse = apiClient.deleteUser(loginResponse.jsonPath().getString("accessToken"));
        deleteResponse.then().statusCode(202);
        deleteResponse.then().body("message", equalTo("User successfully removed"));
    }

    @Test
    public void loginWithInvalidCredentials() {
        String invalidEmail = faker.internet().emailAddress();
        String invalidPassword = faker.internet().password();

        // Логин с неверным логином и паролем
        Response loginResponse = apiClient.loginUser(invalidEmail, invalidPassword);
        loginResponse.then().statusCode(401);
        loginResponse.then().body("success", equalTo(false));
        loginResponse.then().body("message", equalTo("email or password are incorrect"));
    }
}
