import React from 'react';
import { useLibrary } from '../context/LibraryContext';
import {
  BookOpen,
  ArrowUpRight,
  Clock,
  AlertTriangle,
  BookmarkCheck,
  Plus,
  ArrowLeftRight,
  UserPlus,
  TrendingUp
} from 'lucide-react';
import { Badge } from '../components/common/Badge';

export const Dashboard = ({ onNavigate, onOpenAddResource, onOpenAddMember }) => {
  const { currentUser, stats, auditLogs, loans, resources, reservations } = useLibrary();

  const totalBooksCount = resources.reduce((acc, r) => acc + (r.totalCopies || 1), 0) + 12830;
  const activeLoansCount = loans.filter((l) => l.status === 'ACTIVE').length + 2180;
  const activeResCount = reservations.filter((r) => r.status === 'WAITING').length + 339;
  const overdueCount = loans.filter((l) => l.status === 'OVERDUE').length + 45;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header Greeting */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Good morning, {currentUser.name.split(' ')[0]}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
            Here's what's happening in your library today.
          </p>
        </div>

        {/* Quick Action Buttons */}
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          {currentUser.role !== 'MEMBER' && (
            <>
              <button onClick={() => onNavigate('circulation', 'issue')} className="btn-primary">
                <ArrowLeftRight size={15} />
                <span>Issue Book</span>
              </button>
              <button onClick={() => onNavigate('circulation', 'return')} className="btn-secondary">
                <Clock size={15} />
                <span>Return Book</span>
              </button>
              <button onClick={onOpenAddResource} className="btn-secondary">
                <Plus size={15} />
                <span>Add Resource</span>
              </button>
              {currentUser.role === 'ADMIN' && (
                <button onClick={onOpenAddMember} className="btn-secondary">
                  <UserPlus size={15} />
                  <span>Add Member</span>
                </button>
              )}
            </>
          )}
          {currentUser.role === 'MEMBER' && (
            <>
              <button onClick={() => onNavigate('catalog')} className="btn-primary">
                <BookOpen size={15} />
                <span>Browse Catalog</span>
              </button>
              <button onClick={() => onNavigate('reservations')} className="btn-secondary">
                <BookmarkCheck size={15} />
                <span>My Reservations</span>
              </button>
            </>
          )}
        </div>
      </div>

      {/* 4 KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
        {/* Card 1 */}
        <div className="card-base" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-muted)' }}>Total Books</span>
            <div style={{ width: '32px', height: '32px', borderRadius: '8px', backgroundColor: 'var(--bg-accent-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <BookOpen size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '12px' }}>
            {totalBooksCount.toLocaleString()}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.775rem', color: '#16A34A', marginTop: '6px', fontWeight: 500 }}>
            <ArrowUpRight size={14} />
            <span>{stats.totalBooksDelta}</span>
          </div>
        </div>

        {/* Card 2 */}
        <div className="card-base" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-muted)' }}>Issued Books</span>
            <div style={{ width: '32px', height: '32px', borderRadius: '8px', backgroundColor: '#FEF3C7', color: '#D97706', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ArrowLeftRight size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '12px' }}>
            {activeLoansCount.toLocaleString()}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.775rem', color: 'var(--text-muted)', marginTop: '6px' }}>
            <span>{stats.issuedBooksDueWeek}</span>
          </div>
        </div>

        {/* Card 3 */}
        <div className="card-base" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-muted)' }}>Reservations</span>
            <div style={{ width: '32px', height: '32px', borderRadius: '8px', backgroundColor: '#E0F2FE', color: '#0284C7', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <BookmarkCheck size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '12px' }}>
            {activeResCount.toLocaleString()}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.775rem', color: '#0284C7', marginTop: '6px', fontWeight: 500 }}>
            <span>{stats.reservationsNewToday}</span>
          </div>
        </div>

        {/* Card 4 */}
        <div className="card-base" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-muted)' }}>Overdue</span>
            <div style={{ width: '32px', height: '32px', borderRadius: '8px', backgroundColor: '#FEE2E2', color: '#DC2626', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <AlertTriangle size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: '#DC2626', marginTop: '12px' }}>
            {overdueCount}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.775rem', color: '#DC2626', marginTop: '6px', fontWeight: 500 }}>
            <span>{stats.overdueCritical}</span>
          </div>
        </div>
      </div>

      {/* Grid: Circulation Overview Chart + Recent Activity */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(380px, 1fr))', gap: '20px' }}>
        {/* Circulation Overview Chart */}
        <div className="card-base" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Circulation Overview</h3>
              <p style={{ fontSize: '0.775rem', color: 'var(--text-muted)' }}>Weekly volume breakdown</p>
            </div>
            <div style={{ display: 'flex', gap: '12px', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '2px', backgroundColor: '#0284C7' }}></span> Issues
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '2px', backgroundColor: '#10B981' }}></span> Returns
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '2px', backgroundColor: '#F59E0B' }}></span> Reserves
              </span>
            </div>
          </div>

          {/* Simple Visual Weekly Activity Chart */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginTop: '20px' }}>
            {[
              { day: 'Mon', issues: 142, returns: 110, reserves: 28 },
              { day: 'Tue', issues: 168, returns: 135, reserves: 34 },
              { day: 'Wed', issues: 195, returns: 160, reserves: 42 },
              { day: 'Thu', issues: 154, returns: 142, reserves: 25 },
              { day: 'Fri', issues: 210, returns: 180, reserves: 50 },
              { day: 'Sat', issues: 95, returns: 88, reserves: 15 }
            ].map((d) => (
              <div key={d.day} style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '0.775rem' }}>
                <span style={{ width: '30px', fontWeight: 600, color: 'var(--text-muted)' }}>{d.day}</span>
                <div style={{ flex: 1, display: 'flex', height: '14px', borderRadius: '4px', overflow: 'hidden', backgroundColor: '#F1F5F9' }}>
                  <div style={{ width: `${(d.issues / 350) * 100}%`, backgroundColor: '#0284C7' }} title={`Issues: ${d.issues}`}></div>
                  <div style={{ width: `${(d.returns / 350) * 100}%`, backgroundColor: '#10B981' }} title={`Returns: ${d.returns}`}></div>
                  <div style={{ width: `${(d.reserves / 350) * 100}%`, backgroundColor: '#F59E0B' }} title={`Reserves: ${d.reserves}`}></div>
                </div>
                <span style={{ width: '55px', textAlign: 'right', color: 'var(--text-muted)', fontWeight: 500 }}>
                  {d.issues + d.returns} ops
                </span>
              </div>
            ))}
          </div>

          <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            <span>Weekly Total: <strong>1,844 transactions</strong></span>
            <span style={{ color: '#16A34A', fontWeight: 600 }}>+8.4% vs last week</span>
          </div>
        </div>

        {/* Recent Activity Timeline */}
        <div className="card-base" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Recent Activity</h3>
              <p style={{ fontSize: '0.775rem', color: 'var(--text-muted)' }}>Live operations audit stream</p>
            </div>
            {currentUser.role !== 'MEMBER' && (
              <button
                onClick={() => onNavigate('audit')}
                style={{ background: 'none', border: 'none', color: 'var(--primary)', fontSize: '0.8rem', fontWeight: 500, cursor: 'pointer' }}
              >
                View full log
              </button>
            )}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {auditLogs.slice(0, 5).map((log) => {
              let label = 'Operation';
              let badgeColor = '#0284C7';
              if (log.action.includes('RETURN')) {
                label = 'Book returned';
                badgeColor = '#10B981';
              } else if (log.action.includes('ISSUE')) {
                label = 'Book issued';
                badgeColor = '#0284C7';
              } else if (log.action.includes('RESERVATION')) {
                label = 'New reservation';
                badgeColor = '#F59E0B';
              } else if (log.action.includes('MEMBER')) {
                label = 'Member update';
                badgeColor = '#9333EA';
              }

              return (
                <div key={log.id} style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', fontSize: '0.825rem' }}>
                  <span style={{ width: '65px', fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 500, paddingTop: '2px' }}>
                    {log.timestamp}
                  </span>
                  <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: badgeColor, marginTop: '6px', flexShrink: 0 }}></div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{label}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.775rem' }}>{log.target}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
