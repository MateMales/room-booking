package com.mate.room_booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationApiTest {

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


    @Test
    void createReservation_returns201() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, 1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"));

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    void cancelledSlot_canBeBookedAgain() {
        Integer id = jdbcClient.sql("""
                        INSERT INTO reservations (user_id, room_id, start_time, end_time)
                        VALUES (1, 1, '2030-01-15 10:00', '2030-01-15 12:00')
                        RETURNING id
                        """)
                .query(Integer.class)
                .single();

        ResponseEntity<String> cancel = patch("/reservations/" + id + "/cancel");
        assertThat(cancel.getStatusCode().value()).isEqualTo(200);
        assertThat(cancel.getBody()).contains("\"status\":\"CANCELLED\"");

        ResponseEntity<String> rebook = post("/reservations",
                reservationJson(2, 1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"));
        assertThat(rebook.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void overlappingReservation_returns409() {
        post("/reservations",
                reservationJson(1, 1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"));

        ResponseEntity<String> response = post("/reservations",
                reservationJson(2, 1, "2030-01-15T11:00:00", "2030-01-15T13:00:00"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).contains("already booked");
    }

    @Test
    void cancelNonExistingReservation_returns404() {
        ResponseEntity<String> response = patch("/reservations/999999/cancel");

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).contains("not found");
    }

    @Test
    void missingUserId_returns400() {
        String json = """
                {
                    "roomId": 1,
                    "startTime": "2030-01-15T10:00:00",
                    "endTime": "2030-01-15T12:00:00"
                }
                """;

        ResponseEntity<String> response = post("/reservations", json);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("userId");
    }

    @Test
    void startInPast_returns400() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, 1, "2020-01-15T10:00:00", "2020-01-15T12:00:00"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("startTime");
    }

    @Test
    void reservationLongerThan4Hours_returns400() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(1, 1, "2030-01-15T08:00:00", "2030-01-15T13:00:00"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("at most 4 hours");
    }

    @Test
    void nonExistingUser_returns400() {
        ResponseEntity<String> response = post("/reservations",
                reservationJson(999, 1, "2030-01-15T10:00:00", "2030-01-15T12:00:00"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("does not exist");
    }


    private String reservationJson(int userId, int roomId, String start, String end) {
        return """
                {
                    "userId": %d,
                    "roomId": %d,
                    "startTime": "%s",
                    "endTime": "%s"
                }
                """.formatted(userId, roomId, start, end);
    }

    private ResponseEntity<String> post(String path, String json) {
        return client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    private ResponseEntity<String> patch(String path) {
        return client.patch()
                .uri(path)
                .retrieve()
                .toEntity(String.class);
    }
}