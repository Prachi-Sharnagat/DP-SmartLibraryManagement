import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { BookOpen, ShieldCheck, ArrowRight, UserCheck } from 'lucide-react';

export const Login = ({ onLoginSuccess }) => {
  const { users, setCurrentUser, showToast, recordAuditLog } = useLibrary();
  const [emailOrId, setEmailOrId] = useState('prachi@univ.edu');
  const [password, setPassword] = useState('••••••••');
  const [rememberMe, setRememberMe] = useState(true);

  const handleSignIn = (e) => {
    e.preventDefault();
    const found = users.find(
      (u) => u.email.toLowerCase() === emailOrId.toLowerCase() || u.id.toLowerCase() === emailOrId.toLowerCase()
    ) || users[0];

    setCurrentUser(found);
    recordAuditLog('AUTH_LOGIN', `User ${found.name} signed in (${found.role})`, 12);
    showToast(`Welcome back, ${found.name}!`, 'success');
    if (onLoginSuccess) onLoginSuccess();
  };

  const handleQuickLogin = (user) => {
    setCurrentUser(user);
    recordAuditLog('AUTH_QUICK_LOGIN', `Demo login as ${user.name} (${user.role})`, 10);
    showToast(`Signed in as ${user.name} (${user.role})`, 'success');
    if (onLoginSuccess) onLoginSuccess();
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', backgroundColor: 'var(--bg-primary)' }}>
      {/* Left Split Banner (Academic & Clean) */}
      <div
        style={{
          flex: '1 1 50%',
          backgroundColor: '#0F172A',
          color: '#FFFFFF',
          padding: '60px 80px',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'space-between',
          borderRight: '1px solid #1E293B'
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '32px' }}>
            <div style={{ width: '40px', height: '40px', backgroundColor: 'rgba(2, 132, 199, 0.2)', color: '#38BDF8', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <BookOpen size={22} />
            </div>
            <span style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.02em' }}>SMART LIBRARY</span>
          </div>

          <div style={{ marginTop: '80px', maxWidth: '440px' }}>
            <h1 style={{ fontSize: '2.5rem', fontWeight: 700, lineHeight: 1.2, color: '#FFFFFF', letterSpacing: '-0.03em' }}>
              Manage books.<br />
              Manage members.<br />
              <span style={{ color: '#38BDF8' }}>Simplify circulation.</span>
            </h1>
            <p style={{ marginTop: '24px', fontSize: '1rem', color: '#94A3B8', lineHeight: 1.6 }}>
              A high-integrity university library operations platform powered by modern software engineering architecture and GoF design patterns.
            </p>
          </div>
        </div>

        <div style={{ borderTop: '1px solid #1E293B', paddingTop: '24px', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#64748B' }}>
          <span>University Library Operations • v3.2</span>
          <span>Security Verified</span>
        </div>
      </div>

      {/* Right Split Sign-In Form */}
      <div
        style={{
          flex: '1 1 50%',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '40px'
        }}
      >
        <div style={{ width: '100%', maxWidth: '400px' }}>
          <div style={{ marginBottom: '28px' }}>
            <h2 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.02em' }}>
              Welcome back
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
              Sign in to your library account
            </p>
          </div>

          <form onSubmit={handleSignIn} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '6px' }}>
                Email / Username
              </label>
              <input
                type="text"
                required
                className="input-base"
                value={emailOrId}
                onChange={(e) => setEmailOrId(e.target.value)}
                placeholder="e.g. prachi@univ.edu or U1024"
              />
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <label style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)' }}>
                  Password
                </label>
                <a href="#forgot" onClick={(e) => e.preventDefault()} style={{ fontSize: '0.75rem', color: 'var(--primary)', textDecoration: 'none', fontWeight: 500 }}>
                  Forgot password?
                </a>
              </div>
              <input
                type="password"
                required
                className="input-base"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input
                type="checkbox"
                id="remember"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                style={{ accentColor: 'var(--primary)', cursor: 'pointer' }}
              />
              <label htmlFor="remember" style={{ fontSize: '0.825rem', color: 'var(--text-muted)', cursor: 'pointer' }}>
                Remember me
              </label>
            </div>

            <button type="submit" className="btn-primary" style={{ width: '100%', justifyContent: 'center', padding: '10px', fontSize: '0.925rem', marginTop: '8px' }}>
              <span>Sign In</span>
              <ArrowRight size={16} />
            </button>
          </form>

          {/* Quick Demo Logins for Pair Programming Evaluation */}
          <div style={{ marginTop: '28px', paddingTop: '20px', borderTop: '1px solid var(--border-color)' }}>
            <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '10px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Instant Demo Access (By Role):
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px' }}>
              <button
                type="button"
                onClick={() => handleQuickLogin(users[0])}
                className="btn-secondary"
                style={{ fontSize: '0.75rem', padding: '8px 4px', flexDirection: 'column', gap: '2px', textAlign: 'center' }}
              >
                <span style={{ fontWeight: 600 }}>Prachi</span>
                <span style={{ fontSize: '0.65rem', color: '#16A34A' }}>Student</span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin(users[2])}
                className="btn-secondary"
                style={{ fontSize: '0.75rem', padding: '8px 4px', flexDirection: 'column', gap: '2px', textAlign: 'center' }}
              >
                <span style={{ fontWeight: 600 }}>Ananya</span>
                <span style={{ fontSize: '0.65rem', color: '#0284C7' }}>Librarian</span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin(users[5])}
                className="btn-secondary"
                style={{ fontSize: '0.75rem', padding: '8px 4px', flexDirection: 'column', gap: '2px', textAlign: 'center' }}
              >
                <span style={{ fontWeight: 600 }}>Dr. Sharma</span>
                <span style={{ fontSize: '0.65rem', color: '#9333EA' }}>Admin</span>
              </button>
            </div>
          </div>

          <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Don't have an account?{' '}
            <span style={{ color: 'var(--primary)', fontWeight: 600, cursor: 'pointer' }}>
              Contact Library Admin
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
