import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../api/api';

const VehicleCard = ({ vehicle, onPurchaseSuccess }) => {
  const { isAuthenticated } = useAuth();
  const { addToast } = useToast();
  const [purchasing, setPurchasing] = useState(false);

  const handlePurchase = async () => {
    if (!isAuthenticated) {
      addToast('Please login to purchase vehicles', 'info');
      return;
    }

    setPurchasing(true);
    try {
      const response = await api.post(`/vehicles/${vehicle.id}/purchase`);
      addToast(`Successfully purchased ${vehicle.make} ${vehicle.model}!`, 'success');
      if (onPurchaseSuccess) {
        onPurchaseSuccess(response.data); // Update vehicle state in parent dashboard
      }
    } catch (err) {
      addToast(err.response?.data?.message || 'Purchase failed', 'error');
    } finally {
      setPurchasing(false);
    }
  };

  const isOutOfStock = vehicle.quantity <= 0;

  return (
    <div className={`vehicle-card glass-panel ${isOutOfStock ? 'out-of-stock-card' : ''}`}>
      <div className="card-header">
        <span className="vehicle-category">{vehicle.category}</span>
        <span className={`status-badge ${isOutOfStock ? 'red' : 'green'}`}>
          {isOutOfStock ? 'Out of Stock' : `In Stock: ${vehicle.quantity}`}
        </span>
      </div>

      <div className="card-body">
        <h3 className="vehicle-title">{vehicle.make} <span className="vehicle-model">{vehicle.model}</span></h3>
        <p className="vehicle-price">${vehicle.price.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
      </div>

      <div className="card-footer">
        {isAuthenticated ? (
          <button
            onClick={handlePurchase}
            className="btn-primary card-action-btn"
            disabled={isOutOfStock || purchasing}
          >
            {purchasing ? 'Processing...' : isOutOfStock ? 'Out of Stock' : 'Purchase'}
          </button>
        ) : (
          <div className="guest-action-info">Sign in to purchase</div>
        )}
      </div>
    </div>
  );
};

export default VehicleCard;
