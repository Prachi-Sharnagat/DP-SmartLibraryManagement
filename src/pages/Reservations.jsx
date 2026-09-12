import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { BookmarkCheck, Bell, Send, CheckCircle2, Clock } from 'lucide-react';
import { Badge } from '../components/common/Badge';

export const Reservations = ({ onOpenReserveModal }) => {
  const { reservations, notifyReservedMember, currentUser } = useLibrary();
  const [activeFilter, setActiveFilter] = useState('ALL');

  const filtered = reservations.filter((r) => {
    if (activeFilter === 'ALL') return true;
    if (activeFilter === 'WAITING') return r.status === 'WAITING';
    if (activeFilter === 'READY') return r.status === 'READY_FOR_PICKUP';
    if (activeFilter === 'COMPLETED') return r.status === 'COMPLETED';
    return true;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Reservations
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
            Manage active and completed book reservations (Observer Pattern Waitlists).
          </p>
        </div>

        <button onClick={() => onOpenReserveModal(null)} className="btn-primary">
          <BookmarkCheck size={16} />
          <span>New Reservation</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid var(--border-color)', paddingBottom: '8px' }}>
        {[
          { id: 'ALL', label: 'All Reservations' },
          { id: 'WAITING', label: 'Waiting Queue' },
          { id: 'READY', label: 'Ready for Pickup' },
          { id: 'COMPLETED', label: 'Completed' }
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveFilter(tab.id)}
            style={{
              padding: '6px 14px',
              fontSize: '0.85rem',
              fontWeight: activeFilter === tab.id ? 600 : 500,
              color: activeFilter === tab.id ? 'var(--primary)' : 'var(--text-muted)',
              borderBottom: activeFilter === tab.id ? '2px solid var(--primary)' : '2px solid transparent',
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

      {/* Table */}
      <div className="table-container">
        <table className="lib-table">
          <thead>
            <tr>
              <th>Resource</th>
              <th>Member</th>
              <th>Queue Position</th>
              <th>Bridge Channels</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                  No reservations found under this filter.
                </td>
              </tr>
            ) : (
              filtered.map((item) => (
                <tr key={item.id}>
                  <td>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{item.resourceTitle}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {item.resourceId} • Req: {item.requestDate}</div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 500 }}>{item.memberName}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {item.memberId}</div>
                  </td>
                  <td>
                    <span
                      style={{
                        fontSize: '0.8rem',
                        fontWeight: 700,
                        backgroundColor: item.position === 1 ? 'var(--bg-accent-light)' : '#F1F5F9',
                        color: item.position === 1 ? 'var(--primary-dark)' : 'var(--text-muted)',
                        padding: '3px 8px',
                        borderRadius: '6px',
                        border: '1px solid var(--border-color)'
                      }}
                    >
                      #{item.position} in line
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                      {item.notificationChannels?.map((ch) => (
                        <span key={ch} style={{ fontSize: '0.65rem', backgroundColor: '#F8FAFC', border: '1px solid var(--border-color)', padding: '2px 5px', borderRadius: '4px', color: 'var(--text-muted)' }}>
                          {ch}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td>
                    <Badge status={item.status} />
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    {currentUser.role !== 'STUDENT' && item.status !== 'COMPLETED' && (
                      <button
                        onClick={() => notifyReservedMember(item.id)}
                        className="btn-secondary"
                        style={{ padding: '4px 10px', fontSize: '0.775rem', color: 'var(--primary)' }}
                        title="Send manual observer alert via configured bridge channels"
                      >
                        <Send size={12} />
                        <span>Notify Member</span>
                      </button>
                    )}
                    {item.status === 'COMPLETED' && (
                      <span style={{ fontSize: '0.75rem', color: '#16A34A', fontWeight: 600 }}>
                        Fulfillled
                      </span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
