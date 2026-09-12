import React, { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { QrCode, ScanLine, CheckCircle2, Sparkles, Camera, BookOpen, User, RefreshCw } from 'lucide-react';
import { useLibrary } from '../../context/LibraryContext';

export const BarcodeScannerModal = ({ isOpen, onClose, onScan, title = "Scan Barcode / QR Code", scanType = "RESOURCE" }) => {
  const { resources, users } = useLibrary();
  const [scanning, setScanning] = useState(true);
  const [scannedValue, setScannedValue] = useState(null);
  const [manualCode, setManualCode] = useState('');

  useEffect(() => {
    if (isOpen) {
      setScanning(true);
      setScannedValue(null);
      setManualCode('');
    }
  }, [isOpen]);

  const handleSimulateScan = (item) => {
    const code = scanType === 'RESOURCE' ? item.id : item.userId;
    setScannedValue(item);
    setScanning(false);
    setTimeout(() => {
      onScan(code, item);
      onClose();
    }, 600);
  };

  const handleManualSubmit = (e) => {
    e.preventDefault();
    if (!manualCode.trim()) return;
    const target = scanType === 'RESOURCE'
      ? resources.find(r => r.id.toLowerCase() === manualCode.trim().toLowerCase() || r.isbn?.toLowerCase() === manualCode.trim().toLowerCase())
      : users.find(u => u.userId.toLowerCase() === manualCode.trim().toLowerCase() || u.email?.toLowerCase() === manualCode.trim().toLowerCase());

    onScan(manualCode.trim(), target || { id: manualCode.trim(), title: manualCode.trim(), name: manualCode.trim() });
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <div className="space-y-6">
        {/* Scanner Viewport Simulation */}
        <div className="relative h-56 bg-slate-950 rounded-2xl overflow-hidden border border-slate-800 flex flex-col items-center justify-center p-6 text-center shadow-inner">
          <div className="absolute inset-0 bg-gradient-to-b from-blue-500/5 via-transparent to-purple-500/5" />
          
          {/* Laser Scanner animation line */}
          {scanning && (
            <div className="absolute left-4 right-4 h-0.5 bg-gradient-to-r from-transparent via-cyan-400 to-transparent shadow-[0_0_15px_rgba(34,211,238,0.8)] animate-pulse transition-all duration-700 top-1/2 -translate-y-1/2" />
          )}

          {/* Viewfinder Target Box */}
          <div className="relative w-48 h-36 border-2 border-dashed border-cyan-400/60 rounded-xl flex flex-col items-center justify-center bg-cyan-950/20 backdrop-blur-xs">
            {scannedValue ? (
              <div className="flex flex-col items-center text-emerald-400 animate-in zoom-in-95 duration-200">
                <CheckCircle2 className="w-10 h-10 mb-1" />
                <span className="text-xs font-bold uppercase tracking-wider">Identified!</span>
                <span className="text-sm font-semibold text-white mt-1 max-w-[160px] truncate">
                  {scanType === 'RESOURCE' ? scannedValue.title : scannedValue.name}
                </span>
              </div>
            ) : (
              <div className="flex flex-col items-center text-slate-400">
                <ScanLine className="w-10 h-10 text-cyan-400 animate-bounce duration-1000 mb-1" />
                <span className="text-xs font-mono text-cyan-300/80 uppercase tracking-wider">
                  Align Barcode Inside Box
                </span>
              </div>
            )}
          </div>

          <div className="absolute bottom-2 left-0 right-0 flex items-center justify-center space-x-2 text-[11px] font-mono text-slate-500">
            <Camera className="w-3.5 h-3.5 text-cyan-400" />
            <span>AI Optical Code Sensor Ready • 60 FPS</span>
          </div>
        </div>

        {/* Quick Sample Barcode Buttons */}
        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
            Click to Scan Registered {scanType === 'RESOURCE' ? 'Resources' : 'Members'}:
          </label>
          <div className="grid grid-cols-2 gap-2 max-h-40 overflow-y-auto pr-1">
            {scanType === 'RESOURCE' ? (
              resources.slice(0, 6).map(r => (
                <button
                  key={r.id}
                  type="button"
                  onClick={() => handleSimulateScan(r)}
                  className="flex items-center space-x-2.5 p-2 rounded-xl bg-slate-900/60 hover:bg-cyan-950/40 border border-slate-800 hover:border-cyan-500/50 text-left transition-all group"
                >
                  <div className="p-1.5 rounded-lg bg-cyan-500/10 text-cyan-400 group-hover:bg-cyan-500/20">
                    <BookOpen className="w-4 h-4" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-bold text-slate-200 truncate group-hover:text-cyan-300">{r.title}</p>
                    <p className="text-[10px] font-mono text-slate-400">{r.id} • {r.type || 'BOOK'}</p>
                  </div>
                </button>
              ))
            ) : (
              users.slice(0, 6).map(u => (
                <button
                  key={u.userId}
                  type="button"
                  onClick={() => handleSimulateScan(u)}
                  className="flex items-center space-x-2.5 p-2 rounded-xl bg-slate-900/60 hover:bg-cyan-950/40 border border-slate-800 hover:border-cyan-500/50 text-left transition-all group"
                >
                  <div className="p-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 group-hover:bg-indigo-500/20">
                    <User className="w-4 h-4" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-bold text-slate-200 truncate group-hover:text-indigo-300">{u.name}</p>
                    <p className="text-[10px] font-mono text-slate-400">{u.userId} • {u.role}</p>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>

        {/* Manual Barcode Input Fallback */}
        <form onSubmit={handleManualSubmit} className="pt-2 border-t border-slate-800/80">
          <label className="block text-xs font-medium text-slate-400 mb-1.5">
            Or Enter Code Manually:
          </label>
          <div className="flex space-x-2">
            <input
              type="text"
              value={manualCode}
              onChange={(e) => setManualCode(e.target.value)}
              placeholder={scanType === 'RESOURCE' ? "e.g. B001 or 978-0201633610" : "e.g. U101 or alice@example.com"}
              className="flex-1 bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white font-medium text-sm rounded-xl transition-all shadow-md shadow-cyan-950/50"
            >
              Confirm
            </button>
          </div>
        </form>
      </div>
    </Modal>
  );
};
