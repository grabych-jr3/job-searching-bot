const API_BASE_URL = 'http://localhost:8081';

// State
let currentPage = 0;
let pageSize = 20;
let currentSort = 'analyzedAt,desc';
let totalPages = 1;
let totalElements = 0;
let allPageOffers = [];
let activeScoreFilter = 'all';
let searchQuery = '';

// DOM Elements
const userEmailEl = document.getElementById('userEmail');
const logoutBtn = document.getElementById('logoutBtn');
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

// Route Guard: verify session with backend before rendering page
async function checkAuth() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
            method: 'GET',
            credentials: 'include'
        });

        if (!response.ok) {
            window.location.replace('../auth/login.html?expired=true');
            return;
        }

        const data = await response.json();
        if (userEmailEl && data.email) {
            userEmailEl.textContent = data.email;
        }

        document.body.classList.add('authenticated');
        loadHistory(0);
    } catch (error) {
        console.error('Auth verification failed:', error);
        window.location.replace('../auth/login.html?expired=true');
    }
}

// Logout handler
if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        try {
            await fetch(`${API_BASE_URL}/api/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout failed:', error);
        } finally {
            window.location.href = '../auth/login.html?logged_out=true';
        }
    });
}

function getScoreTier(score) {
    if (score >= 80) return 'score-tier-urgent';
    if (score >= 70) return 'score-tier-high';
    if (score >= 50) return 'score-tier-mid';
    return 'score-tier-low';
}

function getFilterRange(filterValue) {
    switch (filterValue) {
        case '0-49':
            return { min: 0, max: 49 };
        case '50-69':
            return { min: 50, max: 69 };
        case '70-79':
            return { min: 70, max: 79 };
        case '80-100':
            return { min: 80, max: 100 };
        default:
            return null;
    }
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

// Fetch history from backend
async function loadHistory(page = 0) {
    currentPage = page;
    resultsContainer.innerHTML = '<div class="empty-state">Loading analysis history...</div>';

    try {
        const url = new URL(`${API_BASE_URL}/api/history`);
        url.searchParams.set('page', String(currentPage));
        url.searchParams.set('size', String(pageSize));
        if (currentSort) {
            url.searchParams.set('sort', currentSort);
        }

        const response = await fetch(url.toString(), {
            method: 'GET',
            credentials: 'include'
        });

        if (response.status === 401 || response.status === 403) {
            window.location.replace('../auth/login.html?expired=true');
            return;
        }

        if (!response.ok) {
            throw new Error(`Failed to load history (status ${response.status})`);
        }

        const data = await response.json();
        
        allPageOffers = Array.isArray(data.content) ? data.content : [];
        totalPages = Number(data.totalPages) || 1;
        totalElements = Number(data.totalElements) || 0;
        currentPage = Number(data.number) || page;

        if (totalCountEl) {
            totalCountEl.textContent = totalElements.toLocaleString();
        }

        renderOffers();
        updatePagination();
    } catch (error) {
        console.error('Error fetching history:', error);
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <p>Failed to load analysis history: ${error.message || 'Unknown error'}</p>
                <button type="button" class="empty-action-btn" onclick="loadHistory(${currentPage})">
                    Try Again
                </button>
            </div>
        `;
        if (paginationNav) paginationNav.style.display = 'none';
    }
}

// Filter and render offers
function renderOffers() {
    resultsContainer.innerHTML = '';

    if (allPageOffers.length === 0 && totalElements === 0) {
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--muted); opacity: 0.6;">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <p>No analyzed offers yet. Run your first job offer analysis!</p>
                <a href="../homePage/home.html" class="empty-action-btn">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="17 8 12 3 7 8"></polyline>
                        <line x1="12" y1="3" x2="12" y2="15"></line>
                    </svg>
                    Analyze New Offers
                </a>
            </div>
        `;
        if (paginationNav) paginationNav.style.display = 'none';
        return;
    }

    const range = getFilterRange(activeScoreFilter);
    const query = searchQuery.trim().toLowerCase();

    const filtered = allPageOffers.filter((offer) => {
        const score = Number(offer.score ?? 0);
        if (range && (score < range.min || score > range.max)) {
            return false;
        }

        if (query) {
            const title = (offer.jobTitle || '').toLowerCase();
            const reason = (offer.reason || '').toLowerCase();
            if (!title.includes(query) && !reason.includes(query)) {
                return false;
            }
        }

        return true;
    });

    if (filtered.length === 0) {
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <p>No analyzed offers match the current filters on this page.</p>
            </div>
        `;
        return;
    }

    filtered.forEach((offer) => {
        const card = document.createElement('article');
        const safeTitle = offer.jobTitle || 'Untitled position';
        const safeUrl = offer.offerUrl || offer.url || '#';
        const safeReason = offer.reason || 'No explanation provided.';
        const numericScore = Number(offer.score ?? 0);
        const normalizedScore = Number.isFinite(numericScore) ? Math.min(Math.max(numericScore, 0), 100) : 0;
        const scoreTier = getScoreTier(normalizedScore);
        const dateStr = formatDate(offer.analyzed_at || offer.analyzedAt);

        card.className = `offer-card ${scoreTier}`;

        const header = document.createElement('div');
        header.className = 'offer-header';

        const title = document.createElement('h2');
        title.className = 'offer-title';
        title.textContent = safeTitle;

        const scoreBadge = document.createElement('span');
        scoreBadge.className = `offer-score ${scoreTier}`;
        scoreBadge.textContent = `${normalizedScore}/100`;

        header.appendChild(title);
        header.appendChild(scoreBadge);

        const meta = document.createElement('div');
        meta.className = 'offer-meta';
        meta.innerHTML = `
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>Analyzed ${dateStr}</span>
        `;

        const reason = document.createElement('p');
        reason.className = 'offer-reason';
        reason.textContent = safeReason;

        const link = document.createElement('a');
        link.className = 'offer-link-btn';
        link.href = safeUrl;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        link.innerHTML = `
            <span>Open offer</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
        `;

        card.appendChild(header);
        card.appendChild(meta);
        card.appendChild(reason);
        card.appendChild(link);
        resultsContainer.appendChild(card);
    });
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
        pageInfoText.textContent = `Page ${displayPage} of ${Math.max(totalPages, 1)} (${totalElements} offers)`;
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
                loadHistory(i);
            }
        });
        pageNumberButtons.appendChild(btn);
    }
}

// Event Listeners
filterButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
        activeScoreFilter = btn.dataset.filter;
        filterButtons.forEach((b) => b.classList.toggle('active', b === btn));
        renderOffers();
    });
});

if (searchInput) {
    searchInput.addEventListener('input', (e) => {
        searchQuery = e.target.value;
        if (clearSearchBtn) {
            clearSearchBtn.style.display = searchQuery.length > 0 ? 'block' : 'none';
        }
        renderOffers();
    });
}

if (clearSearchBtn) {
    clearSearchBtn.addEventListener('click', () => {
        if (searchInput) {
            searchInput.value = '';
            searchQuery = '';
            clearSearchBtn.style.display = 'none';
            renderOffers();
        }
    });
}

if (sortSelect) {
    sortSelect.addEventListener('change', (e) => {
        currentSort = e.target.value;
        loadHistory(0);
    });
}

if (pageSizeSelect) {
    pageSizeSelect.addEventListener('change', (e) => {
        pageSize = Number(e.target.value) || 20;
        loadHistory(0);
    });
}

if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
        loadHistory(currentPage);
    });
}

if (firstPageBtn) {
    firstPageBtn.addEventListener('click', () => {
        if (currentPage > 0) loadHistory(0);
    });
}

if (prevPageBtn) {
    prevPageBtn.addEventListener('click', () => {
        if (currentPage > 0) loadHistory(currentPage - 1);
    });
}

if (nextPageBtn) {
    nextPageBtn.addEventListener('click', () => {
        if (currentPage < totalPages - 1) loadHistory(currentPage + 1);
    });
}

if (lastPageBtn) {
    lastPageBtn.addEventListener('click', () => {
        if (currentPage < totalPages - 1) loadHistory(totalPages - 1);
    });
}

// Run auth check on initialization
checkAuth();
