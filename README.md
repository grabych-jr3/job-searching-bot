# Job Searcher Bot

## Description
**Job Searcher Bot** is an AI-powered microservice application designed to parse job vacancy websites and analyze them using Large Language Models (LLMs).
The bot compares candidate CV against found vacancies and determines match suitability on a scale from 0% to 100%.

## Tech Stack
* **Language & Frameworks:** Java 21, Spring Boot 4.1.0, Spring AI
* **Messaging & Caching:** Apache Kafka, Redis
* **Parsing & Documents:** JSoup, Apache PDFBox
* **AI Integration:** Google GenAI / Gemini API
* **Containerization:** Docker
* **Frontend Basics:** HTML/CSS, JavaScript (SSE handling)

## Architecture
### job-api-service
* **Role:** Entry point of the application.
* **Flow:**
  * Accepts candidate CVs in PDF format.
  * Caches raw file bytes in Redis.
  * Emits an analysis task to Apache Kafka.
  * Streams analyzed job offers back to the client via a Server-Sent Events (SSE) connection.

### search-service
* **Role:** Scrapes job portals (e.g., JustJoin IT, Pracuj.pl, BulldogJob).
* **Flow:**
  + Connects to target websites and fetches raw offer data (HTML/embedded JSON).
  + Cleans HTML tags and sanitizes text fields.
  + Maps extracted offers into a unified internal data model.
  + Publishes scraped offers to Apache Kafka.

### analyzer-service
* **Role:** Core AI analytical engine.
* **Flow:**
  + Extracts text from the user's PDF resume using Apache PDFBox.
  + Analyzes match quality between candidate experience and job requirements via Spring AI + Google GenAI.
  + Outputs evaluation results and publishes them back to Kafka.

#### Sample Output JSON:
```json
{
  "url": "[https://example.com/job/123](https://example.com/job/123)",
  "score": 85,
  "reason": "Candidate has strong Java 21 and Spring Boot experience, but lacks 2+ years of Kubernetes hands-on experience required by the vacancy."
}
```
