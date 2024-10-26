package at.fhtw.MTCG.service.user;

import at.fhtw.MTCG.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        // Add users to the database before running the tests
        createUser(new User(null, "user1", "password1", 100));
        createUser(new User(null, "user2", "password2", 200));
        createUser(new User(null, "user3", "password3", 300));
    }

    private void createUser(User user) throws IOException {
        URL url = new URL("http://localhost:10001/user");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoOutput(true);

        try (OutputStream os = urlConnection.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsBytes(user);
            os.write(input, 0, input.length);
        }

        int responseCode = urlConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Failed to create user. Response code: " + responseCode);
        }
    }

    @Test
    void testUserServiceGetCompleteList() throws Exception {
        URL url = new URL("http://localhost:10001/user");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected HTTP response code 200");

        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        List<User> userList = objectMapper.readValue(bufferedReader.readLine(), new TypeReference<List<User>>() {});
        assertNotNull(userList, "User list should not be null");
        assertEquals(3, userList.size(), "Expected 3 users in the list");

        bufferedReader.close();
    }

    @Test
    void testUserServiceGetByIdCheckString() throws Exception {
        URL url = new URL("http://localhost:10001/user/1");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected HTTP response code 200");

        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String response = bufferedReader.readLine();
        assertEquals("{\"id\":1,\"username\":\"user1\",\"password\":\"password1\",\"coins\":100}", response, "Expected specific JSON response");

        bufferedReader.close();
    }

    @Test
    void testUserServiceGetById() throws Exception {
        URL url = new URL("http://localhost:10001/user/1");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected HTTP response code 200");

        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        User user = objectMapper.readValue(bufferedReader.readLine(), User.class);
        assertNotNull(user, "User should not be null");
        assertEquals(1, user.getId(), "Expected user ID to be 1");
        assertEquals("user1", user.getUsername(), "Expected username to be 'user1'");

        bufferedReader.close();
    }
}
