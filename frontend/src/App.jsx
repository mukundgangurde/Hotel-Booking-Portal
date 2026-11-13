import { useEffect, useState } from 'react'
import SiteHeader from './components/SiteHeader.jsx'
import HomePage from './pages/HomePage.jsx'
import HotelDetailsPage from './pages/HotelDetailsPage.jsx'
import BookingLookupPage from './pages/BookingLookupPage.jsx'
import './App.css'
import { getDefaultDates } from './utils/format.js'

function getFilters(params) {
  const defaultDates = getDefaultDates()
  return {
    location: params.get('location') || '',
    checkIn: params.get('checkIn') || defaultDates.checkIn,
    checkOut: params.get('checkOut') || defaultDates.checkOut,
  }
}

function getCurrentRoute() {
  const [path, query = ''] = window.location.hash.slice(1).split('?')
  const params = new URLSearchParams(query)
  if (path === 'my-booking') return { page: 'booking' }
  if (path.startsWith('hotel/')) {
    return {
      page: 'hotel',
      hotelId: path.slice('hotel/'.length),
      dates: getFilters(params),
      backHref: `#stays?${params.toString()}`,
    }
  }
  return { page: 'home', filters: getFilters(params) }
}

function App() {
  const [route, setRoute] = useState(getCurrentRoute)

  useEffect(() => {
    const updateRoute = () => setRoute(getCurrentRoute())
    window.addEventListener('hashchange', updateRoute)
    return () => window.removeEventListener('hashchange', updateRoute)
  }, [])

  function showHotel(hotelId, filters) {
    const params = new URLSearchParams(filters)
    window.location.hash = `hotel/${hotelId}?${params.toString()}`
  }

  return (
    <div className="app-shell">
      <SiteHeader page={route.page} />
      {route.page === 'booking' ? (
        <BookingLookupPage />
      ) : route.page === 'hotel' ? (
        <HotelDetailsPage
          key={`${route.hotelId}-${route.dates.checkIn}-${route.dates.checkOut}`}
          hotelId={route.hotelId}
          dates={route.dates}
          backHref={route.backHref}
        />
      ) : (
        <HomePage initialFilters={route.filters} onViewHotel={showHotel} />
      )}
      <footer className="site-footer">
        <span>Stayfolk</span>
        <span>Find a stay that feels right.</span>
      </footer>
    </div>
  )
}

export default App
