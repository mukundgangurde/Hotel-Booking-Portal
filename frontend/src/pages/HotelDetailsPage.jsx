import { useEffect, useState } from 'react'
import BookingDialog from '../components/BookingDialog.jsx'
import { bookingApi } from '../services/bookingApi.js'
import { formatDate, formatMoney } from '../utils/format.js'
import { getHotelPhoto } from '../utils/hotelPhoto.js'

function HotelDetailsPage({ hotelId, dates, backHref }) {
  const [hotel, setHotel] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [bookingSelection, setBookingSelection] = useState(null)

  useEffect(() => {
    let ignore = false
    bookingApi.getHotel(hotelId, dates)
      .then((result) => {
        if (!ignore) setHotel(result)
      })
      .catch((requestError) => {
        if (!ignore) setError(requestError.message)
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [hotelId, dates])

  async function refreshAvailability(roomTypeId) {
    setHotel((currentHotel) => currentHotel && ({
      ...currentHotel,
      roomTypes: currentHotel.roomTypes.map((room) => (
        room.id === roomTypeId && room.availableRooms != null
          ? { ...room, availableRooms: Math.max(0, room.availableRooms - 1) }
          : room
      )),
    }))
    setError('')

    try {
      const result = await bookingApi.getHotel(hotelId, dates)
      setHotel(result)
    } catch (requestError) {
      setError(`Booking confirmed, but availability could not be refreshed: ${requestError.message}`)
    }
  }

  return (
    <main className="hotel-details-page">
      <a className="back-link" href={backHref}>← Back to search results</a>
      {loading && <div className="loading-state">Loading hotel details…</div>}
      {error && <div className="message error-message" role="alert">{error}</div>}
      {!loading && !error && !hotel && (
        <div className="empty-state">
          <h1>Hotel not found</h1>
          <p>It may have been removed or the link may be incorrect.</p>
        </div>
      )}
      {hotel && (
        <article className="hotel-page-card">
          <img className="hotel-page-photo" src={getHotelPhoto(hotel, 0)} alt={hotel.name} />
          <div className="hotel-page-content">
            <p className="eyebrow">{hotel.location}</p>
            <h1>{hotel.name}</h1>
            <p className="hotel-page-description">{hotel.description}</p>

            <section aria-labelledby="hotel-amenities-title">
              <h2 id="hotel-amenities-title">Amenities</h2>
              <div className="amenity-list">
                {(hotel.amenities || []).map((amenity) => <span key={amenity}>{amenity}</span>)}
              </div>
            </section>

            <section className="hotel-room-section" aria-labelledby="room-options-title">
              <div className="hotel-room-heading">
                <h2 id="room-options-title">Rooms for your dates</h2>
                <span>{formatDate(dates.checkIn)} – {formatDate(dates.checkOut)}</span>
              </div>
              <div className="room-list">
                {(hotel.roomTypes || []).map((room) => (
                  <div className="room-row" key={room.id}>
                    <div>
                      <h3>{room.roomName}</h3>
                      <p>{room.availableRooms > 0
                        ? `${room.availableRooms} rooms available`
                        : 'No rooms available for these dates'}</p>
                    </div>
                    <div className="room-booking">
                      <strong>{formatMoney(room.pricePerNight)} <small>/ night</small></strong>
                      <button
                        className="button button-dark"
                        type="button"
                        disabled={room.availableRooms === 0}
                        onClick={() => setBookingSelection({
                          hotel,
                          room,
                          dates,
                        })}
                      >
                        Check total
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>
        </article>
      )}
      {bookingSelection && (
        <BookingDialog
          selection={bookingSelection}
          onClose={() => setBookingSelection(null)}
          onCompleted={refreshAvailability}
        />
      )}
    </main>
  )
}

export default HotelDetailsPage
