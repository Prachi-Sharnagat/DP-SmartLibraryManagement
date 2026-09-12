import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  INITIAL_USERS,
  INITIAL_RESOURCES,
  INITIAL_LOANS,
  INITIAL_RESERVATIONS,
  INITIAL_NOTIFICATIONS,
  INITIAL_AUDIT_LOGS,
  INITIAL_STATS
} from '../data/initialData';
import { api } from '../services/api';

const LibraryContext = createContext();

export const LibraryProvider = ({ children }) => {
  // Current user state (Default: Admin / Prachi)
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = localStorage.getItem('smart_lib_user');
    return saved ? JSON.parse(saved) : INITIAL_USERS[0];
  });

  // Backend Connectivity State
  const [isBackendOnline, setIsBackendOnline] = useState(false);
  const [backendPort, setBackendPort] = useState(8080);
  const [isSyncing, setIsSyncing] = useState(false);

  // Core collections with localStorage persistence
  const [users, setUsers] = useState(() => {
    const saved = localStorage.getItem('smart_lib_users');
    return saved ? JSON.parse(saved) : INITIAL_USERS;
  });

  const [resources, setResources] = useState(() => {
    const saved = localStorage.getItem('smart_lib_resources');
    return saved ? JSON.parse(saved) : INITIAL_RESOURCES;
  });

  const [loans, setLoans] = useState(() => {
    const saved = localStorage.getItem('smart_lib_loans');
    return saved ? JSON.parse(saved) : INITIAL_LOANS;
  });

  const [reservations, setReservations] = useState(() => {
    const saved = localStorage.getItem('smart_lib_reservations');
    return saved ? JSON.parse(saved) : INITIAL_RESERVATIONS;
  });

  const [notifications, setNotifications] = useState(() => {
    const saved = localStorage.getItem('smart_lib_notifications');
    return saved ? JSON.parse(saved) : INITIAL_NOTIFICATIONS;
  });

  const [auditLogs, setAuditLogs] = useState(() => {
    const saved = localStorage.getItem('smart_lib_audit_logs');
    return saved ? JSON.parse(saved) : INITIAL_AUDIT_LOGS;
  });

  const [stats, setStats] = useState(INITIAL_STATS);
  const [toasts, setToasts] = useState([]);

  // Toast Notification Manager
  const showToast = (message, type = 'success') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  };

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  // Helper: Proxy Pattern Audit Logger
  const recordAuditLog = (action, target, latencyMs = 15, status = 'SUCCESS') => {
    const newLog = {
      id: `AUD-${Date.now().toString().slice(-4)}`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      date: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
      user: `${currentUser.name} (${currentUser.role})`,
      action,
      target,
      status,
      latency: `${latencyMs}ms`,
      ipAddress: '192.168.1.' + Math.floor(Math.random() * 200 + 10)
    };
    setAuditLogs((prev) => [newLog, ...prev]);
  };

  // Sync to Backend Function
  const syncWithBackend = useCallback(async () => {
    setIsSyncing(true);
    const health = await api.checkHealth();
    if (health.ok) {
      setIsBackendOnline(true);
      // Fetch fresh stats and resources
      try {
        const [resResp, memResp, statsResp, auditResp, notifResp] = await Promise.all([
          api.getResources(),
          api.getMembers(),
          api.getStats(),
          api.getAuditLogs(),
          api.getNotifications()
        ]);

        if (resResp.ok && Array.isArray(resResp.data)) {
          // Normalize and merge resources
          setResources((prev) => {
            const serverIds = new Set(resResp.data.map(r => r.id));
            const extraLocal = prev.filter(r => !serverIds.has(r.id));
            const serverMapped = resResp.data.map(r => ({
              id: r.id,
              title: r.title,
              author: r.author || r.publisher || 'Unknown',
              type: r.type || 'BOOK',
              status: r.isAvailable ? 'AVAILABLE' : 'ISSUED',
              isbn: r.isbn || 'N/A',
              location: r.shelfLocation || 'Main Library',
              borrowDays: r.maxBorrowDays || 14,
              finePerDay: r.lateFeePerDay || 1.5,
              isReferenceOnly: !!r.isReferenceOnly,
              fileSizeMB: r.fileSizeMB,
              availableCopies: r.isAvailable ? 1 : 0,
              totalCopies: 1
            }));
            return [...serverMapped, ...extraLocal];
          });
        }

        if (memResp.ok && Array.isArray(memResp.data)) {
          setUsers((prev) => {
            const serverIds = new Set(memResp.data.map(u => u.userId));
            const extraLocal = prev.filter(u => !serverIds.has(u.id));
            const serverUsers = memResp.data.map(u => ({
              id: u.userId,
              name: u.name,
              email: u.email,
              phone: u.phone,
              role: u.role || 'MEMBER',
              status: u.membershipActive ? 'ACTIVE' : 'INACTIVE',
              outstandingFine: u.outstandingFine || 0,
              currentLoans: u.currentBorrowedCount || 0,
              maxLoans: u.role === 'ADMIN' ? 20 : 5
            }));
            return [...serverUsers, ...extraLocal];
          });
        }

        if (statsResp.ok && statsResp.data?.data) {
          const s = statsResp.data.data;
          setStats((prev) => ({
            ...prev,
            totalResources: s.totalResources || prev.totalResources,
            availableResources: s.availableResources || prev.availableResources,
            activeMembers: s.totalMembers || prev.activeMembers,
            activeLoans: s.activeLoans || prev.activeLoans,
            totalFinesCollected: s.totalFines || prev.totalFinesCollected
          }));
        }
      } catch (err) {
        console.warn('Backend sync failed:', err);
      }
    } else {
      setIsBackendOnline(false);
    }
    setIsSyncing(false);
  }, []);

  // Poll backend health periodically
  useEffect(() => {
    syncWithBackend();
    const interval = setInterval(syncWithBackend, 15000);
    return () => clearInterval(interval);
  }, [syncWithBackend]);

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem('smart_lib_user', JSON.stringify(currentUser));
  }, [currentUser]);

  useEffect(() => {
    localStorage.setItem('smart_lib_users', JSON.stringify(users));
  }, [users]);

  useEffect(() => {
    localStorage.setItem('smart_lib_resources', JSON.stringify(resources));
  }, [resources]);

  useEffect(() => {
    localStorage.setItem('smart_lib_loans', JSON.stringify(loans));
  }, [loans]);

  useEffect(() => {
    localStorage.setItem('smart_lib_reservations', JSON.stringify(reservations));
  }, [reservations]);

  useEffect(() => {
    localStorage.setItem('smart_lib_notifications', JSON.stringify(notifications));
  }, [notifications]);

  useEffect(() => {
    localStorage.setItem('smart_lib_audit_logs', JSON.stringify(auditLogs));
  }, [auditLogs]);

  // Switch User (Role-Based Testing)
  const switchUser = (userId) => {
    const found = users.find((u) => u.id === userId || u.userId === userId);
    if (found) {
      setCurrentUser(found);
      recordAuditLog('USER_SWITCH', `Switched active session to ${found.name}`);
      showToast(`Switched active profile to ${found.name} (${found.role})`, 'info');
    }
  };

  // ==========================================
  // PATTERN 1: CHAIN OF RESPONSIBILITY
  // Visual & Logical Validation Pipeline
  // ==========================================
  const evaluateBorrowValidation = (member, resource) => {
    if (!member || !resource) {
      return {
        allPassed: false,
        steps: [
          { name: 'Membership verification', passed: false, detail: 'Please select a valid member' },
          { name: 'Account status verification', passed: false, detail: 'Pending selection' },
          { name: 'Outstanding fine check', passed: false, detail: 'Pending selection' },
          { name: 'Borrowing limit quota check', passed: false, detail: 'Pending selection' },
          { name: 'Resource availability check', passed: false, detail: 'Please select a resource' },
          { name: 'Active reservation check', passed: false, detail: 'Pending selection' }
        ]
      };
    }

    const steps = [];

    // Step 1: Membership Check
    const isMemberValid = !!(member.id || member.userId);
    steps.push({
      name: 'Membership verification',
      passed: isMemberValid,
      detail: isMemberValid ? `Member ID ${member.id || member.userId} verified` : 'Invalid member ID'
    });

    // Step 2: Account Status Check
    const isAccountActive = member.status === 'ACTIVE' || member.membershipActive === true;
    steps.push({
      name: 'Account status verification',
      passed: isAccountActive,
      detail: isAccountActive ? 'Account is in good standing (Active)' : 'Account is INACTIVE or expired'
    });

    // Step 3: Outstanding Fine Check
    const fineThresholdPassed = (member.outstandingFine || 0) <= 10.0;
    steps.push({
      name: 'Outstanding fine check',
      passed: fineThresholdPassed,
      detail: fineThresholdPassed
        ? `Fine within permissible limit ($${(member.outstandingFine || 0).toFixed(2)})`
        : `Outstanding fine of $${member.outstandingFine.toFixed(2)} exceeds $10.00 threshold`
    });

    // Step 4: Borrow Limit Check
    const currentLoans = member.currentLoans ?? member.currentBorrowedCount ?? 0;
    const maxAllowed = member.maxLoans || 5;
    const limitPassed = currentLoans < maxAllowed;
    steps.push({
      name: 'Borrowing limit quota check',
      passed: limitPassed,
      detail: limitPassed
        ? `Current active loans: ${currentLoans}/${maxAllowed} (Quota available)`
        : `Borrowing limit reached (${currentLoans}/${maxAllowed} items max)`
    });

    // Step 5: Resource Availability & Non-Reference Check
    const isAvailable = resource.status === 'AVAILABLE' || resource.isAvailable === true;
    const isNotReferenceOnly = !resource.isReferenceOnly;
    const availabilityPassed = isAvailable && isNotReferenceOnly;
    let availDetail = 'Item is available in inventory';
    if (resource.isReferenceOnly) {
      availDetail = 'Item is marked as REFERENCE ONLY (cannot be checked out)';
    } else if (!isAvailable) {
      availDetail = `Item is currently checked out / in use`;
    }
    steps.push({
      name: 'Resource availability check',
      passed: availabilityPassed,
      detail: availDetail
    });

    // Step 6: Reservation Priority Check
    const activeRes = reservations.find(
      (r) => r.resourceId === resource.id && (r.status === 'WAITING' || r.status === 'READY_FOR_PICKUP')
    );
    let reservationPassed = true;
    let resDetail = 'No conflicting reservations for this item';
    if (activeRes) {
      const memberId = member.id || member.userId;
      if (activeRes.memberId === memberId || activeRes.userId === memberId) {
        resDetail = 'Reserved by this member (Priority hold cleared)';
        reservationPassed = true;
      } else {
        resDetail = `Reserved for ${activeRes.memberName || activeRes.userName} (#${activeRes.position || 1} in queue)`;
        reservationPassed = false;
      }
    }
    steps.push({
      name: 'Active reservation check',
      passed: reservationPassed,
      detail: resDetail
    });

    const allPassed = steps.every((s) => s.passed);
    return { allPassed, steps };
  };

  // ==========================================
  // ISSUE BOOK (Circulation)
  // ==========================================
  const issueBook = async (memberId, resourceId) => {
    const member = users.find((u) => u.id === memberId || u.userId === memberId);
    const resource = resources.find((r) => r.id === resourceId);

    const validation = evaluateBorrowValidation(member, resource);
    if (!validation.allPassed) {
      const failedStep = validation.steps.find((s) => !s.passed);
      showToast(`Validation Failed: ${failedStep.detail}`, 'error');
      recordAuditLog('ISSUE_REJECTED', `Failed for ${member?.name || memberId}: ${failedStep.detail}`, 12, 'FAILED');
      return false;
    }

    // Call Java REST API if online
    if (isBackendOnline) {
      const apiRes = await api.issueResource(memberId, resourceId, currentUser.id || currentUser.userId);
      if (!apiRes.ok) {
        const errMsg = apiRes.data?.message || apiRes.data?.error || 'Failed to issue via Java Chain of Responsibility';
        showToast(errMsg, 'error');
        recordAuditLog('ISSUE_ERROR', errMsg, 15, 'FAILED');
        return false;
      }
    }

    // Create loan locally
    const borrowDays = resource.borrowDays || 14;
    const today = new Date();
    const dueDate = new Date();
    dueDate.setDate(today.getDate() + borrowDays);

    const newLoan = {
      id: `LN-${Math.floor(Math.random() * 9000 + 1000)}`,
      memberId: member.id || member.userId,
      memberName: member.name,
      resourceId: resource.id,
      resourceTitle: resource.title,
      resourceType: resource.type,
      issueDate: today.toISOString().split('T')[0],
      dueDate: dueDate.toISOString().split('T')[0],
      returnDate: null,
      status: 'ACTIVE',
      calculatedFine: 0,
      issuedBy: `${currentUser.name} (${currentUser.role})`
    };

    // Update resource status
    const updatedResources = resources.map((r) => {
      if (r.id === resource.id) {
        return {
          ...r,
          isAvailable: false,
          availableCopies: 0,
          status: 'ISSUED'
        };
      }
      return r;
    });

    // Update user loan count
    const updatedUsers = users.map((u) => {
      if ((u.id === member.id) || (u.userId === member.userId)) {
        return { ...u, currentLoans: (u.currentLoans || 0) + 1, currentBorrowedCount: (u.currentBorrowedCount || 0) + 1 };
      }
      return u;
    });

    // If reservation existed for this user, fulfill it
    setReservations((prev) =>
      prev.map((r) => {
        if (r.resourceId === resource.id && (r.memberId === memberId || r.userId === memberId)) {
          return { ...r, status: 'COMPLETED' };
        }
        return r;
      })
    );

    setLoans([newLoan, ...loans]);
    setResources(updatedResources);
    setUsers(updatedUsers);

    recordAuditLog('BOOK_ISSUE', `Issued '${resource.title}' to ${member.name} (Due: ${newLoan.dueDate})`, 34);
    showToast(`✓ '${resource.title}' issued successfully to ${member.name}`, 'success');
    return true;
  };

  // ==========================================
  // PATTERN 2: OBSERVER + BRIDGE PATTERN
  // Return Resource & Broadcast to Waitlist Observers
  // ==========================================
  const returnBook = async (loanId) => {
    const targetLoan = loans.find((l) => l.id === loanId || l.loanId === loanId);
    if (!targetLoan) {
      showToast('Loan record not found.', 'error');
      return false;
    }

    const today = new Date().toISOString().split('T')[0];

    // Call Java Backend if online
    if (isBackendOnline && targetLoan.resourceId) {
      await api.returnResource(targetLoan.resourceId, currentUser.id || currentUser.userId);
    }

    // Mark loan returned
    const updatedLoans = loans.map((l) => {
      if (l.id === loanId || l.loanId === loanId) {
        return { ...l, returnDate: today, status: 'RETURNED', isReturned: true };
      }
      return l;
    });

    // Update resource copies and status
    let freedResource = null;
    const updatedResources = resources.map((r) => {
      if (r.id === targetLoan.resourceId) {
        freedResource = r;
        return {
          ...r,
          isAvailable: true,
          availableCopies: (r.availableCopies || 0) + 1,
          status: 'AVAILABLE'
        };
      }
      return r;
    });

    // Decrement user active loans
    const updatedUsers = users.map((u) => {
      if (u.id === targetLoan.memberId || u.userId === targetLoan.memberId || u.userId === targetLoan.userId) {
        return {
          ...u,
          currentLoans: Math.max(0, (u.currentLoans || 1) - 1),
          currentBorrowedCount: Math.max(0, (u.currentBorrowedCount || 1) - 1)
        };
      }
      return u;
    });

    setLoans(updatedLoans);
    setResources(updatedResources);
    setUsers(updatedUsers);

    recordAuditLog('BOOK_RETURN', `Returned '${targetLoan.resourceTitle || targetLoan.resourceId}' from ${targetLoan.memberName || targetLoan.userId}`, 22);

    // ==========================================================
    // OBSERVER NOTIFICATION: Check if members are waiting
    // ==========================================================
    const waitlist = reservations.filter(
      (r) => r.resourceId === targetLoan.resourceId && (r.status === 'WAITING' || r.status === 'PENDING')
    );

    if (waitlist.length > 0) {
      const topObserver = waitlist[0];
      const readyExpiry = new Date();
      readyExpiry.setDate(readyExpiry.getDate() + 2);

      setReservations((prev) =>
        prev.map((r) => {
          if (r.id === topObserver.id) {
            return {
              ...r,
              status: 'READY_FOR_PICKUP',
              readyUntil: readyExpiry.toLocaleString()
            };
          }
          return r;
        })
      );

      const newNotif = {
        id: `NOTIF-${Date.now()}`,
        userId: topObserver.memberId || topObserver.userId,
        type: 'BOOK_AVAILABLE',
        title: 'Book Available for Pickup (Observer Triggered)',
        message: `'${targetLoan.resourceTitle || freedResource?.title}' has been returned and is reserved for you at Rack ${freedResource?.location || 'CS-01'}. Please collect within 48 hours.`,
        timestamp: 'Just now',
        read: false,
        channels: topObserver.notificationChannels || ['EMAIL', 'SMS', 'IN_APP'],
        category: 'RESERVATION'
      };

      setNotifications((prev) => [newNotif, ...prev]);

      showToast(
        `✓ Book returned. Observer pattern triggered: Notified ${topObserver.memberName || topObserver.userName} via ${newNotif.channels.join(', ')}`,
        'info'
      );
    } else {
      showToast(`✓ '${targetLoan.resourceTitle || freedResource?.title || 'Item'}' returned into inventory`, 'success');
    }

    return true;
  };

  // ==========================================
  // PATTERN 3: OBSERVER RESERVATION SUBSCRIBE
  // ==========================================
  const reserveResource = async (resourceId, memberId, selectedChannels = ['EMAIL', 'IN_APP']) => {
    const resource = resources.find((r) => r.id === resourceId);
    const member = users.find((u) => u.id === memberId || u.userId === memberId);

    if (!resource || !member) {
      showToast('Resource or member not found.', 'error');
      return false;
    }

    if (isBackendOnline) {
      await api.createReservation(resourceId, memberId);
    }

    const currentWaitCount = reservations.filter(
      (r) => r.resourceId === resourceId && (r.status === 'WAITING' || r.status === 'READY_FOR_PICKUP')
    ).length;

    const newRes = {
      id: `RSV-${Math.floor(Math.random() * 900 + 100)}`,
      resourceId: resource.id,
      resourceTitle: resource.title,
      memberId: member.id || member.userId,
      memberName: member.name,
      position: currentWaitCount + 1,
      status: 'WAITING',
      requestDate: new Date().toLocaleDateString('en-GB') + ' ' + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      notificationChannels: selectedChannels,
      readyUntil: null
    };

    setReservations((prev) => [...prev, newRes]);

    const confNotif = {
      id: `NOTIF-${Date.now()}`,
      userId: member.id || member.userId,
      type: 'RESERVATION_CONFIRMED',
      title: 'Reservation Confirmed',
      message: `You are #${newRes.position} in the waitlist queue for '${resource.title}'. You will be notified via ${selectedChannels.join(', ')} when available.`,
      timestamp: 'Just now',
      read: false,
      channels: selectedChannels,
      category: 'RESERVATION'
    };
    setNotifications((prev) => [confNotif, ...prev]);

    recordAuditLog('RESERVATION_CREATE', `Reserved '${resource.title}' for ${member.name} (Position #${newRes.position})`, 18);
    showToast(`✓ Reservation confirmed (#${newRes.position} in queue) for ${member.name}`, 'success');
    return true;
  };

  // Fine Payment Handler
  const payFine = async (userId, amount) => {
    if (isBackendOnline) {
      await api.payFine(userId, amount);
    }

    setUsers((prev) =>
      prev.map((u) => {
        if (u.id === userId || u.userId === userId) {
          const current = u.outstandingFine || 0;
          return { ...u, outstandingFine: Math.max(0, current - Number(amount)) };
        }
        return u;
      })
    );

    recordAuditLog('PAY_FINE', `Cleared fine payment of $${Number(amount).toFixed(2)} for user ${userId}`, 14);
    showToast(`✓ Payment of $${Number(amount).toFixed(2)} processed successfully`, 'success');
    return true;
  };

  // ==========================================
  // PATTERN 4: FACTORY METHOD
  // Create Resources Dynamically by Type
  // ==========================================
  const addResource = async (formData) => {
    if (currentUser.role === 'STUDENT') {
      showToast('Access Denied: Only Librarian or Admin staff can add resources.', 'error');
      recordAuditLog('ADD_RESOURCE_DENIED', `Unauthorized attempt by ${currentUser.name}`, 8, 'UNAUTHORIZED');
      return false;
    }

    const { type, title, author, isbn, publisher, year, edition, location, category, fileSizeMB, isReferenceOnly } = formData;

    const newRes = {
      id: `RES-${Math.floor(Math.random() * 900 + 100)}`,
      title,
      author: author || 'Unknown Author',
      type: type || 'BOOK',
      category: category || 'General',
      isbn: isbn || 'N/A',
      publisher: publisher || 'University Press',
      year: parseInt(year) || new Date().getFullYear(),
      edition: edition || '1st Edition',
      location: location || 'Rack A-01',
      status: 'AVAILABLE',
      isAvailable: true,
      totalCopies: type === 'EBOOK' ? 999 : 3,
      availableCopies: type === 'EBOOK' ? 999 : 3,
      borrowDays: type === 'JOURNAL' ? 7 : (type === 'EBOOK' ? 21 : 14),
      finePerDay: type === 'JOURNAL' ? 2.5 : (type === 'EBOOK' ? 0.0 : 1.5),
      isReferenceOnly: isReferenceOnly || false,
      fileSizeMB: type === 'EBOOK' ? (parseFloat(fileSizeMB) || 10.0) : null,
      description: formData.description || `Catalog entry for ${title} (${type})`
    };

    if (isBackendOnline) {
      await api.createResource({
        id: newRes.id,
        title: newRes.title,
        type: newRes.type,
        author: newRes.author,
        isbn: newRes.isbn,
        shelfLocation: newRes.location,
        fileSizeMB: newRes.fileSizeMB,
        publisher: newRes.publisher
      }, currentUser.id || currentUser.userId);
    }

    setResources((prev) => [newRes, ...prev]);
    recordAuditLog('ADD_RESOURCE', `Created ${type} via Factory: '${title}' (ID: ${newRes.id})`, 28);
    showToast(`✓ Added new ${type.toLowerCase()}: '${title}'`, 'success');
    return true;
  };

  // Add Member
  const addMember = async (userData) => {
    if (currentUser.role !== 'ADMIN' && currentUser.role !== 'LIBRARIAN') {
      showToast('Access Denied: Only staff can register members.', 'error');
      return false;
    }

    const newId = `U${Math.floor(Math.random() * 9000 + 1000)}`;
    const newUser = {
      id: newId,
      userId: newId,
      name: userData.name,
      email: userData.email,
      phone: userData.phone || '+91 99999 00000',
      role: userData.role || 'STUDENT',
      status: 'ACTIVE',
      membershipActive: true,
      currentLoans: 0,
      currentBorrowedCount: 0,
      maxLoans: userData.role === 'LIBRARIAN' ? 10 : (userData.role === 'ADMIN' ? 20 : 5),
      outstandingFine: 0,
      department: userData.department || 'General',
      semester: userData.semester || '1st Semester',
      joinedDate: new Date().toISOString().split('T')[0]
    };

    if (isBackendOnline) {
      await api.createMember({
        userId: newId,
        name: newUser.name,
        email: newUser.email,
        phone: newUser.phone,
        role: newUser.role,
        membershipActive: true
      });
    }

    setUsers((prev) => [newUser, ...prev]);
    recordAuditLog('MEMBER_CREATE', `Registered member ${newUser.name} (${newUser.id})`, 20);
    showToast(`✓ Registered member: ${newUser.name} (${newId})`, 'success');
    return true;
  };

  // Mark Notification Read
  const markNotificationRead = (notifId) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === notifId ? { ...n, read: true } : n))
    );
  };

  const markAllNotificationsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    showToast('All notifications marked as read', 'info');
  };

  return (
    <LibraryContext.Provider
      value={{
        currentUser,
        setCurrentUser,
        switchUser,
        users,
        resources,
        loans,
        reservations,
        notifications,
        auditLogs,
        stats,
        toasts,
        showToast,
        removeToast,
        evaluateBorrowValidation,
        issueBook,
        returnBook,
        reserveResource,
        payFine,
        addResource,
        addMember,
        markNotificationRead,
        markAllNotificationsRead,
        recordAuditLog,
        isBackendOnline,
        backendPort,
        isSyncing,
        syncWithBackend
      }}
    >
      {children}
    </LibraryContext.Provider>
  );
};

export const useLibrary = () => {
  const context = useContext(LibraryContext);
  if (!context) {
    throw new Error('useLibrary must be used within a LibraryProvider');
  }
  return context;
};
