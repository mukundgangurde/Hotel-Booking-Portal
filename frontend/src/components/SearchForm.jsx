import { TODAY } from '../utils/format.js'

function SearchForm({ filters, onChange, onSubmit, loading }) {
  return (
    <form className="search-form" onSubmit={onSubmit}>
      <label className="form-field destination-field">
        <span>Destination</span>
        <input
          type="search"
          placeholder="Try Mumbai or Pune"
          value={filters.location}
          onChange={(event) => onChange('location', event.target.value)}
        />
      </label>
      <label className="form-field">
        <span>Check-in</span>
        <input
          type="date"
          min={TODAY}
          value={filters.checkIn}
          onChange={(event) => onChange('checkIn', event.target.value)}
          required
        />
      </label>
      <label className="form-field">
        <span>Check-out</span>
        <input
          type="date"
          min={filters.checkIn || TODAY}
          value={filters.checkOut}
          onChange={(event) => onChange('checkOut', event.target.value)}
          required
        />
      </label>
      <button className="button button-dark search-button" type="submit" disabled={loading}>
        {loading ? 'Searching…' : 'Search stays'}
      </button>
    </form>
  )
}

export default SearchForm
