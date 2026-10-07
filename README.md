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

*Work in progress.*