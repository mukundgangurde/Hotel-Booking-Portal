# Design and Architecture

## Product

Stayfolk is a small hotel-booking portal for guests who want to compare stays, check room
availability for exact dates, and manage a reservation without creating an account.

## Application structure

```text
React + Vite
  App and hash navigation
  Home page -> hotel search, budget estimate, hotel cards
  Hotel details page -> current rooms, quote, booking confirmation
  Booking lookup page -> reservation details and cancellation
  bookingApi -> REST requests
        |
        v
Spring Boot REST API
  controller -> request validation and HTTP responses
  service -> pricing, availability, booking lifecycle
  repository -> JPA persistence and locking
  DTOs -> API request and response shapes
  entities -> Hotel, RoomType, Booking
        |
        v
MySQL (H2 for tests)
```

The frontend keeps its page navigation in the URL hash. Hotel and date information is included
when opening a details page so that returning to search preserves the selected stay. The details
page fetches the hotel again instead of relying on possibly stale card data.

## Data model

- A `Hotel` has one or more `RoomType` records.
- A `RoomType` stores its nightly price and total room capacity.
- A `Booking` reserves one room type for a check-in/check-out range and has a `CONFIRMED` or
  `CANCELLED` status.
- Booking lookup and cancellation require both the booking reference and matching email.

## Availability and booking

Check-in is inclusive and check-out is exclusive. A booking overlaps a requested stay when its
check-in is before the requested check-out and its check-out is after the requested check-in.
For multi-night stays, the service calculates peak simultaneous occupancy across the requested
range and subtracts it from the room type's capacity. Cancelled bookings do not occupy rooms.

Booking creation locks the room type row and recalculates availability inside the transaction
before saving. This prevents simultaneous requests from reserving the same last room. The
backend calculates the final booking amount; the frontend's budget estimates are informational.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/hotels` | Search hotels by optional location and dates |
| `GET` | `/api/hotels/{hotelId}` | Fetch hotel details and availability for dates |
| `POST` | `/api/bookings/quote` | Validate dates and calculate a server-side quote |
| `POST` | `/api/bookings` | Create a one-room reservation |
| `GET` | `/api/bookings/{reference}?email=...` | Retrieve a reservation |
| `DELETE` | `/api/bookings/{reference}?email=...` | Cancel a reservation |

Dates use ISO `YYYY-MM-DD` strings. API errors use an HTTP status and a message; invalid date
ranges return `400`, unavailable inventory returns `409`, and missing bookings return `404`.

## UI design

- Cream backgrounds and dark ink text keep the interface readable; yellow is reserved for the
  brand and small highlights.
- Search results show nightly prices, estimated stay totals, and date-specific room availability.
- The optional Trip Budget Estimator filters by the total price of one room for the selected
  number of nights. The booking quote remains authoritative.
- Forms use visible labels, native date/email/number inputs, and explicit loading and error
  states.

## Configuration and testing

The backend reads database credentials from `backend\.env` or environment variables. The example
environment file contains only placeholders. Automated backend tests use an isolated in-memory
H2 database, including HTTP lifecycle and concurrent last-room cases; they do not require or
write to MySQL.

## Screenshots

- [Home and search](./docs/screenshots/01-home.png)
- [Hotel details and room choices](./docs/screenshots/02-hotel-details.png)
- [Booking confirmation](./docs/screenshots/03-booking-confirmation.png)
- [Manage booking](./docs/screenshots/04-manage-booking.png)
- [Cancelled booking](./docs/screenshots/05-cancelled-booking.png)
- [Budget estimator](./docs/screenshots/06-budget-estimator.png)
- [Mobile layout](./docs/screenshots/07-mobile-layout.png)
