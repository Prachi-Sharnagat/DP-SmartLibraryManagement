import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Search, UserPlus, Eye, BookOpen, Clock, ShieldCheck, Mail, Phone, DollarSign } from 'lucide-react';
import { Badge } from '../components/common/Badge';
import { Modal } from '../components/common/Modal';
import { PayFineModal } from '../components/common/PayFineModal';

export const Members = ({ onOpenAddMember }) => {
  const { users, loans, reservations, currentUser } = useLibrary();
  const [searchQuery, setSearchQuery] = useState('');
  const [inspectMember, setInspectMember] = useState(null);
  const [payFineMember, setPayFineMember] = useState(null);

  const filtered = users.filter((u) => {
    const name = u.name || '';
    const id = u.id || u.userId || '';
    const email = u.email || '';
    const dept = u.department || u.role || '';
    const q = searchQuery.toLowerCase();
    return name.toLowerCase().includes(q) || id.toLowerCase().includes(q) || email.toLowerCase().includes(q) || dept.toLowerCase().includes(q);
  });

  // Get active loans for a member
  const getMemberLoans = (memberId) => {
    return loans.filter((l) => (l.memberId === memberId || l.userId === memberId) && (l.status === 'ACTIVE' || l.status === 'OVERDUE' || !l.isReturned));
  };

  const getMemberHistory = (memberId) => {
    return loans.filter((l) => (l.memberId === memberId || l.userId === memberId) && (l.status === 'RETURNED' || l.isReturned));
  };

  const getMemberReservations = (memberId) => {
    return reservations.filter((r) => (r.memberId === memberId || r.userId === memberId) && (r.status === 'WAITING' || r.status === 'READY_FOR_PICKUP' || r.status === 'PENDING'));
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Library Members
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
            Manage registered students, faculty, and institutional library patrons with real-time fine ledger.
          </p>
        </div>

        {currentUser.role !== 'MEMBER' && (
          <button onClick={onOpenAddMember} className="btn-primary">
            <UserPlus size={16} />
            <span>Add Member</span>
          </button>
        )}
      </div>

      {/* Search Filter */}
      <div className="card-base" style={{ padding: '16px' }}>
        <div style={{ position: 'relative' }}>
          <Search size={16} color="var(--text-light)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
          <input
            type="text"
            placeholder="🔍 Search members by name, ID, email, role..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input-base"
            style={{ paddingLeft: '36px' }}
          />
        </div>
      </div>

      {/* Members Table */}
      <div className="table-container">
        <table className="lib-table">
          <thead>
            <tr>
              <th>Member</th>
              <th>Role & Dept</th>
              <th>Status</th>
              <th>Current Loans</th>
              <th>Outstanding Fines</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((item) => {
              const mId = item.id || item.userId;
              const fine = item.outstandingFine || 0;
              return (
                <tr key={mId}>
                  <td>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{item.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      ID: {mId} • {item.email}
                    </div>
                  </td>
                  <td>
                    <span
                      style={{
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        backgroundColor: item.role === 'ADMIN' ? '#F3E8FF' : (item.role === 'LIBRARIAN' ? '#E0F2FE' : '#F1F5F9'),
                        color: item.role === 'ADMIN' ? '#9333EA' : (item.role === 'LIBRARIAN' ? '#0284C7' : '#475569'),
                        padding: '2px 6px',
                        borderRadius: '4px'
                      }}
                    >
                      {item.role || 'MEMBER'}
                    </span>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>{item.department || 'General'}</div>
                  </td>
                  <td>
                    <Badge status={item.status || (item.membershipActive ? 'ACTIVE' : 'INACTIVE')} />
                  </td>
                  <td>
                    <span style={{ fontWeight: 600, fontSize: '0.85rem' }}>
                      {item.currentLoans ?? item.currentBorrowedCount ?? 0} / {item.maxLoans || 5}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontWeight: 600, fontSize: '0.85rem', color: fine > 0 ? '#DC2626' : '#16A34A' }}>
                      ${fine.toFixed(2)}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '6px' }}>
                      {fine > 0 && (
                        <button
                          onClick={() => setPayFineMember(item)}
                          className="btn-secondary"
                          style={{ padding: '4px 10px', fontSize: '0.75rem', color: '#D97706', borderColor: '#FDE68A', backgroundColor: '#FFFBEB' }}
                          title="Settle Outstanding Fine"
                        >
                          <DollarSign size={13} />
                          <span>Pay Fine</span>
                        </button>
                      )}
                      <button
                        onClick={() => setInspectMember(item)}
                        className="btn-secondary"
                        style={{ padding: '4px 10px', fontSize: '0.775rem' }}
                      >
                        <Eye size={13} />
                        <span>Profile</span>
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Member Profile Modal */}
      {inspectMember && (
        <Modal
          isOpen={!!inspectMember}
          onClose={() => setInspectMember(null)}
          title={`Member Profile: ${inspectMember.name}`}
          subtitle={`Account ID: ${inspectMember.id || inspectMember.userId}`}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Top Stat Boxes */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px' }}>
              <div style={{ backgroundColor: '#F8FAFC', padding: '12px', borderRadius: '8px', textAlign: 'center', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Current Loans</span>
                <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '2px' }}>
                  {inspectMember.currentLoans ?? inspectMember.currentBorrowedCount ?? 0}
                </div>
              </div>

              <div style={{ backgroundColor: '#F8FAFC', padding: '12px', borderRadius: '8px', textAlign: 'center', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>History Total</span>
                <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '2px' }}>
                  {getMemberHistory(inspectMember.id || inspectMember.userId).length + 4}
                </div>
              </div>

              <div style={{ backgroundColor: '#F8FAFC', padding: '12px', borderRadius: '8px', textAlign: 'center', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Fines Due</span>
                <div style={{ fontSize: '1.25rem', fontWeight: 700, color: (inspectMember.outstandingFine || 0) > 0 ? '#DC2626' : '#16A34A', marginTop: '2px' }}>
                  ${(inspectMember.outstandingFine || 0).toFixed(2)}
                </div>
              </div>

              <div style={{ backgroundColor: '#F8FAFC', padding: '12px', borderRadius: '8px', textAlign: 'center', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Reservations</span>
                <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--primary)', marginTop: '2px' }}>
                  {getMemberReservations(inspectMember.id || inspectMember.userId).length}
                </div>
              </div>
            </div>

            {/* Currently Borrowed Books */}
            <div>
              <h4 style={{ fontSize: '0.9rem', fontWeight: 600, marginBottom: '10px' }}>Active Checked-Out Books</h4>
              {getMemberLoans(inspectMember.id || inspectMember.userId).length === 0 ? (
                <div style={{ padding: '12px', backgroundColor: '#F8FAFC', borderRadius: '6px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  No active loans currently.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {getMemberLoans(inspectMember.id || inspectMember.userId).map((loan) => (
                    <div key={loan.id || loan.loanId} style={{ padding: '10px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem' }}>
                      <div>
                        <div style={{ fontWeight: 600 }}>{loan.resourceTitle || loan.resourceId}</div>
                        <div style={{ color: 'var(--text-muted)', fontSize: '0.725rem' }}>Due date: {loan.dueDate}</div>
                      </div>
                      <Badge status={loan.status || 'ACTIVE'} />
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Fine Settlement in Profile */}
            {(inspectMember.outstandingFine || 0) > 0 && (
              <div style={{ padding: '14px', backgroundColor: '#FFFBEB', borderRadius: '8px', border: '1px solid #FDE68A', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 600, color: '#92400E', fontSize: '0.85rem' }}>Outstanding Fines: ${(inspectMember.outstandingFine || 0).toFixed(2)}</div>
                  <div style={{ fontSize: '0.75rem', color: '#B45309' }}>Settling fines restores borrowing privileges.</div>
                </div>
                <button
                  onClick={() => {
                    const m = inspectMember;
                    setInspectMember(null);
                    setPayFineMember(m);
                  }}
                  className="btn-primary"
                  style={{ backgroundColor: '#D97706', fontSize: '0.775rem', padding: '6px 14px' }}
                >
                  Pay Fine Now
                </button>
              </div>
            )}
          </div>
        </Modal>
      )}

      {/* Pay Fine Simulation Modal */}
      <PayFineModal
        isOpen={!!payFineMember}
        onClose={() => setPayFineMember(null)}
        member={payFineMember}
      />
    </div>
  );
};
