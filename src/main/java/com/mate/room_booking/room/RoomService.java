package com.mate.room_booking.room;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(20, 0);

    private static final String FREE_SLOTS_SQL = """
            WITH day_reservations AS (
                SELECT GREATEST(start_time, CAST(:dayStart AS timestamp)) AS start_time,
                       LEAST(end_time, CAST(:dayEnd AS timestamp))        AS end_time
                FROM reservations
                WHERE room_id = :roomId
                  AND status = 'ACTIVE'
                  AND start_time < CAST(:dayEnd AS timestamp)
                  AND end_time   > CAST(:dayStart AS timestamp)
            ),
            points AS (
                SELECT CAST(:dayStart AS timestamp) AS start_time,
                       CAST(:dayStart AS timestamp) AS end_time
                UNION ALL
                SELECT start_time, end_time FROM day_reservations
                UNION ALL
                SELECT CAST(:dayEnd AS timestamp),
                       CAST(:dayEnd AS timestamp)
            ),
            with_previous AS (
                SELECT start_time,
                       LAG(end_time) OVER (ORDER BY start_time, end_time) AS previous_end
                FROM points
            )
            SELECT previous_end AS free_from,
                   start_time   AS free_to
            FROM with_previous
            WHERE previous_end < start_time
            ORDER BY free_from
            """;

    private final JdbcClient jdbcClient;

    public RoomService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<FreeSlot> findFreeSlots(Integer roomId, LocalDate date) {
        return jdbcClient.sql(FREE_SLOTS_SQL)
                .param("roomId", roomId)
                .param("dayStart", date.atTime(OPENING_TIME))
                .param("dayEnd", date.atTime(CLOSING_TIME))
                .query(FreeSlot.class)
                .list();
    }
}