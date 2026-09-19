import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { BookOpen, ShieldCheck, ArrowRight, UserCheck } from 'lucide-react';

export const Login = ({ onLoginSuccess }) => {
  const { login, showToast, recordAuditLog } = useLibrary();
  const [emailOrId, setEmailOrId] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const handleSignIn = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setErrorMsg('');

    const res = await login(emailOrId, password);
    if (res.success) {
      showToast(`Welcome back!`, 'success');
      if (onLoginSuccess) onLoginSuccess();
    } else {
      setErrorMsg('Invalid email or password.');
    }
    setIsLoading(false);
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
              A high-integrity university library operations platform powered by modern software engineering architecture.
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

          {errorMsg && (
            <div style={{ marginBottom: '16px', padding: '12px', backgroundColor: '#FEE2E2', color: '#B91C1C', borderRadius: '6px', fontSize: '0.875rem' }}>
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSignIn} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '6px' }}>
                Email / User ID
              </label>
              <input
                type="text"
                required
                className="input-base"
                value={emailOrId}
                onChange={(e) => setEmailOrId(e.target.value)}
                placeholder="e.g. U101 or email"
              />
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <label style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)' }}>
                  Password
                </label>
              </div>
              <input
                type="password"
                required
                className="input-base"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
              />
            </div>

            <button type="submit" className="btn-primary" disabled={isLoading} style={{ width: '100%', justifyContent: 'center', padding: '10px', fontSize: '0.925rem', marginTop: '8px' }}>
              <span>{isLoading ? 'Signing In...' : 'Sign In'}</span>
              {!isLoading && <ArrowRight size={16} />}
            </button>
          </form>

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
