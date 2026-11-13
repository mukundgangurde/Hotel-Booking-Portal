function SiteHeader({ page }) {
  return (
    <header className="site-header">
      <a className="brand" href="#stays" aria-label="Stayfolk home">
        <span className="brand-icon" aria-hidden="true">S</span>
        <span>stayfolk</span>
      </a>
      <nav className="main-nav" aria-label="Main navigation">
        <a className={page !== 'booking' ? 'active' : ''} href="#stays">Find a stay</a>
        <a className={page === 'booking' ? 'active' : ''} href="#my-booking">My booking</a>
      </nav>
    </header>
  )
}

export default SiteHeader
