import { useEffect, useState } from 'react'
import { bookingApi } from '../services/bookingApi.js'
import { formatDate, formatMoney } from '../utils/format.js'

function BookingDialog({ selection, onClose, onCompleted }) {
  const [quote, setQuote] = useState(null)
  const [quoteError, setQuoteError] = useState('')
  const [loadingQuote, setLoadingQuote] = useState(true)
  const [customerName, setCustomerName] = useState('')
  const [email, setEmail] = useState('')
  const [booking, setBooking] = useState(null)
  const [bookingError, setBookingError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let ignore = false
    bookingApi.getQuote({
      roomTypeId: selection.room.id,
      checkIn: selection.dates.checkIn,
      checkOut: selection.dates.checkOut,
    })
      .then((result) => {
        if (!ignore) setQuote(result)
      })
      .catch((error) => {
        if (!ignore) setQuoteError(error.message)
      })
      .finally(() => {
        if (!ignore) setLoadingQuote(false)
      })

    return () => {
      ignore = true
    }
  }, [selection])

  async function submitBooking(event) {
    event.preventDefault()
    setSaving(true)
    setBookingError('')
    try {
      const result = await bookingApi.createBooking({
        roomTypeId: selection.room.id,
        customerName,
        email,
        checkIn: selection.dates.checkIn,
        checkOut: selection.dates.checkOut,
      })
      setBooking(result)
      onCompleted(result.roomTypeId)
    } catch (error) {
      setBookingError(error.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="dialog-backdrop" onMouseDown={onClose}>
      <section
        className="dialog booking-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="booking-dialog-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="dialog-close" type="button" onClick={onClose} aria-label="Close booking">
          ×
        </button>
        {booking ? (
          <div className="confirmation">
            <span className="confirmation-check" aria-hidden="true">✓</span>
            <p className="eyebrow">BOOKING CONFIRMED</p>
            <h2 id="booking-dialog-title">You’re all set.</h2>
            <p>Your stay at <strong>{booking.hotelName}</strong> is confirmed.</p>
            <div className="reference-box">
              <span>BOOKING REFERENCE</span>
              <strong>{booking.bookingReference}</strong>
              <small>Save this reference and your email to look up your booking.</small>
            </div>
            <div className="confirmation-total">
              <span>{formatDate(booking.checkIn)} – {formatDate(booking.checkOut)}</span>
              <strong>{formatMoney(booking.totalAmount)}</strong>
            </div>
            <button className="button button-dark confirm-close" type="button" onClick={onClose}>Done</button>
          </div>
        ) : (
          <div className="dialog-content">
            <p className="eyebrow">BOOK YOUR STAY</p>
            <h2 id="booking-dialog-title">{selection.hotel.name}</h2>
            <p className="dialog-description">
              {selection.room.roomName} · {formatDate(selection.dates.checkIn)} – {formatDate(selection.dates.checkOut)}
            </p>

            {loadingQuote && <p className="inline-message">Checking availability and total…</p>}
            {quoteError && <p className="message error-message" role="alert">{quoteError}</p>}
            {quote && (
              <>
                <div className="quote-total">
                  <span>{formatMoney(quote.pricePerNight)} × {quote.nights} {quote.nights === 1 ? 'night' : 'nights'}</span>
                  <strong>{formatMoney(quote.totalAmount)}</strong>
                </div>
                <p className="quote-caption">Total for your stay, calculated by the hotel service.</p>
                <form className="guest-form" onSubmit={submitBooking}>
                  <label>
                    <span>Your name</span>
                    <input
                      value={customerName}
                      onChange={(event) => setCustomerName(event.target.value)}
                      autoComplete="name"
                      required
                    />
                  </label>
                  <label>
                    <span>Email address</span>
                    <input
                      type="email"
                      value={email}
                      onChange={(event) => setEmail(event.target.value)}
                      autoComplete="email"
                      required
                    />
                  </label>
                  {bookingError && <p className="message error-message" role="alert">{bookingError}</p>}
                  <button className="button button-dark confirm-button" type="submit" disabled={saving}>
                    {saving ? 'Confirming…' : 'Confirm booking'}
                  </button>
                </form>
              </>
            )}
          </div>
        )}
      </section>
    </div>
  )
}

export default BookingDialog
