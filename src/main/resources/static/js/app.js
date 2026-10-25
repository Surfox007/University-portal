/**
 * University Management System - Frontend Application Logic
 * Implements JWT Auth, RBAC Role Isolation, Dynamic Modals, and Live Traffic Logging
 */

const App = (() => {
  // Application State
  const state = {
    token: localStorage.getItem('up_token') || null,
    user: JSON.parse(localStorage.getItem('up_user') || 'null'),
    activeTab: 'students',
    students: [],
    teachers: [],
    admins: []
  };

  // Base API URL
  const API_BASE = '';

  // DOM Elements cache
  const elements = {};

  function init() {
    cacheDom();
    bindEvents();
    updateAuthUI();

    // Auto-login with student account if no token present to demonstrate live data immediately
    if (!state.token) {
      quickLogin('admin', 'admin123');
    } else {
      loadTabData(state.activeTab);
    }
  }

  function cacheDom() {
    elements.userBadge = document.getElementById('currentUserBadge');
    elements.navLoginBtn = document.getElementById('navLoginBtn');
    elements.navLogoutBtn = document.getElementById('navLogoutBtn');
    elements.jwtUserDisplay = document.getElementById('jwtUserDisplay');
    elements.jwtRoleDisplay = document.getElementById('jwtRoleDisplay');
    elements.jwtTokenString = document.getElementById('jwtTokenString');
    elements.jwtTokenBox = document.getElementById('jwtTokenBox');

    elements.trafficLogs = document.getElementById('trafficLogs');
    elements.toastContainer = document.getElementById('toastContainer');

    // Modals
    elements.loginModal = document.getElementById('loginModal');
    elements.studentModal = document.getElementById('studentModal');
    elements.teacherModal = document.getElementById('teacherModal');
    elements.adminModal = document.getElementById('adminModal');
  }

  function bindEvents() {
    // Close modals on clicking overlay background
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
      overlay.addEventListener('click', (e) => {
        if (e.target === overlay) closeModals();
      });
    });
  }

  // ==========================================
  // HTTP Traffic Logging & API Client
  // ==========================================
  function logTraffic(method, url, status, timeMs) {
    if (!elements.trafficLogs) return;
    const timeStr = new Date().toLocaleTimeString();
    const entry = document.createElement('div');
    entry.className = 'log-entry';

    let codeClass = 'code-200';
    if (status === 401 || status === 500) codeClass = 'code-401';
    else if (status === 403) codeClass = 'code-403';
    else if (status === 201) codeClass = 'code-201';

    entry.innerHTML = `
      <span class="time">[${timeStr}]</span> 
      <strong>${method}</strong> ${url} 
      &rarr; <span class="${codeClass}">${status}</span> 
      <span style="color:var(--text-muted); font-size:0.75rem;">(${timeMs}ms)</span>
    `;
    elements.trafficLogs.prepend(entry);
  }

  async function apiFetch(endpoint, options = {}) {
    const startTime = performance.now();
    const headers = options.headers || {};
    headers['Content-Type'] = 'application/json';

    if (state.token) {
      headers['Authorization'] = `Bearer ${state.token}`;
    }
    options.headers = headers;

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, options);
      const timeMs = Math.round(performance.now() - startTime);
      logTraffic(options.method || 'GET', endpoint, response.status, timeMs);

      if (response.status === 401) {
        showToast('Unauthorized: Please login first', 'error');
        throw new Error('Unauthorized');
      }

      if (response.status === 403) {
        showToast('HTTP 403 Forbidden: Permission Denied for your role', 'warning');
        throw new Error('Forbidden');
      }

      const json = await response.json();
      return json;
    } catch (err) {
      if (err.message !== 'Forbidden' && err.message !== 'Unauthorized') {
        const timeMs = Math.round(performance.now() - startTime);
        logTraffic(options.method || 'GET', endpoint, 'ERR', timeMs);
        showToast(err.message || 'Network request failed', 'error');
      }
      throw err;
    }
  }

  // ==========================================
  // Authentication & State Management
  // ==========================================
  async function login(username, password) {
    try {
      const result = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      });

      if (result.success && result.data) {
        state.token = result.data.token;
        state.user = {
          id: result.data.id,
          username: result.data.username,
          email: result.data.email,
          role: result.data.role
        };

        localStorage.setItem('up_token', state.token);
        localStorage.setItem('up_user', JSON.stringify(state.user));

        updateAuthUI();
        closeModals();
        showToast(`Authenticated as ${state.user.username} (${state.user.role})`, 'success');
        loadTabData(state.activeTab);
      }
    } catch (err) {
      showToast('Login failed: Invalid credentials', 'error');
    }
  }

  function quickLogin(username, password) {
    login(username, password);
  }

  async function logout() {
    try {
      if (state.token) {
        await apiFetch('/api/auth/logout', { method: 'POST' });
      }
    } catch (e) {
      // Continue client cleanup even if network fails
    }

    state.token = null;
    state.user = null;
    localStorage.removeItem('up_token');
    localStorage.removeItem('up_user');

    updateAuthUI();
    showToast('Logged out successfully', 'info');
    loadTabData(state.activeTab);
  }

  function updateAuthUI() {
    const role = state.user ? state.user.role : null;
    const username = state.user ? state.user.username : 'Anonymous';

    if (elements.jwtUserDisplay) elements.jwtUserDisplay.textContent = username;
    if (elements.jwtRoleDisplay) elements.jwtRoleDisplay.textContent = role || 'ROLE_ANONYMOUS';
    if (elements.jwtTokenString) elements.jwtTokenString.textContent = state.token ? `Bearer ${state.token}` : 'No active token';

    if (role) {
      elements.userBadge.textContent = `${username} • ${role.replace('ROLE_', '')}`;
      elements.userBadge.className = 'role-badge ' + (
        role === 'ROLE_ADMIN' ? 'role-admin' :
        role === 'ROLE_TEACHER' ? 'role-teacher' : 'role-student'
      );
      elements.navLoginBtn.style.display = 'none';
      elements.navLogoutBtn.style.display = 'inline-flex';
    } else {
      elements.userBadge.textContent = 'Not Authenticated';
      elements.userBadge.className = 'role-badge role-none';
      elements.navLoginBtn.style.display = 'inline-flex';
      elements.navLogoutBtn.style.display = 'none';
    }

    // Refresh active tab views based on permissions
    enforceTabPermissions();
  }

  function enforceTabPermissions() {
    const role = state.user ? state.user.role : null;

    // Students Tab Guard: ADMIN & STUDENT allowed
    const canAccessStudents = role === 'ROLE_ADMIN' || role === 'ROLE_STUDENT';
    const studentsDenied = document.getElementById('studentsAccessDenied');
    const studentsTable = document.getElementById('studentsTableContainer');
    const addStudentBtn = document.getElementById('addStudentBtn');

    if (studentsDenied && studentsTable) {
      studentsDenied.style.display = canAccessStudents ? 'none' : 'block';
      studentsTable.style.display = canAccessStudents ? 'block' : 'none';
      if (addStudentBtn) addStudentBtn.style.display = canAccessStudents ? 'inline-flex' : 'none';
    }

    // Teachers Tab Guard: ADMIN & TEACHER allowed
    const canAccessTeachers = role === 'ROLE_ADMIN' || role === 'ROLE_TEACHER';
    const teachersDenied = document.getElementById('teachersAccessDenied');
    const teachersTable = document.getElementById('teachersTableContainer');
    const addTeacherBtn = document.getElementById('addTeacherBtn');

    if (teachersDenied && teachersTable) {
      teachersDenied.style.display = canAccessTeachers ? 'none' : 'block';
      teachersTable.style.display = canAccessTeachers ? 'block' : 'none';
      if (addTeacherBtn) addTeacherBtn.style.display = canAccessTeachers ? 'inline-flex' : 'none';
    }

    // Admins Tab Guard: ONLY ADMIN allowed
    const canAccessAdmins = role === 'ROLE_ADMIN';
    const adminsDenied = document.getElementById('adminsAccessDenied');
    const adminsTable = document.getElementById('adminsTableContainer');
    const addAdminBtn = document.getElementById('addAdminBtn');

    if (adminsDenied && adminsTable) {
      adminsDenied.style.display = canAccessAdmins ? 'none' : 'block';
      adminsTable.style.display = canAccessAdmins ? 'block' : 'none';
      if (addAdminBtn) addAdminBtn.style.display = canAccessAdmins ? 'inline-flex' : 'none';
    }
  }

  // ==========================================
  // Tab Switching & Data Loading
  // ==========================================
  function switchTab(tabName) {
    state.activeTab = tabName;
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.tab === tabName);
    });

    document.querySelectorAll('.tab-content').forEach(content => {
      content.style.display = content.id === `tab-${tabName}` ? 'block' : 'none';
    });

    loadTabData(tabName);
  }

  function loadTabData(tabName) {
    enforceTabPermissions();
    if (tabName === 'students') loadStudents();
    else if (tabName === 'teachers') loadTeachers();
    else if (tabName === 'admins') loadAdmins();
  }

  // ==========================================
  // Students Resource CRUD
  // ==========================================
  async function loadStudents() {
    const role = state.user ? state.user.role : null;
    if (role !== 'ROLE_ADMIN' && role !== 'ROLE_STUDENT') return;

    const tbody = document.getElementById('studentsTableBody');
    try {
      const res = await apiFetch('/api/students');
      if (res.success && res.data) {
        state.students = res.data;
        document.getElementById('studentCountBadge').textContent = state.students.length;
        renderStudentsTable(state.students);
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger); padding:24px;">Failed to load students (HTTP 403 Forbidden or Server Error)</td></tr>`;
    }
  }

  function renderStudentsTable(students) {
    const tbody = document.getElementById('studentsTableBody');
    if (!students || students.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted); padding:24px;">No students found in directory</td></tr>`;
      return;
    }

    tbody.innerHTML = students.map(s => `
      <tr>
        <td><strong>#${s.id}</strong></td>
        <td><span class="code-pill">${escapeHtml(s.studentId)}</span></td>
        <td><strong>${escapeHtml(s.fullName)}</strong></td>
        <td>${escapeHtml(s.email)}</td>
        <td>${escapeHtml(s.department)}</td>
        <td>${escapeHtml(s.semester)}</td>
        <td><span style="color:var(--accent-lime); font-weight:700;">${Number(s.cgpa).toFixed(2)}</span></td>
        <td>
          <div class="table-actions">
            <button class="btn btn-secondary btn-sm" onclick="App.editStudent(${s.id})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="App.deleteStudent(${s.id})">Delete</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  function openStudentModal(id = null) {
    document.getElementById('studentForm').reset();
    document.getElementById('studentFormId').value = id || '';
    const userFields = document.getElementById('studentUserFields');

    if (id) {
      document.getElementById('studentModalTitle').textContent = 'Edit Student Details';
      const student = state.students.find(s => s.id === id);
      if (student) {
        document.getElementById('studentFormCode').value = student.studentId;
        document.getElementById('studentFormName').value = student.fullName;
        document.getElementById('studentFormEmail').value = student.email;
        document.getElementById('studentFormDept').value = student.department;
        document.getElementById('studentFormSemester').value = student.semester;
        document.getElementById('studentFormCgpa').value = student.cgpa;
        document.getElementById('studentFormPhone').value = student.phoneNumber || '';
      }
      userFields.style.display = 'none';
    } else {
      document.getElementById('studentModalTitle').textContent = 'Add New Student';
      userFields.style.display = 'grid';
    }

    elements.studentModal.classList.add('active');
  }

  async function handleStudentSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('studentFormId').value;
    const payload = {
      studentId: document.getElementById('studentFormCode').value,
      fullName: document.getElementById('studentFormName').value,
      email: document.getElementById('studentFormEmail').value,
      department: document.getElementById('studentFormDept').value,
      semester: document.getElementById('studentFormSemester').value,
      cgpa: parseFloat(document.getElementById('studentFormCgpa').value),
      phoneNumber: document.getElementById('studentFormPhone').value
    };

    if (!id) {
      payload.username = document.getElementById('studentFormUsername').value;
      payload.password = document.getElementById('studentFormPassword').value || 'student123';
    }

    try {
      if (id) {
        await apiFetch(`/api/students/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showToast('Student resource updated successfully', 'success');
      } else {
        await apiFetch('/api/students', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Student created with linked user credentials', 'success');
      }
      closeModals();
      loadStudents();
    } catch (err) {
      // Error handled by apiFetch
    }
  }

  async function deleteStudent(id) {
    if (!confirm(`Are you sure you want to delete Student #${id}?`)) return;
    try {
      await apiFetch(`/api/students/${id}`, { method: 'DELETE' });
      showToast(`Student #${id} deleted successfully`, 'success');
      loadStudents();
    } catch (err) {}
  }

  // ==========================================
  // Teachers Resource CRUD
  // ==========================================
  async function loadTeachers() {
    const role = state.user ? state.user.role : null;
    if (role !== 'ROLE_ADMIN' && role !== 'ROLE_TEACHER') return;

    const tbody = document.getElementById('teachersTableBody');
    try {
      const res = await apiFetch('/api/teachers');
      if (res.success && res.data) {
        state.teachers = res.data;
        document.getElementById('teacherCountBadge').textContent = state.teachers.length;
        renderTeachersTable(state.teachers);
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger); padding:24px;">Failed to load teachers (HTTP 403 Forbidden or Server Error)</td></tr>`;
    }
  }

  function renderTeachersTable(teachers) {
    const tbody = document.getElementById('teachersTableBody');
    if (!teachers || teachers.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted); padding:24px;">No faculty members found</td></tr>`;
      return;
    }

    tbody.innerHTML = teachers.map(t => `
      <tr>
        <td><strong>#${t.id}</strong></td>
        <td><span class="code-pill">${escapeHtml(t.teacherCode)}</span></td>
        <td><strong>${escapeHtml(t.fullName)}</strong></td>
        <td>${escapeHtml(t.email)}</td>
        <td>${escapeHtml(t.department)}</td>
        <td><span style="color:var(--info); font-weight:600;">${escapeHtml(t.designation)}</span></td>
        <td>${escapeHtml(t.phoneNumber || 'N/A')}</td>
        <td>
          <div class="table-actions">
            <button class="btn btn-secondary btn-sm" onclick="App.editTeacher(${t.id})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="App.deleteTeacher(${t.id})">Delete</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  function openTeacherModal(id = null) {
    document.getElementById('teacherForm').reset();
    document.getElementById('teacherFormId').value = id || '';
    const userFields = document.getElementById('teacherUserFields');

    if (id) {
      document.getElementById('teacherModalTitle').textContent = 'Edit Teacher Details';
      const teacher = state.teachers.find(t => t.id === id);
      if (teacher) {
        document.getElementById('teacherFormCode').value = teacher.teacherCode;
        document.getElementById('teacherFormName').value = teacher.fullName;
        document.getElementById('teacherFormEmail').value = teacher.email;
        document.getElementById('teacherFormDept').value = teacher.department;
        document.getElementById('teacherFormDesignation').value = teacher.designation;
        document.getElementById('teacherFormPhone').value = teacher.phoneNumber || '';
      }
      userFields.style.display = 'none';
    } else {
      document.getElementById('teacherModalTitle').textContent = 'Add New Teacher';
      userFields.style.display = 'grid';
    }

    elements.teacherModal.classList.add('active');
  }

  async function handleTeacherSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('teacherFormId').value;
    const payload = {
      teacherCode: document.getElementById('teacherFormCode').value,
      fullName: document.getElementById('teacherFormName').value,
      email: document.getElementById('teacherFormEmail').value,
      department: document.getElementById('teacherFormDept').value,
      designation: document.getElementById('teacherFormDesignation').value,
      phoneNumber: document.getElementById('teacherFormPhone').value
    };

    if (!id) {
      payload.username = document.getElementById('teacherFormUsername').value;
      payload.password = document.getElementById('teacherFormPassword').value || 'teacher123';
    }

    try {
      if (id) {
        await apiFetch(`/api/teachers/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showToast('Teacher resource updated successfully', 'success');
      } else {
        await apiFetch('/api/teachers', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Teacher created with linked user credentials', 'success');
      }
      closeModals();
      loadTeachers();
    } catch (err) {}
  }

  async function deleteTeacher(id) {
    if (!confirm(`Are you sure you want to delete Teacher #${id}?`)) return;
    try {
      await apiFetch(`/api/teachers/${id}`, { method: 'DELETE' });
      showToast(`Teacher #${id} deleted successfully`, 'success');
      loadTeachers();
    } catch (err) {}
  }

  // ==========================================
  // Admins Resource CRUD
  // ==========================================
  async function loadAdmins() {
    const role = state.user ? state.user.role : null;
    if (role !== 'ROLE_ADMIN') return;

    const tbody = document.getElementById('adminsTableBody');
    try {
      const res = await apiFetch('/api/admins');
      if (res.success && res.data) {
        state.admins = res.data;
        document.getElementById('adminCountBadge').textContent = state.admins.length;
        renderAdminsTable(state.admins);
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger); padding:24px;">Failed to load admins (HTTP 403 Forbidden or Server Error)</td></tr>`;
    }
  }

  function renderAdminsTable(admins) {
    const tbody = document.getElementById('adminsTableBody');
    if (!admins || admins.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted); padding:24px;">No administrators found</td></tr>`;
      return;
    }

    tbody.innerHTML = admins.map(a => `
      <tr>
        <td><strong>#${a.id}</strong></td>
        <td><span class="code-pill">${escapeHtml(a.adminCode)}</span></td>
        <td><strong>${escapeHtml(a.fullName)}</strong></td>
        <td>${escapeHtml(a.email)}</td>
        <td>${escapeHtml(a.department)}</td>
        <td>${escapeHtml(a.phoneNumber || 'N/A')}</td>
        <td><span style="color:var(--accent-lime);">${escapeHtml(a.username || 'admin')}</span></td>
        <td>
          <div class="table-actions">
            <button class="btn btn-secondary btn-sm" onclick="App.editAdmin(${a.id})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="App.deleteAdmin(${a.id})">Delete</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  function openAdminModal(id = null) {
    document.getElementById('adminForm').reset();
    document.getElementById('adminFormId').value = id || '';
    const userFields = document.getElementById('adminUserFields');

    if (id) {
      document.getElementById('adminModalTitle').textContent = 'Edit Admin Details';
      const admin = state.admins.find(a => a.id === id);
      if (admin) {
        document.getElementById('adminFormCode').value = admin.adminCode;
        document.getElementById('adminFormName').value = admin.fullName;
        document.getElementById('adminFormEmail').value = admin.email;
        document.getElementById('adminFormDept').value = admin.department;
        document.getElementById('adminFormPhone').value = admin.phoneNumber || '';
      }
      userFields.style.display = 'none';
    } else {
      document.getElementById('adminModalTitle').textContent = 'Add New Admin';
      userFields.style.display = 'grid';
    }

    elements.adminModal.classList.add('active');
  }

  async function handleAdminSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('adminFormId').value;
    const payload = {
      adminCode: document.getElementById('adminFormCode').value,
      fullName: document.getElementById('adminFormName').value,
      email: document.getElementById('adminFormEmail').value,
      department: document.getElementById('adminFormDept').value,
      phoneNumber: document.getElementById('adminFormPhone').value
    };

    if (!id) {
      payload.username = document.getElementById('adminFormUsername').value;
      payload.password = document.getElementById('adminFormPassword').value || 'admin123';
    }

    try {
      if (id) {
        await apiFetch(`/api/admins/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showToast('Admin resource updated successfully', 'success');
      } else {
        await apiFetch('/api/admins', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Admin created with linked user credentials', 'success');
      }
      closeModals();
      loadAdmins();
    } catch (err) {}
  }

  async function deleteAdmin(id) {
    if (!confirm(`Are you sure you want to delete Admin #${id}?`)) return;
    try {
      await apiFetch(`/api/admins/${id}`, { method: 'DELETE' });
      showToast(`Admin #${id} deleted successfully`, 'success');
      loadAdmins();
    } catch (err) {}
  }

  // ==========================================
  // Testing 403 Forbidden Live Verification
  // ==========================================
  async function testForbidden(endpoint) {
    try {
      await apiFetch(endpoint, { method: 'GET' });
      showToast('Unexpected: Request succeeded (Allowed)', 'info');
    } catch (e) {
      // apiFetch automatically handles and logs 403
    }
  }

  // ==========================================
  // UI Helpers (Modals, Toasts, Escaping)
  // ==========================================
  function openLoginModal() {
    elements.loginModal.classList.add('active');
  }

  function handleLoginSubmit(e) {
    e.preventDefault();
    const u = document.getElementById('loginUsername').value;
    const p = document.getElementById('loginPassword').value;
    login(u, p);
  }

  function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
  }

  function toggleTokenBox() {
    if (!elements.jwtTokenBox) return;
    const isHidden = elements.jwtTokenBox.style.display === 'none';
    elements.jwtTokenBox.style.display = isHidden ? 'block' : 'none';
  }

  function clearLogs() {
    if (elements.trafficLogs) {
      elements.trafficLogs.innerHTML = '<div class="log-entry"><span class="time">[CLEARED]</span> Log cleared.</div>';
    }
  }

  function showToast(message, type = 'info') {
    if (!elements.toastContainer) return;
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    else if (type === 'error') icon = '❌';
    else if (type === 'warning') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(20px)';
      toast.style.transition = 'all 0.25s ease';
      setTimeout(() => toast.remove(), 250);
    }, 3800);
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Expose public API
  return {
    init,
    quickLogin,
    login,
    logout,
    openLoginModal,
    handleLoginSubmit,
    closeModals,
    switchTab,
    toggleTokenBox,
    clearLogs,
    testForbidden,
    // Student
    openStudentModal,
    handleStudentSubmit,
    editStudent: (id) => openStudentModal(id),
    deleteStudent,
    // Teacher
    openTeacherModal,
    handleTeacherSubmit,
    editTeacher: (id) => openTeacherModal(id),
    deleteTeacher,
    // Admin
    openAdminModal,
    handleAdminSubmit,
    editAdmin: (id) => openAdminModal(id),
    deleteAdmin
  };
})();

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  App.init();
});
