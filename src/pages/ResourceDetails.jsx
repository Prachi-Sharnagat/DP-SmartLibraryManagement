import React from 'react';
import { useLibrary } from '../context/LibraryContext';
import { ArrowLeft, BookOpen, Download, BookmarkCheck, ArrowLeftRight, MapPin, Calendar, Building, Layers } from 'lucide-react';
import { Badge } from '../components/common/Badge';

export const ResourceDetails = ({ resource, onBack, onNavigateToIssue, onOpenReserveModal }) => {
  const { currentUser, loans } = useLibrary();

  if (!resource) return null;

  const activeLoan = loans.find((l) => l.resourceId === resource.id && l.status === 'ACTIVE');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '800px', margin: '0 auto' }}>
      {/* Back Button */}
      <button
        onClick={onBack}
        style={{
          background: 'none',
          border: 'none',
          color: 'var(--primary)',
          display: 'inline-flex',
          alignItems: 'center',
          gap: '6px',
          fontWeight: 600,
          fontSize: '0.875rem',
          cursor: 'pointer',
          width: 'fit-content'
        }}
      >
        <ArrowLeft size={16} />
        <span>Back to Catalog</span>
      </button>

      {/* Main Resource Card */}
      <div className="card-base" style={{ padding: '32px' }}>
        {/* Title Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '16px', borderBottom: '1px solid var(--border-color)', paddingBottom: '20px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-light)', textTransform: 'uppercase' }}>
                {resource.id}
              </span>
              <span>•</span>
              <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--primary)' }}>
                {resource.category}
              </span>
            </div>
            <h1 style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--text-main)', lineHeight: 1.3 }}>
              {resource.title}
            </h1>
            <p style={{ fontSize: '1.05rem', color: 'var(--text-muted)', marginTop: '6px' }}>
              {resource.author}
            </p>
          </div>

          <div>
            <Badge status={resource.status} />
          </div>
        </div>

        {/* Key Attributes Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', padding: '24px 0', borderBottom: '1px solid var(--border-color)' }}>
          <div>
            <span style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-light)', textTransform: 'uppercase' }}>
              ISBN / ISSN
            </span>
            <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)', marginTop: '4px', display: 'block' }}>
              {resource.isbn}
            </span>
          </div>

          <div>
            <span style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-light)', textTransform: 'uppercase' }}>
              Resource Type
            </span>
            <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)', marginTop: '4px', display: 'block' }}>
              {resource.type === 'EBOOK' ? 'Digital E-Book' : (resource.type === 'JOURNAL' ? 'Academic Journal' : 'Physical Book')}
            </span>
          </div>

          <div>
            <span style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-light)', textTransform: 'uppercase' }}>
              Shelf / Rack Location
            </span>
            <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <MapPin size={14} color="var(--primary)" />
              {resource.location}
            </span>
          </div>
        </div>

        {/* Resource Information Section */}
        <div style={{ padding: '24px 0', borderBottom: '1px solid var(--border-color)' }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '16px', color: 'var(--text-main)' }}>
            Resource Information
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', fontSize: '0.875rem' }}>
            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>Publisher:</span>
              <strong style={{ color: 'var(--text-main)' }}>{resource.publisher}</strong>
            </div>

            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>Publication Year:</span>
              <strong style={{ color: 'var(--text-main)' }}>{resource.year}</strong>
            </div>

            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>Edition:</span>
              <strong style={{ color: 'var(--text-main)' }}>{resource.edition}</strong>
            </div>

            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>Standard Loan Duration:</span>
              <strong style={{ color: 'var(--text-main)' }}>{resource.borrowDays} Days</strong>
            </div>

            {resource.type === 'EBOOK' && (
              <div>
                <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>File Size & DRM:</span>
                <strong style={{ color: 'var(--text-main)' }}>{resource.fileSizeMB || 15} MB (Secure PDF/EPUB)</strong>
              </div>
            )}

            {resource.isReferenceOnly && (
              <div style={{ gridColumn: 'span 2', backgroundColor: '#FEF2F2', padding: '10px 14px', borderRadius: '6px', border: '1px solid #FECACA', color: '#991B1B', fontSize: '0.825rem' }}>
                ⚠️ <strong>Reference Only Resource:</strong> This academic journal is designated for in-library reading and research archive access only. Home checkout is disabled.
              </div>
            )}
          </div>

          {resource.description && (
            <div style={{ marginTop: '20px' }}>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem', marginBottom: '4px' }}>Overview & Scope:</span>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-main)', lineHeight: 1.6 }}>{resource.description}</p>
            </div>
          )}
        </div>

        {/* Current Circulation Status & Actions */}
        <div style={{ paddingTop: '24px' }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '12px', color: 'var(--text-main)' }}>
            Current Status
          </h3>

          {resource.status === 'AVAILABLE' ? (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#16A34A', fontWeight: 600, fontSize: '0.9rem' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#16A34A' }}></span>
                  <span>Available for immediate issue</span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                  {resource.availableCopies} {resource.availableCopies === 1 ? 'copy' : 'copies'} in stock at {resource.location}
                </div>
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                {currentUser.role !== 'STUDENT' && !resource.isReferenceOnly && (
                  <button onClick={() => onNavigateToIssue(resource)} className="btn-primary">
                    <ArrowLeftRight size={15} />
                    <span>Issue Book</span>
                  </button>
                )}

                {resource.type === 'EBOOK' && (
                  <button
                    onClick={() => alert(`Downloading secure digital copy for ${currentUser.name}...`)}
                    className="btn-primary"
                    style={{ backgroundColor: '#1E293B', borderColor: '#1E293B' }}
                  >
                    <Download size={15} />
                    <span>Download E-Book</span>
                  </button>
                )}

                <button onClick={() => onOpenReserveModal(resource)} className="btn-secondary">
                  <BookmarkCheck size={15} />
                  <span>Reserve</span>
                </button>
              </div>
            </div>
          ) : (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#D97706', fontWeight: 600, fontSize: '0.9rem' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#D97706' }}></span>
                  <span>Currently {resource.status === 'ISSUED' ? 'Issued' : 'Reserved'}</span>
                </div>
                {activeLoan && (
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                    Expected Return / Due Date: <strong>{activeLoan.dueDate}</strong>
                  </div>
                )}
              </div>

              <button onClick={() => onOpenReserveModal(resource)} className="btn-primary">
                <BookmarkCheck size={15} />
                <span>Reserve Book (Waitlist)</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
