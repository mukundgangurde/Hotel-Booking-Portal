import { formatMoney } from '../utils/format.js'
import { getHotelPhoto } from '../utils/hotelPhoto.js'

function HotelCard({ hotel, index, nights, onDetails }) {
  const room = hotel.roomTypes?.[0]
  const availability = room?.availableRooms
  const estimatedTotal = room && nights > 0
    ? Number(room.pricePerNight) * nights
    : null

  return (
    <article className="hotel-card">
      <button className="hotel-photo-button" type="button" onClick={onDetails}>
        <img src={getHotelPhoto(hotel, index)} alt={`${hotel.name} in ${hotel.location}`} />
        <span className="photo-location">{hotel.location}</span>
      </button>
      <div className="hotel-card-content">
        <div className="hotel-title-row">
          <div>
            <h2>{hotel.name}</h2>
            <p className="hotel-location">{hotel.location}</p>
          </div>
          {room && <span className="room-type">{room.roomName}</span>}
        </div>
        <p className="hotel-description">{hotel.description}</p>
        <div className="amenity-list">
          {(hotel.amenities || []).slice(0, 3).map((amenity) => (
            <span key={amenity}>{amenity}</span>
          ))}
        </div>
        <div className="hotel-card-bottom">
          <div>
            <strong>{room ? formatMoney(room.pricePerNight) : 'Price unavailable'}</strong>
            {room && <span className="per-night"> / night</span>}
            {estimatedTotal != null && (
              <p className="stay-estimate">
                {formatMoney(estimatedTotal)} estimated for {nights} {nights === 1 ? 'night' : 'nights'}
              </p>
            )}
            <p className="availability">
              {availability == null
                ? 'Select dates to check rooms'
                : availability > 0
                  ? `${availability} rooms available`
                  : 'No rooms available for these dates'}
            </p>
          </div>
          <button className="button button-outline" type="button" onClick={onDetails}>
            View details
          </button>
        </div>
      </div>
    </article>
  )
}

export default HotelCard
