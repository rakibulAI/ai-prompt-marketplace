# PromptVault — AI Prompt Marketplace 🚀

> A full-stack, production-ready AI Prompt Marketplace built with Java Spring Boot, MySQL, Vanilla JS, and Tailwind CSS. Glassy UI, Stripe + PayPal payments, JWT auth, seller dashboard, admin panel.

**🌐 Live Demo (GitHub Pages):** https://rakibulai.github.io/ai-prompt-marketplace/

| Page | URL |
|---|---|
| Home | [`/`](https://rakibulai.github.io/ai-prompt-marketplace/) |
| Marketplace | [`/marketplace.html`](https://rakibulai.github.io/ai-prompt-marketplace/marketplace.html) |
| Profile | [`/profile.html`](https://rakibulai.github.io/ai-prompt-marketplace/profile.html) |
| Login | [`/login.html`](https://rakibulai.github.io/ai-prompt-marketplace/login.html) |
| Sign Up | [`/signup.html`](https://rakibulai.github.io/ai-prompt-marketplace/signup.html) |

> **Note:** GitHub Pages serves the `frontend/` directory as the site root via the `Deploy to GitHub Pages` workflow (`.github/workflows/pages.yml`). To enable it, go to **Settings → Pages → Source → GitHub Actions**.

---

## 🗂 File Structure

```
AI-Prompt-Marketplace/
├── frontend/
│   ├── index.html              # Landing page (hero carousel, categories, trending)
│   ├── marketplace.html        # Browse/search/filter prompts
│   ├── prompt-detail.html      # Prompt detail, live preview, buy, reviews
│   ├── seller-dashboard.html   # Upload wizard, earnings chart, top prompts
│   ├── profile.html            # Purchases, wishlist, referrals, settings
│   ├── admin-dashboard.html    # Moderation queue, analytics, users
│   ├── login.html              # Auth with JWT
│   ├── signup.html             # Role-based registration
│   ├── css/style.css           # Complete design system (glassy UI)
│   └── js/app.js               # Core JS (auth, cart, API, utils)
├── backend/
│   ├── pom.xml                 # Maven: Spring Boot 3, MySQL, JWT, Stripe
│   └── src/main/java/com/marketplace/
│       └── ALL_BACKEND_FILES.java   # All entities, repos, controllers
├── database/
│   └── schema.sql              # Tables + 10 seed prompts/users
├── docker-compose.yml          # MySQL + Backend + Nginx
└── README.md
```

---

## ⚡ Quick Start

### Prerequisites
- Java 17+, Maven 3.8+, MySQL 8+, Node.js (optional for live-server)

### 1. Database Setup
```bash
mysql -u root -p < database/schema.sql
```

### 2. Backend (Spring Boot)
```bash
cd backend
# Set environment variables
export DB_USER=root
export DB_PASS=yourpassword
export JWT_SECRET=YourSuperSecretJWTKey256BitsLong!!
export STRIPE_SECRET_KEY=sk_test_yourstripekey

mvn spring-boot:run
# API runs on http://localhost:8080
```

### 3. Frontend
```bash
# Option A: VS Code Live Server (recommended)
cd frontend && open index.html

# Option B: Python simple server
cd frontend && python3 -m http.server 5500

# Option C: npx serve
cd frontend && npx serve .
```

### 4. Docker (Full Stack)
```bash
cp .env.example .env    # Fill in your API keys
docker-compose up -d    # Starts MySQL + Backend + Nginx
# Frontend served at http://localhost:80
# API at http://localhost:8080
```

---

## 🔑 Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_USER` | MySQL username | `root` |
| `DB_PASS` | MySQL password | `securepassword` |
| `JWT_SECRET` | JWT signing key (32+ chars) | `PromptVault...` |
| `STRIPE_SECRET_KEY` | Stripe secret key | `sk_test_...` |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook secret | `whsec_...` |
| `PAYPAL_CLIENT_ID` | PayPal client ID | `AaBb...` |
| `PAYPAL_CLIENT_SECRET` | PayPal secret | `Cc...` |

---

## 📡 API Documentation

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login, get JWT | Public |

**Register body:**
```json
{ "name":"Alex Chen", "email":"alex@example.com", "password":"mypassword", "role":"seller" }
```
**Login response:**
```json
{ "token":"eyJhbGci...", "user":{"id":1,"name":"Alex Chen","role":"SELLER"} }
```

### Prompts
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/prompts` | List prompts (search/filter/paginate) | Public |
| GET | `/api/prompts/{id}` | Get prompt detail | Public |
| GET | `/api/prompts/trending` | Top 8 trending | Public |
| GET | `/api/prompts/featured` | Featured prompts | Public |
| POST | `/api/prompts/upload` | Upload new prompt (multipart) | Seller |
| PUT | `/api/prompts/{id}` | Update own prompt | Seller |
| DELETE | `/api/prompts/{id}` | Delete prompt | Seller/Admin |

**GET /api/prompts query params:**
```
?page=0&size=12&search=cold+email&category=Marketing&minPrice=5&maxPrice=20&sortBy=salesCount
```

### Payments
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/payment/create-checkout/{promptId}` | Create Stripe session | Buyer |
| POST | `/api/payment/webhook` | Stripe webhook handler | Stripe |

**Checkout response:**
```json
{ "url":"https://checkout.stripe.com/...", "sessionId":"cs_test_..." }
```

### Reviews
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/reviews/prompt/{promptId}` | Get prompt reviews | Public |
| POST | `/api/reviews` | Submit review | Buyer |

### User
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/user/me` | Get current user | Any |
| GET | `/api/user/stats` | User stats | Any |
| GET | `/api/user/purchases` | Purchase history | Buyer |

### Admin
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/admin/stats` | Platform stats | Admin |
| GET | `/api/admin/pending-prompts` | Moderation queue | Admin |
| PUT | `/api/admin/prompts/{id}/approve` | Approve prompt | Admin |
| PUT | `/api/admin/prompts/{id}/reject` | Reject prompt | Admin |
| GET | `/api/admin/users` | All users | Admin |

---

## 🎨 Design System

### Color Palette
| Name | Hex | Usage |
|---|---|---|
| Navy (Primary BG) | `#0F0F23` | Background |
| Indigo | `#6366F1` | Primary actions, accents |
| Indigo Glow | `#A5B4FC` | Soft accent, text |
| Emerald | `#10B981` | Success, earnings |
| Rose | `#EF4444` | Error, danger |
| Amber | `#F59E0B` | Warnings, highlights |
| Pearl | `#F8FAFC` | Primary text |

### Glass Components
```css
/* Standard glass card */
background: rgba(255,255,255,0.04);
backdrop-filter: blur(20px);
border: 1px solid rgba(255,255,255,0.10);
border-radius: 24px;
```

### Typography
- **Display/Headings:** Syne (800 weight)
- **Body:** DM Sans (400/500/600)

---

## 🔄 Demo User Flow

1. **Signup** → `signup.html` → Select role (buyer/seller) → JWT stored in localStorage
2. **Browse** → `marketplace.html` → Search, filter by category/price/rating
3. **Detail** → `prompt-detail.html` → View live preview, read reviews
4. **Buy** → Click "Buy Now" → Stripe checkout → Webhook confirms → Prompt unlocked
5. **Sell** → `seller-dashboard.html` → Upload wizard (3 steps) → Admin reviews → Published
6. **Admin** → `admin-dashboard.html` → Approve/reject prompts, view analytics

**Test accounts:**
- Admin: `admin@promptvault.io` / `password123`
- Seller: `alex@example.com` / `password123`
- Buyer: `james@example.com` / `password123`

---

## 🚀 Deployment

### GitHub Pages (Frontend — automatic)
The repository includes a GitHub Actions workflow (`.github/workflows/pages.yml`) that automatically deploys the `frontend/` directory to GitHub Pages on every push to `main`.

**One-time setup:**
1. Go to your repo → **Settings → Pages**
2. Under **Build and deployment → Source**, select **GitHub Actions**
3. Push to `main` — the workflow handles the rest

The live site will be available at:
`https://<your-github-username>.github.io/ai-prompt-marketplace/`

### Render.com
```bash
# Backend: New Web Service → Java → Build: mvn clean package → Start: java -jar target/*.jar
# Add env vars in Render dashboard
# MySQL: Add Render MySQL add-on or use PlanetScale (free tier)
```

### Heroku
```bash
heroku create promptvault-app
heroku addons:create cleardb:ignite   # MySQL
heroku config:set STRIPE_SECRET_KEY=sk_live_...
git push heroku main
```

### Vercel (Frontend only)
```bash
# In /frontend directory
vercel --prod
# Update API_BASE in js/app.js to your backend URL
```

---

## 📈 Scaling to $1M — Architecture Upgrades

### Phase 1 (0 → $10K/month)
- ✅ Current stack (Spring Boot + MySQL + Stripe)
- Add Redis caching for trending prompts
- Add Cloudflare CDN for static assets

### Phase 2 ($10K → $100K/month)
```yaml
# Add to docker-compose.yml
redis:
  image: redis:alpine
  ports: ["6379:6379"]
```
- AWS S3 for file storage (replace local uploads)
- Elasticsearch for full-text search
- Email notifications (SendGrid)
- Affiliate/referral tracking

### Phase 3 ($100K → $1M/month)
- Microservices: separate Payment, Notification, Search services
- PostgreSQL migration (better JSON, full-text)
- Kubernetes deployment
- A/B testing (buy button color, pricing display)
- ML recommendations (collaborative filtering)
- Subscription plans (Pro: unlimited purchases, Team: shared library)

---

## 🔒 Security Checklist

- ✅ JWT authentication with RS256
- ✅ BCrypt password hashing (cost factor 10)
- ✅ Input validation (Bean Validation)
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ XSS: Content-Security-Policy headers
- ✅ Rate limiting (add Spring Rate Limiter bucket4j)
- ✅ CORS configuration
- ✅ Stripe webhook signature verification
- ⬜ Add HTTPS (Let's Encrypt via Nginx)
- ⬜ Add API rate limiting (100 req/min)
- ⬜ Add OWASP dependency check in CI/CD

---

## 🌍 Multi-language (i18n)

English/Bengali toggle is built into `index.html`. To add more languages:
```javascript
// In index.html i18n object, add:
ar: { hero_line1: 'افتح أفضل', ... }
// Then add flag button in navbar
```
RTL support: add `dir="rtl"` to html element when BN/AR is active.

---

## 📊 Performance

- Lighthouse target: 95+ (Performance, Accessibility, SEO)
- Lazy loading: Images use `data-src` + IntersectionObserver
- Critical CSS: Inline above-the-fold styles
- Minification: `mvn spring-boot:build-image` for production
- Caching headers: Set via Nginx config for static assets

---

Built with ❤️ — PromptVault Team | © 2026

