package com.mate.room_booking.room;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RoomApiTest {

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
    void emptyDay_isFreeAllDay() {
        ResponseEntity<String> response = get("/rooms/3/availability?date=2030-01-15");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(
                "[{\"freeFrom\":\"2030-01-15T08:00:00\",\"freeTo\":\"2030-01-15T20:00:00\"}]");
    }

    @Test
    void reservationInMiddle_splitsDayIntoTwoSlots() {
        insertReservation(3, "2030-01-15 11:00", "2030-01-15 13:00", "ACTIVE");

        ResponseEntity<String> response = get("/rooms/3/availability?date=2030-01-15");

        assertThat(response.getBody()).isEqualTo(
                "[{\"freeFrom\":\"2030-01-15T08:00:00\",\"freeTo\":\"2030-01-15T11:00:00\"},"
                        + "{\"freeFrom\":\"2030-01-15T13:00:00\",\"freeTo\":\"2030-01-15T20:00:00\"}]");
    }

    @Test
    void reservationBeforeOpening_isCutAtOpeningTime() {
        insertReservation(3, "2030-01-15 07:00", "2030-01-15 09:00", "ACTIVE");

        ResponseEntity<String> response = get("/rooms/3/availability?date=2030-01-15");

        assertThat(response.getBody()).isEqualTo(
                "[{\"freeFrom\":\"2030-01-15T09:00:00\",\"freeTo\":\"2030-01-15T20:00:00\"}]");
    }

    @Test
    void cancelledReservation_isIgnored() {
        insertReservation(3, "2030-01-15 11:00", "2030-01-15 13:00", "CANCELLED");

        ResponseEntity<String> response = get("/rooms/3/availability?date=2030-01-15");

        assertThat(response.getBody()).isEqualTo(
                "[{\"freeFrom\":\"2030-01-15T08:00:00\",\"freeTo\":\"2030-01-15T20:00:00\"}]");
    }

    @Test
    void otherRoomsReservations_doNotAffectThisRoom() {
        insertReservation(4, "2030-01-15 11:00", "2030-01-15 13:00", "ACTIVE");

        ResponseEntity<String> response = get("/rooms/3/availability?date=2030-01-15");

        assertThat(response.getBody()).isEqualTo(
                "[{\"freeFrom\":\"2030-01-15T08:00:00\",\"freeTo\":\"2030-01-15T20:00:00\"}]");
    }

    private void insertReservation(int roomId, String start, String end, String status) {
        jdbcClient.sql("""
                        INSERT INTO reservations (user_id, room_id, start_time, end_time, status)
                        VALUES (1, :roomId, CAST(:start AS timestamp), CAST(:end AS timestamp), :status)
                        """)
                .param("roomId", roomId)
                .param("start", start)
                .param("end", end)
                .param("status", status)
                .update();
    }

    private ResponseEntity<String> get(String path) {
        return client.get()
                .uri(path)
                .retrieve()
                .toEntity(String.class);
    }
}
