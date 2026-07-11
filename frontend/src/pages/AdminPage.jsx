import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../api/api';
import Navbar from '../components/Navbar';

const AdminPage = () => {
  const { user, isAdmin, loading: authLoading } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState(null);
  
  const [make, setMake] = useState('');
  const [model, setModel] = useState('');
  const [category, setCategory] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState('');
  
  const [restockAmount, setRestockAmount] = useState({});

  useEffect(() => {
    // Redirect non-admins back to dashboard
    if (!authLoading) {
      if (!user || !isAdmin) {
        addToast('Access denied: Admins only', 'error');
        navigate('/');
      } else {
        fetchVehicles();
      }
    }
  }, [user, isAdmin, authLoading]);

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const response = await api.get('/vehicles');
      setVehicles(response.data);
    } catch (err) {
      addToast('Failed to load inventory', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAddModal = () => {
    setEditingVehicle(null);
    setMake('');
    setModel('');
    setCategory('');
    setPrice('');
    setQuantity('');
    setShowModal(true);
  };

  const handleOpenEditModal = (vehicle) => {
    setEditingVehicle(vehicle);
    setMake(vehicle.make);
    setModel(vehicle.model);
    setCategory(vehicle.category);
    setPrice(vehicle.price);
    setQuantity(vehicle.quantity);
    setShowModal(true);
  };

  const handleSaveVehicle = async (e) => {
    e.preventDefault();
    if (!make || !model || !category || !price || quantity === '') {
      addToast('Please fill in all fields', 'info');
      return;
    }

    const payload = { make, model, category, price: parseFloat(price), quantity: parseInt(quantity) };

    try {
      if (editingVehicle) {
        // Edit flow
        const response = await api.put(`/vehicles/${editingVehicle.id}`, payload);
        setVehicles((prev) => prev.map((v) => (v.id === editingVehicle.id ? response.data : v)));
        addToast('Vehicle updated successfully', 'success');
      } else {
        // Add flow
        const response = await api.post('/vehicles', payload);
        setVehicles((prev) => [response.data, ...prev]);
        addToast('Vehicle added successfully', 'success');
      }
      setShowModal(false);
    } catch (err) {
      addToast(err.response?.data?.message || 'Operation failed', 'error');
    }
  };

  const handleDeleteVehicle = async (id) => {
    if (!window.confirm('Are you sure you want to delete this vehicle from stock?')) {
      return;
    }

    try {
      await api.delete(`/vehicles/${id}`);
      setVehicles((prev) => prev.filter((v) => v.id !== id));
      addToast('Vehicle deleted successfully', 'success');
    } catch (err) {
      addToast('Deletion failed: Only admin roles allowed', 'error');
    }
  };

  const handleRestock = async (id) => {
    const amount = parseInt(restockAmount[id]);
    if (!amount || amount <= 0) {
      addToast('Please input a valid restocking amount (> 0)', 'info');
      return;
    }

    try {
      const response = await api.post(`/vehicles/${id}/restock`, { amount });
      setVehicles((prev) => prev.map((v) => (v.id === id ? response.data : v)));
      setRestockAmount((prev) => ({ ...prev, [id]: '' }));
      addToast(`Restocked ${response.data.make} by ${amount} units`, 'success');
    } catch (err) {
      addToast('Restocking failed', 'error');
    }
  };

  const handleRestockChange = (id, val) => {
    setRestockAmount((prev) => ({ ...prev, [id]: val }));
  };

  if (authLoading || !user || !isAdmin) {
    return <div className="loading-container"><div className="spinner"></div></div>;
  }

  return (
    <>
      <Navbar />
      <div className="admin-page page-container">
        
        {/* Header Block */}
        <header className="admin-header">
          <div className="header-meta">
            <h1 className="admin-title">Inventory Console</h1>
            <p className="admin-subtitle">Add, edit, restock, or remove catalog vehicles</p>
          </div>
          <button onClick={handleOpenAddModal} className="btn-primary">
            + Add Vehicle
          </button>
        </header>

        {/* Console Table */}
        <main className="admin-console glass-panel">
          {loading ? (
            <div className="loading-container"><div className="spinner"></div></div>
          ) : vehicles.length > 0 ? (
            <div className="table-responsive">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Make / Brand</th>
                    <th>Model</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Stock Quantity</th>
                    <th>Restock Action</th>
                    <th className="text-center">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {vehicles.map((v) => (
                    <tr key={v.id}>
                      <td>{v.id}</td>
                      <td className="text-highlight">{v.make}</td>
                      <td>{v.model}</td>
                      <td><span className="category-badge">{v.category}</span></td>
                      <td className="text-bright">${v.price.toLocaleString(undefined, { minimumFractionDigits: 2 })}</td>
                      <td className={`stock-level ${v.quantity === 0 ? 'out' : ''}`}>
                        {v.quantity === 0 ? 'OUT OF STOCK' : v.quantity}
                      </td>
                      <td>
                        <div className="restock-box">
                          <input
                            type="number"
                            className="form-input restock-input"
                            placeholder="Qty"
                            value={restockAmount[v.id] || ''}
                            onChange={(e) => handleRestockChange(v.id, e.target.value)}
                          />
                          <button onClick={() => handleRestock(v.id)} className="btn-restock">
                            Add
                          </button>
                        </div>
                      </td>
                      <td className="actions-cell">
                        <button onClick={() => handleOpenEditModal(v)} className="btn-edit">
                          Edit
                        </button>
                        <button onClick={() => handleDeleteVehicle(v.id)} className="btn-delete-row">
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-table-state">
              <p>No vehicles found. Click "+ Add Vehicle" to populate the showroom.</p>
            </div>
          )}
        </main>
      </div>

      {/* Add/Edit Modal Overlay */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel">
            <div className="modal-header">
              <h2>{editingVehicle ? 'Edit Vehicle details' : 'Add New Vehicle'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
            </div>

            <form id="vehicle-form" onSubmit={handleSaveVehicle} className="modal-form">
              <div className="form-group">
                <label className="form-label">Make / Brand</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Toyota, BMW, Tesla"
                  value={make}
                  onChange={(e) => setMake(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Model</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Camry, X5, Model 3"
                  value={model}
                  onChange={(e) => setModel(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Category</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Sedan, SUV, Sports"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  required
                />
              </div>

              <div className="modal-form-row">
                <div className="form-group">
                  <label className="form-label">Price ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-input"
                    placeholder="e.g. 25000"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Quantity</label>
                  <input
                    type="number"
                    min="0"
                    className="form-input"
                    placeholder="e.g. 10"
                    value={quantity}
                    onChange={(e) => setQuantity(e.target.value)}
                    required
                  />
                </div>
              </div>
            </form>

            <div className="modal-actions">
              <button type="button" className="btn-clear" onClick={() => setShowModal(false)}>
                Cancel
              </button>
              <button type="submit" form="vehicle-form" className="btn-primary">
                {editingVehicle ? 'Save Changes' : 'Add Vehicle'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
export default AdminPage;