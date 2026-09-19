import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { ShieldCheck, Search, Filter, Activity, Clock } from 'lucide-react';
import { Badge } from '../components/common/Badge';

export const AuditLogs = () => {
  const { auditLogs } = useLibrary();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedAction, setSelectedAction] = useState('ALL');

  const actions = ['ALL', ...new Set(auditLogs.map((l) => l.action))];

  const filtered = auditLogs.filter((log) => {
    const matchesSearch =
      log.user.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.target.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.action.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesAction = selectedAction === 'ALL' || log.action === selectedAction;
    return matchesSearch && matchesAction;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
          <ShieldCheck size={22} color="var(--primary)" />
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            System Audit Logs
          </h1>
        </div>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          High-resolution security & execution telemetry.
        </p>
      </div>

      {/* Filter Controls */}
      <div className="card-base" style={{ padding: '16px', display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
        <div style={{ position: 'relative', flex: 1, minWidth: '240px' }}>
          <Search size={16} color="var(--text-light)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
          <input
            type="text"
            placeholder="🔍 Search audit log by user, operation, target entity..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input-base"
            style={{ paddingLeft: '36px' }}
          />
        </div>

        <div style={{ width: '200px' }}>
          <select
            value={selectedAction}
            onChange={(e) => setSelectedAction(e.target.value)}
            className="input-base"
            style={{ cursor: 'pointer' }}
          >
            {actions.map((act) => (
              <option key={act} value={act}>
                {act === 'ALL' ? 'All Operations' : act}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Audit Table */}
      <div className="table-container">
        <table className="lib-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>User & Session</th>
              <th>Operation</th>
              <th>Target Details</th>
              <th>Latency</th>
              <th style={{ textAlign: 'right' }}>Security Status</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                  No audit log entries found matching criteria.
                </td>
              </tr>
            ) : (
              filtered.map((log) => (
                <tr key={log.id}>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{log.timestamp}</div>
                    <div style={{ fontSize: '0.725rem', color: 'var(--text-muted)' }}>{log.date}</div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{log.user}</div>
                    <div style={{ fontSize: '0.725rem', color: 'var(--text-light)' }}>IP: {log.ipAddress || '192.168.1.45'}</div>
                  </td>
                  <td>
                    <span
                      style={{
                        fontFamily: 'monospace',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        backgroundColor: '#F1F5F9',
                        color: '#334155',
                        padding: '3px 6px',
                        borderRadius: '4px',
                        border: '1px solid var(--border-color)'
                      }}
                    >
                      {log.action}
                    </span>
                  </td>
                  <td style={{ fontSize: '0.825rem', color: 'var(--text-main)', maxWidth: '300px' }}>
                    {log.target}
                  </td>
                  <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                    {log.latency}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <Badge status={log.status} />
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
