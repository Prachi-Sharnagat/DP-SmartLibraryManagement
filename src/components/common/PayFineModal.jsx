import React, { useState } from 'react';
import { Modal } from './Modal';
import { DollarSign, CreditCard, Wallet, Banknote, CheckCircle2, ShieldCheck } from 'lucide-react';
import { useLibrary } from '../../context/LibraryContext';

export const PayFineModal = ({ isOpen, onClose, member }) => {
  const { payFine } = useLibrary();
  const [paymentMethod, setPaymentMethod] = useState('CARD'); // CARD | CASH | DIGITAL
  const [isProcessing, setIsProcessing] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);

  if (!member) return null;

  const currentFine = member.outstandingFine || 0;

  const handlePay = async (e) => {
    e.preventDefault();
    setIsProcessing(true);

    try {
      await payFine(member.userId, currentFine);
      setIsProcessing(false);
      setIsSuccess(true);
      setTimeout(() => {
        setIsSuccess(false);
        onClose();
      }, 1400);
    } catch (err) {
      setIsProcessing(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Settle Dues: ${member.name}`}>
      {isSuccess ? (
        <div className="py-8 flex flex-col items-center justify-center text-center space-y-3">
          <div className="w-16 h-16 rounded-full bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center text-emerald-400 animate-in zoom-in-50 duration-300">
            <CheckCircle2 className="w-10 h-10" />
          </div>
          <h3 className="text-lg font-bold text-white">Payment Received!</h3>
          <p className="text-sm text-slate-400 max-w-xs">
            Fine balance of ${currentFine.toFixed(2)} has been cleared and recorded in the audit log.
          </p>
        </div>
      ) : (
        <form onSubmit={handlePay} className="space-y-6">
          {/* Due Amount Highlight Card */}
          <div className="p-5 rounded-2xl bg-gradient-to-br from-amber-500/10 via-slate-900 to-slate-900 border border-amber-500/30 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-amber-400 uppercase tracking-wider">Outstanding Overdue Fine</p>
              <p className="text-sm text-slate-300 mt-0.5">Member ID: <span className="font-mono text-white">{member.userId}</span></p>
            </div>
            <div className="text-right">
              <span className="text-3xl font-extrabold text-amber-400 font-mono">${currentFine.toFixed(2)}</span>
            </div>
          </div>

          {/* Payment Method Selector */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
              Select Payment Method
            </label>
            <div className="grid grid-cols-3 gap-2.5">
              {[
                { id: 'CARD', label: 'Credit Card', icon: CreditCard },
                { id: 'CASH', label: 'Cash at Desk', icon: Banknote },
                { id: 'DIGITAL', label: 'Digital Wallet', icon: Wallet }
              ].map((m) => {
                const Icon = m.icon;
                const isSelected = paymentMethod === m.id;
                return (
                  <button
                    key={m.id}
                    type="button"
                    onClick={() => setPaymentMethod(m.id)}
                    className={`flex flex-col items-center p-3 rounded-xl border text-center transition-all ${
                      isSelected
                        ? 'bg-amber-500/20 border-amber-500 text-amber-300 shadow-md shadow-amber-950/40'
                        : 'bg-slate-900 border-slate-800 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                    }`}
                  >
                    <Icon className="w-5 h-5 mb-1.5" />
                    <span className="text-xs font-semibold">{m.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="flex items-center space-x-2 text-xs text-slate-400 bg-slate-900/60 p-3 rounded-xl border border-slate-800">
            <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>Simulated Instant Payment Gateway with SQLite Ledger reconciliation.</span>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isProcessing || currentFine <= 0}
              className="px-5 py-2 text-sm font-bold text-slate-950 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 rounded-xl transition-all shadow-lg shadow-amber-950/60 disabled:opacity-50 flex items-center space-x-2"
            >
              <DollarSign className="w-4 h-4" />
              <span>{isProcessing ? 'Processing...' : `Confirm Pay $${currentFine.toFixed(2)}`}</span>
            </button>
          </div>
        </form>
      )}
    </Modal>
  );
};
