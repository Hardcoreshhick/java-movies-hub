package ru.practicum.moviehub.http;

import org.junit.jupiter.api.*;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        MoviesStore store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();
    }

    @AfterEach
    void afterEach() {
        server.stop();
    }

    @Test
    @DisplayName("GET /movies при пустом хранилище возвращает 200 и []")
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, response.statusCode());
        assertEquals("application/json; charset=UTF-8",
                response.headers().firstValue("Content-Type").orElse(""));
        assertEquals("[]", response.body());
    }

    @Test
    @DisplayName("POST /movies добавляет фильм и возвращает 201")
    void postMovie_createsMovie() throws Exception {
        String json = "{\"title\": \"" + TestConstants.MATRIX_TITLE + "\", \"year\": " + TestConstants.MATRIX_YEAR + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, response.statusCode());
        assertEquals("application/json; charset=UTF-8",
                response.headers().firstValue("Content-Type").orElse(""));
        assertTrue(response.body().contains("\"title\":\"" + TestConstants.MATRIX_TITLE + "\""));
        assertTrue(response.body().contains("\"year\":" + TestConstants.MATRIX_YEAR));
        assertTrue(response.body().contains("\"id\""));
    }

    @Test
    @DisplayName("POST /movies с пустым title возвращает 422")
    void postMovie_emptyTitle_returns422() throws Exception {
        String json = "{\"title\": \"\", \"year\": " + TestConstants.MATRIX_YEAR + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains(TestConstants.VALIDATION_TITLE_EMPTY));
    }

    @Test
    @DisplayName("GET /movies/{id} возвращает фильм по ID")
    void getMovieById_returnsMovie() throws Exception {
        String json = "{\"title\": \"" + TestConstants.MATRIX_TITLE + "\", \"year\": " + TestConstants.MATRIX_YEAR + "}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"title\":\"" + TestConstants.MATRIX_TITLE + "\""));
        assertTrue(response.body().contains("\"id\":1"));
    }

    @Test
    @DisplayName("GET /movies/{id} с несуществующим ID возвращает 404")
    void getMovieById_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains(TestConstants.MOVIE_NOT_FOUND));
    }

    @Test
    @DisplayName("DELETE /movies/{id} удаляет фильм")
    void deleteMovie_removesMovie() throws Exception {
        String json = "{\"title\": \"" + TestConstants.MATRIX_TITLE + "\", \"year\": " + TestConstants.MATRIX_YEAR + "}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, response.statusCode());
        assertEquals("", response.body());
    }

    @Test
    @DisplayName("DELETE /movies/{id} с несуществующим ID возвращает 404")
    void deleteMovie_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains(TestConstants.MOVIE_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /movies?year=YYYY возвращает фильмы по году")
    void getMoviesByYear_returnsFilteredMovies() throws Exception {
        String json1 = "{\"title\": \"" + TestConstants.MATRIX_TITLE + "\", \"year\": " + TestConstants.MATRIX_YEAR + "}";
        String json2 = "{\"title\": \"" + TestConstants.INTERSTELLAR_TITLE + "\", \"year\": " + TestConstants.INTERSTELLAR_YEAR + "}";

        HttpRequest postRequest1 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json1))
                .build();
        client.send(postRequest1, HttpResponse.BodyHandlers.ofString());

        HttpRequest postRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json2))
                .build();
        client.send(postRequest2, HttpResponse.BodyHandlers.ofString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies?year=" + TestConstants.MATRIX_YEAR))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(TestConstants.MATRIX_TITLE));
        assertFalse(response.body().contains(TestConstants.INTERSTELLAR_TITLE));
    }

    @Test
    @DisplayName("GET /movies?year=YYYY с нечисловым значением возвращает 400")
    void getMoviesByYear_invalidYear_returns400() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies?year=abc"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains(TestConstants.INVALID_QUERY_YEAR));
    }

    @Test
    @DisplayName("POST /movies с неверным year возвращает 422")
    void postMovie_invalidYear_returns422() throws Exception {
        String json = "{\"title\": \"" + TestConstants.MATRIX_TITLE + "\", \"year\": 1800}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains(TestConstants.VALIDATION_YEAR_RANGE));
    }

    @Test
    @DisplayName("POST /movies с некорректным JSON возвращает 422")
    void postMovie_invalidJson_returns422() throws Exception {
        String json = "{\"title\": \"" + TestConstants.MATRIX_TITLE + "\", \"year\": 1999,}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.BASE_URL + "/movies"))
                .header("Content-Type", TestConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains(TestConstants.INVALID_JSON));
    }
}