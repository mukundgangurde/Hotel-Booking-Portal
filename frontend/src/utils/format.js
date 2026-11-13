export function localDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function dateOffset(days) {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return localDate(date)
}

export const TODAY = localDate(new Date())

export function getDefaultDates() {
  return {
    checkIn: dateOffset(1),
    checkOut: dateOffset(2),
  }
}

export function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date(`${value}T00:00:00`))
}

export function formatMoney(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value || 0)
}

export function countNights(checkIn, checkOut) {
  if (!checkIn || !checkOut) return 0
  return Math.max(
    0,
    (new Date(`${checkOut}T00:00:00`) - new Date(`${checkIn}T00:00:00`)) / 86400000,
  )
}
