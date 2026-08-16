const form = document.getElementById('uploadForm');
const fileInput = document.getElementById('fileInput');
const statusEl = document.getElementById('status');
const resultsContainer = document.getElementById('resultsContainer');
const submitBtn = document.getElementById('submitBtn');
const sortBtn = document.getElementById('sortBtn');
const filterButtons = document.querySelectorAll('.filter-btn');

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
    const cards = Array.from(resultsContainer.querySelectorAll('.offer-card'));
    cards.forEach((card) => {
        const score = Number(card.dataset.score ?? 0);
        const range = getFilterRange(activeFilter);

        if (!range) {
            card.hidden = false;
            return;
        }

        const matches = score >= range.min && score <= range.max;
        card.hidden = !matches;
    });
}

function sortCardsByScore() {
    const cards = Array.from(resultsContainer.children).filter((node) => node.classList.contains('offer-card'));
    cards.sort((left, right) => Number(right.dataset.score) - Number(left.dataset.score));
    cards.forEach((card) => resultsContainer.appendChild(card));
    renderFilteredCards();
}

function renderOfferCard(offerResult) {
    if (!offerResult || typeof offerResult !== 'object') {
        return;
    }

    const safeUrl = offerResult.url || 'No URL provided';
    const safeReason = offerResult.reason || 'No explanation provided.';
    const numericScore = Number(offerResult.score ?? 0);
    const normalizedScore = Number.isFinite(numericScore) ? Math.min(Math.max(numericScore, 0), 100) : 0;
    const scoreLabel = `${normalizedScore}/100`;
    const scoreTier = getScoreTier(normalizedScore);

    const card = document.createElement('article');
    card.className = `offer-card ${scoreTier}`;
    card.dataset.score = String(normalizedScore);

    const header = document.createElement('div');
    header.className = 'offer-header';

    const scoreBadge = document.createElement('span');
    scoreBadge.className = `offer-score ${scoreTier}`;
    scoreBadge.textContent = scoreLabel;

    const urlLink = document.createElement('a');
    urlLink.className = 'offer-url';
    urlLink.href = safeUrl;
    urlLink.target = '_blank';
    urlLink.rel = 'noopener noreferrer';
    urlLink.textContent = safeUrl;

    header.appendChild(scoreBadge);
    header.appendChild(urlLink);

    const reason = document.createElement('p');
    reason.className = 'offer-reason';
    reason.textContent = safeReason;

    card.appendChild(header);
    card.appendChild(reason);
    resultsContainer.prepend(card);

    offerResults.push({ ...offerResult, score: normalizedScore });
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

    taskStream = new EventSource(streamUrl);

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
        const response = await fetch('http://localhost:8081/api/analyze', {
            method: 'POST',
            body: formData
        });

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
