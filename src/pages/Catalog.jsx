import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Search, Filter, BookOpen, Plus, Eye, ArrowLeftRight, BookmarkCheck, Sparkles } from 'lucide-react';
import { Badge } from '../components/common/Badge';

export const Catalog = ({ onSelectResource, onOpenAddResource, onNavigateToIssue, onOpenReserveModal }) => {
  const { resources, currentUser } = useLibrary();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedAvailability, setSelectedAvailability] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  // Categories list
  const categories = ['ALL', ...new Set(resources.map((r) => r.category).filter(Boolean))];

  // Filtered resources
  const filtered = resources.filter((item) => {
    const title = item.title || '';
    const author = item.author || item.publisher || '';
    const isbn = item.isbn || '';
    const id = item.id || '';
    const q = searchQuery.toLowerCase();

    const matchesSearch =
      title.toLowerCase().includes(q) ||
      author.toLowerCase().includes(q) ||
      isbn.toLowerCase().includes(q) ||
      id.toLowerCase().includes(q);

    const matchesType = selectedType === 'ALL' || item.type === selectedType || (selectedType === 'BOOK' && item.type === 'PHYSICAL_BOOK');
    
    const isAvail = item.status === 'AVAILABLE' || item.isAvailable === true;
    const matchesAvailability =
      selectedAvailability === 'ALL' ||
      (selectedAvailability === 'AVAILABLE' && isAvail) ||
      (selectedAvailability === 'ISSUED' && !isAvail) ||
      (selectedAvailability === 'REFERENCE' && item.isReferenceOnly);

    const matchesCategory = selectedCategory === 'ALL' || item.category === selectedCategory;

    return matchesSearch && matchesType && matchesAvailability && matchesCategory;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
            Library Catalog
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
            Browse and search library resources.
          </p>
        </div>

        {currentUser.role !== 'MEMBER' && (
          <button onClick={onOpenAddResource} className="btn-primary">
            <Plus size={16} />
            <span>Add Resource</span>
          </button>
        )}
      </div>

      {/* Search & Filter Controls */}
      <div className="card-base" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {/* Search Bar */}
        <div style={{ position: 'relative', width: '100%' }}>
          <Search
            size={16}
            color="var(--text-light)"
            style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }}
          />
          <input
            type="text"
            placeholder="🔍 Search by title, author, publisher, ISBN, or shelf location..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input-base"
            style={{ paddingLeft: '36px' }}
          />
        </div>

        {/* Filter Dropdowns */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '4px' }}>
              Resource Type
            </label>
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="input-base"
              style={{ cursor: 'pointer' }}
            >
              <option value="ALL">All Media Types</option>
              <option value="BOOK">Physical Books</option>
              <option value="EBOOK">E-Books (Digital)</option>
              <option value="JOURNAL">Academic Journals</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '4px' }}>
              Availability Status
            </label>
            <select
              value={selectedAvailability}
              onChange={(e) => setSelectedAvailability(e.target.value)}
              className="input-base"
              style={{ cursor: 'pointer' }}
            >
              <option value="ALL">All Statuses</option>
              <option value="AVAILABLE">Available for Issue</option>
              <option value="ISSUED">Currently Checked Out</option>
              <option value="REFERENCE">Reference Only</option>
            </select>
          </div>

          {categories.length > 1 && (
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '4px' }}>
                Category
              </label>
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="input-base"
                style={{ cursor: 'pointer' }}
              >
                {categories.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>
          )}
        </div>
      </div>

      {/* Catalog Table */}
      <div className="table-container">
        <table className="lib-table">
          <thead>
            <tr>
              <th>Resource</th>
              <th>Format</th>
              <th>Author / Publisher</th>
              <th>Location</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((item) => {
              const isAvail = item.status === 'AVAILABLE' || item.isAvailable === true;
              return (
                <tr key={item.id}>
                  <td>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{item.title}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      ID: {item.id} {item.isbn ? `• ${item.isbn}` : ''}
                    </div>
                  </td>
                  <td>
                    <span
                      style={{
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        backgroundColor: (item.type === 'EBOOK') ? '#EFF6FF' : (item.type === 'JOURNAL' ? '#FAF5FF' : '#F1F5F9'),
                        color: (item.type === 'EBOOK') ? '#2563EB' : (item.type === 'JOURNAL' ? '#9333EA' : '#475569'),
                        padding: '2px 8px',
                        borderRadius: '4px'
                      }}
                    >
                      {item.type || 'BOOK'}
                    </span>
                  </td>
                  <td>
                    <div style={{ fontSize: '0.85rem' }}>{item.author || item.publisher}</div>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {item.location || item.shelfLocation || 'Main Stacks'}
                    </span>
                  </td>
                  <td>
                    <Badge status={isAvail ? 'AVAILABLE' : 'ISSUED'} />
                    {item.isReferenceOnly && (
                      <span style={{ marginLeft: '6px', fontSize: '0.7rem', backgroundColor: '#FEF3C7', color: '#92400E', padding: '1px 5px', borderRadius: '4px', fontWeight: 600 }}>
                        Ref
                      </span>
                    )}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '6px' }}>
                      {isAvail && !item.isReferenceOnly && (
                        <button
                          onClick={() => onNavigateToIssue(item)}
                          className="btn-secondary"
                          style={{ padding: '4px 10px', fontSize: '0.75rem', color: 'var(--primary)', borderColor: 'var(--primary)' }}
                          title="Issue this item"
                        >
                          <ArrowLeftRight size={13} />
                          <span>Issue</span>
                        </button>
                      )}
                      {!isAvail && (
                        <button
                          onClick={() => onOpenReserveModal(item)}
                          className="btn-secondary"
                          style={{ padding: '4px 10px', fontSize: '0.75rem', color: '#9333EA', borderColor: '#E9D5FF', backgroundColor: '#FAF5FF' }}
                          title="Join Waitlist"
                        >
                          <BookmarkCheck size={13} />
                          <span>Reserve</span>
                        </button>
                      )}
                      <button
                        onClick={() => onSelectResource(item)}
                        className="btn-secondary"
                        style={{ padding: '4px 10px', fontSize: '0.775rem' }}
                      >
                        <Eye size={13} />
                        <span>View</span>
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
