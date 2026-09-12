import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Download, FileText, TrendingUp, BarChart, Users, DollarSign, BookOpen, CheckCircle2 } from 'lucide-react';

export const Reports = () => {
  const { resources, loans, users, showToast } = useLibrary();
  const [activeReportTab, setActiveReportTab] = useState('CIRCULATION'); // 'CIRCULATION' | 'INVENTORY' | 'MEMBERS'

  const exportCsv = (filename, headers, rows) => {
    const csvContent = 'data:text/csv;charset=utf-8,' +
      headers.join(',') + '\n' +
      rows.map(e => e.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(',')).join('\n');
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `${filename}_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast(`✓ Exported ${filename}.csv successfully`, 'success');
  };

  const handleExportCatalog = () => {
    const headers = ['ID', 'Title', 'Type', 'Author/Publisher', 'ISBN/Format', 'Shelf Location', 'Status', 'Loan Period Days', 'Fine Per Day'];
    const rows = resources.map(r => [
      r.id,
      r.title,
      r.type || 'BOOK',
      r.author || r.publisher || 'N/A',
      r.isbn || r.downloadFormat || 'N/A',
      r.location || 'Stacks',
      (r.status === 'AVAILABLE' || r.isAvailable) ? 'AVAILABLE' : 'CHECKED_OUT',
      r.borrowDays || 14,
      `$${(r.finePerDay || 1.5).toFixed(2)}`
    ]);
    exportCsv('Library_Master_Catalog', headers, rows);
  };

  const handleExportLoans = () => {
    const headers = ['Loan ID', 'Resource ID', 'Resource Title', 'Member ID', 'Member Name', 'Issue Date', 'Due Date', 'Return Date', 'Status', 'Fine Amount'];
    const rows = loans.map(l => [
      l.id || l.loanId,
      l.resourceId,
      l.resourceTitle || l.resourceId,
      l.memberId || l.userId,
      l.memberName || l.userId,
      l.issueDate || l.borrowDate || 'N/A',
      l.dueDate || 'N/A',
      l.returnDate || 'NOT_RETURNED',
      l.status || (l.isReturned ? 'RETURNED' : 'ACTIVE'),
      `$${(l.calculatedFine || l.fine || 0).toFixed(2)}`
    ]);
    exportCsv('Library_Circulation_Ledger', headers, rows);
  };

  const handleExportMembers = () => {
    const headers = ['User ID', 'Full Name', 'Email', 'Phone', 'Role', 'Status', 'Active Loans', 'Outstanding Fine'];
    const rows = users.map(u => [
      u.id || u.userId,
      u.name,
      u.email,
      u.phone || 'N/A',
      u.role || 'MEMBER',
      u.status || (u.membershipActive ? 'ACTIVE' : 'INACTIVE'),
      u.currentLoans ?? u.currentBorrowedCount ?? 0,
      `$${(u.outstandingFine || 0).toFixed(2)}`
    ]);
    exportCsv('Library_Member_Roster', headers, rows);
  };

  const handlePrintPdf = () => {
    window.print();
  };

  const totalFinesCalculated = users.reduce((acc, u) => acc + (u.outstandingFine || 0), 0);
  const activeLoansCount = loans.filter(l => l.status === 'ACTIVE' || !l.isReturned).length;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header with Export Actions */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Operations Reports & Analytics
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
            Generate institutional circulation ledgers, inventory audits, and export PDF/CSV dossiers.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <button onClick={handleExportCatalog} className="btn-secondary" style={{ fontSize: '0.8rem' }}>
            <Download size={14} />
            <span>Catalog CSV</span>
          </button>
          <button onClick={handleExportLoans} className="btn-secondary" style={{ fontSize: '0.8rem' }}>
            <Download size={14} />
            <span>Circulation CSV</span>
          </button>
          <button onClick={handleExportMembers} className="btn-secondary" style={{ fontSize: '0.8rem' }}>
            <Download size={14} />
            <span>Members CSV</span>
          </button>
          <button onClick={handlePrintPdf} className="btn-primary" style={{ fontSize: '0.8rem' }}>
            <FileText size={14} />
            <span>Print / Save PDF</span>
          </button>
        </div>
      </div>

      {/* Circulation KPI Highlights */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
        <div className="card-base" style={{ padding: '20px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500 }}>Active Checked-Out Loans</span>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--primary)', marginTop: '6px' }}>{activeLoansCount}</div>
          <span style={{ fontSize: '0.75rem', color: '#16A34A', fontWeight: 600 }}>Real-time Chain of Responsibility validated</span>
        </div>

        <div className="card-base" style={{ padding: '20px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500 }}>Total Registered Catalog Items</span>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '6px' }}>{resources.length}</div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Books, Digital eBooks & Academic Journals</span>
        </div>

        <div className="card-base" style={{ padding: '20px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500 }}>Registered Library Members</span>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '6px' }}>{users.length}</div>
          <span style={{ fontSize: '0.75rem', color: '#16A34A', fontWeight: 600 }}>Active user accounts</span>
        </div>

        <div className="card-base" style={{ padding: '20px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 500 }}>Outstanding Overdue Dues</span>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: totalFinesCalculated > 0 ? '#DC2626' : '#16A34A', marginTop: '6px' }}>
            ${totalFinesCalculated.toFixed(2)}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Calculated at $0.50/day overdue rate</span>
        </div>
      </div>

      {/* Report Tables Section */}
      <div className="card-base" style={{ padding: '20px' }}>
        <div style={{ display: 'flex', gap: '10px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px', marginBottom: '16px' }}>
          {[
            { id: 'CIRCULATION', label: 'Recent Circulation Loans', count: loans.length },
            { id: 'INVENTORY', label: 'Resource Inventory', count: resources.length },
            { id: 'MEMBERS', label: 'Member Ledger', count: users.length }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveReportTab(tab.id)}
              style={{
                background: 'none',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '6px',
                fontSize: '0.85rem',
                fontWeight: activeReportTab === tab.id ? 700 : 500,
                color: activeReportTab === tab.id ? 'var(--primary)' : 'var(--text-muted)',
                backgroundColor: activeReportTab === tab.id ? 'var(--bg-accent-light)' : 'transparent',
                cursor: 'pointer'
              }}
            >
              {tab.label} ({tab.count})
            </button>
          ))}
        </div>

        {activeReportTab === 'CIRCULATION' && (
          <div className="table-container">
            <table className="lib-table">
              <thead>
                <tr>
                  <th>Loan ID</th>
                  <th>Resource</th>
                  <th>Member</th>
                  <th>Due Date</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Fine</th>
                </tr>
              </thead>
              <tbody>
                {loans.slice(0, 8).map(l => (
                  <tr key={l.id || l.loanId}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>{l.id || l.loanId}</td>
                    <td>
                      <div style={{ fontWeight: 600 }}>{l.resourceTitle || l.resourceId}</div>
                    </td>
                    <td>{l.memberName || l.userId}</td>
                    <td>{l.dueDate || 'N/A'}</td>
                    <td>
                      <span style={{
                        padding: '2px 8px', borderRadius: '12px', fontSize: '0.725rem', fontWeight: 600,
                        backgroundColor: (l.status === 'RETURNED' || l.isReturned) ? '#DCFCE7' : '#EFF6FF',
                        color: (l.status === 'RETURNED' || l.isReturned) ? '#15803D' : '#1D4ED8'
                      }}>
                        {l.status || (l.isReturned ? 'RETURNED' : 'ACTIVE')}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: (l.calculatedFine || l.fine || 0) > 0 ? '#DC2626' : 'var(--text-muted)' }}>
                      ${(l.calculatedFine || l.fine || 0).toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeReportTab === 'INVENTORY' && (
          <div className="table-container">
            <table className="lib-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Type</th>
                  <th>Author/Publisher</th>
                  <th>Location</th>
                  <th style={{ textAlign: 'right' }}>Availability</th>
                </tr>
              </thead>
              <tbody>
                {resources.slice(0, 8).map(r => (
                  <tr key={r.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>{r.id}</td>
                    <td style={{ fontWeight: 600 }}>{r.title}</td>
                    <td>
                      <span style={{ fontSize: '0.75rem', padding: '2px 6px', borderRadius: '4px', backgroundColor: '#F1F5F9' }}>
                        {r.type || 'BOOK'}
                      </span>
                    </td>
                    <td>{r.author || r.publisher}</td>
                    <td>{r.location || 'Stacks'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <span style={{
                        padding: '2px 8px', borderRadius: '12px', fontSize: '0.725rem', fontWeight: 600,
                        backgroundColor: (r.status === 'AVAILABLE' || r.isAvailable) ? '#DCFCE7' : '#FEE2E2',
                        color: (r.status === 'AVAILABLE' || r.isAvailable) ? '#15803D' : '#B91C1C'
                      }}>
                        {(r.status === 'AVAILABLE' || r.isAvailable) ? 'Available' : 'Issued'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeReportTab === 'MEMBERS' && (
          <div className="table-container">
            <table className="lib-table">
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Name</th>
                  <th>Role</th>
                  <th>Active Loans</th>
                  <th style={{ textAlign: 'right' }}>Outstanding Fine</th>
                </tr>
              </thead>
              <tbody>
                {users.slice(0, 8).map(u => (
                  <tr key={u.id || u.userId}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>{u.id || u.userId}</td>
                    <td style={{ fontWeight: 600 }}>{u.name}</td>
                    <td>{u.role || 'MEMBER'}</td>
                    <td>{u.currentLoans ?? u.currentBorrowedCount ?? 0}</td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: (u.outstandingFine || 0) > 0 ? '#DC2626' : '#16A34A' }}>
                      ${(u.outstandingFine || 0).toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
