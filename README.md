# Room Booking System

A system for booking classrooms and labs at a university faculty.
Users can view available rooms, make reservations, and cancel them.
Overlapping reservations are prevented at the database level.

## Tech stack
- Java 25
- Spring Boot
- PostgreSQL

## Database setup
1. Create a database named `room_booking`
2. Run `database/schema.sql`
3. Run `database/data.sql` (sample data)

## API endpoints
| Method | Endpoint | Description |
|---|---|---|
| GET | `/rooms` | List all rooms |
| GET | `/rooms/{id}/availability?date=YYYY-MM-DD` | Free time slots for a room on a given day |
| GET | `/reservations` | List all reservations |
| GET | `/reservations?userId={id}` | Reservations of one user |
| POST | `/reservations` | Create a reservation |
| PATCH | `/reservations/{id}/cancel` | Cancel a reservation |

## Error handling
Errors are returned in the standard Problem Details format (RFC 9457).

| Situation | Status |
|---|---|
| Reservation does not exist | 404 Not Found |
| Room already booked in that time slot | 409 Conflict |
| Missing or invalid fields, start time in the past | 400 Bad Request |
| End before start, or longer than 4 hours | 400 Bad Request |
| User or room does not exist | 400 Bad Request |

Overlapping reservations are blocked by a PostgreSQL exclusion constraint,
so the rule holds even when two requests arrive at the same moment.
*Work in progress.*