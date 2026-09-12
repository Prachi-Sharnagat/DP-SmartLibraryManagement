import React, { useState } from 'react';
import { LibraryProvider, useLibrary } from './context/LibraryContext';
import { Shell } from './components/layout/Shell';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Catalog } from './pages/Catalog';
import { ResourceDetails } from './pages/ResourceDetails';
import { IssueBook } from './pages/IssueBook';
import { ReturnBook } from './pages/ReturnBook';
import { Reservations } from './pages/Reservations';
import { Notifications } from './pages/Notifications';
import { Members } from './pages/Members';
import { Reports } from './pages/Reports';
import { AuditLogs } from './pages/AuditLogs';
import { Settings } from './pages/Settings';
import { AddResource } from './pages/AddResource';
import { Modal } from './components/common/Modal';
import { ArrowLeftRight, Clock, BookmarkCheck, UserPlus } from 'lucide-react';

const MainApp = () => {
  const { currentUser, reserveResource, addMember, resources, users } = useLibrary();

  const [isAuthenticated, setIsAuthenticated] = useState(true);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [circulationMode, setCirculationMode] = useState('issue'); // 'issue' | 'return'
  const [inspectedResource, setInspectedResource] = useState(null);
  const [preselectedIssueResource, setPreselectedIssueResource] = useState(null);

  // Modals state
  const [isAddResourceOpen, setIsAddResourceOpen] = useState(false);
  const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
  const [reserveModalResource, setReserveModalResource] = useState(null);
  const [reserveSelectedChannels, setReserveSelectedChannels] = useState(['EMAIL', 'IN_APP']);
  const [reserveTargetResourceId, setReserveTargetResourceId] = useState('');

  // Add Member Form state
  const [newMemberName, setNewMemberName] = useState('');
  const [newMemberEmail, setNewMemberEmail] = useState('');
  const [newMemberRole, setNewMemberRole] = useState('STUDENT');
  const [newMemberDept, setNewMemberDept] = useState('Computer Science & Engineering');

  if (!isAuthenticated) {
    return <Login onLoginSuccess={() => setIsAuthenticated(true)} />;
  }

  // Navigation helper
  const handleNavigate = (tab, subMode = null) => {
    setActiveTab(tab);
    if (subMode) {
      setCirculationMode(subMode);
    }
  };

  const handleSelectResource = (resource) => {
    setInspectedResource(resource);
    setActiveTab('details');
  };

  const handleNavigateToIssue = (resource) => {
    setPreselectedIssueResource(resource);
    setCirculationMode('issue');
    setActiveTab('circulation');
  };

  const handleOpenReserveModal = (resource) => {
    setReserveModalResource(resource);
    setReserveTargetResourceId(resource ? resource.id : (resources[0]?.id || ''));
  };

  const handleConfirmReservation = (e) => {
    e.preventDefault();
    const resId = reserveModalResource ? reserveModalResource.id : reserveTargetResourceId;
    reserveResource(resId, currentUser.id, reserveSelectedChannels);
    setReserveModalResource(null);
  };

  const handleConfirmAddMember = (e) => {
    e.preventDefault();
    if (!newMemberName.trim()) return;
    addMember({
      name: newMemberName,
      email: newMemberEmail,
      role: newMemberRole,
      department: newMemberDept
    });
    setIsAddMemberOpen(false);
    setNewMemberName('');
    setNewMemberEmail('');
  };

  return (
    <Shell
      activeTab={activeTab === 'details' ? 'catalog' : activeTab}
      setActiveTab={(tab) => {
        setActiveTab(tab);
        if (tab !== 'details') setInspectedResource(null);
      }}
      onLogout={() => setIsAuthenticated(false)}
      onSelectResource={handleSelectResource}
    >
      {/* 1. Dashboard */}
      {activeTab === 'dashboard' && (
        <Dashboard
          onNavigate={handleNavigate}
          onOpenAddResource={() => setIsAddResourceOpen(true)}
          onOpenAddMember={() => setIsAddMemberOpen(true)}
        />
      )}

      {/* 2. Catalog */}
      {activeTab === 'catalog' && (
        <Catalog
          onSelectResource={handleSelectResource}
          onOpenAddResource={() => setIsAddResourceOpen(true)}
          onNavigateToIssue={handleNavigateToIssue}
          onOpenReserveModal={handleOpenReserveModal}
        />
      )}

      {/* 2b. Resource Details */}
      {activeTab === 'details' && inspectedResource && (
        <ResourceDetails
          resource={inspectedResource}
          onBack={() => setActiveTab('catalog')}
          onNavigateToIssue={handleNavigateToIssue}
          onOpenReserveModal={handleOpenReserveModal}
        />
      )}

      {/* 3. Circulation (Issue / Return) */}
      {activeTab === 'circulation' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Submode Switcher */}
          <div style={{ display: 'flex', gap: '10px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
            <button
              onClick={() => setCirculationMode('issue')}
              className={circulationMode === 'issue' ? 'btn-primary' : 'btn-secondary'}
            >
              <ArrowLeftRight size={15} />
              <span>Issue Resource (Chain of Responsibility)</span>
            </button>
            <button
              onClick={() => setCirculationMode('return')}
              className={circulationMode === 'return' ? 'btn-primary' : 'btn-secondary'}
            >
              <Clock size={15} />
              <span>Return Resource (Observer Alerts)</span>
            </button>
          </div>

          {circulationMode === 'issue' ? (
            <IssueBook
              preselectedResource={preselectedIssueResource}
              onSuccess={() => {
                setPreselectedIssueResource(null);
                setActiveTab('dashboard');
              }}
            />
          ) : (
            <ReturnBook
              onReturnSuccess={() => {
                setActiveTab('dashboard');
              }}
            />
          )}
        </div>
      )}

      {/* 4. Reservations */}
      {activeTab === 'reservations' && (
        <Reservations onOpenReserveModal={handleOpenReserveModal} />
      )}

      {/* 5. Members */}
      {activeTab === 'members' && (
        <Members onOpenAddMember={() => setIsAddMemberOpen(true)} />
      )}

      {/* 6. Notifications */}
      {activeTab === 'notifications' && <Notifications />}

      {/* 7. Reports */}
      {activeTab === 'reports' && <Reports />}

      {/* 8. Audit Logs */}
      {activeTab === 'audit' && <AuditLogs />}

      {/* 9. Settings */}
      {activeTab === 'settings' && <Settings />}

      {/* Modal: Add Resource (Factory Method) */}
      <Modal
        isOpen={isAddResourceOpen}
        onClose={() => setIsAddResourceOpen(false)}
        title="Add Library Resource"
        subtitle="Instantiate resources via Factory Method pattern (Book, DRM E-Book, Journal)"
        maxWidth="680px"
      >
        <AddResource
          onClose={() => setIsAddResourceOpen(false)}
          onSuccess={() => setIsAddResourceOpen(false)}
        />
      </Modal>

      {/* Modal: Add Member */}
      <Modal
        isOpen={isAddMemberOpen}
        onClose={() => setIsAddMemberOpen(false)}
        title="Register New Library Member"
        subtitle="Add student, faculty, or library administrative patron"
        maxWidth="500px"
      >
        <form onSubmit={handleConfirmAddMember} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Full Name *</label>
            <input
              type="text"
              required
              placeholder="e.g. Vikramaditya Deshmukh"
              value={newMemberName}
              onChange={(e) => setNewMemberName(e.target.value)}
              className="input-base"
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Institutional Email *</label>
            <input
              type="email"
              required
              placeholder="e.g. vikram@univ.edu"
              value={newMemberEmail}
              onChange={(e) => setNewMemberEmail(e.target.value)}
              className="input-base"
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Role</label>
            <select value={newMemberRole} onChange={(e) => setNewMemberRole(e.target.value)} className="input-base">
              <option value="STUDENT">Student (5 max loans)</option>
              <option value="LIBRARIAN">Librarian (10 max loans)</option>
              <option value="ADMIN">Admin Staff (20 max loans)</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Department</label>
            <input
              type="text"
              placeholder="e.g. Information Technology"
              value={newMemberDept}
              onChange={(e) => setNewMemberDept(e.target.value)}
              className="input-base"
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px', paddingTop: '14px', borderTop: '1px solid var(--border-color)' }}>
            <button type="button" onClick={() => setIsAddMemberOpen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" className="btn-primary">Register Member</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Reserve Resource (Observer + Bridge) */}
      <Modal
        isOpen={!!reserveModalResource || !!reserveTargetResourceId}
        onClose={() => {
          setReserveModalResource(null);
          setReserveTargetResourceId('');
        }}
        title="Reserve Resource & Subscribe to Alerts"
        subtitle="Observer Pattern Waitlist Registration with Bridge Channel Delivery"
        maxWidth="520px"
      >
        <form onSubmit={handleConfirmReservation} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Target Resource</label>
            {reserveModalResource ? (
              <div style={{ padding: '10px 12px', backgroundColor: '#F8FAFC', borderRadius: '6px', border: '1px solid var(--border-color)', fontWeight: 600, fontSize: '0.875rem' }}>
                {reserveModalResource.title} ({reserveModalResource.id})
              </div>
            ) : (
              <select
                value={reserveTargetResourceId}
                onChange={(e) => setReserveTargetResourceId(e.target.value)}
                className="input-base"
              >
                {resources.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.title} ({r.id} - {r.status})
                  </option>
                ))}
              </select>
            )}
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Subscriber Member</label>
            <div style={{ padding: '10px 12px', backgroundColor: '#F8FAFC', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
              <strong>{currentUser.name}</strong> ({currentUser.id} • {currentUser.email})
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '8px' }}>
              Dispatch Notification Channels (Bridge Pattern)
            </label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {[
                { id: 'EMAIL', label: 'Email Notification' },
                { id: 'SMS', label: 'SMS Text Alert' },
                { id: 'IN_APP', label: 'In-App Push Banner' }
              ].map((ch) => {
                const checked = reserveSelectedChannels.includes(ch.id);
                return (
                  <label key={ch.id} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem', cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => {
                        if (checked) {
                          if (reserveSelectedChannels.length > 1) {
                            setReserveSelectedChannels(reserveSelectedChannels.filter((c) => c !== ch.id));
                          }
                        } else {
                          setReserveSelectedChannels([...reserveSelectedChannels, ch.id]);
                        }
                      }}
                      style={{ accentColor: 'var(--primary)', cursor: 'pointer' }}
                    />
                    <span>{ch.label}</span>
                  </label>
                );
              })}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px', paddingTop: '14px', borderTop: '1px solid var(--border-color)' }}>
            <button
              type="button"
              onClick={() => {
                setReserveModalResource(null);
                setReserveTargetResourceId('');
              }}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button type="submit" className="btn-primary">
              <BookmarkCheck size={16} />
              <span>Confirm Reservation</span>
            </button>
          </div>
        </form>
      </Modal>
    </Shell>
  );
};

export default function App() {
  return (
    <LibraryProvider>
      <MainApp />
    </LibraryProvider>
  );
}
