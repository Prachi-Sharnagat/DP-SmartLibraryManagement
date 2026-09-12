import React from 'react';
import { useLibrary } from '../../context/LibraryContext';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export const ToastContainer = () => {
  const { toasts, removeToast } = useLibrary();

  if (!toasts || toasts.length === 0) return null;

  return (
    <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '8px', maxWidth: '420px' }}>
      {toasts.map((toast) => {
        let bg = '#0F172A';
        let border = '#334155';
        let IconComp = Info;
        let iconColor = '#38BDF8';

        if (toast.type === 'success') {
          bg = '#064E3B';
          border = '#059669';
          IconComp = CheckCircle2;
          iconColor = '#34D399';
        } else if (toast.type === 'error') {
          bg = '#7F1D1D';
          border = '#DC2626';
          IconComp = AlertCircle;
          iconColor = '#F87171';
        }

        return (
          <div
            key={toast.id}
            style={{
              backgroundColor: bg,
              color: '#FFFFFF',
              border: `1px solid ${border}`,
              borderRadius: '8px',
              padding: '10px 14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '12px',
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              fontSize: '0.875rem'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <IconComp size={18} color={iconColor} style={{ flexShrink: 0 }} />
              <span>{toast.message}</span>
            </div>
            <button
              onClick={() => removeToast(toast.id)}
              style={{ background: 'none', border: 'none', color: '#94A3B8', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
            >
              <X size={16} />
            </button>
          </div>
        );
      })}
    </div>
  );
};
