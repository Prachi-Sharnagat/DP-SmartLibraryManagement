import React from 'react';
import { useLibrary } from '../../context/LibraryContext';
import {
  LayoutDashboard,
  BookOpen,
  ArrowLeftRight,
  BookmarkCheck,
  Users,
  Bell,
  BarChart3,
  ShieldCheck,
  Settings,
  LogOut,
  Sparkles
} from 'lucide-react';

export const Sidebar = ({ activeTab, setActiveTab, onLogout }) => {
  const { currentUser, switchUser, users, notifications } = useLibrary();

  const unreadCount = notifications.filter((n) => !n.read).length;

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: ['MEMBER', 'LIBRARIAN', 'ADMIN'] },
    { id: 'catalog', label: 'Catalog', icon: BookOpen, roles: ['MEMBER', 'LIBRARIAN', 'ADMIN'] },
    { id: 'circulation', label: 'Issue / Return', icon: ArrowLeftRight, roles: ['LIBRARIAN', 'ADMIN'] },
    { id: 'reservations', label: 'Reservations', icon: BookmarkCheck, roles: ['MEMBER', 'LIBRARIAN', 'ADMIN'] },
    { id: 'members', label: 'Members', icon: Users, roles: ['LIBRARIAN', 'ADMIN'] },
    { id: 'notifications', label: 'Notifications', icon: Bell, badge: unreadCount, roles: ['MEMBER', 'LIBRARIAN', 'ADMIN'] },
    { id: 'reports', label: 'Reports', icon: BarChart3, roles: ['LIBRARIAN', 'ADMIN'] },
    { id: 'audit', label: 'Audit Logs', icon: ShieldCheck, roles: ['LIBRARIAN', 'ADMIN'] },
    { id: 'settings', label: 'Settings', icon: Settings, roles: ['MEMBER', 'LIBRARIAN', 'ADMIN'] }
  ];

  const visibleNav = navItems.filter((item) => item.roles.includes(currentUser.role));

  return (
    <aside
      style={{
        width: '240px',
        backgroundColor: '#FFFFFF',
        borderRight: '1px solid var(--border-color)',
        display: 'flex',
        flexDirection: 'column',
        height: '100vh',
        position: 'sticky',
        top: 0,
        flexShrink: 0
      }}
    >
      {/* Brand Header */}
      <div
        style={{
          padding: '18px 20px',
          borderBottom: '1px solid var(--border-color)',
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}
      >
        <div
          style={{
            width: '32px',
            height: '32px',
            backgroundColor: 'var(--bg-accent-light)',
            color: 'var(--primary)',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700
          }}
        >
          <BookOpen size={18} />
        </div>
        <div>
          <h2 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-main)', lineHeight: 1.2 }}>
            Smart Library
          </h2>
          <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Operations Platform</span>
        </div>
      </div>

      {/* Navigation List */}
      <div style={{ flex: 1, padding: '16px 12px', overflowY: 'auto' }}>
        <div style={{ fontSize: '0.7rem', fontWeight: 600, color: 'var(--text-light)', padding: '0 8px 8px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          Menu
        </div>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {visibleNav.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  border: 'none',
                  backgroundColor: isActive ? 'var(--bg-accent-light)' : 'transparent',
                  color: isActive ? 'var(--primary-dark)' : 'var(--text-main)',
                  fontWeight: isActive ? 600 : 400,
                  fontSize: '0.875rem',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.12s ease'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <Icon size={18} color={isActive ? 'var(--primary)' : 'var(--text-muted)'} />
                  <span>{item.label}</span>
                </div>
                {item.badge > 0 && (
                  <span
                    style={{
                      backgroundColor: 'var(--primary)',
                      color: '#FFFFFF',
                      fontSize: '0.7rem',
                      fontWeight: 700,
                      padding: '2px 6px',
                      borderRadius: '10px'
                    }}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Role Demo Switcher */}
        <div style={{ marginTop: '24px', padding: '12px', backgroundColor: 'var(--bg-primary)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.725rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '8px' }}>
            <Sparkles size={13} color="var(--primary)" />
            <span>Switch Role (RBAC Demo)</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            {users.slice(0, 3).map((u) => (
              <button
                key={u.id}
                onClick={() => switchUser(u.id)}
                style={{
                  padding: '5px 8px',
                  fontSize: '0.75rem',
                  borderRadius: '4px',
                  border: currentUser.id === u.id ? '1px solid var(--primary)' : '1px solid transparent',
                  backgroundColor: currentUser.id === u.id ? '#FFFFFF' : 'transparent',
                  color: currentUser.id === u.id ? 'var(--primary)' : 'var(--text-main)',
                  fontWeight: currentUser.id === u.id ? 600 : 400,
                  cursor: 'pointer',
                  textAlign: 'left',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}
              >
                <span>{u.name.split(' ')[0]}</span>
                <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>{u.role}</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* User Footer */}
      <div
        style={{
          padding: '14px 16px',
          borderTop: '1px solid var(--border-color)',
          backgroundColor: '#FFFFFF',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }}>
          <div
            style={{
              width: '34px',
              height: '34px',
              borderRadius: '50%',
              backgroundColor: 'var(--bg-accent-light)',
              color: 'var(--primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 600,
              fontSize: '0.85rem',
              flexShrink: 0
            }}
          >
            {currentUser.name.charAt(0)}
          </div>
          <div style={{ minWidth: 0 }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {currentUser.name}
            </div>
            <span
              style={{
                fontSize: '0.675rem',
                fontWeight: 600,
                color: currentUser.role === 'ADMIN' ? '#9333EA' : (currentUser.role === 'LIBRARIAN' ? '#0284C7' : '#16A34A'),
                backgroundColor: currentUser.role === 'ADMIN' ? '#F3E8FF' : (currentUser.role === 'LIBRARIAN' ? '#E0F2FE' : '#DCFCE7'),
                padding: '1px 6px',
                borderRadius: '4px',
                display: 'inline-block'
              }}
            >
              {currentUser.role}
            </span>
          </div>
        </div>
        <button
          onClick={onLogout}
          title="Sign out"
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--text-muted)',
            cursor: 'pointer',
            padding: '6px',
            borderRadius: '6px',
            display: 'flex',
            alignItems: 'center'
          }}
        >
          <LogOut size={16} />
        </button>
      </div>
    </aside>
  );
};
