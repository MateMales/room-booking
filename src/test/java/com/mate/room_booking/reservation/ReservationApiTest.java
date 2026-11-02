package com.mate.room_booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.mate.room_booking.TestcontainersConfiguration;
import com.mate.room_booking.security.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ReservationApiTest {

    // korisnici iz V2__sample_data.sql (id: mate=1, ana=2, admin=4), svi imaju lozinku "password123"
    private static final String MATE = "mate@etf.rs";
    private static final String ANA = "ana@etf.rs";
    private static final String ADMIN = "admin@etf.rs";

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcClient jdbcClient;

    private RestClient client;

    @BeforeEach
    void setUp() {
        jdbcClient.sql("DELETE FROM reservations").update();

        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();
    }

    // ---------- osnovno ponašanje ----------

    @Test
    void createReservation_returns201() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"), login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
        assertThat(response.getBody()).contains("\"userId\":1");   // userId je uzet iz tokena
    }

    @Test
    void cancelledSlot_canBeBookedAgain() {
        Integer id = insertReservation(1, 1);   // Matina rezervacija

        ResponseEntity<String> cancel = patch("/reservations/" + id + "/cancel", login(MATE));
        assertThat(cancel.getStatusCode().value()).isEqualTo(200);
        assertThat(cancel.getBody()).contains("\"status\":\"CANCELLED\"");

        ResponseEntity<String> rebook = post("/reservations",
                reservationJson(1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"), login(ANA));
        assertThat(rebook.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void overlappingReservation_returns409() {
        post("/reservations",
                reservationJson(1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"), login(MATE));

        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, "2030-01-15T11:00:00", "2030-01-15T13:00:00"), login(ANA));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).contains("already booked");
    }

    @Test
    void cancelNonExistingReservation_returns404() {
        ResponseEntity<String> response = patch("/reservations/999999/cancel", login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).contains("not found");
    }

    @Test
    void missingRoomId_returns400() {
        String json = """
                {
                    "startTime": "2030-01-15T10:00:00",
                    "endTime": "2030-01-15T12:00:00"
                }
                """;

        ResponseEntity<String> response = post("/reservations", json, login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("roomId");
    }

    @Test
    void startInPast_returns400() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, "2020-01-15T10:00:00", "2020-01-15T12:00:00"), login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("startTime");
    }

    @Test
    void reservationLongerThan4Hours_returns400() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, "2030-01-15T08:00:00", "2030-01-15T13:00:00"), login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("at most 4 hours");
    }

    @Test
    void nonExistingRoom_returns400() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(999, "2030-01-15T10:00:00", "2030-01-15T12:00:00"), login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("does not exist");
    }

    // ---------- bezbednost ----------

    @Test
    void wrongPassword_returns401() {
        ResponseEntity<String> response = client.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "email": "mate@etf.rs", "password": "wrong" }
                        """)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void createWithoutToken_returns401() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"), null);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void cancelSomeoneElsesReservation_returns403() {
        Integer id = insertReservation(1, 1);   // Matina rezervacija

        ResponseEntity<String> response = patch("/reservations/" + id + "/cancel", login(ANA));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void adminCanCancelAnyReservation() {
        Integer id = insertReservation(1, 1);   // Matina rezervacija

        ResponseEntity<String> response = patch("/reservations/" + id + "/cancel", login(ADMIN));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void listAllReservations_asStudent_returns403() {
        ResponseEntity<String> response = get("/reservations", login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void listAllReservations_asAdmin_returns200() {
        ResponseEntity<String> response = get("/reservations", login(ADMIN));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void mine_returnsOnlyOwnReservations() {
        insertReservation(1, 1);   // Matina
        insertReservation(2, 2);   // Anina

        ResponseEntity<String> response = get("/reservations/mine", login(MATE));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"userId\":1");
        assertThat(response.getBody()).doesNotContain("\"userId\":2");
    }

    // ---------- pomoćne metode ----------

    private String login(String email) {
        LoginResponse response = client.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "email": "%s", "password": "password123" }
                        """.formatted(email))
                .retrieve()
                .body(LoginResponse.class);
        return response.token();
    }

    private Integer insertReservation(int userId, int roomId) {
        return jdbcClient.sql("""
                        INSERT INTO reservations (user_id, room_id, start_time, end_time)
                        VALUES (:userId, :roomId, '2030-01-15 10:00', '2030-01-15 12:00')
                        RETURNING id
                        """)
                .param("userId", userId)
                .param("roomId", roomId)
                .query(Integer.class)
                .single();
    }

    private String reservationJson(int roomId, String start, String end) {
        return """
                {
                    "roomId": %d,
                    "startTime": "%s",
                    "endTime": "%s"
                }
                """.formatted(roomId, start, end);
    }

    private ResponseEntity<String> post(String path, String json, String token) {
        return client.post()
                .uri(path)
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    private ResponseEntity<String> patch(String path, String token) {
        return client.patch()
                .uri(path)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toEntity(String.class);
    }

    private ResponseEntity<String> get(String path, String token) {
        return client.get()
                .uri(path)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toEntity(String.class);
    }
}