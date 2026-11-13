package com.hotelbookingportal;

import com.hotelbookingportal.dto.BookingRequest;
import com.hotelbookingportal.dto.BookingResponse;
import com.hotelbookingportal.dto.QuoteRequest;
import com.hotelbookingportal.entity.RoomType;
import com.hotelbookingportal.repository.HotelRepository;
import com.hotelbookingportal.repository.RoomTypeRepository;
import com.hotelbookingportal.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BackendApplicationTests {

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private RoomTypeRepository roomTypeRepository;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void sampleHotelsAreSeededAndCancellationRestoresAvailability() {
		assertEquals(5, hotelRepository.count());
		RoomType roomType = roomTypeRepository.findAll().get(0);
		LocalDate checkIn = LocalDate.now().plusDays(2);
		LocalDate checkOut = checkIn.plusDays(2);
		int initialAvailability = bookingService.quote(
				new QuoteRequest(roomType.getId(), checkIn, checkOut)).availableRooms();

		BookingResponse booking = bookingService.createBooking(new BookingRequest(
				roomType.getId(), "Test Guest", "test@example.com", checkIn, checkOut));
		assertNotNull(booking.bookingReference());
		assertEquals(initialAvailability - 1, bookingService.quote(
				new QuoteRequest(roomType.getId(), checkIn, checkOut)).availableRooms());

		bookingService.cancelBooking(booking.bookingReference(), "test@example.com");
		assertEquals(initialAvailability, bookingService.quote(
				new QuoteRequest(roomType.getId(), checkIn, checkOut)).availableRooms());
	}

	@Test
	@Transactional
	void hotelSearchUpdatesRoomAvailabilityAndRejectsBookingWhenSoldOut() throws Exception {
		RoomType roomType = roomTypeRepository.findAll().get(0);
		roomType.setTotalRooms(1);
		roomTypeRepository.save(roomType);
		LocalDate checkIn = LocalDate.now().plusDays(40);
		LocalDate checkOut = checkIn.plusDays(2);
		Long hotelId = roomType.getHotel().getId();
		String bookingRequest = "{\"roomTypeId\":" + roomType.getId()
				+ ",\"customerName\":\"Availability Test\""
				+ ",\"email\":\"availability-test@example.com\""
				+ ",\"checkIn\":\"" + checkIn + "\""
				+ ",\"checkOut\":\"" + checkOut + "\"}";

		mockMvc.perform(get("/api/hotels/{hotelId}", hotelId)
						.param("checkIn", checkIn.toString())
						.param("checkOut", checkOut.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roomTypes[0].availableRooms").value(1));
		mockMvc.perform(post("/api/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(bookingRequest))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/hotels/{hotelId}", hotelId)
						.param("checkIn", checkIn.toString())
						.param("checkOut", checkOut.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roomTypes[0].availableRooms").value(0));
		mockMvc.perform(post("/api/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(bookingRequest))
				.andExpect(status().isConflict());
	}

	@Test
	@Transactional
	void bookingCanBeRetrievedAndCancelledThroughHttp() throws Exception {
		RoomType roomType = roomTypeRepository.findAll().get(0);
		roomType.setTotalRooms(1);
		roomTypeRepository.save(roomType);
		LocalDate checkIn = LocalDate.now().plusDays(60);
		LocalDate checkOut = checkIn.plusDays(2);
		String bookingRequest = bookingRequest(roomType.getId(), checkIn, checkOut);

		String createResponse = mockMvc.perform(post("/api/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(bookingRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Matcher referenceMatcher = Pattern.compile("\"bookingReference\":\"([^\"]+)\"")
				.matcher(createResponse);
		assertTrue(referenceMatcher.find(), "Booking response should contain its reference");
		String reference = referenceMatcher.group(1);

		mockMvc.perform(get("/api/bookings/{reference}", reference)
						.param("email", "availability-test@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookingReference").value(reference))
				.andExpect(jsonPath("$.status").value("CONFIRMED"));

		mockMvc.perform(delete("/api/bookings/{reference}", reference)
						.param("email", "availability-test@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		mockMvc.perform(get("/api/bookings/{reference}", reference)
						.param("email", "availability-test@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	void simultaneousBookingsCannotReserveTheLastRoomTwice() throws Exception {
		RoomType roomType = roomTypeRepository.findAll().get(0);
		roomType.setTotalRooms(1);
		roomTypeRepository.saveAndFlush(roomType);
		LocalDate checkIn = LocalDate.now().plusDays(80);
		LocalDate checkOut = checkIn.plusDays(2);
		String request = bookingRequest(roomType.getId(), checkIn, checkOut);
		CountDownLatch startTogether = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<Integer> first = executor.submit(() -> submitWhenReady(startTogether, request));
			Future<Integer> second = executor.submit(() -> submitWhenReady(startTogether, request));
			startTogether.countDown();

			int firstStatus = first.get(10, TimeUnit.SECONDS);
			int secondStatus = second.get(10, TimeUnit.SECONDS);
			assertEquals(1, (firstStatus == 200 ? 1 : 0) + (secondStatus == 200 ? 1 : 0));
			assertEquals(1, (firstStatus == 409 ? 1 : 0) + (secondStatus == 409 ? 1 : 0));
		} finally {
			executor.shutdownNow();
		}
	}

	private String bookingRequest(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
		return "{\"roomTypeId\":" + roomTypeId
				+ ",\"customerName\":\"Availability Test\""
				+ ",\"email\":\"availability-test@example.com\""
				+ ",\"checkIn\":\"" + checkIn + "\""
				+ ",\"checkOut\":\"" + checkOut + "\"}";
	}

	private int submitWhenReady(CountDownLatch startTogether, String request) throws Exception {
		startTogether.await();
		return mockMvc.perform(post("/api/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andReturn()
				.getResponse()
				.getStatus();
	}
}
