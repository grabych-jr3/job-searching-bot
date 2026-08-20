const form = document.getElementById('uploadForm');
const fileInput = document.getElementById('fileInput');
const statusEl = document.getElementById('status');
const resultsContainer = document.getElementById('resultsContainer');
const submitBtn = document.getElementById('submitBtn');
const sortBtn = document.getElementById('sortBtn');
const filterButtons = document.querySelectorAll('.filter-btn');
const technologyOptions = document.querySelectorAll('input[name="technology"]');
const experienceOptions = document.querySelectorAll('input[name="experience"]');
const workModeOptions = document.querySelectorAll('input[name="workMode"]');

let taskStream = null;
let offerResults = [];
let activeFilter = 'all';

function closeTaskStream() {
    if (taskStream) {
        taskStream.close();
        taskStream = null;
    }
}

function parseStreamMessage(rawData) {
    try {
        return JSON.parse(rawData);
    } catch (error) {
        return rawData;
    }
}

function clearResults() {
    resultsContainer.innerHTML = '';
    offerResults = [];
    activeFilter = 'all';
    filterButtons.forEach((button) => {
        const isActive = button.dataset.filter === 'all';
        button.classList.toggle('active', isActive);
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

function renderFilteredCards() {
    const range = getFilterRange(activeFilter);
    const visibleOffers = [...offerResults]
        .filter((offer) => {
            if (!range) {
                return true;
            }

            const score = Number(offer.score ?? 0);
            return score >= range.min && score <= range.max;
        })
        .sort((left, right) => Number(right.score) - Number(left.score));

    resultsContainer.innerHTML = '';

    if (visibleOffers.length === 0) {
        const emptyState = document.createElement('div');
        emptyState.className = 'empty-state';
        emptyState.textContent = 'No job offers match this score range.';
        resultsContainer.appendChild(emptyState);
        return;
    }

    visibleOffers.forEach((offerResult) => {
        const card = document.createElement('article');
        const safeJobTitle = offerResult.jobTitle || 'Untitled position';
        const safeUrl = offerResult.url || '#';
        const safeReason = offerResult.reason || 'No explanation provided.';
        const numericScore = Number(offerResult.score ?? 0);
        const normalizedScore = Number.isFinite(numericScore) ? Math.min(Math.max(numericScore, 0), 100) : 0;
        const scoreLabel = `${normalizedScore}/100`;
        const scoreTier = getScoreTier(normalizedScore);

        card.className = `offer-card ${scoreTier}`;
        card.dataset.score = String(normalizedScore);

        const header = document.createElement('div');
        header.className = 'offer-header';

        const title = document.createElement('h2');
        title.className = 'offer-title';
        title.textContent = safeJobTitle;

        const scoreBadge = document.createElement('span');
        scoreBadge.className = `offer-score ${scoreTier}`;
        scoreBadge.textContent = scoreLabel;

        header.appendChild(title);
        header.appendChild(scoreBadge);

        const reason = document.createElement('p');
        reason.className = 'offer-reason';
        reason.textContent = safeReason;

        const actionLink = document.createElement('a');
        actionLink.className = 'offer-link-btn';
        actionLink.href = safeUrl;
        actionLink.target = '_blank';
        actionLink.rel = 'noopener noreferrer';
        actionLink.textContent = 'Open offer';

        card.appendChild(header);
        card.appendChild(reason);
        card.appendChild(actionLink);
        resultsContainer.appendChild(card);
    });
}

function sortCardsByScore() {
    offerResults.sort((left, right) => Number(right.score) - Number(left.score));
    renderFilteredCards();
}

function renderOfferCard(offerResult) {
    if (!offerResult || typeof offerResult !== 'object') {
        return;
    }

    const safeJobTitle = offerResult.jobTitle || 'Untitled position';
    const numericScore = Number(offerResult.score ?? 0);
    const normalizedScore = Number.isFinite(numericScore) ? Math.min(Math.max(numericScore, 0), 100) : 0;

    offerResults.push({ ...offerResult, score: normalizedScore, jobTitle: safeJobTitle });
    renderFilteredCards();
}


function handleIncomingOffer(payload) {
    const parsed = parseStreamMessage(payload);

    if (parsed && typeof parsed === 'object') {
        renderOfferCard(parsed);
        return;
    }

    if (typeof parsed === 'string' && parsed.trim().length > 0) {
        statusEl.textContent = parsed;
    }
}

sortBtn.addEventListener('click', () => {
    sortCardsByScore();
});

filterButtons.forEach((button) => {
    button.addEventListener('click', () => {
        activeFilter = button.dataset.filter;

        filterButtons.forEach((item) => {
            item.classList.toggle('active', item === button);
        });

        renderFilteredCards();
    });
});

function openTaskStream(taskId) {
    closeTaskStream();
    clearResults();

    const streamUrl = `http://localhost:8081/api/tasks/${taskId}/stream`;
    statusEl.textContent = `Listening for vacancy results for task ${taskId}...`;

    taskStream = new EventSource(streamUrl, { withCredentials: true });

    taskStream.onopen = () => {
        statusEl.textContent = `Connected to task stream for ${taskId}.`;
    };

    taskStream.addEventListener('vacancy_analyzed', (event) => {
        handleIncomingOffer(event.data);
    });

    taskStream.addEventListener('task_completed', (event) => {
        const completionValue = parseStreamMessage(event.data);
        statusEl.textContent = completionValue === 'FINISHED'
            ? `Task ${taskId} completed.`
            : `Task ${taskId} finished.`;
        closeTaskStream();
    });

    taskStream.onmessage = (event) => {
        handleIncomingOffer(event.data);
    };

    taskStream.onerror = () => {
        statusEl.textContent = `Connection to task ${taskId} stream failed.`;
        closeTaskStream();
    };
}

function getSelectedValues(selector) {
    return Array.from(document.querySelectorAll(selector))
        .filter((checkbox) => checkbox.checked)
        .map((checkbox) => checkbox.value);
}

function buildAnalyzeRequestParams() {
    const technology = getSelectedValues('input[name="technology"]')[0];
    const experiences = getSelectedValues('input[name="experience"]');
    const workModes = getSelectedValues('input[name="workMode"]');

    if (!technology) {
        throw new Error('Please select a technology before sending the file.');
    }

    if (experiences.length === 0) {
        throw new Error('Please select at least one experience level.');
    }

    const params = new URLSearchParams();
    params.set('technology', String(technology).toLowerCase());
    experiences.forEach((level) => params.append('experience', String(level).toLowerCase()));
    workModes.forEach((mode) => params.append('workMode', String(mode).toLowerCase()));

    return params;
}

const logoutBtn = document.getElementById('logoutBtn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        try {
            await fetch('http://localhost:8081/api/auth/logout', {
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

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const file = fileInput.files[0];
    if (!file) {
        statusEl.textContent = 'Please select a file before sending.';
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    submitBtn.disabled = true;
    submitBtn.textContent = 'Sending...';
    statusEl.textContent = 'Uploading file...';
    clearResults();

    try {
        const params = buildAnalyzeRequestParams();
        const requestUrl = new URL('http://localhost:8081/api/analyze');
        requestUrl.search = params.toString();

        const response = await fetch(requestUrl.toString(), {
            method: 'POST',
            body: formData,
            // Include HttpOnly cookie credentials
            credentials: 'include'
        });

        if (response.status === 401 || response.status === 403) {
            statusEl.textContent = 'Session expired or unauthenticated. Redirecting to login...';
            setTimeout(() => {
                window.location.href = '../auth/login.html?expired=true';
            }, 800);
            return;
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Request failed with status ${response.status}`);
        }

        const responseData = await response.json();
        const taskId = responseData?.taskId;

        if (!taskId) {
            throw new Error('The server response did not include a taskId.');
        }

        openTaskStream(taskId);
    } catch (error) {
        console.error(error);
        statusEl.textContent = error.message || 'The upload failed.';
        resultsContainer.innerHTML = `<div class="empty-state">${error.message || 'The upload failed.'}</div>`;
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Send';
    }
});
