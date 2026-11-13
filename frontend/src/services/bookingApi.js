const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api')
  .replace(/\/+$/, '')

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers,
      },
    })
  } catch {
    throw new Error('Cannot reach the booking service. Check that the backend is running.')
  }

  const result = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(result?.message || `Request failed (${response.status})`)
  }
  return result
}

export const bookingApi = {
  findHotels(filters) {
    const params = new URLSearchParams({
      checkIn: filters.checkIn,
      checkOut: filters.checkOut,
    })
    if (filters.location.trim()) params.set('location', filters.location.trim())
    return request(`/hotels?${params.toString()}`)
  },

  getHotel(hotelId, dates) {
    const params = new URLSearchParams({
      checkIn: dates.checkIn,
      checkOut: dates.checkOut,
    })
    return request(`/hotels/${encodeURIComponent(hotelId)}?${params.toString()}`)
  },

  getQuote(bookingDetails) {
    return request('/bookings/quote', {
      method: 'POST',
      body: JSON.stringify(bookingDetails),
    })
  },

  createBooking(bookingDetails) {
    return request('/bookings', {
      method: 'POST',
      body: JSON.stringify(bookingDetails),
    })
  },

  findBooking(reference, email) {
    return request(
      `/bookings/${encodeURIComponent(reference)}?email=${encodeURIComponent(email)}`,
    )
  },

  cancelBooking(reference, email) {
    return request(
      `/bookings/${encodeURIComponent(reference)}?email=${encodeURIComponent(email)}`,
      { method: 'DELETE' },
    )
  },
}
