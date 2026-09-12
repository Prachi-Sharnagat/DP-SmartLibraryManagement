import React from 'react';

export const Badge = ({ status, text, type }) => {
  let badgeClass = 'badge badge-muted';
  let dotColor = '#94A3B8';
  let label = text || status;

  const normalized = (status || type || '').toUpperCase();

  if (normalized === 'AVAILABLE' || normalized === 'ACTIVE' || normalized === 'SUCCESS' || normalized === 'COMPLETED') {
    badgeClass = 'badge badge-available';
    dotColor = '#16A34A';
  } else if (normalized === 'ISSUED' || normalized === 'WAITING' || normalized === 'PENDING') {
    badgeClass = 'badge badge-issued';
    dotColor = '#D97706';
  } else if (normalized === 'RESERVED' || normalized === 'READY_FOR_PICKUP' || normalized === 'INFO') {
    badgeClass = 'badge badge-reserved';
    dotColor = '#0284C7';
    if (normalized === 'READY_FOR_PICKUP') label = 'Ready for Pickup';
  } else if (normalized === 'OVERDUE' || normalized === 'INACTIVE' || normalized === 'FAILED' || normalized === 'SUSPENDED') {
    badgeClass = 'badge badge-overdue';
    dotColor = '#DC2626';
  }

  return (
    <span className={badgeClass}>
      <span className="status-dot" style={{ backgroundColor: dotColor }}></span>
      {label}
    </span>
  );
};
