const API_BASE_URL = 'http://localhost:8081';

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
        const userEmailEl = document.getElementById('userEmail');
        if (userEmailEl && data.email) {
            userEmailEl.textContent = data.email;
        }

        // Show page content
        document.body.classList.add('authenticated');
    } catch (error) {
        console.error('Auth verification failed:', error);
        window.location.replace('../auth/login.html?expired=true');
    }
}

checkAuth();

const form = document.getElementById('uploadForm');
const fileInput = document.getElementById('fileInput');
const filePickerLabel = document.getElementById('filePickerLabel');
const filePickerText = document.getElementById('filePickerText');
const selectedFileInfo = document.getElementById('selectedFileInfo');
const fileNameDisplay = document.getElementById('fileNameDisplay');
const clearFileBtn = document.getElementById('clearFileBtn');
const statusEl = document.getElementById('status');
const resultsContainer = document.getElementById('resultsContainer');
const submitBtn = document.getElementById('submitBtn');
const sortBtn = document.getElementById('sortBtn');
const filterButtons = document.querySelectorAll('.filter-btn');
const technologyOptions = document.querySelectorAll('input[name="technology"]');
const experienceOptions = document.querySelectorAll('input[name="experience"]');
const workModeOptions = document.querySelectorAll('input[name="workMode"]');

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

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
        emptyState.textContent = offerResults.length === 0
            ? 'No offers by this request or nothing new'
            : 'No job offers match this score range.';
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

function showStatus(message, type = 'normal') {
    statusEl.textContent = message;
    statusEl.className = 'status';
    if (type !== 'normal') {
        statusEl.classList.add(type);
    }
}

function formatFileSize(bytes) {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

function resetFileInput() {
    fileInput.value = '';
    if (selectedFileInfo) selectedFileInfo.style.display = 'none';
    if (filePickerText) filePickerText.textContent = 'Choose PDF resume (Max 5MB)';
    showStatus('No file selected.');
}

function validateSelectedFile(file) {
    if (!file) {
        resetFileInput();
        return false;
    }

    // 1. Validate file extension & MIME type
    const fileName = file.name || '';
    const isPdfExtension = fileName.toLowerCase().endsWith('.pdf');
    const isPdfMime = file.type === 'application/pdf' || file.type === '';

    if (!isPdfExtension || (!isPdfMime && file.type)) {
        showStatus('Only PDF files (.pdf) are supported. Please select a valid document.', 'error');
        resetFileInput();
        return false;
    }

    // 2. Validate file size
    if (file.size > MAX_FILE_SIZE_BYTES) {
        showStatus(`File size (${formatFileSize(file.size)}) exceeds the 5MB limit. Please upload a smaller PDF.`, 'error');
        resetFileInput();
        return false;
    }

    // Valid file selected
    if (selectedFileInfo && fileNameDisplay) {
        fileNameDisplay.textContent = `${fileName} (${formatFileSize(file.size)})`;
        selectedFileInfo.style.display = 'flex';
    }
    if (filePickerText) {
        filePickerText.textContent = 'Change PDF file';
    }
    showStatus(`Selected: ${fileName} (${formatFileSize(file.size)})`, 'success');
    return true;
}

if (fileInput) {
    fileInput.addEventListener('change', () => {
        if (fileInput.files && fileInput.files[0]) {
            validateSelectedFile(fileInput.files[0]);
        }
    });
}

if (clearFileBtn) {
    clearFileBtn.addEventListener('click', (e) => {
        e.preventDefault();
        resetFileInput();
    });
}

if (filePickerLabel) {
    ['dragenter', 'dragover'].forEach(eventName => {
        filePickerLabel.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            filePickerLabel.classList.add('dragover');
        });
    });

    ['dragleave', 'drop'].forEach(eventName => {
        filePickerLabel.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            filePickerLabel.classList.remove('dragover');
        });
    });

    filePickerLabel.addEventListener('drop', (e) => {
        if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files[0]) {
            fileInput.files = e.dataTransfer.files;
            validateSelectedFile(e.dataTransfer.files[0]);
        }
    });
}

function renderErrorState(errorMessage, customTips = null) {
    resultsContainer.innerHTML = '';

    const errorCard = document.createElement('div');
    errorCard.className = 'empty-state error-state';

    const header = document.createElement('div');
    header.className = 'error-state-header';
    header.innerHTML = `
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
        <span>Analysis Could Not Be Completed</span>
    `;

    const msg = document.createElement('p');
    msg.className = 'error-state-message';
    msg.textContent = errorMessage || 'The uploaded file could not be analyzed.';

    const tipsContainer = document.createElement('div');
    tipsContainer.innerHTML = '<strong style="display: block; margin-bottom: 6px; font-size: 0.92rem; color: #374151;">Common reasons & solutions:</strong>';

    const tipsList = document.createElement('ul');
    tipsList.className = 'error-state-tips';

    const defaultTips = customTips || [
        'The PDF contains only scanned images without selectable text (OCR is required for images).',
        'The PDF is password-protected or encrypted.',
        'The document is corrupted or not a valid PDF file.',
        'Please export your CV as a clean PDF (e.g. from Word, Google Docs, or Canva) and try again.'
    ];

    defaultTips.forEach(tip => {
        const li = document.createElement('li');
        li.textContent = tip;
        tipsList.appendChild(li);
    });

    tipsContainer.appendChild(tipsList);

    errorCard.appendChild(header);
    errorCard.appendChild(msg);
    errorCard.appendChild(tipsContainer);

    resultsContainer.appendChild(errorCard);
}

function openTaskStream(taskId) {
    closeTaskStream();
    clearResults();

    const streamUrl = `${API_BASE_URL}/api/tasks/${taskId}/stream`;
    showStatus(`Analyzing vacancies with your CV (Task ID: ${taskId})...`, 'loading');
    resultsContainer.innerHTML = '<div class="empty-state">Analyzing vacancies and matching skills, please wait...</div>';

    taskStream = new EventSource(streamUrl, { withCredentials: true });

    taskStream.onopen = () => {
        showStatus(`Connected! Processing vacancies for task ${taskId}...`, 'loading');
    };

    taskStream.addEventListener('vacancy_analyzed', (event) => {
        handleIncomingOffer(event.data);
    });

    taskStream.addEventListener('task_completed', (event) => {
        const completionValue = parseStreamMessage(event.data);
        showStatus(
            completionValue === 'FINISHED'
                ? `Task ${taskId} completed.`
                : `Task ${taskId} finished.`,
            'success'
        );
        closeTaskStream();

        if (offerResults.length === 0) {
            resultsContainer.innerHTML = '<div class="empty-state">No matching job offers found for this criteria.</div>';
        }
        submitBtn.disabled = false;
        submitBtn.textContent = 'Start Analysis';
    });

    taskStream.addEventListener('task_failed', (event) => {
        const raw = event.data;
        let errorMsg = raw;
        try {
            const parsed = JSON.parse(raw);
            errorMsg = parsed.message || parsed.errorMessage || raw;
        } catch {
            errorMsg = raw;
        }

        showStatus(`Analysis failed: ${errorMsg}`, 'error');
        renderErrorState(errorMsg);
        closeTaskStream();
        submitBtn.disabled = false;
        submitBtn.textContent = 'Start Analysis';
    });

    taskStream.onmessage = (event) => {
        handleIncomingOffer(event.data);
    };

    taskStream.onerror = () => {
        // If stream closed after receiving results or task failure, don't overwrite if error already shown
        if (taskStream && taskStream.readyState === EventSource.CLOSED) {
            return;
        }
        showStatus(`Connection to task stream closed or interrupted.`, 'error');
        closeTaskStream();

        if (offerResults.length === 0) {
            renderErrorState('The connection to the analysis server was closed unexpectedly. Please check your connection and try again.');
        }
        submitBtn.disabled = false;
        submitBtn.textContent = 'Start Analysis';
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
        throw new Error('Please select a technology before starting analysis.');
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

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const file = fileInput.files[0];
    if (!validateSelectedFile(file)) {
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    submitBtn.disabled = true;
    submitBtn.textContent = 'Uploading & Starting...';
    showStatus('Uploading resume and starting search...', 'loading');
    clearResults();

    try {
        const params = buildAnalyzeRequestParams();
        const requestUrl = new URL(`${API_BASE_URL}/api/analyze`);
        requestUrl.search = params.toString();

        const response = await fetch(requestUrl.toString(), {
            method: 'POST',
            body: formData,
            // Include HttpOnly cookie credentials
            credentials: 'include'
        });

        if (response.status === 401 || response.status === 403) {
            showStatus('Session expired or unauthenticated. Redirecting to login...', 'error');
            setTimeout(() => {
                window.location.href = '../auth/login.html?expired=true';
            }, 800);
            return;
        }

        if (!response.ok) {
            let errorText = await response.text();
            try {
                const parsedJson = JSON.parse(errorText);
                errorText = parsedJson.message || parsedJson.error || errorText;
            } catch {
                // errorText is already plain text
            }
            throw new Error(errorText || `Request failed with status ${response.status}`);
        }

        const responseData = await response.json();
        const taskId = responseData?.taskId;

        if (!taskId) {
            throw new Error('The server response did not include a valid taskId.');
        }

        openTaskStream(taskId);
    } catch (error) {
        console.error('Submission error:', error);
        const errorMsg = error.message || 'The upload or analysis request failed.';
        showStatus(errorMsg, 'error');
        renderErrorState(errorMsg);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Start Analysis';
    }
});
