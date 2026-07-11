import React, { useState, useEffect } from 'react';
import api from '../api/api';
import Navbar from '../components/Navbar';
import VehicleCard from '../components/VehicleCard';
import { useToast } from '../context/ToastContext';

const DashboardPage = () => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [make, setMake] = useState('');
  const [category, setCategory] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');

  const { addToast } = useToast();

  const fetchVehicles = async (searchParams = {}) => {
    setLoading(true);
    try {
      // Build query string
      const params = new URLSearchParams();
      if (searchParams.make) params.append('make', searchParams.make);
      if (searchParams.category) params.append('category', searchParams.category);
      if (searchParams.minPrice) params.append('minPrice', searchParams.minPrice);
      if (searchParams.maxPrice) params.append('maxPrice', searchParams.maxPrice);

      const url = params.toString() ? `/vehicles/search?${params.toString()}` : '/vehicles';
      const response = await api.get(url);
      setVehicles(response.data);
    } catch (err) {
      addToast('Failed to load vehicles catalog', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchVehicles({ make, category, minPrice, maxPrice });
  };

  const handleClearFilters = () => {
    setMake('');
    setCategory('');
    setMinPrice('');
    setMaxPrice('');
    fetchVehicles();
  };

  const handlePurchaseSuccess = (updatedVehicle) => {
    setVehicles((prevVehicles) =>
      prevVehicles.map((v) => (v.id === updatedVehicle.id ? updatedVehicle : v))
    );
  };

  return (
    <>
      <Navbar />
      <div className="dashboard-page page-container">
        
        {/* Welcome Section */}
        <header className="dashboard-header">
          <h1 className="dashboard-title">Find Your Perfect Match</h1>
          <p className="dashboard-subtitle">Explore our elite car catalog with real-time stock booking</p>
        </header>

        {/* Dynamic Search / Filtering Panel */}
        <section className="search-section glass-panel">
          <form onSubmit={handleSearch} className="search-form">
            <div className="search-grid">
              <div className="search-group">
                <label className="search-label">Brand / Make</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Toyota, Honda"
                  value={make}
                  onChange={(e) => setMake(e.target.value)}
                />
              </div>

              <div className="search-group">
                <label className="search-label">Category</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Sedan, SUV, Sports"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                />
              </div>

              <div className="search-group">
                <label className="search-label">Min Price ($)</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="Min"
                  value={minPrice}
                  onChange={(e) => setMinPrice(e.target.value)}
                />
              </div>

              <div className="search-group">
                <label className="search-label">Max Price ($)</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="Max"
                  value={maxPrice}
                  onChange={(e) => setMaxPrice(e.target.value)}
                />
              </div>
            </div>

            <div className="search-actions">
              <button type="submit" className="btn-primary">Apply Filters</button>
              <button type="button" className="btn-clear" onClick={handleClearFilters}>
                Clear
              </button>
            </div>
          </form>
        </section>

        {/* Catalog Catalog List */}
        <main className="catalog-section">
          {loading ? (
            <div className="loading-container">
              <div className="spinner"></div>
              <p>Fetching active inventory catalog...</p>
            </div>
          ) : vehicles.length > 0 ? (
            <div className="vehicles-grid">
              {vehicles.map((vehicle) => (
                <VehicleCard
                  key={vehicle.id}
                  vehicle={vehicle}
                  onPurchaseSuccess={handlePurchaseSuccess}
                />
              ))}
            </div>
          ) : (
            <div className="empty-state glass-panel">
              <svg className="empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <h3>No Vehicles Match Your Search</h3>
              <p>Try clearing some filters or exploring alternative price brackets.</p>
              <button onClick={handleClearFilters} className="btn-primary empty-btn">View All Vehicles</button>
            </div>
          )}
        </main>
      </div>
    </>
  );
};

export default DashboardPage;
