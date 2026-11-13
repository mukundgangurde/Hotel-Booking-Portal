# Hotel Booking Portal

A hotel-booking project with a Java 17 / Spring Boot REST API, MySQL persistence, and a
React + Vite frontend.

The frontend lets guests search hotels, check availability, view a price quote, create a
booking, retrieve it by reference and email, and cancel it.
Hotel cards open a dedicated details page that requests the latest room availability for the
selected dates.

## Requirements

- JDK 17 (set as the Project SDK in IntelliJ IDEA)
- IntelliJ IDEA
- MySQL 8 or later
- Node.js and npm for the frontend

## 1. Create the MySQL database

Start MySQL, then open Command Prompt and connect:

```bat
mysql -u root -p
```

Enter your MySQL password, then run:

```sql
CREATE DATABASE hotel_booking_db;
EXIT;
```

If the `mysql` command is not recognized, start MySQL and create the database using MySQL
Workbench instead.

## 2. Configure backend environment variables

The backend reads local settings from `backend\.env`. This file is ignored by Git so your
local database password is not committed. For a fresh clone, create it from the example in
Command Prompt at the repository root:

```bat
copy backend\.env.example backend\.env
```

Open `backend\.env` and replace `change_this_to_your_mysql_password` with your MySQL
password. Change `DB_USERNAME` too if your MySQL user is not `root`. The file contains:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/hotel_booking_db
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
DDL_AUTO=update
FRONTEND_ORIGIN=http://localhost:5173
```

Keep `backend\.env` local. Commit changes to `backend\.env.example` only when changing the
names or adding non-secret configuration values; never put a real password in the example.
The backend does not supply a default database password; configure `DB_PASSWORD` before
starting it.

## 3. Run the backend in IntelliJ IDEA

1. In IntelliJ, choose **File > Open** and open the repository root folder
   (`Hotel-Booking-Portal`).
2. Set **Project SDK** to JDK 17 in **File > Project Structure > Project**.
3. Open `backend\pom.xml` as a Maven project and allow IntelliJ to import it.
4. Open
   `backend\src\main\java\com\hotelbookingportal\BackendApplication.java` and run
   `BackendApplication`.
5. In **Run > Edit Configurations**, set **Working directory** to the repository root. Spring
   Boot loads `backend\.env` from there; no IntelliJ EnvFile plugin or manually entered
   environment variables are needed.
6. The API starts at `http://localhost:8080`. On first startup, five sample hotels and their
   room types are inserted if the hotels table is empty.

You can also run the backend from **Command Prompt** at the repository root. The same
`backend\.env` file is loaded automatically:

```bat
backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Tests use an in-memory H2 database, so they do not need MySQL:

```bat
backend\mvnw.cmd -f backend\pom.xml test
```

## 4. Run the frontend

The frontend is a separate Node.js app. Open a second Command Prompt at the repository root:

```bat
cd frontend
copy .env.example .env
npm install
npm run dev
```

`frontend\.env` sets `VITE_API_BASE_URL` to `http://localhost:8080/api`. The UI uses that
default if the file is not present. Open the local address printed by Vite (usually
`http://localhost:5173`). Make sure the backend is running first.

## Backend API

Start the backend before sending requests. In Postman, create an environment named
`Hotel Booking Local` and add these variables:

| Variable | Initial value |
| --- | --- |
| `baseUrl` | `http://localhost:8080` |
| `checkIn` | `2027-01-10` |
| `checkOut` | `2027-01-12` |
| `guestName` | `Asha Rao` |
| `guestEmail` | `asha@example.com` |
| `hotelId` | Leave empty; the search request sets it |
| `roomTypeId` | Leave empty; the search request sets it |
| `bookingReference` | Leave empty; the booking request sets it |

Select this environment in Postman. The example dates are in `YYYY-MM-DD` format and must
be today or later when you run the requests. Update both dates together if they are in the
past. Check-in is inclusive and check-out is exclusive.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/hotels?location=Mumbai` | Browse hotels and optionally search by location |
| `GET` | `/api/hotels?location=Mumbai&checkIn=2027-01-10&checkOut=2027-01-12` | Search hotels and include room availability |
| `GET` | `/api/hotels/{hotelId}?checkIn=2027-01-10&checkOut=2027-01-12` | View hotel details and room availability |
| `POST` | `/api/bookings/quote` | Get a server-calculated price quote and current availability |
| `POST` | `/api/bookings` | Book one room |
| `GET` | `/api/bookings/{reference}?email=guest@example.com` | Retrieve a booking using its reference and email |
| `DELETE` | `/api/bookings/{reference}?email=guest@example.com` | Cancel a booking |

### Postman test sequence

Run these requests in order. The first request saves the hotel and room-type IDs; the booking
request saves its generated reference for lookup and cancellation.

#### 1. Search for a hotel

- Method: `GET`
- URL: `{{baseUrl}}/api/hotels`
- In **Params**, add `location` = `Mumbai`, `checkIn` = `{{checkIn}}`, and `checkOut` =
  `{{checkOut}}`.
- Expected status: `200 OK`. The result is a JSON array. For the seeded Mumbai hotel, its
  room type is `Standard Room` at `6500.00` per night, with 8 rooms before any matching
  bookings. Database IDs and current availability can differ.

In the **Scripts > Post-response** tab, add this script to save the IDs from the response:

```javascript
const hotels = pm.response.json();
const hotel = hotels.find(item =>
  item.location.toLowerCase().includes("mumbai")
);

pm.test("Mumbai hotel is returned", () => {
  pm.expect(hotel).to.exist;
});

if (hotel && hotel.roomTypes.length > 0) {
  pm.environment.set("hotelId", hotel.id);
  pm.environment.set("roomTypeId", hotel.roomTypes[0].id);
}
```

#### 2. View hotel details and date-specific availability

- Method: `GET`
- URL: `{{baseUrl}}/api/hotels/{{hotelId}}?checkIn={{checkIn}}&checkOut={{checkOut}}`
- Expected status: `200 OK`.
- Check `roomTypes[0].availableRooms`. This is the live availability for the selected dates;
  without dates the field is `null`.

#### 3. Get a price quote

- Method: `POST`
- URL: `{{baseUrl}}/api/bookings/quote`
- In **Headers**, set `Content-Type` = `application/json`.
- In **Body > raw > JSON**, enter:

```json
{
  "roomTypeId": {{roomTypeId}},
  "checkIn": "{{checkIn}}",
  "checkOut": "{{checkOut}}"
}
```

Expected status: `200 OK`. On a fresh database with no overlapping bookings, the seeded
Mumbai room returns a two-night quote of `13000.00` and 8 available rooms. Availability may
be lower if there are already confirmed bookings.

#### 4. Create a booking

- Method: `POST`
- URL: `{{baseUrl}}/api/bookings`
- In **Headers**, set `Content-Type` = `application/json`.
- In **Body > raw > JSON**, enter:

```json
{
  "roomTypeId": {{roomTypeId}},
  "customerName": "{{guestName}}",
  "email": "{{guestEmail}}",
  "checkIn": "{{checkIn}}",
  "checkOut": "{{checkOut}}"
}
```

Expected status: `200 OK`. The response contains a generated `bookingReference`, a
server-calculated `totalAmount`, and status `CONFIRMED`. For example:

```json
{
  "bookingReference": "HB-7F3A9D2C1B4E",
  "hotelId": 1,
  "hotelName": "The Harbour House",
  "roomTypeId": 1,
  "roomName": "Standard Room",
  "customerName": "Asha Rao",
  "email": "asha@example.com",
  "checkIn": "2027-01-10",
  "checkOut": "2027-01-12",
  "totalAmount": 13000.00,
  "status": "CONFIRMED",
  "createdAt": "2026-11-13T12:00:00"
}
```

The reference and `createdAt` above are examples; the API generates new values. In
**Scripts > Post-response**, save the actual reference:

```javascript
const booking = pm.response.json();

pm.test("Booking is confirmed", () => {
  pm.response.to.have.status(200);
  pm.expect(booking.status).to.eql("CONFIRMED");
});

pm.environment.set("bookingReference", booking.bookingReference);
```

After a successful booking, repeat request 2 with the same dates. The available room count
should decrease by one.

#### 5. Retrieve the booking

- Method: `GET`
- URL: `{{baseUrl}}/api/bookings/{{bookingReference}}`
- In **Params**, add `email` = `{{guestEmail}}`.
- Expected status: `200 OK`, with the same reference and `CONFIRMED` status.

The email must match the email used to create the booking. An unknown reference or mismatched
email returns `404 Not Found`.

#### 6. Cancel the booking

- Method: `DELETE`
- URL: `{{baseUrl}}/api/bookings/{{bookingReference}}`
- In **Params**, add `email` = `{{guestEmail}}`.
- Expected status: `200 OK`, with status `CANCELLED`.

Repeat request 2 once more. Availability for the selected dates should return to its
pre-booking count. Repeating the cancellation is safe; it returns the already-cancelled
booking.

### Example error checks

To check invalid date handling, send this body to `POST {{baseUrl}}/api/bookings/quote`.
With the configured two-night date range, both dates are in the future but check-out is
before check-in:

```json
{
  "roomTypeId": {{roomTypeId}},
  "checkIn": "{{checkOut}}",
  "checkOut": "{{checkIn}}"
}
```

Expected status: `400 Bad Request`, with a response similar to:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Check-out must be after check-in",
  "fieldErrors": {}
}
```

When all rooms for a date range are reserved, the quote and booking endpoints return
`409 Conflict` with message `No rooms are available for the selected dates`. The seeded
Mumbai room type has 8 rooms, so use a test database and cancel any test bookings after
checking this case. Automated tests cover sold-out and simultaneous-booking behavior:

```bat
backend\mvnw.cmd -f backend\pom.xml test
```

Booking amounts are calculated by the backend from the room price and number of nights; do
not send a price in the booking request. Error responses include `timestamp`, `status`,
`error`, `message`, and `fieldErrors`.

## Booking assumptions and surprise feature

- Each booking reserves one room.
- Check-in is inclusive and check-out is exclusive. A guest checking out on a date does not
  overlap a guest checking in on that same date.
- For a multi-night stay, availability is based on the peak number of overlapping confirmed
  reservations on any night in the stay.
- Booking creation locks the room type and checks availability inside a database transaction,
  preventing concurrent requests from booking beyond the available capacity.
- Cancelling a confirmed booking releases the room. Lookup and cancellation both require the
  booking reference and matching email.
- The Trip Budget Estimator compares one-room stay totals for the selected dates and can filter
  out hotels above an optional budget. Estimates use the listed nightly price; the backend quote
  remains the final price before booking.

## Design and screenshots

See [DESIGN.md](./DESIGN.md) for the application structure, data model, booking flow, and UI
design notes. Project screenshots are in [`docs/screenshots/`](./docs/screenshots/).

![Stayfolk hotel search page](./docs/screenshots/01-home.png)

Additional captures: [hotel details](./docs/screenshots/02-hotel-details.png),
[booking confirmation](./docs/screenshots/03-booking-confirmation.png),
[manage booking](./docs/screenshots/04-manage-booking.png),
[cancelled booking](./docs/screenshots/05-cancelled-booking.png),
[budget estimator](./docs/screenshots/06-budget-estimator.png), and
[mobile layout](./docs/screenshots/07-mobile-layout.png).

## AI assistance

GitHub Copilot was used to help implement and document this project. The project author should
review the final code. The project author reports spending **8 hours** on the project.

## Current limitations

- Hotel photos are loaded from Unsplash URLs and require an internet connection.
- The displayed budget estimate is for one room only; the server-generated booking quote is
  authoritative.
