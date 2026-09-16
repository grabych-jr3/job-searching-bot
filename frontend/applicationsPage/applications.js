const API_BASE_URL = 'http://localhost:8081';

// State
let currentPage = 0;
let pageSize = 20;
let currentSort = 'appliedAt,desc';
let totalPages = 1;
let totalElements = 0;
let allApplications = [];
let activeStatusFilter = 'all';
let searchQuery = '';

// DOM Elements
const resultsContainer = document.getElementById('resultsContainer');
const totalCountEl = document.getElementById('totalCount');
const searchInput = document.getElementById('searchInput');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const filterButtons = document.querySelectorAll('.filter-btn');
const sortSelect = document.getElementById('sortSelect');
const refreshBtn = document.getElementById('refreshBtn');
const paginationNav = document.getElementById('paginationNav');
const pageInfoText = document.getElementById('pageInfoText');
const pageSizeSelect = document.getElementById('pageSizeSelect');
const firstPageBtn = document.getElementById('firstPageBtn');
const prevPageBtn = document.getElementById('prevPageBtn');
const nextPageBtn = document.getElementById('nextPageBtn');
const lastPageBtn = document.getElementById('lastPageBtn');
const pageNumberButtons = document.getElementById('pageNumberButtons');

// Add Application Modal DOM Elements
const addAppModal = document.getElementById('addAppModal');
const openAddModalBtn = document.getElementById('openAddModalBtn');
const closeAddModalBtn = document.getElementById('closeAddModalBtn');
const cancelAddModalBtn = document.getElementById('cancelAddModalBtn');
const addAppForm = document.getElementById('addAppForm');
const modalJobTitle = document.getElementById('modalJobTitle');
const modalCompanyName = document.getElementById('modalCompanyName');
const modalOfferUrl = document.getElementById('modalOfferUrl');
const modalNotes = document.getElementById('modalNotes');
const modalErrorAlert = document.getElementById('modalErrorAlert');
const submitAddModalBtn = document.getElementById('submitAddModalBtn');
const jobTitleError = document.getElementById('jobTitleError');
const companyNameError = document.getElementById('companyNameError');
const offerUrlError = document.getElementById('offerUrlError');

// Toast Notification
let toastTimeout = null;
function showToast(message, type = 'success') {
    const toastEl = document.getElementById('appToast');
    if (!toastEl) return;

    toastEl.textContent = message;
    toastEl.className = `app-toast show ${type}`;

    if (toastTimeout) {
        clearTimeout(toastTimeout);
    }

    toastTimeout = setTimeout(() => {
        toastEl.classList.remove('show');
    }, 3500);
}

function formatDate(dateString) {
    if (!dateString) return 'Date unknown';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return dateString;
        return date.toLocaleDateString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch {
        return dateString;
    }
}

function getStatusLabel(status) {
    switch (status) {
        case 'APPLIED': return 'Applied';
        case 'SCREENING': return 'Screening';
        case 'INTERVIEW': return 'Interview';
        case 'OFFER': return 'Offer 🎉';
        case 'REJECTED': return 'Rejected';
        case 'NO_RESPONSE': return 'No Response';
        default: return status;
    }
}

// Fetch applications from backend
async function loadApplications(page = 0) {
    currentPage = page;
    resultsContainer.innerHTML = '<div class="empty-state">Loading applications...</div>';

    try {
        const url = new URL(`${API_BASE_URL}/api/applications`);
        url.searchParams.set('page', String(currentPage));
        url.searchParams.set('size', String(pageSize));
        if (currentSort) {
            url.searchParams.set('sort', currentSort);
        }

        if (activeStatusFilter && activeStatusFilter !== 'all') {
            url.searchParams.set('status', activeStatusFilter);
        }

        const response = await fetch(url.toString(), {
            method: 'GET'
        });

        if (!response.ok) {
            throw new Error(`Failed to load applications (status ${response.status})`);
        }

        const data = await response.json();

        // Handle Spring Data VIA_DTO (data.page), standard PageImpl (data), and array fallbacks
        const pageObj = data.page || {};
        allApplications = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : []);

        totalElements = Number(
            pageObj.totalElements ??
            pageObj.total_elements ??
            data.totalElements ??
            data.total_elements ??
            data.total ??
            allApplications.length
        ) || 0;

        totalPages = Number(
            pageObj.totalPages ??
            pageObj.total_pages ??
            data.totalPages ??
            data.total_pages ??
            (totalElements > 0 ? Math.ceil(totalElements / pageSize) : 1)
        ) || 1;

        currentPage = Number(
            pageObj.number ??
            data.number ??
            page
        ) || 0;

        if (totalCountEl) {
            totalCountEl.textContent = totalElements.toLocaleString();
        }

        renderApplications();
        updatePagination();
        updateStageCounters();
    } catch (error) {
        console.error('Error fetching applications:', error);
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <p>Failed to load applications: ${error.message || 'Unknown error'}</p>
                <button type="button" class="empty-action-btn" onclick="loadApplications(${currentPage})">
                    Try Again
                </button>
            </div>
        `;
        if (paginationNav) paginationNav.style.display = 'none';
    }
}

// Fetch counts for pipeline summary strip
async function updateStageCounters() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/applications/stats`);
        if (!res.ok) return;

        const stats = await res.json();

        const idMap = {
            APPLIED: 'countApplied',
            SCREENING: 'countScreening',
            INTERVIEW: 'countInterview',
            OFFER: 'countOffer',
            REJECTED: 'countRejected',
            NO_RESPONSE: 'countNoResponse'
        };

        Object.entries(idMap).forEach(([statusKey, elementId]) => {
            const el = document.getElementById(elementId);
            if (el) {
                el.textContent = String(stats[statusKey] ?? 0);
            }
        });
    } catch (err) {
        // Non-critical background count metric failure
    }
}

// Render applications cards
function renderApplications() {
    resultsContainer.innerHTML = '';

    // Apply client-side search query
    let visibleApps = [...allApplications];
    if (searchQuery && searchQuery.trim().length > 0) {
        const q = searchQuery.trim().toLowerCase();
        visibleApps = visibleApps.filter(app => {
            const title = (app.jobTitle || '').toLowerCase();
            const company = (app.companyName || '').toLowerCase();
            const notes = (app.notes || '').toLowerCase();
            return title.includes(q) || company.includes(q) || notes.includes(q);
        });
    }

    if (visibleApps.length === 0) {
        const isFiltered = activeStatusFilter !== 'all' || (searchQuery && searchQuery.trim().length > 0);
        if (isFiltered) {
            resultsContainer.innerHTML = `
                <div class="empty-state">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                    <p>No job applications match the selected status filter or search keywords.</p>
                </div>
            `;
        } else {
            resultsContainer.innerHTML = `
                <div class="empty-state">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                    </svg>
                    <p>No job applications tracked yet. Start analyzing vacancies, apply to matched roles, or add an external application manually.</p>
                    <div style="display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; margin-top: 6px;">
                        <button type="button" class="empty-action-btn" onclick="openAddModal()">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                            </svg>
                            Add External Application
                        </button>
                        <a href="../historyPage/history.html" class="empty-action-btn" style="background: transparent; border: 1px solid var(--border); color: var(--text);">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <polyline points="12 6 12 12 16 14"></polyline>
                            </svg>
                            Browse Analysis History
                        </a>
                    </div>
                </div>
            `;
        }
        if (paginationNav) paginationNav.style.display = 'none';
        return;
    }

    visibleApps.forEach(app => {
        const card = document.createElement('article');
        const status = (app.status || 'APPLIED').toUpperCase();
        const statusLower = status.toLowerCase();
        const safeTitle = app.jobTitle || 'Untitled position';
        const safeCompany = app.companyName || '';
        const safeUrl = app.offerUrl || '#';
        const dateFormatted = formatDate(app.appliedAt);
        const appId = app.id;

        card.className = `app-card stage-${statusLower}`;

        // 1. Header with Title & Stage Badge
        const header = document.createElement('div');
        header.className = 'app-header';

        const titleGroup = document.createElement('div');
        titleGroup.className = 'app-title-group';

        const title = document.createElement('h2');
        title.className = 'app-job-title';
        title.textContent = safeTitle;
        titleGroup.appendChild(title);

        if (safeCompany) {
            const company = document.createElement('div');
            company.className = 'app-company-name';
            company.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                </svg>
                <span>${safeCompany}</span>
            `;
            titleGroup.appendChild(company);
        }

        const dateMeta = document.createElement('div');
        dateMeta.className = 'app-date-meta';
        dateMeta.innerHTML = `
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>Applied ${dateFormatted}</span>
        `;
        titleGroup.appendChild(dateMeta);

        const stageBadge = document.createElement('span');
        stageBadge.className = `stage-badge badge-${statusLower}`;
        stageBadge.textContent = getStatusLabel(status);

        header.appendChild(titleGroup);
        header.appendChild(stageBadge);

        // 2. Interactive Notes Section
        const notesSection = document.createElement('div');
        notesSection.className = 'app-notes-section';

        const notesHeader = document.createElement('div');
        notesHeader.className = 'notes-header';
        notesHeader.innerHTML = `
            <span>Notes & Activity</span>
        `;

        const editNotesBtn = document.createElement('button');
        editNotesBtn.type = 'button';
        editNotesBtn.className = 'edit-notes-btn';
        editNotesBtn.innerHTML = `
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
            </svg>
            <span>${app.notes ? 'Edit' : 'Add Note'}</span>
        `;
        notesHeader.appendChild(editNotesBtn);

        const notesView = document.createElement('div');
        notesView.className = 'notes-view';
        if (app.notes && app.notes.trim().length > 0) {
            notesView.innerHTML = `<p class="notes-content-text">${app.notes}</p>`;
        } else {
            notesView.innerHTML = `<p class="notes-content-empty">No notes recorded yet. Click to add interview details, salary target, or recruiter info.</p>`;
        }

        const notesEditor = document.createElement('div');
        notesEditor.className = 'notes-editor';
        notesEditor.innerHTML = `
            <textarea class="notes-textarea" placeholder="e.g., HR phone screening scheduled on Friday, discussed 140k PLN salary target...">${app.notes || ''}</textarea>
            <div class="notes-editor-actions">
                <button type="button" class="btn-cancel-note">Cancel</button>
                <button type="button" class="btn-save-note">Save Note</button>
            </div>
        `;

        const textarea = notesEditor.querySelector('.notes-textarea');
        const saveBtn = notesEditor.querySelector('.btn-save-note');
        const cancelBtn = notesEditor.querySelector('.btn-cancel-note');

        editNotesBtn.addEventListener('click', () => {
            notesEditor.classList.add('active');
            notesView.style.display = 'none';
            editNotesBtn.style.display = 'none';
            textarea.focus();
        });

        cancelBtn.addEventListener('click', () => {
            notesEditor.classList.remove('active');
            notesView.style.display = 'block';
            editNotesBtn.style.display = 'inline-flex';
            textarea.value = app.notes || '';
        });

        saveBtn.addEventListener('click', async () => {
            const newNotes = textarea.value.trim();
            await saveApplicationNotes(appId, newNotes, card, app, notesView, editNotesBtn, notesEditor);
        });

        notesSection.appendChild(notesHeader);
        notesSection.appendChild(notesView);
        notesSection.appendChild(notesEditor);

        // 3. Actions Row: Open Vacancy & Quick Stage Selector
        const actionsRow = document.createElement('div');
        actionsRow.className = 'app-actions-row';

        const openBtn = document.createElement('a');
        openBtn.className = 'app-link-btn';
        openBtn.href = safeUrl;
        openBtn.target = '_blank';
        openBtn.rel = 'noopener noreferrer';
        openBtn.innerHTML = `
            <span>Open Vacancy</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
        `;

        const statusSelectWrap = document.createElement('div');
        statusSelectWrap.className = 'app-status-select-wrap';

        const statusSelect = document.createElement('select');
        statusSelect.className = `app-status-select sel-${statusLower}`;
        statusSelect.dataset.previousStatus = status;
        statusSelect.setAttribute('aria-label', `Update status for ${safeTitle}`);
        statusSelect.innerHTML = `
            <option value="APPLIED" ${status === 'APPLIED' ? 'selected' : ''}>Applied</option>
            <option value="SCREENING" ${status === 'SCREENING' ? 'selected' : ''}>Screening</option>
            <option value="INTERVIEW" ${status === 'INTERVIEW' ? 'selected' : ''}>Interview</option>
            <option value="OFFER" ${status === 'OFFER' ? 'selected' : ''}>Offer 🎉</option>
            <option value="REJECTED" ${status === 'REJECTED' ? 'selected' : ''}>Rejected</option>
            <option value="NO_RESPONSE" ${status === 'NO_RESPONSE' ? 'selected' : ''}>No Response</option>
        `;

        statusSelect.addEventListener('change', async (e) => {
            const newStatus = e.target.value;
            await changeApplicationStatus(appId, newStatus, statusSelect, card, stageBadge, app);
        });

        statusSelectWrap.appendChild(statusSelect);

        actionsRow.appendChild(openBtn);
        actionsRow.appendChild(statusSelectWrap);

        card.appendChild(header);
        card.appendChild(notesSection);
        card.appendChild(actionsRow);

        resultsContainer.appendChild(card);
    });
}

// Update application status
async function changeApplicationStatus(id, newStatus, selectEl, cardEl, badgeEl, appObj) {
    if (!id) {
        showToast('Application ID is missing.', 'error');
        return;
    }

    const previousStatus = selectEl.dataset.previousStatus || 'APPLIED';
    selectEl.disabled = true;
    selectEl.classList.add('is-updating');

    try {
        const url = new URL(`${API_BASE_URL}/api/applications/${id}/change-status`);
        url.searchParams.set('status', newStatus);

        const response = await fetch(url.toString(), {
            method: 'PATCH'
        });

        if (!response.ok) {
            let errorMsg = `HTTP Error ${response.status}`;
            try {
                const text = await response.text();
                if (text) {
                    try {
                        const parsed = JSON.parse(text);
                        errorMsg = parsed.message || parsed.error || errorMsg;
                    } catch {
                        errorMsg = text.length < 120 ? text : errorMsg;
                    }
                }
            } catch {}
            throw new Error(errorMsg);
        }

        // Update local object & DOM
        appObj.status = newStatus;
        selectEl.dataset.previousStatus = newStatus;
        const newLower = newStatus.toLowerCase();

        selectEl.className = `app-status-select sel-${newLower}`;
        badgeEl.className = `stage-badge badge-${newLower}`;
        badgeEl.textContent = getStatusLabel(newStatus);
        cardEl.className = `app-card stage-${newLower}`;

        showToast(`Application moved to "${getStatusLabel(newStatus)}"`, 'success');
        updateStageCounters();
    } catch (error) {
        console.error('Failed to change application status:', error);
        selectEl.value = previousStatus;
        showToast(`Failed to update status: ${error.message || 'Please try again.'}`, 'error');
    } finally {
        selectEl.disabled = false;
        selectEl.classList.remove('is-updating');
    }
}

// Save application notes
async function saveApplicationNotes(id, newNotes, cardEl, appObj, notesView, editBtn, notesEditor) {
    if (!id) {
        showToast('Application ID is missing.', 'error');
        return;
    }

    const saveBtn = notesEditor.querySelector('.btn-save-note');
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    try {
        const response = await fetch(`${API_BASE_URL}/api/applications/${id}/addNotes`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ notes: newNotes })
        });

        if (!response.ok) {
            let errorMsg = `HTTP Error ${response.status}`;
            try {
                const text = await response.text();
                if (text) {
                    try {
                        const parsed = JSON.parse(text);
                        errorMsg = parsed.message || parsed.error || errorMsg;
                    } catch {
                        errorMsg = text.length < 120 ? text : errorMsg;
                    }
                }
            } catch {}
            throw new Error(errorMsg);
        }

        appObj.notes = newNotes;
        if (newNotes.length > 0) {
            notesView.innerHTML = `<p class="notes-content-text">${newNotes}</p>`;
            editBtn.querySelector('span').textContent = 'Edit';
        } else {
            notesView.innerHTML = `<p class="notes-content-empty">No notes recorded yet. Click to add interview details, salary target, or recruiter info.</p>`;
            editBtn.querySelector('span').textContent = 'Add Note';
        }

        notesEditor.classList.remove('active');
        notesView.style.display = 'block';
        editBtn.style.display = 'inline-flex';

        showToast('Notes saved successfully.', 'success');
    } catch (error) {
        console.error('Failed to save notes:', error);
        showToast(`Failed to save notes: ${error.message || 'Please try again.'}`, 'error');
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save Note';
    }
}

// Pagination updates
function updatePagination() {
    if (!paginationNav) return;

    if (totalPages <= 1 && totalElements <= pageSize) {
        paginationNav.style.display = totalElements > 0 ? 'flex' : 'none';
    } else {
        paginationNav.style.display = 'flex';
    }

    const displayPage = currentPage + 1;
    if (pageInfoText) {
        pageInfoText.textContent = `Page ${displayPage} of ${Math.max(totalPages, 1)} (${totalElements} applications)`;
    }

    firstPageBtn.disabled = currentPage <= 0;
    prevPageBtn.disabled = currentPage <= 0;
    nextPageBtn.disabled = currentPage >= totalPages - 1;
    lastPageBtn.disabled = currentPage >= totalPages - 1;

    renderPageNumbers();
}

function renderPageNumbers() {
    if (!pageNumberButtons) return;
    pageNumberButtons.innerHTML = '';

    const maxButtons = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

    if (endPage - startPage + 1 < maxButtons) {
        startPage = Math.max(0, endPage - maxButtons + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = `page-num-btn ${i === currentPage ? 'active' : ''}`;
        btn.textContent = String(i + 1);
        btn.addEventListener('click', () => {
            if (i !== currentPage) {
                loadApplications(i);
            }
        });
        pageNumberButtons.appendChild(btn);
    }
}

// Event Listeners
filterButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
        activeStatusFilter = btn.dataset.status;
        filterButtons.forEach((b) => b.classList.toggle('active', b === btn));
        loadApplications(0);
    });
});

// Clickable stage metrics in top strip
document.querySelectorAll('.stage-metric').forEach((metric) => {
    metric.addEventListener('click', () => {
        const stage = metric.dataset.stage;
        activeStatusFilter = stage;
        filterButtons.forEach((b) => b.classList.toggle('active', b.dataset.status === stage));
        loadApplications(0);
    });
});

let searchDebounceTimer = null;
if (searchInput) {
    searchInput.addEventListener('input', (e) => {
        searchQuery = e.target.value;
        if (clearSearchBtn) {
            clearSearchBtn.style.display = searchQuery.length > 0 ? 'block' : 'none';
        }
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = setTimeout(() => {
            renderApplications();
        }, 250);
    });
}

if (clearSearchBtn) {
    clearSearchBtn.addEventListener('click', () => {
        if (searchInput) {
            searchInput.value = '';
            searchQuery = '';
            clearSearchBtn.style.display = 'none';
            renderApplications();
        }
    });
}

if (sortSelect) {
    sortSelect.addEventListener('change', (e) => {
        currentSort = e.target.value;
        loadApplications(0);
    });
}

if (pageSizeSelect) {
    pageSizeSelect.addEventListener('change', (e) => {
        pageSize = Number(e.target.value) || 20;
        loadApplications(0);
    });
}

if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
        loadApplications(currentPage);
    });
}

if (firstPageBtn) {
    firstPageBtn.addEventListener('click', () => {
        if (currentPage > 0) loadApplications(0);
    });
}

if (prevPageBtn) {
    prevPageBtn.addEventListener('click', () => {
        if (currentPage > 0) loadApplications(currentPage - 1);
    });
}

if (nextPageBtn) {
    nextPageBtn.addEventListener('click', () => {
        if (currentPage < totalPages - 1) loadApplications(currentPage + 1);
    });
}

if (lastPageBtn) {
    lastPageBtn.addEventListener('click', () => {
        if (currentPage < totalPages - 1) loadApplications(totalPages - 1);
    });
}

// ==========================================
// Modal Window Controller (Add External App)
// ==========================================

function clearModalValidation() {
    if (modalErrorAlert) {
        modalErrorAlert.style.display = 'none';
        modalErrorAlert.textContent = '';
    }

    [modalJobTitle, modalCompanyName, modalOfferUrl, modalNotes].forEach((input) => {
        if (input) input.classList.remove('has-error');
    });

    [jobTitleError, companyNameError, offerUrlError].forEach((errEl) => {
        if (errEl) {
            errEl.textContent = '';
            errEl.classList.remove('visible');
        }
    });
}

function openAddModal() {
    if (!addAppModal) return;
    clearModalValidation();
    if (addAppForm) addAppForm.reset();

    addAppModal.classList.add('show');
    addAppModal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';

    // Focus first input
    setTimeout(() => {
        if (modalJobTitle) modalJobTitle.focus();
    }, 100);
}

function closeAddModal() {
    if (!addAppModal) return;
    addAppModal.classList.remove('show');
    addAppModal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    clearModalValidation();
}

async function handleAddApplicationSubmit(e) {
    e.preventDefault();
    clearModalValidation();

    const jobTitle = modalJobTitle ? modalJobTitle.value.trim() : '';
    const companyName = modalCompanyName ? modalCompanyName.value.trim() : '';
    const offerUrl = modalOfferUrl ? modalOfferUrl.value.trim() : '';
    const notes = modalNotes ? modalNotes.value.trim() : '';

    let hasClientError = false;

    if (!jobTitle) {
        if (modalJobTitle) modalJobTitle.classList.add('has-error');
        if (jobTitleError) {
            jobTitleError.textContent = 'Job title is required.';
            jobTitleError.classList.add('visible');
        }
        hasClientError = true;
    }

    if (!companyName) {
        if (modalCompanyName) modalCompanyName.classList.add('has-error');
        if (companyNameError) {
            companyNameError.textContent = 'Company name is required.';
            companyNameError.classList.add('visible');
        }
        hasClientError = true;
    }

    if (!offerUrl) {
        if (modalOfferUrl) modalOfferUrl.classList.add('has-error');
        if (offerUrlError) {
            offerUrlError.textContent = 'Offer URL is required.';
            offerUrlError.classList.add('visible');
        }
        hasClientError = true;
    }

    if (hasClientError) return;

    // Send POST /api/applications request
    if (submitAddModalBtn) {
        submitAddModalBtn.disabled = true;
        submitAddModalBtn.querySelector('span').textContent = 'Saving...';
    }

    try {
        const payload = {
            offerUrl,
            jobTitle,
            companyName,
            notes: notes.length > 0 ? notes : null
        };

        const response = await fetch(`${API_BASE_URL}/api/applications`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let errorMsg = `Server returned status ${response.status}`;
            try {
                const data = await response.json();
                if (data.fieldErrors && typeof data.fieldErrors === 'object') {
                    // Highlight specific invalid fields
                    Object.entries(data.fieldErrors).forEach(([field, msg]) => {
                        if (field === 'jobTitle' && jobTitleError) {
                            modalJobTitle.classList.add('has-error');
                            jobTitleError.textContent = msg;
                            jobTitleError.classList.add('visible');
                        } else if (field === 'companyName' && companyNameError) {
                            modalCompanyName.classList.add('has-error');
                            companyNameError.textContent = msg;
                            companyNameError.classList.add('visible');
                        } else if (field === 'offerUrl' && offerUrlError) {
                            modalOfferUrl.classList.add('has-error');
                            offerUrlError.textContent = msg;
                            offerUrlError.classList.add('visible');
                        }
                    });
                    errorMsg = Object.values(data.fieldErrors).join('. ');
                } else if (data.message) {
                    errorMsg = data.message;
                }
            } catch {
                const text = await response.text();
                if (text && text.length < 200) errorMsg = text;
            }
            throw new Error(errorMsg);
        }

        // Success
        closeAddModal();
        showToast(`Application for "${jobTitle}" at "${companyName}" added successfully!`, 'success');

        // Reload data and counters
        await loadApplications(0);
        await updateStageCounters();
    } catch (error) {
        console.error('Failed to add application:', error);
        if (modalErrorAlert) {
            modalErrorAlert.textContent = error.message || 'Failed to save application. Please try again.';
            modalErrorAlert.style.display = 'block';
        }
    } finally {
        if (submitAddModalBtn) {
            submitAddModalBtn.disabled = false;
            submitAddModalBtn.querySelector('span').textContent = 'Save Application';
        }
    }
}

// Modal Event Listeners
if (openAddModalBtn) {
    openAddModalBtn.addEventListener('click', openAddModal);
}

if (closeAddModalBtn) {
    closeAddModalBtn.addEventListener('click', closeAddModal);
}

if (cancelAddModalBtn) {
    cancelAddModalBtn.addEventListener('click', closeAddModal);
}

if (addAppForm) {
    addAppForm.addEventListener('submit', handleAddApplicationSubmit);
}

// Close on click outside modal content
if (addAppModal) {
    addAppModal.addEventListener('click', (e) => {
        if (e.target === addAppModal) {
            closeAddModal();
        }
    });
}

// Close on Escape key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && addAppModal && addAppModal.classList.contains('show')) {
        closeAddModal();
    }
});

// Initial Load
document.addEventListener('DOMContentLoaded', () => {
    loadApplications(0);
});
