import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Settings as SettingsIcon, Save, RefreshCw, Layers, ShieldCheck, Mail, Smartphone, Bell } from 'lucide-react';

export const Settings = () => {
  const { showToast, currentUser } = useLibrary();

  const [finePerDay, setFinePerDay] = useState(5);
  const [bookLoanDays, setBookLoanDays] = useState(14);
  const [ebookLoanDays, setEbookLoanDays] = useState(21);
  const [journalLoanDays, setJournalLoanDays] = useState(7);
  const [maxStudentLoans, setMaxStudentLoans] = useState(5);

  const [enableEmail, setEnableEmail] = useState(true);
  const [enableSMS, setEnableSMS] = useState(true);
  const [enableInApp, setEnableInApp] = useState(true);

  const handleSave = (e) => {
    e.preventDefault();
    showToast('✓ Library system policies and notification channels saved', 'success');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '840px', margin: '0 auto' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
          System Settings & Policies
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
          Configure circulation policies, lending quotas, fine calculations, and notification bridges.
        </p>
      </div>

      <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        {/* Circulation Policies */}
        <div className="card-base" style={{ padding: '24px' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, marginBottom: '16px' }}>Circulation & Loan Durations</h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                Physical Book Duration (Days)
              </label>
              <input
                type="number"
                value={bookLoanDays}
                onChange={(e) => setBookLoanDays(e.target.value)}
                className="input-base"
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                Digital E-Book Duration (Days)
              </label>
              <input
                type="number"
                value={ebookLoanDays}
                onChange={(e) => setEbookLoanDays(e.target.value)}
                className="input-base"
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                Academic Journal Duration (Days)
              </label>
              <input
                type="number"
                value={journalLoanDays}
                onChange={(e) => setJournalLoanDays(e.target.value)}
                className="input-base"
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                Overdue Fine Rate (₹ per day)
              </label>
              <input
                type="number"
                value={finePerDay}
                onChange={(e) => setFinePerDay(e.target.value)}
                className="input-base"
              />
            </div>
          </div>
        </div>

        {/* Bridge Notification Dispatchers */}
        <div className="card-base" style={{ padding: '24px' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, marginBottom: '4px' }}>Notification Bridge Channels</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
            Enable or disable active delivery channels decoupled via Bridge Pattern.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border-color)', cursor: 'pointer' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Mail size={18} color="var(--primary)" />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>Email Notification Channel</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Institutional SMTP gateway for reservation & overdue notices</div>
                </div>
              </div>
              <input
                type="checkbox"
                checked={enableEmail}
                onChange={(e) => setEnableEmail(e.target.checked)}
                style={{ accentColor: 'var(--primary)', cursor: 'pointer', width: '18px', height: '18px' }}
              />
            </label>

            <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border-color)', cursor: 'pointer' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Smartphone size={18} color="var(--primary)" />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>SMS Alert Channel</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Urgent SMS delivery for pickup readiness within 48h</div>
                </div>
              </div>
              <input
                type="checkbox"
                checked={enableSMS}
                onChange={(e) => setEnableSMS(e.target.checked)}
                style={{ accentColor: 'var(--primary)', cursor: 'pointer', width: '18px', height: '18px' }}
              />
            </label>

            <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', borderRadius: '6px', border: '1px solid var(--border-color)', cursor: 'pointer' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Bell size={18} color="var(--primary)" />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>In-App Push Channel</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Direct web notification bell indicator and badge updates</div>
                </div>
              </div>
              <input
                type="checkbox"
                checked={enableInApp}
                onChange={(e) => setEnableInApp(e.target.checked)}
                style={{ accentColor: 'var(--primary)', cursor: 'pointer', width: '18px', height: '18px' }}
              />
            </label>
          </div>
        </div>

        {/* Design Pattern Architecture Summary Card */}
        <div className="card-base" style={{ padding: '24px', backgroundColor: '#F8FAFC' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, marginBottom: '12px' }}>Software Engineering Architecture Overview</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px', fontSize: '0.8rem' }}>
            <div style={{ padding: '10px', backgroundColor: '#FFFFFF', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
              <strong>Factory Pattern:</strong> Polymorphic creation of Books, E-Books, and Journals.
            </div>
            <div style={{ padding: '10px', backgroundColor: '#FFFFFF', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
              <strong>Chain of Responsibility:</strong> 6-stage circulation policy validation pipeline.
            </div>
            <div style={{ padding: '10px', backgroundColor: '#FFFFFF', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
              <strong>Observer Pattern:</strong> Automated waitlist reservation events & alerts on return.
            </div>
            <div style={{ padding: '10px', backgroundColor: '#FFFFFF', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
              <strong>Bridge Pattern:</strong> Notification abstraction decoupled from delivery channels.
            </div>
            <div style={{ padding: '10px', backgroundColor: '#FFFFFF', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
              <strong>Proxy Pattern:</strong> RBAC security access control and latency audit logging.
            </div>
          </div>
        </div>

        {/* Save Button */}
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <button type="submit" className="btn-primary" style={{ padding: '10px 24px' }}>
            <Save size={16} />
            <span>Save Policies</span>
          </button>
        </div>
      </form>
    </div>
  );
};
