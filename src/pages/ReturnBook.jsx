import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Search, Clock, CheckCircle2, AlertTriangle, ArrowRight, User, BookOpen, QrCode, ScanLine } from 'lucide-react';
import { Badge } from '../components/common/Badge';
import { BarcodeScannerModal } from '../components/common/BarcodeScannerModal';

export const ReturnBook = ({ onReturnSuccess }) => {
  const { loans, returnBook, reservations } = useLibrary();

  const [searchLoanId, setSearchLoanId] = useState('');
  const [selectedLoan, setSelectedLoan] = useState(null);
  const [scannerOpen, setScannerOpen] = useState(false);

  // Active loans available for return
  const activeLoans = loans.filter((l) => l.status === 'ACTIVE' || l.status === 'OVERDUE' || !l.isReturned);

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    if (!searchLoanId.trim()) return;
    const found = activeLoans.find(
      (l) =>
        (l.id && l.id.toLowerCase() === searchLoanId.trim().toLowerCase()) ||
        (l.loanId && l.loanId.toLowerCase() === searchLoanId.trim().toLowerCase()) ||
        (l.memberId && l.memberId.toLowerCase() === searchLoanId.trim().toLowerCase()) ||
        (l.userId && l.userId.toLowerCase() === searchLoanId.trim().toLowerCase()) ||
        (l.resourceId && l.resourceId.toLowerCase() === searchLoanId.trim().toLowerCase())
    );
    if (found) {
      setSelectedLoan(found);
    } else {
      setSelectedLoan(null);
    }
  };

  // Fine & Overdue Calculation
  const calculateOverdueStats = (loan) => {
    if (!loan) return { isOverdue: false, daysOverdue: 0, fine: 0 };
    const today = new Date();
    const dueDate = new Date(loan.dueDate);
    const diffTime = today - dueDate;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays > 0) {
      const finePerDay = 0.50; // $0.50 per day
      return {
        isOverdue: true,
        daysOverdue: diffDays,
        fine: diffDays * finePerDay
      };
    }
    return { isOverdue: false, daysOverdue: 0, fine: 0 };
  };

  const overdueStats = calculateOverdueStats(selectedLoan);

  const handleConfirmReturn = async () => {
    if (!selectedLoan) return;
    const loanKey = selectedLoan.id || selectedLoan.loanId;
    const ok = await returnBook(loanKey);
    if (ok) {
      setSelectedLoan(null);
      setSearchLoanId('');
      if (onReturnSuccess) onReturnSuccess();
    }
  };

  const handleScanComplete = (code) => {
    const found = activeLoans.find(
      (l) =>
        (l.id && l.id.toLowerCase() === code.toLowerCase()) ||
        (l.loanId && l.loanId.toLowerCase() === code.toLowerCase()) ||
        (l.resourceId && l.resourceId.toLowerCase() === code.toLowerCase()) ||
        (l.memberId && l.memberId.toLowerCase() === code.toLowerCase()) ||
        (l.userId && l.userId.toLowerCase() === code.toLowerCase())
    );
    if (found) {
      setSelectedLoan(found);
      setSearchLoanId(code);
    }
  };

  // Check if someone is waiting in Observer queue for this item
  const waitingObserver = selectedLoan
    ? reservations.find((r) => r.resourceId === selectedLoan.resourceId && (r.status === 'WAITING' || r.status === 'PENDING'))
    : null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '840px', margin: '0 auto' }}>
      {/* Page Header */}
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
          Return Book
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
          Process resource returns, calculate overdue fines, and trigger automated reservation Observer notifications.
        </p>
      </div>

      {/* Loan ID Search / Barcode Scanner */}
      <div className="card-base" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
          <div>
            <h2 style={{ fontSize: '1.15rem', fontWeight: 600, marginBottom: '4px' }}>Identify Active Loan Record</h2>
            <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>
              Scan the resource barcode or search by Loan ID, Resource ID, or Member ID.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setScannerOpen(true)}
            className="btn-secondary"
            style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', padding: '6px 12px' }}
          >
            <ScanLine size={16} />
            <span>Scan Return Barcode</span>
          </button>
        </div>

        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '10px', marginBottom: '16px' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <Search size={16} color="var(--text-light)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="e.g. LN-1001, B001, U101..."
              value={searchLoanId}
              onChange={(e) => setSearchLoanId(e.target.value)}
              className="input-base"
              style={{ paddingLeft: '36px' }}
            />
          </div>
          <button type="submit" className="btn-primary">
            Find Loan
          </button>
        </form>

        {/* Quick select active loans list */}
        <div>
          <label style={{ fontSize: '0.75rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--text-light)', letterSpacing: '0.05em' }}>
            Or Select from Active Borrow Records ({activeLoans.length}):
          </label>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px', maxHeight: '200px', overflowY: 'auto' }}>
            {activeLoans.map((l) => {
              const lId = l.id || l.loanId;
              const isSelected = (selectedLoan?.id === lId) || (selectedLoan?.loanId === lId);
              return (
                <div
                  key={lId}
                  onClick={() => setSelectedLoan(l)}
                  style={{
                    padding: '10px 14px',
                    borderRadius: '8px',
                    border: isSelected ? '1.5px solid var(--primary)' : '1px solid var(--border-color)',
                    backgroundColor: isSelected ? 'var(--bg-accent-light)' : '#FFFFFF',
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    fontSize: '0.825rem'
                  }}
                >
                  <div>
                    <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{l.resourceTitle || l.resourceId}</span>
                    <span style={{ color: 'var(--text-muted)', marginLeft: '8px' }}>• Borrowed by {l.memberName || l.userId || l.memberId}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Due: {l.dueDate}</span>
                    <Badge status={l.status || 'ACTIVE'} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Selected Loan Return Inspector Card */}
      {selectedLoan && (
        <div className="card-base" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 600, marginBottom: '16px' }}>Return Details & Settlement</h2>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
            <div style={{ padding: '12px', backgroundColor: 'var(--bg-subtle)', borderRadius: '8px' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Resource Details</span>
              <div style={{ fontWeight: 600, marginTop: '2px' }}>{selectedLoan.resourceTitle || selectedLoan.resourceId}</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {selectedLoan.resourceId}</div>
            </div>
            <div style={{ padding: '12px', backgroundColor: 'var(--bg-subtle)', borderRadius: '8px' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Borrower Info</span>
              <div style={{ fontWeight: 600, marginTop: '2px' }}>{selectedLoan.memberName || selectedLoan.userId}</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {selectedLoan.memberId || selectedLoan.userId}</div>
            </div>
          </div>

          {/* Overdue Calculation Warning */}
          {overdueStats.isOverdue ? (
            <div style={{ padding: '14px 18px', backgroundColor: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '8px', marginBottom: '20px', display: 'flex', gap: '12px' }}>
              <AlertTriangle size={20} color="#DC2626" style={{ shrink: 0, marginTop: '2px' }} />
              <div>
                <div style={{ fontWeight: 600, color: '#991B1B', fontSize: '0.875rem' }}>
                  Overdue Fine Incurred: ${overdueStats.fine.toFixed(2)} ({overdueStats.daysOverdue} days late)
                </div>
                <div style={{ fontSize: '0.775rem', color: '#7F1D1D', marginTop: '2px' }}>
                  A fine of $0.50 per day will be added to the member's account balance upon return.
                </div>
              </div>
            </div>
          ) : (
            <div style={{ padding: '14px 18px', backgroundColor: '#F0FDF4', border: '1px solid #BBF7D0', borderRadius: '8px', marginBottom: '20px', display: 'flex', gap: '12px' }}>
              <CheckCircle2 size={20} color="#16A34A" style={{ shrink: 0, marginTop: '2px' }} />
              <div>
                <div style={{ fontWeight: 600, color: '#15803D', fontSize: '0.875rem' }}>
                  Returned On Time (No Overdue Fine)
                </div>
                <div style={{ fontSize: '0.775rem', color: '#166534', marginTop: '2px' }}>
                  Resource will be checked in and made available in the library inventory.
                </div>
              </div>
            </div>
          )}

          {/* Observer Queue Alert */}
          {waitingObserver && (
            <div style={{ padding: '14px 18px', backgroundColor: '#EFF6FF', border: '1px solid #BFDBFE', borderRadius: '8px', marginBottom: '20px', display: 'flex', gap: '12px' }}>
              <Clock size={20} color="#2563EB" style={{ shrink: 0, marginTop: '2px' }} />
              <div>
                <div style={{ fontWeight: 600, color: '#1E40AF', fontSize: '0.875rem' }}>
                  Observer Pattern Alert: Waitlist Member Waiting
                </div>
                <div style={{ fontSize: '0.775rem', color: '#1E3A8A', marginTop: '2px' }}>
                  {waitingObserver.memberName || waitingObserver.userName} is waiting for this item. Returning it will automatically dispatch notifications via {waitingObserver.notificationChannels ? waitingObserver.notificationChannels.join(', ') : 'Email & In-App'}.
                </div>
              </div>
            </div>
          )}

          {/* Confirm Action */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '16px', borderTop: '1px solid var(--border-color)' }}>
            <button onClick={() => setSelectedLoan(null)} className="btn-secondary">
              Cancel
            </button>
            <button onClick={handleConfirmReturn} className="btn-primary">
              <CheckCircle2 size={16} />
              <span>Process Return & Update Inventory</span>
            </button>
          </div>
        </div>
      )}

      {/* Barcode Scanner Modal */}
      <BarcodeScannerModal
        isOpen={scannerOpen}
        onClose={() => setScannerOpen(false)}
        onScan={handleScanComplete}
        title="Scan Return Barcode"
        scanType="RESOURCE"
      />
    </div>
  );
};
