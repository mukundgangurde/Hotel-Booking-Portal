# Stayfolk frontend

React + Vite frontend for the Hotel Booking Portal.

## Run locally

Start the backend first (see the [project setup guide](../README.md)). Then open Command
Prompt in this folder:

```bat
copy .env.example .env
npm install
npm run dev
```

Open the address printed by Vite, usually `http://localhost:5173`. The default backend API
URL is `http://localhost:8080/api`; set `VITE_API_BASE_URL` in `.env` if your API runs
elsewhere.

## Available booking flow

- Search hotels by location and dates.
- Estimate a one-room stay total and optionally filter by a room budget.
- View hotel details, amenities, prices, and current room counts on a dedicated page.
- Ask the backend for a stay quote before entering guest details.
- Create a booking and receive its unique reference.
- Look up a booking with its reference and email, then cancel it.

## Code layout

- `src/pages/HomePage.jsx` handles hotel search, budget filtering, and results.
- `src/pages/HotelDetailsPage.jsx` fetches current hotel and room availability.
- `src/pages/BookingLookupPage.jsx` handles finding and cancelling a reservation.
- `src/components/` contains reusable search, hotel card, budget estimator, header, and booking dialog components.
- `src/services/bookingApi.js` keeps HTTP requests separate from the page components.
- `src/utils/` contains date, currency, and hotel-photo helpers.
- `src/App.jsx` switches between search, hotel detail, and booking lookup routes using the URL hash.
