# 🌿 RecoverEase AI

> A multi-modal, GenAI-powered recovery and prevention platform for individuals navigating substance use disorders and their caregivers.

[![Live Demo](https://img.shields.io/badge/Live-Demo-green)](https://recoverease-ai.vercel.app)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-blue)](https://recoverease-api.onrender.com)

---

## 🎯 What It Does

RecoverEase AI uses **Google Gemini** as the core AI engine to deliver:

- 🎤 **Zero-typing voice crisis intervention** — speak and get instant AI support
- 📝 **Personalized emergency scripts** — for users, caregivers, and support contacts
- 🤝 **Caregiver guidance module** — what to say, what not to say, next steps
- 📊 **Daily check-in & risk assessment** — mood, craving, stress tracking + AI analysis
- 📚 **Educational resource hub** — trusted recovery info from SAMHSA, NIDA, CDC, WHO
- 🛡️ **Safety plan management** — emergency contacts, triggers, coping strategies

---

## 🤖 GenAI Services Used

**Google Gemini 1.5 Flash API** is used in 5 core features:

| Feature | API Endpoint | GenAI Role |
|---|---|---|
| Crisis Response | `POST /api/crisis/respond` | Generates calm, trauma-informed AI response + emergency script |
| Emergency Script Generator | `POST /api/scripts/generate` | Personalizes recovery scripts for user/caregiver/support |
| Check-in Analysis | `POST /api/checkins` | AI-powered summary + suggestions (after rule-based risk scoring) |
| Caregiver Guidance | `POST /api/caregiver/guidance` | Context-aware communication scripts and action guidance |
| Resource Explainer | `POST /api/resources/explain` | Converts recovery topics into plain-language explanations |

All AI calls are **real, live Gemini API calls** — no hardcoding, no canned responses.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18 + Vite + Tailwind CSS |
| Voice | Web Speech API (browser-native) |
| Backend | Spring Boot 3.2 + Java 17 |
| Auth | JWT (JJWT) + BCrypt |
| Database | H2 In-Memory (demo) / PostgreSQL (production) |
| AI | Google Gemini 1.5 Flash API |
| Deployment | Vercel (frontend) + Render (backend) |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+
- Google Gemini API key (free at [aistudio.google.com](https://aistudio.google.com/app/apikey))

### 1. Clone & setup backend
```bash
git clone https://github.com/kantheti1306/RecoverEase-AI.git
cd RecoverEase-AI/backend

# Set your Gemini API key in application.properties
# Replace: gemini.api.key=YOUR_GEMINI_API_KEY_HERE

mvn spring-boot:run
# Backend runs at http://localhost:8080
```

### 2. Setup frontend
```bash
cd ../frontend
cp .env.example .env
# .env already points to http://localhost:8080

npm install
npm run dev
# Frontend runs at http://localhost:5173
```

### 3. Open the app
Navigate to [http://localhost:5173](http://localhost:5173)

---

## 🧪 Test Credentials (Demo)

| Role | Email | Password |
|---|---|---|
| Individual (user in recovery) | `testuser@recoverease.com` | `Demo@123` |
| Caregiver | `caregiver@recoverease.com` | `Demo@123` |

> These are auto-seeded on first backend startup.

---

## 🗂️ Project Structure

```
RecoverEase-AI/
├── backend/                          # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/recoverease/
│       ├── config/                   # JWT, Security, CORS
│       ├── controller/               # 7 REST controllers
│       ├── dto/                      # Request/Response DTOs
│       ├── entity/                   # JPA entities
│       ├── repository/               # JPA repositories
│       └── service/
│           ├── GeminiAiService.java  # Real Gemini API integration
│           ├── CrisisService.java    # AI crisis response
│           ├── ScriptService.java    # Script generation
│           ├── CheckInService.java   # Risk scoring + AI analysis
│           ├── CaregiverService.java # Caregiver guidance
│           └── ResourceService.java  # Resource AI explainer
└── frontend/                         # React + Vite app
    └── src/
        ├── pages/                    # 10 full pages
        ├── components/               # Reusable components
        ├── api/                      # Axios API modules
        ├── context/                  # Auth state (JWT)
        └── utils/                    # Web Speech API wrapper
```

---

## 🔐 Security

- Passwords hashed with **BCrypt**
- Authentication via **JWT tokens**
- Gemini API key stored as **environment variable** (never in frontend)
- CORS restricted to frontend domain
- All private routes protected by JWT filter
- Input validation on all DTOs

---

## 🌐 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login |
| POST | `/api/user/onboarding` | Save safety profile |
| POST | `/api/crisis/respond` | **AI crisis response** |
| POST | `/api/scripts/generate` | **AI script generation** |
| POST | `/api/checkins` | **AI check-in analysis** |
| GET | `/api/checkins/history` | Check-in history |
| POST | `/api/caregiver/guidance` | **AI caregiver guidance** |
| GET | `/api/resources` | Get all resources |
| POST | `/api/resources/explain` | **AI resource explainer** |

---

## ♿ Accessibility

- Large tap targets for crisis mode
- Voice input (Web Speech API) — zero-typing support
- Text-to-speech output for AI responses
- ARIA labels on all interactive elements
- High contrast colors, readable fonts
- Keyboard navigable

---

## ⚠️ Disclaimer

RecoverEase AI provides supportive guidance only. It is **not a substitute for professional medical advice, diagnosis, or treatment**. For medical emergencies, call **911**. For substance use support: **SAMHSA National Helpline: 1-800-662-4357** (free, confidential, 24/7).

---

## 📄 License

MIT
