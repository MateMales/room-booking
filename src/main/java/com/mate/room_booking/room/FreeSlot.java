package com.mate.room_booking.room;

import java.time.LocalDateTime;

public record FreeSlot(LocalDateTime freeFrom, LocalDateTime freeTo) {
}