import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Bell, CheckCheck, Mail, MessageSquare, Smartphone, Check } from 'lucide-react';

export const Notifications = () => {
  const { notifications, markNotificationRead, markAllNotificationsRead } = useLibrary();
  const [filter, setFilter] = useState('ALL');

  const filtered = notifications.filter((n) => {
    if (filter === 'UNREAD') return !n.read;
    if (filter === 'RESERVATION') return n.category === 'RESERVATION';
    if (filter === 'DUE_DATES') return n.category === 'CIRCULATION' || n.category === 'FINES';
    return true;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '840px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Notification Center
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
            Multi-channel alerts dispatched (Email, SMS, In-App).
          </p>
        </div>

        <button onClick={markAllNotificationsRead} className="btn-secondary" style={{ fontSize: '0.85rem' }}>
          <CheckCheck size={16} />
          <span>Mark All as Read</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid var(--border-color)', paddingBottom: '8px' }}>
        {[
          { id: 'ALL', label: 'All' },
          { id: 'UNREAD', label: 'Unread' },
          { id: 'RESERVATION', label: 'Reservations' },
          { id: 'DUE_DATES', label: 'Due Dates & Fines' }
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setFilter(tab.id)}
            style={{
              padding: '6px 14px',
              fontSize: '0.85rem',
              fontWeight: filter === tab.id ? 600 : 500,
              color: filter === tab.id ? 'var(--primary)' : 'var(--text-muted)',
              borderBottom: filter === tab.id ? '2px solid var(--primary)' : '2px solid transparent',
              background: 'none',
              borderTop: 'none',
              borderLeft: 'none',
              borderRight: 'none',
              cursor: 'pointer'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Notification Cards List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {filtered.length === 0 ? (
          <div className="card-base" style={{ padding: '36px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No notifications in this category.
          </div>
        ) : (
          filtered.map((item) => (
            <div
              key={item.id}
              className="card-base"
              onClick={() => markNotificationRead(item.id)}
              style={{
                padding: '18px 20px',
                backgroundColor: item.read ? '#FFFFFF' : 'var(--bg-accent-light)',
                borderLeft: item.read ? '1px solid var(--border-color)' : '4px solid var(--primary)',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
                cursor: 'pointer',
                transition: 'background-color 0.15s ease'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {!item.read && (
                    <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--primary)' }}></span>
                  )}
                  <h3 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)' }}>
                    {item.title}
                  </h3>
                </div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-light)', fontWeight: 500 }}>
                  {item.timestamp}
                </span>
              </div>

              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>
                {item.message}
              </p>

              {/* Bridge Channels Indicator */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginTop: '6px', paddingTop: '8px', borderTop: '1px solid rgba(0,0,0,0.05)', fontSize: '0.725rem', color: 'var(--text-muted)' }}>
                <span style={{ fontWeight: 600 }}>Dispatch:</span>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: item.channels.includes('EMAIL') ? '#16A34A' : 'var(--text-light)' }}>
                    <Mail size={12} /> Email {item.channels.includes('EMAIL') && '✓'}
                  </span>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: item.channels.includes('SMS') ? '#16A34A' : 'var(--text-light)' }}>
                    <Smartphone size={12} /> SMS {item.channels.includes('SMS') && '✓'}
                  </span>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: item.channels.includes('IN_APP') ? '#16A34A' : 'var(--text-light)' }}>
                    <Bell size={12} /> In-App {item.channels.includes('IN_APP') && '✓'}
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
