import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { BookOpen, Layers, Sparkles, CheckCircle2 } from 'lucide-react';

export const AddResource = ({ onClose, onSuccess }) => {
  const { addResource } = useLibrary();

  const [type, setType] = useState('BOOK'); // BOOK, EBOOK, JOURNAL
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [isbn, setIsbn] = useState('');
  const [category, setCategory] = useState('Computer Science');
  const [publisher, setPublisher] = useState('');
  const [year, setYear] = useState(new Date().getFullYear().toString());
  const [edition, setEdition] = useState('1st Edition');
  const [location, setLocation] = useState('Rack A-01');
  const [fileSizeMB, setFileSizeMB] = useState('12.5');
  const [isReferenceOnly, setIsReferenceOnly] = useState(false);
  const [description, setDescription] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    const ok = addResource({
      type,
      title,
      author: type === 'JOURNAL' ? publisher : author,
      isbn,
      category,
      publisher,
      year,
      edition,
      location: type === 'EBOOK' ? 'Cloud DRM Storage' : location,
      fileSizeMB,
      isReferenceOnly,
      description
    });

    if (ok) {
      if (onSuccess) onSuccess();
      if (onClose) onClose();
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Resource Type Selection (Factory Method) */}
      <div>
        <label style={{ display: 'block', fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '8px' }}>
          Resource Type (Factory Method Pattern)
        </label>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
          {[
            { id: 'BOOK', label: 'Physical Book', sub: 'Standard print edition' },
            { id: 'EBOOK', label: 'E-Book (Digital)', sub: 'DRM download license' },
            { id: 'JOURNAL', label: 'Academic Journal', sub: 'Research periodical' }
          ].map((item) => (
            <div
              key={item.id}
              onClick={() => setType(item.id)}
              style={{
                border: type === item.id ? '2px solid var(--primary)' : '1px solid var(--border-color)',
                backgroundColor: type === item.id ? 'var(--bg-accent-light)' : '#FFFFFF',
                borderRadius: '8px',
                padding: '12px',
                cursor: 'pointer',
                textAlign: 'left'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <input
                  type="radio"
                  name="resourceType"
                  checked={type === item.id}
                  onChange={() => setType(item.id)}
                  style={{ accentColor: 'var(--primary)', cursor: 'pointer' }}
                />
                <span style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-main)' }}>
                  {item.label}
                </span>
              </div>
              <div style={{ fontSize: '0.725rem', color: 'var(--text-muted)', marginTop: '4px', paddingLeft: '18px' }}>
                {item.sub}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Form Fields */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
        <div style={{ gridColumn: 'span 2' }}>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            Title *
          </label>
          <input
            type="text"
            required
            placeholder="e.g. Distributed Operating Systems"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="input-base"
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            {type === 'JOURNAL' ? 'Chief Editor / Organization' : 'Author(s) *'}
          </label>
          <input
            type="text"
            required={type !== 'JOURNAL'}
            placeholder="e.g. Andrew S. Tanenbaum"
            value={author}
            onChange={(e) => setAuthor(e.target.value)}
            className="input-base"
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            ISBN / ISSN Code
          </label>
          <input
            type="text"
            placeholder="e.g. 978-0130313584"
            value={isbn}
            onChange={(e) => setIsbn(e.target.value)}
            className="input-base"
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            Academic Category
          </label>
          <select value={category} onChange={(e) => setCategory(e.target.value)} className="input-base">
            <option value="Computer Science">Computer Science</option>
            <option value="Software Engineering">Software Engineering</option>
            <option value="Networking">Networking</option>
            <option value="Artificial Intelligence">Artificial Intelligence</option>
            <option value="Algorithms">Algorithms</option>
            <option value="Research Periodical">Research Periodical</option>
            <option value="Mathematics">Mathematics</option>
          </select>
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            Publisher
          </label>
          <input
            type="text"
            placeholder="e.g. Pearson / MIT Press"
            value={publisher}
            onChange={(e) => setPublisher(e.target.value)}
            className="input-base"
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            Publication Year
          </label>
          <input
            type="number"
            value={year}
            onChange={(e) => setYear(e.target.value)}
            className="input-base"
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            Edition / Volume
          </label>
          <input
            type="text"
            placeholder="e.g. 3rd Edition or Vol 52"
            value={edition}
            onChange={(e) => setEdition(e.target.value)}
            className="input-base"
          />
        </div>

        {type !== 'EBOOK' && (
          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
              Physical Shelf / Rack Location
            </label>
            <input
              type="text"
              placeholder="e.g. Rack CS-04"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              className="input-base"
            />
          </div>
        )}

        {type === 'EBOOK' && (
          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
              Digital File Size (MB)
            </label>
            <input
              type="number"
              step="0.1"
              value={fileSizeMB}
              onChange={(e) => setFileSizeMB(e.target.value)}
              className="input-base"
            />
          </div>
        )}

        {type === 'JOURNAL' && (
          <div style={{ gridColumn: 'span 2', display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px', backgroundColor: '#F8FAFC', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
            <input
              type="checkbox"
              id="refOnly"
              checked={isReferenceOnly}
              onChange={(e) => setIsReferenceOnly(e.target.checked)}
              style={{ accentColor: 'var(--primary)', cursor: 'pointer' }}
            />
            <label htmlFor="refOnly" style={{ fontSize: '0.825rem', fontWeight: 500, color: 'var(--text-main)', cursor: 'pointer' }}>
              Mark as Reference Only (Restricts home loans; reading room use only)
            </label>
          </div>
        )}

        <div style={{ gridColumn: 'span 2' }}>
          <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
            Abstract / Description
          </label>
          <textarea
            rows="2"
            placeholder="Brief overview of contents..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="input-base"
            style={{ resize: 'vertical' }}
          ></textarea>
        </div>
      </div>

      {/* Action Footer */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '16px', borderTop: '1px solid var(--border-color)' }}>
        <button type="button" onClick={onClose} className="btn-secondary">
          Cancel
        </button>
        <button type="submit" className="btn-primary" style={{ padding: '10px 20px' }}>
          <span>Create Resource</span>
        </button>
      </div>
    </form>
  );
};
