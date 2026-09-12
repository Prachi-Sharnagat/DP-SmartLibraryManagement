import React from 'react';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { ToastContainer } from '../common/Toast';

export const Shell = ({ activeTab, setActiveTab, onLogout, onSelectResource, children }) => {
  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-primary)' }}>
      {/* Left Sidebar */}
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} onLogout={onLogout} />

      {/* Main Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <Header
          onNavigateToNotifications={() => setActiveTab('notifications')}
          onGlobalSearchSelect={(item) => {
            if (onSelectResource) onSelectResource(item);
          }}
        />

        <main style={{ flex: 1, padding: '28px 32px', maxWidth: '1440px', width: '100%', margin: '0 auto' }}>
          {children}
        </main>
      </div>

      {/* Persistent Toasts */}
      <ToastContainer />
    </div>
  );
};
