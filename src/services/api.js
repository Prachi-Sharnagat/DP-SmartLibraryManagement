const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Helper to handle fetch responses safely
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
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
  // Health & Telemetry
  checkHealth: () => request('/health'),
  getStats: () => request('/stats'),

  // Catalog / Resources (Factory Pattern)
  getResources: () => request('/resources'),
  createResource: (resourceData, callerUserId = 'U999') =>
    request('/resources', {
      method: 'POST',
      headers: { 'X-User-Id': callerUserId },
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
  issueResource: (userId, resourceId, callerUserId = 'U999') =>
    request('/circulation/issue', {
      method: 'POST',
      headers: { 'X-User-Id': callerUserId },
      body: JSON.stringify({ userId, resourceId })
    }),
  returnResource: (resourceId, callerUserId = 'U999') =>
    request('/circulation/return', {
      method: 'POST',
      headers: { 'X-User-Id': callerUserId },
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
  getAuditLogs: () => request('/audit-logs')
};
