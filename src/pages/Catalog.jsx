import React, { useState } from 'react';
import { useLibrary } from '../context/LibraryContext';
import { Search, Filter, BookOpen, Plus, Eye, ArrowLeftRight, BookmarkCheck, Copy, Edit3, Undo2, CheckCircle2 } from 'lucide-react';
import { Badge } from '../components/common/Badge';
import { api } from '../services/api';

export const Catalog = ({ onSelectResource, onOpenAddResource, onNavigateToIssue, onOpenReserveModal }) => {
  const { resources, currentUser, showToast, refreshData } = useLibrary();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedAvailability, setSelectedAvailability] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  // Prototype Pattern: Clone Copy State
  const [cloneModalItem, setCloneModalItem] = useState(null);
  const [newCopyId, setNewCopyId] = useState('');
  const [newShelfLocation, setNewShelfLocation] = useState('');
  const [isCloning, setIsCloning] = useState(false);

  // Memento Pattern: Edit Metadata State
  const [editModalItem, setEditModalItem] = useState(null);
  const [editTitle, setEditTitle] = useState('');
  const [editAuthor, setEditAuthor] = useState('');
  const [editShelf, setEditShelf] = useState('');
  const [editCategory, setEditCategory] = useState('');
  const [editPublisher, setEditPublisher] = useState('');
  const [editEdition, setEditEdition] = useState('');
  const [editYear, setEditYear] = useState(2024);
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [canUndo, setCanUndo] = useState(true);

  // Open Clone Modal (Prototype)
  const handleOpenClone = (item) => {
    setCloneModalItem(item);
    const suffix = Math.floor(100 + Math.random() * 900);
    setNewCopyId(`${item.id}-COPY${suffix}`);
    setNewShelfLocation(item.shelfLocation || item.location || 'Shelf-Copy-A');
  };

  // Submit Clone (Prototype)
  const handleConfirmClone = async (e) => {
    e.preventDefault();
    if (!newCopyId.trim()) return;
    setIsCloning(true);
    try {
      const res = await api.copyResource(cloneModalItem.id, newCopyId.trim(), newShelfLocation.trim());
      if (res.ok && res.data?.success) {
        showToast(`✓ Prototype cloned copy created: ${newCopyId}`, 'success');
        setCloneModalItem(null);
        if (refreshData) refreshData();
      } else {
        showToast(res.data?.message || 'Failed to clone copy', 'error');
      }
    } catch (err) {
      showToast('Error cloning book copy: ' + err.message, 'error');
    } finally {
      setIsCloning(false);
    }
  };

  // Open Edit Metadata Modal (Memento)
  const handleOpenEdit = (item) => {
    setEditModalItem(item);
    setEditTitle(item.title || '');
    setEditAuthor(item.author || item.publisher || '');
    setEditShelf(item.shelfLocation || item.location || '');
    setEditCategory(item.category || '');
    setEditPublisher(item.publisher || '');
    setEditEdition(item.edition || '1st Edition');
    setEditYear(item.publicationYear || item.year || 2024);
  };

  // Submit Edit Metadata (Memento saves snapshot before mutation)
  const handleConfirmEdit = async (e) => {
    e.preventDefault();
    if (!editTitle.trim()) return;
    setIsSavingEdit(true);
    try {
      const res = await api.updateResource({
        id: editModalItem.id,
        title: editTitle.trim(),
        author: editAuthor.trim(),
        shelfLocation: editShelf.trim(),
        category: editCategory.trim(),
        publisher: editPublisher.trim(),
        edition: editEdition.trim(),
        publicationYear: Number(editYear)
      });
      if (res.ok && res.data?.success) {
        showToast(`✓ Metadata updated. Pre-edit snapshot stored in Memento history!`, 'success');
        setEditModalItem(null);
        setCanUndo(true);
        if (refreshData) refreshData();
      } else {
        showToast(res.data?.message || 'Failed to update metadata', 'error');
      }
    } catch (err) {
      showToast('Error updating metadata: ' + err.message, 'error');
    } finally {
      setIsSavingEdit(false);
    }
  };

  // Undo Last Librarian Edit (Memento compensating restore)
  const handleUndoLastEdit = async () => {
    try {
      const res = await api.undoMemento();
      if (res.ok && res.data?.success) {
        showToast(`✓ Memento Undo: ${res.data.message}`, 'success');
        if (refreshData) refreshData();
      } else {
        showToast(res.data?.message || 'No recent edits to undo', 'info');
      }
    } catch (err) {
      showToast('Undo failed: ' + err.message, 'error');
    }
  };

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
            Browse and manage library resources powered by Builder, Prototype & Memento patterns.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          {currentUser.role !== 'MEMBER' && (
            <>
              <button
                onClick={handleUndoLastEdit}
                className="btn-secondary"
                style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#D97706', borderColor: '#FCD34D', backgroundColor: '#FFFBEB' }}
                title="Undo most recent metadata edit (Memento Pattern)"
              >
                <Undo2 size={16} />
                <span>Undo Last Edit</span>
              </button>
              <button onClick={onOpenAddResource} className="btn-primary">
                <Plus size={16} />
                <span>Add Resource</span>
              </button>
            </>
          )}
        </div>
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
                      {currentUser.role !== 'MEMBER' && (item.type === 'BOOK' || item.type === 'PHYSICAL_BOOK') && (
                        <button
                          onClick={() => handleOpenClone(item)}
                          className="btn-secondary"
                          style={{ padding: '4px 8px', fontSize: '0.75rem', color: '#059669', borderColor: '#A7F3D0', backgroundColor: '#ECFDF5' }}
                          title="Clone copy via Prototype Design Pattern"
                        >
                          <Copy size={13} />
                          <span>Clone</span>
                        </button>
                      )}
                      {currentUser.role !== 'MEMBER' && (
                        <button
                          onClick={() => handleOpenEdit(item)}
                          className="btn-secondary"
                          style={{ padding: '4px 8px', fontSize: '0.75rem', color: '#D97706', borderColor: '#FCD34D', backgroundColor: '#FFFBEB' }}
                          title="Edit metadata with Memento snapshot tracking"
                        >
                          <Edit3 size={13} />
                          <span>Edit</span>
                        </button>
                      )}
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

      {/* Prototype Pattern: Clone Physical Copy Modal */}
      {cloneModalItem && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}>
          <div className="card-base" style={{ maxWidth: '480px', width: '100%', padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{ backgroundColor: '#ECFDF5', color: '#059669', padding: '8px', borderRadius: '8px' }}>
                <Copy size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Clone Physical Copy (Prototype Pattern)</h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Clones catalog metadata without duplicating loans or primary keys.
                </p>
              </div>
            </div>

            <div style={{ backgroundColor: '#F8FAFC', padding: '12px', borderRadius: '6px', fontSize: '0.85rem' }}>
              <strong>Source Template:</strong> {cloneModalItem.title} (ID: {cloneModalItem.id})
            </div>

            <form onSubmit={handleConfirmClone} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>
                  New Copy ID *
                </label>
                <input
                  type="text"
                  required
                  value={newCopyId}
                  onChange={(e) => setNewCopyId(e.target.value)}
                  className="input-base"
                  placeholder="e.g. B001-COPY2"
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>
                  New Shelf Location *
                </label>
                <input
                  type="text"
                  required
                  value={newShelfLocation}
                  onChange={(e) => setNewShelfLocation(e.target.value)}
                  className="input-base"
                  placeholder="e.g. Shelf-A2-Reserve"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => setCloneModalItem(null)}
                  className="btn-secondary"
                  disabled={isCloning}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={isCloning}
                  style={{ backgroundColor: '#059669', borderColor: '#059669' }}
                >
                  {isCloning ? 'Cloning Copy...' : 'Clone via Prototype'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Memento Pattern: Edit Metadata Modal */}
      {editModalItem && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}>
          <div className="card-base" style={{ maxWidth: '520px', width: '100%', padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{ backgroundColor: '#FFFBEB', color: '#D97706', padding: '8px', borderRadius: '8px' }}>
                <Edit3 size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Edit Book Metadata (Memento Pattern)</h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  A snapshot is captured before saving so you can undo changes safely.
                </p>
              </div>
            </div>

            <form onSubmit={handleConfirmEdit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Title *</label>
                <input
                  type="text"
                  required
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  className="input-base"
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Author</label>
                  <input
                    type="text"
                    value={editAuthor}
                    onChange={(e) => setEditAuthor(e.target.value)}
                    className="input-base"
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Shelf Location</label>
                  <input
                    type="text"
                    value={editShelf}
                    onChange={(e) => setEditShelf(e.target.value)}
                    className="input-base"
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Category</label>
                  <input
                    type="text"
                    value={editCategory}
                    onChange={(e) => setEditCategory(e.target.value)}
                    className="input-base"
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '4px' }}>Publisher</label>
                  <input
                    type="text"
                    value={editPublisher}
                    onChange={(e) => setEditPublisher(e.target.value)}
                    className="input-base"
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => setEditModalItem(null)}
                  className="btn-secondary"
                  disabled={isSavingEdit}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={isSavingEdit}
                >
                  {isSavingEdit ? 'Saving...' : 'Save & Capture Snapshot'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

