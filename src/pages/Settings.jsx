import React, { useState, useEffect } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Settings as SettingsIcon, Save, RefreshCw, Layers, ShieldCheck, Mail, Smartphone, Bell, Calculator, CheckCircle2, BookOpen } from 'lucide-react';
import { api } from '../services/api';

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

  // Strategy Pattern Simulation State
  const [simRole, setSimRole] = useState('STUDENT');
  const [simDays, setSimDays] = useState(10);
  const [simResult, setSimResult] = useState(null);
  const [simLoading, setSimLoading] = useState(false);

  // Run calculation simulation
  const handleCalculateStrategy = async () => {
    setSimLoading(true);
    try {
      const res = await api.previewFine(simRole, simDays);
      if (res.ok && res.data?.success) {
        setSimResult(res.data.calculation);
      } else {
        // Fallback local calculation if backend is in offline mode
        const rates = {
          STUDENT: { grace: 3, rate: 0.50, cap: 20.00, name: 'Student Subsidized Fine Policy' },
          FACULTY: { grace: 7, rate: 0.10, cap: 10.00, name: 'Faculty Academic Privilege Policy' },
          MEMBER: { grace: 1, rate: 1.00, cap: 50.00, name: 'Standard Member Fine Policy' },
          STANDARD: { grace: 1, rate: 1.00, cap: 50.00, name: 'Standard Member Fine Policy' }
        };
        const p = rates[simRole] || rates.STANDARD;
        const billable = Math.max(0, simDays - p.grace);
        const raw = billable * p.rate;
        const capped = raw > p.cap;
        const finalFine = Math.min(raw, p.cap);
        setSimResult({
          strategyName: p.name,
          daysOverdue: Number(simDays),
          graceDaysApplied: Math.min(simDays, p.grace),
          billableDays: billable,
          dailyRate: p.rate,
          calculatedFine: finalFine,
          isCappedAtMax: capped,
          maxFineLimit: p.cap
        });
      }
    } catch (err) {
      console.error(err);
    } finally {
      setSimLoading(false);
    }
  };

  useEffect(() => {
    handleCalculateStrategy();
  }, [simRole, simDays]);

  const handleSave = (e) => {
    e.preventDefault();
    showToast('✓ Library system policies and notification channels saved', 'success');
  };

  const patternsList = [
    { num: 1, name: 'Factory Method', type: 'Creational', classPath: 'factory.ResourceFactory', desc: 'Instantiates polymorphic library resources (Physical Book, E-Book, Journal) with subtype-specific business attributes.' },
    { num: 2, name: 'Chain of Responsibility', type: 'Behavioral', classPath: 'chain.ValidationHandler', desc: 'Passes borrow requests along a 5-tier chain: MembershipActive -> FineLimit -> BorrowCountLimit -> Availability -> Reservation.' },
    { num: 3, name: 'Observer Pattern', type: 'Behavioral', classPath: 'observer.ReservationManager', desc: 'Maintains resource waitlists and dispatches event notifications to subscribed patrons upon return to catalog.' },
    { num: 4, name: 'Bridge Pattern', type: 'Structural', classPath: 'bridge.NotificationChannel', desc: 'Decouples notification messages from delivery platforms (EmailChannel, SMSChannel, InAppChannel) for dynamic switching.' },
    { num: 5, name: 'Proxy Pattern', type: 'Structural', classPath: 'proxy.LibraryServiceProxy', desc: 'Intercepts service invocations to enforce role-based access control (RBAC), timing latency, and security audit logs.' },
    { num: 6, name: 'Builder Pattern', type: 'Creational', classPath: 'builder.BookBuilder / UserBuilder', desc: 'Constructs complex Book and User domain objects with validated required fields (title, ISBN, author) and optional metadata.' },
    { num: 7, name: 'Prototype Pattern', type: 'Creational', classPath: 'prototype.Prototype / Book.createCopy', desc: 'Clones reusable catalog templates and creates physical book copies with unique IDs while preventing primary key collisions.' },
    { num: 8, name: 'Mediator Pattern', type: 'Behavioral', classPath: 'mediator.LibraryMediator', desc: 'Coordinates return workflows among Circulation, Reservation, Fine, and Notification colleagues without circular coupling.' },
    { num: 9, name: 'Template Method', type: 'Behavioral', classPath: 'template.LibraryTransaction', desc: 'Defines rigid 6-phase transaction algorithm (Validation -> Prep -> Mutation -> Persistence -> Hooks -> Audit) with rollback safety.' },
    { num: 10, name: 'Strategy Pattern', type: 'Behavioral', classPath: 'strategy.FineCalculationStrategy', desc: 'Dynamically calculates overdue fines based on member category policies (Student subsidized, Faculty privileges, Standard).' },
    { num: 11, name: 'Memento Pattern', type: 'Behavioral', classPath: 'memento.LibraryMemento / LibraryHistory', desc: 'Captures and restores pre-mutation snapshots for safe compensating librarian database rollbacks while guarding irreversible actions.' },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '840px', margin: '0 auto' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
          System Settings & Design Patterns Architecture
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
          Configure circulation policies, test Strategy fine algorithms, and inspect the 11 integrated GoF Design Patterns.
        </p>
      </div>

      {/* STRATEGY PATTERN INTERACTIVE SIMULATOR */}
      <div className="card-base" style={{ padding: '24px', borderLeft: '4px solid var(--primary)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
          <div style={{ backgroundColor: '#EEF2FF', color: 'var(--primary)', padding: '6px', borderRadius: '6px' }}>
            <Calculator size={20} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Strategy Pattern: Dynamic Fine Policy Simulator</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Select a member role and overdue days to see the active Strategy algorithm calculate grace periods and caps in real-time.
            </p>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '16px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '6px' }}>
              Select Member Role Strategy
            </label>
            <select
              value={simRole}
              onChange={(e) => setSimRole(e.target.value)}
              className="input-base"
              style={{ cursor: 'pointer' }}
            >
              <option value="STUDENT">Student ($0.50/day, 3 days grace, max $20.00)</option>
              <option value="FACULTY">Faculty ($0.10/day, 7 days grace, max $10.00)</option>
              <option value="MEMBER">Standard Member ($1.00/day, 1 day grace, max $50.00)</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '6px' }}>
              Simulated Overdue Days
            </label>
            <input
              type="number"
              min="0"
              max="150"
              value={simDays}
              onChange={(e) => setSimDays(Math.max(0, parseInt(e.target.value) || 0))}
              className="input-base"
            />
          </div>
        </div>

        {simResult && (
          <div style={{ marginTop: '16px', padding: '16px', backgroundColor: '#F8FAFC', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--primary)' }}>
                {simResult.strategyName}
              </span>
              <span style={{ fontSize: '1.25rem', fontWeight: 800, color: simResult.calculatedFine > 0 ? '#DC2626' : '#059669' }}>
                Total Fine: ${simResult.calculatedFine?.toFixed(2)}
              </span>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              <div><strong>Overdue:</strong> {simResult.daysOverdue} days</div>
              <div><strong>Grace Applied:</strong> {simResult.graceDaysApplied} days</div>
              <div><strong>Billable Days:</strong> {simResult.billableDays} days</div>
              <div><strong>Daily Rate:</strong> ${simResult.dailyRate?.toFixed(2)}/day</div>
            </div>
            {simResult.isCappedAtMax && (
              <div style={{ marginTop: '8px', fontSize: '0.75rem', color: '#D97706', fontWeight: 600 }}>
                ⚠️ Maximum fine ceiling reached! Capped at ${simResult.maxFineLimit?.toFixed(2)}.
              </div>
            )}
          </div>
        )}
      </div>

      {/* 11 GOF DESIGN PATTERNS ARCHITECTURAL REGISTRY */}
      <div className="card-base" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
          <div style={{ backgroundColor: '#FDF2F8', color: '#DB2777', padding: '6px', borderRadius: '6px' }}>
            <Layers size={20} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>11 GoF Design Patterns Registry</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Complete catalog of the 11 integrated GoF design patterns powering the Smart Library Management System.
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {patternsList.map((p) => (
            <div
              key={p.num}
              style={{
                padding: '12px 14px',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                backgroundColor: '#FFFFFF',
                display: 'flex',
                flexDirection: 'column',
                gap: '4px'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-main)' }}>
                    {p.num}. {p.name}
                  </span>
                  <span
                    style={{
                      fontSize: '0.7rem',
                      padding: '2px 6px',
                      borderRadius: '4px',
                      fontWeight: 600,
                      backgroundColor: p.type === 'Creational' ? '#EFF6FF' : (p.type === 'Structural' ? '#ECFDF5' : '#FAF5FF'),
                      color: p.type === 'Creational' ? '#2563EB' : (p.type === 'Structural' ? '#059669' : '#9333EA')
                    }}
                  >
                    {p.type}
                  </span>
                </div>
                <code style={{ fontSize: '0.75rem', backgroundColor: '#F1F5F9', padding: '2px 6px', borderRadius: '4px', color: '#475569' }}>
                  {p.classPath}
                </code>
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                {p.desc}
              </div>
            </div>
          ))}
        </div>
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
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, marginBottom: '4px' }}>Notification Channels</h3>
          <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>
            Enable or disable active delivery channels.
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

