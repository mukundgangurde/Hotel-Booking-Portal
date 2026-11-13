import { useState } from 'react'
import { bookingApi } from '../services/bookingApi.js'
import { formatDate, formatMoney } from '../utils/format.js'

function BookingLookupPage() {
  const [reference, setReference] = useState('')
  const [email, setEmail] = useState('')
  const [booking, setBooking] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [cancelling, setCancelling] = useState(false)

  async function lookUpBooking(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setBooking(null)
    try {
      const result = await bookingApi.findBooking(reference.trim(), email.trim())
      setBooking(result)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  async function cancelBooking() {
    if (!booking || booking.status !== 'CONFIRMED') return
    if (!window.confirm('Cancel this booking?')) return

    setCancelling(true)
    setError('')
    try {
      const result = await bookingApi.cancelBooking(booking.bookingReference, email.trim())
      setBooking(result)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setCancelling(false)
    }
  }

  return (
    <main className="lookup-page">
      <section className="lookup-intro">
        <p className="eyebrow">MANAGE YOUR STAY</p>
        <h1>Find your booking.</h1>
        <p>Use your booking reference and email address to view or cancel a reservation.</p>
      </section>

      <section className="lookup-card">
        <h2>Booking details</h2>
        <form className="lookup-form" onSubmit={lookUpBooking}>
          <label className="form-field">
            <span>Booking reference</span>
            <input
              value={reference}
              onChange={(event) => setReference(event.target.value)}
              placeholder="Example: HB-AB12CD34EF56"
              required
            />
          </label>
          <label className="form-field">
            <span>Email address</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="The email used to book"
              required
            />
          </label>
          {error && <p className="message error-message" role="alert">{error}</p>}
          <button className="button button-dark" type="submit" disabled={loading}>
            {loading ? 'Searching…' : 'Find booking'}
          </button>
        </form>

        {booking && (
          <article className="booking-result">
            <div className="booking-result-top">
              <div>
                <p className="eyebrow">{booking.bookingReference}</p>
                <h3>{booking.hotelName}</h3>
              </div>
              <span className={`booking-status ${booking.status === 'CANCELLED' ? 'cancelled' : ''}`}>
                {booking.status}
              </span>
            </div>
            <p>{booking.roomName}</p>
            <p>{formatDate(booking.checkIn)} – {formatDate(booking.checkOut)}</p>
            <div className="booking-result-bottom">
              <strong>{formatMoney(booking.totalAmount)}</strong>
              {booking.status === 'CONFIRMED' && (
                <button className="button button-cancel" type="button" onClick={cancelBooking} disabled={cancelling}>
                  {cancelling ? 'Cancelling…' : 'Cancel booking'}
                </button>
              )}
            </div>
          </article>
        )}
      </section>
    </main>
  )
}

export default BookingLookupPage
