const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Helper to handle fetch responses safely
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const token = localStorage.getItem('token');
  const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  
  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...(options.headers || {})
    }
  };

  try {
    const res = await fetch(url, config);
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  } catch (err) {
    console.warn(`[API Client] Network error fetching ${endpoint}:`, err.message);
    return { ok: false, status: 0, error: err.message, networkError: true };
  }
}

export const api = {
  // Auth
  login: (emailOrId, password) => request('/login', { method: 'POST', body: JSON.stringify({ emailOrId, password }) }),
  logout: () => request('/logout', { method: 'POST' }),
  getMe: () => request('/users/me'),

  // Health & Telemetry
  checkHealth: () => request('/health'),
  getStats: () => request('/stats'),

  // Catalog / Resources (Factory Pattern)
  getResources: () => request('/resources'),
  createResource: (resourceData) =>
    request('/resources', {
      method: 'POST',
      body: JSON.stringify(resourceData)
    }),
  deleteResource: (resourceId) =>
    request(`/resources?id=${encodeURIComponent(resourceId)}`, { method: 'DELETE' }),

  // Members
  getMembers: () => request('/members'),
  createMember: (memberData) =>
    request('/members', {
      method: 'POST',
      body: JSON.stringify(memberData)
    }),
  payFine: (userId, amount) =>
    request('/members/pay-fine', {
      method: 'POST',
      body: JSON.stringify({ userId, amount: Number(amount) })
    }),

  // Circulation (Chain of Responsibility & Observer Patterns)
  issueResource: (userId, resourceId) =>
    request('/circulation/issue', {
      method: 'POST',
      body: JSON.stringify({ userId, resourceId })
    }),
  returnResource: (resourceId) =>
    request('/circulation/return', {
      method: 'POST',
      body: JSON.stringify({ resourceId })
    }),
  getLoans: () => request('/loans'),

  // Reservations (Observer & Bridge Patterns)
  getReservations: () => request('/reservations'),
  createReservation: (resourceId, userId) =>
    request('/reservations', {
      method: 'POST',
      body: JSON.stringify({ resourceId, userId })
    }),

  // Notifications (Bridge Pattern)
  getNotifications: () => request('/notifications'),
  sendNotification: (channel, title, message, userId = 'U101') =>
    request('/notifications/send', {
      method: 'POST',
      body: JSON.stringify({ channel, title, message, userId })
    }),

  // Audit Logs (Proxy Pattern & Latency Telemetry)
  getAuditLogs: () => request('/audit-logs'),

  // Prototype Pattern: Clone Book Copy
  copyResource: (sourceId, newCopyId, newShelf) =>
    request('/resources/copy', {
      method: 'POST',
      body: JSON.stringify({
        sourceId,
        sourceResourceId: sourceId,
        newCopyId,
        newShelf,
        newShelfLocation: newShelf
      })
    }),

  // Memento Pattern: Metadata Updates & Compensating Undo
  updateResource: (updateData) =>
    request('/resources/update', {
      method: 'POST',
      body: JSON.stringify({
        ...updateData,
        resourceId: updateData.resourceId || updateData.id
      })
    }),
  undoMemento: () =>
    request('/memento/undo', {
      method: 'POST'
    }),
  getMementoHistory: () => request('/memento/history'),

  // Strategy Pattern: Dynamic Fine Policies & Overdue Calculation
  getFinePolicies: () => request('/policies/fine-strategy'),
  previewFine: (role, daysOverdue) =>
    request('/policies/preview-fine', {
      method: 'POST',
      body: JSON.stringify({
        role,
        daysOverdue: Number(daysOverdue),
        overdueDays: Number(daysOverdue)
      })
    }),

  // Patterns Architectural Registry
  getPatterns: () => request('/patterns')
};

