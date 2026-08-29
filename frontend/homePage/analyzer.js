const API_BASE_URL = 'http://localhost:8081';

// DOM Elements
const userEmailEl = document.getElementById('userEmail');
const logoutBtn = document.getElementById('logoutBtn');
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
const submitBtnText = document.getElementById('submitBtnText');
const sortBtn = document.getElementById('sortBtn');
const filterButtons = document.querySelectorAll('.filter-btn');
const offersCountBadge = document.getElementById('offersCountBadge');

const techFieldset = document.getElementById('technologyFieldset');
const expFieldset = document.getElementById('experienceFieldset');
const techErrorMsg = document.getElementById('techErrorMsg');
const expErrorMsg = document.getElementById('expErrorMsg');

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

let taskStream = null;
let offerResults = [];
let activeFilter = 'all';

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
    } catch (error) {
        console.error('Auth verification failed:', error);
        window.location.replace('../auth/login.html?expired=true');
    }
}

checkAuth();

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
    if (offersCountBadge) {
        offersCountBadge.style.display = 'none';
        offersCountBadge.textContent = '0 Offers';
    }
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
            ? 'No matching job offers found on the scraped portals for this criteria.'
            : 'No analyzed job offers match this score range.';
        resultsContainer.appendChild(emptyState);
        return;
    }

    visibleOffers.forEach((offerResult) => {
        const card = document.createElement('article');
        const safeJobTitle = offerResult.jobTitle || 'Untitled position';
        const safeUrl = offerResult.url || offerResult.offerUrl || '#';
        const safeReason = offerResult.reason || 'No explanation provided.';
        const numericScore = Number(offerResult.score ?? 0);
        const normalizedScore = Number.isFinite(numericScore) ? Math.min(Math.max(numericScore, 0), 100) : 0;
        const scoreLabel = `${normalizedScore}% Match`;
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
        actionLink.innerHTML = `
            <span>Open Vacancy</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
        `;

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

    if (offersCountBadge) {
        offersCountBadge.style.display = 'inline-block';
        offersCountBadge.textContent = `${offerResults.length} Offer${offerResults.length === 1 ? '' : 's'} Found`;
    }

    renderFilteredCards();
}

function handleIncomingOffer(payload) {
    const parsed = parseStreamMessage(payload);

    if (parsed && typeof parsed === 'object') {
        renderOfferCard(parsed);
        return;
    }

    if (typeof parsed === 'string' && parsed.trim().length > 0) {
        showStatus(parsed, 'loading');
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
    if (filePickerText) filePickerText.textContent = 'Choose PDF Resume (Max 5MB)';
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
        showStatus('Only PDF files (.pdf) are supported. Please select a valid PDF document.', 'error');
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
        filePickerText.textContent = 'Change Selected PDF File';
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

/**
 * Maps all backend exception messages to structured, user-friendly error details and troubleshooting steps.
 */
function mapBackendException(rawError) {
    const errorStr = (rawError || '').toString().toLowerCase();

    // 1. Password Protected PDF
    if (errorStr.includes('password') || errorStr.includes('encrypted')) {
        return {
            category: 'Password Protected PDF',
            title: 'Encrypted or Password-Protected PDF Detected',
            message: 'Our automated parser cannot open password-protected or encrypted PDF documents.',
            tips: [
                'Open your PDF file in your browser or PDF reader.',
                'Choose "Print" -> "Save as PDF" to create an unencrypted copy.',
                'Ensure the document opens directly without requiring a password prompt.',
                'Re-upload the unencrypted file and try again.'
            ]
        };
    }

    // 2. Scanned / Image-only Document (No text extracted)
    if (errorStr.includes('scanned') || errorStr.includes('extract any text') || errorStr.includes('ocr')) {
        return {
            category: 'Scanned Document (No Text)',
            title: 'Image-Only / Scanned PDF Detected',
            message: 'Could not extract selectable text from the uploaded PDF. Scanned image files without an embedded OCR text layer are not supported.',
            tips: [
                'Export your resume directly as a PDF from Microsoft Word, Google Docs, or Canva.',
                'Check if you can highlight and copy text in your PDF with your cursor.',
                'If using a physical scan, run an OCR (Optical Character Recognition) tool before exporting to PDF.',
                'Upload the text-based PDF version.'
            ]
        };
    }

    // 3. Corrupted or Invalid PDF
    if (errorStr.includes('corrupt') || errorStr.includes('not a valid pdf') || errorStr.includes('header')) {
        return {
            category: 'Invalid / Corrupt File',
            title: 'Corrupted or Non-Standard PDF File',
            message: 'The file header does not conform to the standard PDF specification or the document is corrupted.',
            tips: [
                'Ensure the file is not a renamed .docx, .png, or .txt file.',
                'Re-save or re-download your resume from your original document editor.',
                'Make sure the file size is non-zero and finishes downloading completely before upload.'
            ]
        };
    }

    // 4. File Size Exceeded (5MB Limit)
    if (errorStr.includes('size') || errorStr.includes('5mb') || errorStr.includes('too large') || errorStr.includes('413')) {
        return {
            category: 'File Size Exceeded',
            title: 'File Exceeds 5MB Maximum Size',
            message: 'The uploaded file is larger than the 5MB maximum limit allowed by the analysis engine.',
            tips: [
                'Compress your PDF using an online PDF compressor (e.g. Adobe Acrobat Compress, SmallPDF).',
                'Remove large high-resolution background images or decorative graphics.',
                'Ensure the PDF size is below 5.0 MB.'
            ]
        };
    }

    // 5. Empty CV File
    if (errorStr.includes('empty') || errorStr.includes('0 bytes')) {
        return {
            category: 'Empty Document',
            title: 'Uploaded PDF is Empty',
            message: 'The selected file contains 0 bytes or has no content.',
            tips: [
                'Select a valid resume file from your computer.',
                'Verify that the PDF opens and contains your work experience and skills.'
            ]
        };
    }

    // 6. Gemini AI Safety or Parsing Issue
    if (errorStr.includes('gemini') || errorStr.includes('safety') || errorStr.includes('candidate profile')) {
        return {
            category: 'AI Parsing Issue',
            title: 'AI Model Encountered a Parsing Restriction',
            message: 'The Google Gemini AI parser was unable to structure your resume into standardized candidate sections.',
            tips: [
                'Ensure your resume contains clear headings: Experience, Skills, Education, Projects.',
                'Avoid unusual character encodings or non-standard symbols in your document.',
                'Re-run the analysis in a few moments if this was a transient AI service blip.'
            ]
        };
    }

    // 7. CV Cache Expiry
    if (errorStr.includes('not found for task') || errorStr.includes('cache')) {
        return {
            category: 'Session Timeout',
            title: 'Analysis Task Data Expired',
            message: 'The cached resume data for this task expired before analysis could finish.',
            tips: [
                'Simply re-submit the upload form to start a fresh analysis session.'
            ]
        };
    }

    // 8. Default / Fallback
    return {
        category: 'Analysis Error',
        title: 'Analysis Could Not Be Completed',
        message: rawError || 'An unexpected error occurred during resume processing or vacancy analysis.',
        tips: [
            'Check that your resume is a clean, text-based PDF under 5MB.',
            'Ensure the backend microservices (Job API, Search Service, Analyzer Service, Kafka, Redis) are running.',
            'Click "Try Again" to re-run the analysis.'
        ]
    };
}

/**
 * Renders an actionable, beautiful Error Resolution Card.
 */
function renderErrorState(rawErrorMessage) {
    resultsContainer.innerHTML = '';

    const errorDetails = mapBackendException(rawErrorMessage);

    const errorCard = document.createElement('div');
    errorCard.className = 'empty-state error-state';

    const header = document.createElement('div');
    header.className = 'error-state-header';
    header.innerHTML = `
        <div class="error-title-group">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <span>${errorDetails.title}</span>
        </div>
        <span class="error-category-badge">${errorDetails.category}</span>
    `;

    const msg = document.createElement('p');
    msg.className = 'error-state-message';
    msg.textContent = errorDetails.message;

    const detailsBox = document.createElement('div');
    detailsBox.className = 'error-state-details';
    
    const detailsTitle = document.createElement('span');
    detailsTitle.className = 'error-details-title';
    detailsTitle.textContent = 'How to resolve this issue:';
    detailsBox.appendChild(detailsTitle);

    const tipsList = document.createElement('ul');
    tipsList.className = 'error-state-tips';
    errorDetails.tips.forEach(tip => {
        const li = document.createElement('li');
        li.textContent = tip;
        tipsList.appendChild(li);
    });
    detailsBox.appendChild(tipsList);

    const actionsGroup = document.createElement('div');
    actionsGroup.className = 'error-actions-group';

    const retryBtn = document.createElement('button');
    retryBtn.type = 'button';
    retryBtn.className = 'error-action-btn retry-btn';
    retryBtn.innerHTML = `
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"></polyline>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
        </svg>
        <span>Re-Upload & Retry</span>
    `;
    retryBtn.addEventListener('click', () => {
        fileInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        fileInput.click();
    });

    const guideLink = document.createElement('a');
    guideLink.href = 'home.html#guidelines';
    guideLink.className = 'error-action-btn guide-btn';
    guideLink.style.textDecoration = 'none';
    guideLink.innerHTML = `
        <span>View Resume Guidelines</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
        </svg>
    `;

    actionsGroup.appendChild(retryBtn);
    actionsGroup.appendChild(guideLink);

    errorCard.appendChild(header);
    errorCard.appendChild(msg);
    errorCard.appendChild(detailsBox);
    errorCard.appendChild(actionsGroup);

    resultsContainer.appendChild(errorCard);
}

function openTaskStream(taskId) {
    closeTaskStream();
    clearResults();

    const streamUrl = `${API_BASE_URL}/api/tasks/${taskId}/stream`;
    showStatus(`Scraping portals & evaluating vacancies with your CV (Task: ${taskId})...`, 'loading');
    resultsContainer.innerHTML = `
        <div class="empty-state initial-empty-state">
            <div class="status loading" style="display: inline-flex; margin: 0 0 14px;">
                Searching JustJoin IT, BulldogJob & Pracuj.pl...
            </div>
            <p class="empty-title">AI Analysis In Progress</p>
            <p class="empty-desc">Evaluating vacancies against your skills with Gemini LLM. Matching offers will appear below in real-time.</p>
        </div>
    `;

    taskStream = new EventSource(streamUrl, { withCredentials: true });

    taskStream.onopen = () => {
        showStatus(`Live SSE stream connected! Processing vacancies...`, 'loading');
    };

    taskStream.addEventListener('vacancy_analyzed', (event) => {
        handleIncomingOffer(event.data);
    });

    taskStream.addEventListener('task_completed', (event) => {
        const completionValue = parseStreamMessage(event.data);
        showStatus(
            completionValue === 'FINISHED'
                ? `Analysis completed! Found ${offerResults.length} analyzed vacancies.`
                : `Task ${taskId} finished.`,
            'success'
        );
        closeTaskStream();

        if (offerResults.length === 0) {
            resultsContainer.innerHTML = `
                <div class="empty-state">
                    <p style="font-weight: 700; font-size: 1.1rem; color: var(--text); margin-bottom: 6px;">No vacancies matched your criteria</p>
                    <p style="font-size: 0.9rem; color: var(--muted); max-width: 500px; margin: 0 auto 16px;">
                        The scrapers found 0 matching active postings for this combination of technology and experience.
                    </p>
                    <p style="font-size: 0.85rem; color: #4b5563;">
                        <strong>Tip:</strong> Try selecting multiple experience levels (e.g. Junior + Mid) or expanding work modes (Remote + Hybrid).
                    </p>
                </div>
            `;
        }
        submitBtn.disabled = false;
        submitBtnText.textContent = 'Start AI Analysis';
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
        submitBtnText.textContent = 'Start AI Analysis';
    });

    taskStream.onmessage = (event) => {
        handleIncomingOffer(event.data);
    };

    taskStream.onerror = () => {
        if (taskStream && taskStream.readyState === EventSource.CLOSED) {
            return;
        }
        showStatus(`Connection to analysis stream interrupted.`, 'error');
        closeTaskStream();

        if (offerResults.length === 0) {
            renderErrorState('The connection to the analysis server was closed unexpectedly. Please check your backend connection and try again.');
        }
        submitBtn.disabled = false;
        submitBtnText.textContent = 'Start AI Analysis';
    };
}

function getSelectedValues(selector) {
    return Array.from(document.querySelectorAll(selector))
        .filter((checkbox) => checkbox.checked)
        .map((checkbox) => checkbox.value);
}

function validateFormInputs() {
    let isValid = true;

    const technology = getSelectedValues('input[name="technology"]')[0];
    const experiences = getSelectedValues('input[name="experience"]');

    if (!technology) {
        techFieldset.classList.add('has-error');
        techErrorMsg.style.display = 'block';
        isValid = false;
    } else {
        techFieldset.classList.remove('has-error');
        techErrorMsg.style.display = 'none';
    }

    if (experiences.length === 0) {
        expFieldset.classList.add('has-error');
        expErrorMsg.style.display = 'block';
        isValid = false;
    } else {
        expFieldset.classList.remove('has-error');
        expErrorMsg.style.display = 'none';
    }

    return isValid;
}

// Clear error highlights on change
document.querySelectorAll('input[name="technology"]').forEach(input => {
    input.addEventListener('change', () => {
        techFieldset.classList.remove('has-error');
        techErrorMsg.style.display = 'none';
    });
});

document.querySelectorAll('input[name="experience"]').forEach(input => {
    input.addEventListener('change', () => {
        expFieldset.classList.remove('has-error');
        expErrorMsg.style.display = 'none';
    });
});

function buildAnalyzeRequestParams() {
    const technology = getSelectedValues('input[name="technology"]')[0];
    const experiences = getSelectedValues('input[name="experience"]');
    const workModes = getSelectedValues('input[name="workMode"]');

    const params = new URLSearchParams();
    params.set('technology', String(technology).toLowerCase());
    experiences.forEach((level) => params.append('experience', String(level).toLowerCase()));
    workModes.forEach((mode) => params.append('workMode', String(mode).toLowerCase()));

    return params;
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (!validateFormInputs()) {
        showStatus('Please fill in all required fields (Technology and Experience Level).', 'error');
        return;
    }

    const file = fileInput.files[0];
    if (!validateSelectedFile(file)) {
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    submitBtn.disabled = true;
    submitBtnText.textContent = 'Uploading & Analyzing...';
    showStatus('Uploading resume and initiating scrapers...', 'loading');
    clearResults();

    try {
        const params = buildAnalyzeRequestParams();
        const requestUrl = new URL(`${API_BASE_URL}/api/analyze`);
        requestUrl.search = params.toString();

        const response = await fetch(requestUrl.toString(), {
            method: 'POST',
            body: formData,
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
            let parsedMessage = errorText;
            try {
                const parsedJson = JSON.parse(errorText);
                if (parsedJson.fieldErrors && Object.keys(parsedJson.fieldErrors).length > 0) {
                    parsedMessage = Object.values(parsedJson.fieldErrors).join('. ');
                } else {
                    parsedMessage = parsedJson.message || parsedJson.error || errorText;
                }
            } catch {
                // errorText is already plain string
            }
            throw new Error(parsedMessage || `Request failed with status ${response.status}`);
        }

        const responseData = await response.json();
        const taskId = responseData?.taskId;

        if (!taskId) {
            throw new Error('The server did not return a valid taskId.');
        }

        openTaskStream(taskId);
    } catch (error) {
        console.error('Submission error:', error);
        const errorMsg = error.message || 'The upload or analysis request failed.';
        showStatus(errorMsg, 'error');
        renderErrorState(errorMsg);
        submitBtn.disabled = false;
        submitBtnText.textContent = 'Start AI Analysis';
    }
});
