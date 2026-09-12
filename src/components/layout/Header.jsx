import React, { useState } from 'react';
import { useLibrary } from '../../context/LibraryContext';
import { Bell, Search, ExternalLink, Server, RefreshCw, CheckCircle2, AlertCircle } from 'lucide-react';

export const Header = ({ onNavigateToNotifications, onGlobalSearchSelect }) => {
  const {
    currentUser,
    notifications,
    markNotificationRead,
    markAllNotificationsRead,
    resources,
    isBackendOnline,
    backendPort,
    isSyncing,
    syncWithBackend
  } = useLibrary();

  const [showNotifMenu, setShowNotifMenu] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);

  const unreadNotifs = notifications.filter((n) => !n.read);

  const handleSearchChange = (e) => {
    const q = e.target.value;
    setSearchQuery(q);
    if (!q.trim()) {
      setSearchResults([]);
      return;
    }
    const filtered = resources.filter(
      (r) =>
        r.title.toLowerCase().includes(q.toLowerCase()) ||
        (r.author && r.author.toLowerCase().includes(q.toLowerCase())) ||
        (r.isbn && r.isbn.toLowerCase().includes(q.toLowerCase())) ||
        r.id.toLowerCase().includes(q.toLowerCase())
    ).slice(0, 5);
    setSearchResults(filtered);
  };

  return (
    <header
      style={{
        height: '60px',
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid var(--border-color)',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 30
      }}
    >
      {/* Global Quick Search */}
      <div style={{ position: 'relative', width: '360px' }}>
        <div style={{ position: 'relative' }}>
          <Search
            size={16}
            color="var(--text-light)"
            style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }}
          />
          <input
            type="text"
            placeholder="Search catalog by title, author, ISBN..."
            value={searchQuery}
            onChange={handleSearchChange}
            className="input-base"
            style={{ paddingLeft: '36px', height: '36px', fontSize: '0.825rem' }}
          />
        </div>

        {/* Search Results Dropdown */}
        {searchResults.length > 0 && (
          <div
            className="card-base"
            style={{
              position: 'absolute',
              top: '42px',
              left: 0,
              right: 0,
              backgroundColor: '#FFFFFF',
              zIndex: 40,
              padding: '6px',
              boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'
            }}
          >
            {searchResults.map((item) => (
              <div
                key={item.id}
                onClick={() => {
                  if (onGlobalSearchSelect) onGlobalSearchSelect(item);
                  setSearchQuery('');
                  setSearchResults([]);
                }}
                style={{
                  padding: '8px 10px',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  fontSize: '0.825rem'
                }}
                onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'var(--bg-subtle)')}
                onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
              >
                <div>
                  <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{item.title}</div>
                  <div style={{ fontSize: '0.725rem', color: 'var(--text-muted)' }}>
                    {item.author || item.publisher} • {item.location}
                  </div>
                </div>
                <span style={{ fontSize: '0.7rem', color: 'var(--primary)', fontWeight: 500 }}>View</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Right controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        {/* Java REST API Status Indicator */}
        <button
          onClick={syncWithBackend}
          title="Click to re-check Java REST API connection"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '5px 10px',
            borderRadius: '20px',
            fontSize: '0.75rem',
            fontWeight: 600,
            border: isBackendOnline ? '1px solid #BBF7D0' : '1px solid #FED7AA',
            backgroundColor: isBackendOnline ? '#F0FDF4' : '#FFF7ED',
            color: isBackendOnline ? '#15803D' : '#C2410C',
            cursor: 'pointer',
            transition: 'all 0.2s'
          }}
        >
          <span
            style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: isBackendOnline ? '#22C55E' : '#F97316',
              boxShadow: isBackendOnline ? '0 0 8px #22C55E' : '0 0 6px #F97316',
              display: 'inline-block'
            }}
          />
          <Server size={13} />
          <span>{isBackendOnline ? `Java API :${backendPort} Online` : 'Java API Offline (Local Mode)'}</span>
          <RefreshCw size={11} className={isSyncing ? 'animate-spin' : ''} />
        </button>

        {/* Notification Bell Dropdown */}
        <div style={{ position: 'relative' }}>
          <button
            onClick={() => setShowNotifMenu(!showNotifMenu)}
            style={{
              background: 'none',
              border: '1px solid var(--border-color)',
              borderRadius: '8px',
              width: '36px',
              height: '36px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              position: 'relative',
              backgroundColor: '#FFFFFF'
            }}
          >
            <Bell size={18} color="var(--text-main)" />
            {unreadNotifs.length > 0 && (
              <span
                style={{
                  position: 'absolute',
                  top: '-4px',
                  right: '-4px',
                  backgroundColor: '#DC2626',
                  color: '#FFFFFF',
                  width: '18px',
                  height: '18px',
                  borderRadius: '50%',
                  fontSize: '0.675rem',
                  fontWeight: 700,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                {unreadNotifs.length}
              </span>
            )}
          </button>

          {/* Quick Notification Preview Panel */}
          {showNotifMenu && (
            <div
              className="card-base"
              style={{
                position: 'absolute',
                top: '46px',
                right: 0,
                width: '320px',
                backgroundColor: '#FFFFFF',
                boxShadow: '0 12px 24px rgba(0,0,0,0.1)',
                zIndex: 50,
                padding: 0,
                overflow: 'hidden'
              }}
            >
              <div
                style={{
                  padding: '12px 16px',
                  borderBottom: '1px solid var(--border-color)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  backgroundColor: '#F8FAFC'
                }}
              >
                <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>Notifications ({unreadNotifs.length} unread)</div>
                {unreadNotifs.length > 0 && (
                  <button
                    onClick={() => {
                      markAllNotificationsRead();
                      setShowNotifMenu(false);
                    }}
                    style={{ background: 'none', border: 'none', color: 'var(--primary)', fontSize: '0.75rem', cursor: 'pointer', fontWeight: 500 }}
                  >
                    Mark all read
                  </button>
                )}
              </div>

              <div style={{ maxHeight: '280px', overflowY: 'auto' }}>
                {notifications.slice(0, 4).map((n) => (
                  <div
                    key={n.id}
                    onClick={() => markNotificationRead(n.id)}
                    style={{
                      padding: '10px 14px',
                      borderBottom: '1px solid var(--border-subtle)',
                      backgroundColor: n.read ? '#FFFFFF' : 'var(--bg-accent-light)',
                      cursor: 'pointer'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <span style={{ fontWeight: 600, fontSize: '0.8rem', color: 'var(--text-main)' }}>{n.title}</span>
                      <span style={{ fontSize: '0.675rem', color: 'var(--text-light)' }}>{n.timestamp}</span>
                    </div>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '3px', lineHeight: 1.3 }}>{n.message}</p>
                    <div style={{ display: 'flex', gap: '4px', marginTop: '6px' }}>
                      {(n.channels || [n.channel || 'IN_APP']).map((ch) => (
                        <span key={ch} style={{ fontSize: '0.625rem', backgroundColor: '#FFFFFF', border: '1px solid var(--border-color)', padding: '1px 5px', borderRadius: '4px', color: 'var(--text-muted)' }}>
                          {ch}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              <div style={{ padding: '8px 12px', borderTop: '1px solid var(--border-color)', textAlign: 'center', backgroundColor: '#F8FAFC' }}>
                <button
                  onClick={() => {
                    setShowNotifMenu(false);
                    if (onNavigateToNotifications) onNavigateToNotifications();
                  }}
                  style={{ background: 'none', border: 'none', color: 'var(--primary)', fontSize: '0.8rem', fontWeight: 600, cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                >
                  <span>Open Notification Center</span>
                  <ExternalLink size={12} />
                </button>
              </div>
            </div>
          )}
        </div>

        {/* User Pill */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '4px 10px', backgroundColor: 'var(--bg-primary)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
          <div style={{ width: '24px', height: '24px', borderRadius: '50%', backgroundColor: 'var(--primary)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.75rem', fontWeight: 700 }}>
            {currentUser.name ? currentUser.name.charAt(0) : 'U'}
          </div>
          <span style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)' }}>{currentUser.name || 'User'}</span>
        </div>
      </div>
    </header>
  );
};
