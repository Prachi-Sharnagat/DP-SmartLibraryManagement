import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Search, CheckCircle2, XCircle, ArrowRight, UserCheck, BookOpen, AlertTriangle, QrCode, ScanLine } from 'lucide-react';
import { Badge } from '../components/common/Badge';
import { BarcodeScannerModal } from '../components/common/BarcodeScannerModal';

export const IssueBook = ({ preselectedResource, onSuccess }) => {
  const { users, resources, evaluateBorrowValidation, issueBook } = useLibrary();

  const [step, setStep] = useState(preselectedResource ? 1 : 1);
  const [memberQuery, setMemberQuery] = useState('');
  const [selectedMember, setSelectedMember] = useState(null);

  const [resourceQuery, setResourceQuery] = useState(preselectedResource ? preselectedResource.title : '');
  const [selectedResource, setSelectedResource] = useState(preselectedResource || null);

  // Scanner modal state
  const [scannerOpen, setScannerOpen] = useState(false);
  const [scanType, setScanType] = useState('MEMBER'); // 'MEMBER' | 'RESOURCE'

  // Filter members
  const memberMatches = memberQuery.trim()
    ? users.filter(
        (u) =>
          u.name.toLowerCase().includes(memberQuery.toLowerCase()) ||
          (u.id && u.id.toLowerCase().includes(memberQuery.toLowerCase())) ||
          (u.userId && u.userId.toLowerCase().includes(memberQuery.toLowerCase())) ||
          (u.email && u.email.toLowerCase().includes(memberQuery.toLowerCase()))
      )
    : users;

  // Filter resources
  const resourceMatches = resourceQuery.trim()
    ? resources.filter(
        (r) =>
          r.title.toLowerCase().includes(resourceQuery.toLowerCase()) ||
          r.id.toLowerCase().includes(resourceQuery.toLowerCase()) ||
          (r.isbn && r.isbn.toLowerCase().includes(resourceQuery.toLowerCase()))
      )
    : resources;

  // Real-time Chain of Responsibility Validation
  const validation = evaluateBorrowValidation(selectedMember, selectedResource);

  const handleConfirmIssue = async () => {
    if (!selectedMember || !selectedResource) return;
    const ok = await issueBook(selectedMember.id || selectedMember.userId, selectedResource.id);
    if (ok) {
      setSelectedMember(null);
      setSelectedResource(null);
      setStep(1);
      if (onSuccess) onSuccess();
    }
  };

  const handleOpenScanner = (type) => {
    setScanType(type);
    setScannerOpen(true);
  };

  const handleScanComplete = (code, item) => {
    if (scanType === 'MEMBER') {
      const found = users.find(u => (u.id && u.id.toLowerCase() === code.toLowerCase()) || (u.userId && u.userId.toLowerCase() === code.toLowerCase()) || u.name.toLowerCase() === code.toLowerCase());
      if (found) {
        setSelectedMember(found);
        setStep(2);
      } else if (item) {
        setSelectedMember(item);
        setStep(2);
      }
    } else {
      const found = resources.find(r => r.id.toLowerCase() === code.toLowerCase() || (r.isbn && r.isbn.toLowerCase() === code.toLowerCase()) || r.title.toLowerCase() === code.toLowerCase());
      if (found) {
        setSelectedResource(found);
        setStep(3);
      } else if (item) {
        setSelectedResource(item);
        setStep(3);
      }
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '840px', margin: '0 auto' }}>
      {/* Page Header */}
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
          Issue Book
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
          Process library circulation rules.
        </p>
      </div>

      {/* Workflow Stepper */}
      <div className="card-base" style={{ padding: '16px 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        {[
          { num: 1, label: 'Select Member' },
          { num: 2, label: 'Select Resource' },
          { num: 3, label: 'Validation & Confirmation' }
        ].map((s, idx) => {
          const isDone = (s.num === 1 && selectedMember) || (s.num === 2 && selectedResource);
          const isCurrent = step === s.num;
          return (
            <React.Fragment key={s.num}>
              <div
                onClick={() => {
                  if (s.num === 1 || (s.num === 2 && selectedMember) || (s.num === 3 && selectedMember && selectedResource)) {
                    setStep(s.num);
                  }
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  cursor: 'pointer',
                  opacity: isCurrent ? 1 : 0.75
                }}
              >
                <div
                  style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: '50%',
                    backgroundColor: isCurrent ? 'var(--primary)' : (isDone ? '#16A34A' : 'var(--bg-subtle)'),
                    color: isCurrent || isDone ? '#FFFFFF' : 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 600,
                    fontSize: '0.8rem'
                  }}
                >
                  {isDone && !isCurrent ? '✓' : s.num}
                </div>
                <div>
                  <span style={{ fontSize: '0.85rem', fontWeight: isCurrent ? 700 : 500, color: isCurrent ? 'var(--text-main)' : 'var(--text-muted)' }}>
                    {s.label}
                  </span>
                </div>
              </div>
              {idx < 2 && <ArrowRight size={16} color="var(--border-color)" />}
            </React.Fragment>
          );
        })}
      </div>

      {/* Step 1: Member Search & Select */}
      {step === 1 && (
        <div className="card-base" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
            <div>
              <h2 style={{ fontSize: '1.15rem', fontWeight: 600, marginBottom: '4px' }}>Step 1: Select Library Member</h2>
              <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>
                Search registered students, faculty, or scan member ID card barcode.
              </p>
            </div>
            <button
              type="button"
              onClick={() => handleOpenScanner('MEMBER')}
              className="btn-secondary"
              style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', padding: '6px 12px' }}
            >
              <QrCode size={16} />
              <span>Scan Member ID</span>
            </button>
          </div>

          <div style={{ position: 'relative', marginBottom: '16px' }}>
            <Search size={16} color="var(--text-light)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="🔍 Search Member ID or Name (e.g. Alice, Bob, U101)..."
              value={memberQuery}
              onChange={(e) => setMemberQuery(e.target.value)}
              className="input-base"
              style={{ paddingLeft: '36px' }}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '280px', overflowY: 'auto' }}>
            {memberMatches.map((m) => {
              const mId = m.id || m.userId;
              const isSelected = (selectedMember?.id === mId) || (selectedMember?.userId === mId);
              return (
                <div
                  key={mId}
                  onClick={() => setSelectedMember(m)}
                  style={{
                    padding: '12px 16px',
                    borderRadius: '8px',
                    border: isSelected ? '1.5px solid var(--primary)' : '1px solid var(--border-color)',
                    backgroundColor: isSelected ? 'var(--bg-accent-light)' : '#FFFFFF',
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{m.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      ID: {mId} • {m.department || m.role} • Loans: {m.currentLoans ?? m.currentBorrowedCount ?? 0}/{m.maxLoans || 5}
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Badge status={m.status || (m.membershipActive ? 'ACTIVE' : 'INACTIVE')} />
                    {m.outstandingFine > 0 && (
                      <span style={{ fontSize: '0.75rem', color: '#DC2626', fontWeight: 600 }}>
                        Fine: ${m.outstandingFine.toFixed(2)}
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {selectedMember && (
            <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Selected Member: </span>
                <strong style={{ color: 'var(--text-main)' }}>{selectedMember.name} ({selectedMember.id || selectedMember.userId})</strong>
              </div>
              <button onClick={() => setStep(2)} className="btn-primary">
                <span>Proceed to Resource</span>
                <ArrowRight size={15} />
              </button>
            </div>
          )}
        </div>
      )}

      {/* Step 2: Resource Search & Select */}
      {step === 2 && (
        <div className="card-base" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
            <div>
              <h2 style={{ fontSize: '1.15rem', fontWeight: 600, marginBottom: '4px' }}>Step 2: Select Library Resource</h2>
              <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>
                Select physical book, eBook, or journal to checkout.
              </p>
            </div>
            <button
              type="button"
              onClick={() => handleOpenScanner('RESOURCE')}
              className="btn-secondary"
              style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', padding: '6px 12px' }}
            >
              <ScanLine size={16} />
              <span>Scan Book Barcode</span>
            </button>
          </div>

          <div style={{ position: 'relative', marginBottom: '16px' }}>
            <Search size={16} color="var(--text-light)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="🔍 Search Resource by Title, ISBN, or ID..."
              value={resourceQuery}
              onChange={(e) => setResourceQuery(e.target.value)}
              className="input-base"
              style={{ paddingLeft: '36px' }}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '280px', overflowY: 'auto' }}>
            {resourceMatches.map((r) => {
              const isSelected = selectedResource?.id === r.id;
              const isAvail = r.status === 'AVAILABLE' || r.isAvailable === true;
              return (
                <div
                  key={r.id}
                  onClick={() => setSelectedResource(r)}
                  style={{
                    padding: '12px 16px',
                    borderRadius: '8px',
                    border: isSelected ? '1.5px solid var(--primary)' : '1px solid var(--border-color)',
                    backgroundColor: isSelected ? 'var(--bg-accent-light)' : '#FFFFFF',
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{r.title}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      ID: {r.id} • {r.author || r.publisher} • {r.type || 'BOOK'} • {r.location || 'Stacks'}
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Badge status={isAvail ? 'AVAILABLE' : 'ISSUED'} />
                    {r.isReferenceOnly && (
                      <span style={{ fontSize: '0.7rem', backgroundColor: '#FEF3C7', color: '#92400E', padding: '2px 6px', borderRadius: '4px', fontWeight: 600 }}>
                        Ref Only
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button onClick={() => setStep(1)} className="btn-secondary">
              Back to Member
            </button>
            {selectedResource && (
              <button onClick={() => setStep(3)} className="btn-primary">
                <span>Proceed to Validation</span>
                <ArrowRight size={15} />
              </button>
            )}
          </div>
        </div>
      )}

      {/* Step 3: Chain of Responsibility Validation Display */}
      {step === 3 && selectedMember && selectedResource && (
        <div className="card-base" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 600, marginBottom: '6px' }}>Step 3: Multi-Tier Loan Validation</h2>

          {/* Validation Pipeline Checklist */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
            {validation.steps.map((rule, index) => (
              <div
                key={index}
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '12px',
                  padding: '12px 16px',
                  borderRadius: '8px',
                  backgroundColor: rule.passed ? '#F0FDF4' : '#FEF2F2',
                  border: rule.passed ? '1px solid #BBF7D0' : '1px solid #FECACA'
                }}
              >
                {rule.passed ? (
                  <CheckCircle2 size={18} color="#16A34A" style={{ marginTop: '2px', shrink: 0 }} />
                ) : (
                  <XCircle size={18} color="#DC2626" style={{ marginTop: '2px', shrink: 0 }} />
                )}
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem', color: rule.passed ? '#15803D' : '#991B1B' }}>
                    Rule {index + 1}: {rule.name}
                  </div>
                  <div style={{ fontSize: '0.775rem', color: rule.passed ? '#166534' : '#7F1D1D', marginTop: '2px' }}>
                    {rule.detail}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Issue Confirmation Actions */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '16px', borderTop: '1px solid var(--border-color)' }}>
            <button onClick={() => setStep(2)} className="btn-secondary">
              Back
            </button>
            <button
              onClick={handleConfirmIssue}
              disabled={!validation.allPassed}
              className="btn-primary"
              style={{ opacity: validation.allPassed ? 1 : 0.5, cursor: validation.allPassed ? 'pointer' : 'not-allowed' }}
            >
              <CheckCircle2 size={16} />
              <span>Confirm & Issue Resource</span>
            </button>
          </div>
        </div>
      )}

      {/* Barcode Scanner Modal */}
      <BarcodeScannerModal
        isOpen={scannerOpen}
        onClose={() => setScannerOpen(false)}
        onScan={handleScanComplete}
        title={scanType === 'MEMBER' ? 'Scan Member ID Card' : 'Scan Resource Barcode'}
        scanType={scanType}
      />
    </div>
  );
};
