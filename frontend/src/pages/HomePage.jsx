import { useCallback, useEffect, useState } from 'react'
import BudgetEstimator from '../components/BudgetEstimator.jsx'
import HotelCard from '../components/HotelCard.jsx'
import SearchForm from '../components/SearchForm.jsx'
import { bookingApi } from '../services/bookingApi.js'
import { countNights, getDefaultDates, localDate } from '../utils/format.js'

const INITIAL_DATES = getDefaultDates()

function HomePage({ initialFilters, onViewHotel }) {
  const [filters, setFilters] = useState(() => ({
    location: '',
    ...INITIAL_DATES,
    ...initialFilters,
  }))
  const [hotels, setHotels] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [maxBudget, setMaxBudget] = useState('')
  const nights = countNights(filters.checkIn, filters.checkOut)
  const visibleHotels = maxBudget === ''
    ? hotels
    : hotels.filter((hotel) => {
      const room = hotel.roomTypes?.[0]
      return room && Number(room.pricePerNight) * nights <= Number(maxBudget)
    })
  useEffect(() => {
    let ignore = false
    bookingApi.findHotels({
      location: initialFilters?.location || '',
      checkIn: initialFilters?.checkIn || INITIAL_DATES.checkIn,
      checkOut: initialFilters?.checkOut || INITIAL_DATES.checkOut,
    })
      .then((results) => {
        if (!ignore) setHotels(results)
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
  }, [initialFilters])

  const searchHotels = useCallback(async (searchFilters) => {
    if (searchFilters.checkIn < localDate(new Date())) {
      setError('Check-in cannot be in the past.')
      return
    }
    if (searchFilters.checkOut <= searchFilters.checkIn) {
      setError('Check-out must be after check-in.')
      return
    }

    setLoading(true)
    setError('')
    try {
      const results = await bookingApi.findHotels(searchFilters)
      setHotels(results)
    } catch (requestError) {
      setHotels([])
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }, [])

  function updateFilter(field, value) {
    setFilters((current) => ({ ...current, [field]: value }))
  }

  function submitSearch(event) {
    event.preventDefault()
    searchHotels(filters)
  }

  return (
    <main>
      <section className="welcome-panel">
        <div className="welcome-copy">
          <p className="eyebrow">HOTEL BOOKING, MADE SIMPLE</p>
          <h1>Find a stay<br />that feels <span>right.</span></h1>
          <p>Search hotels, check your dates, and see the full price before you book.</p>
        </div>
        <div className="welcome-note">
          <span aria-hidden="true">✦</span>
          <p>Pick your place.<br />We’ll take care of the details.</p>
        </div>
      </section>

      <section className="search-section" aria-label="Search hotels">
        <h2>Where are you going?</h2>
        <SearchForm
          filters={filters}
          onChange={updateFilter}
          onSubmit={submitSearch}
          loading={loading}
        />
        <BudgetEstimator
          maxBudget={maxBudget}
          nights={nights}
          onChange={setMaxBudget}
        />
        <p className="search-caption">
          {nights} {nights === 1 ? 'night' : 'nights'} · One room
        </p>
      </section>

      <section className="results-section" id="stays">
        <div className="results-heading">
          <div>
            <p className="eyebrow">YOUR NEXT STAY</p>
            <h2>{filters.location ? `Hotels in ${filters.location}` : 'Hotels to explore'}</h2>
          </div>
          {!loading && !error && (
            <span className="result-count">
              {maxBudget === ''
                ? `${hotels.length} ${hotels.length === 1 ? 'hotel' : 'hotels'}`
                : `${visibleHotels.length} of ${hotels.length} within budget`}
            </span>
          )}
        </div>

        {error && (
          <div className="message error-message" role="alert">
            <span>{error}</span>
            <button type="button" onClick={() => searchHotels(filters)}>Try again</button>
          </div>
        )}

        {loading ? (
          <div className="loading-state">Loading hotels…</div>
        ) : !error && visibleHotels.length === 0 ? (
          <div className="empty-state">
            {hotels.length > 0 && maxBudget !== '' ? (
              <>
                <h3>No stays fit this budget</h3>
                <p>Increase your room budget or clear the limit to see all matching hotels.</p>
                <button className="button button-outline" type="button" onClick={() => setMaxBudget('')}>
                  Clear budget limit
                </button>
              </>
            ) : (
              <>
                <h3>No hotels found</h3>
                <p>Try another destination, or clear the search to see all hotels.</p>
                <button
                  className="button button-outline"
                  type="button"
                  onClick={() => {
                    const resetFilters = { ...filters, location: '' }
                    setFilters(resetFilters)
                    searchHotels(resetFilters)
                  }}
                >
                  Show all hotels
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="hotel-grid">
            {visibleHotels.map((hotel, index) => (
              <HotelCard
                hotel={hotel}
                index={index}
                nights={nights}
                key={hotel.id}
                onDetails={() => onViewHotel(hotel.id, filters)}
              />
            ))}
          </div>
        )}
      </section>
    </main>
  )
}

export default HomePage
