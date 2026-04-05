-- ═══════════════════════════════════════════════════════════
-- PromptVault — Database Schema (MySQL 8)
-- ═══════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS marketplace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE marketplace;

-- USERS
CREATE TABLE users (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  email      VARCHAR(100) UNIQUE NOT NULL,
  password   VARCHAR(255) NOT NULL,
  role       ENUM('buyer','seller','admin') DEFAULT 'buyer',
  balance    DECIMAL(10,2) DEFAULT 0.00,
  avatar_url VARCHAR(255),
  bio        TEXT,
  referral_code VARCHAR(20) UNIQUE,
  referred_by   BIGINT REFERENCES users(id),
  is_verified   BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- PROMPTS
CREATE TABLE prompts (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(255) NOT NULL,
  description  TEXT,
  content      LONGTEXT,
  category     VARCHAR(50),
  ai_model     VARCHAR(50),
  price        DECIMAL(5,2) NOT NULL,
  example_file VARCHAR(255),
  seller_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  approved     BOOLEAN DEFAULT FALSE,
  featured     BOOLEAN DEFAULT FALSE,
  sales_count  INT DEFAULT 0,
  view_count   INT DEFAULT 0,
  tags         VARCHAR(500),
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (category),
  INDEX idx_seller (seller_id),
  INDEX idx_approved (approved),
  INDEX idx_price (price),
  INDEX idx_sales (sales_count DESC)
);

-- TRANSACTIONS
CREATE TABLE transactions (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  buyer_id       BIGINT NOT NULL REFERENCES users(id),
  prompt_id      BIGINT NOT NULL REFERENCES prompts(id),
  seller_id      BIGINT NOT NULL REFERENCES users(id),
  amount         DECIMAL(5,2) NOT NULL,
  seller_earnings DECIMAL(5,2),
  platform_fee   DECIMAL(5,2),
  payment_method ENUM('stripe','paypal','wallet') DEFAULT 'stripe',
  payment_status ENUM('pending','completed','failed','refunded') DEFAULT 'pending',
  stripe_session_id VARCHAR(255),
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_buyer (buyer_id),
  INDEX idx_seller_txn (seller_id),
  INDEX idx_status (payment_status)
);

-- REVIEWS
CREATE TABLE reviews (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  prompt_id  BIGINT NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
  rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_review (user_id, prompt_id),
  INDEX idx_prompt_reviews (prompt_id)
);

-- WISHLIST
CREATE TABLE wishlists (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  prompt_id  BIGINT NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_wishlist (user_id, prompt_id)
);

-- REFERRALS
CREATE TABLE referrals (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  referrer_id   BIGINT NOT NULL REFERENCES users(id),
  referred_id   BIGINT NOT NULL REFERENCES users(id),
  bonus_paid    BOOLEAN DEFAULT FALSE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_referral (referrer_id, referred_id)
);

-- SUBSCRIPTIONS (Growth feature)
CREATE TABLE subscriptions (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT UNIQUE NOT NULL REFERENCES users(id),
  plan        ENUM('free','pro','team') DEFAULT 'free',
  started_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at  TIMESTAMP,
  stripe_sub_id VARCHAR(255)
);

-- ═══════════════════════════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════════════════════════

INSERT INTO users (name, email, password, role, balance, referral_code, is_verified) VALUES
('Alex Chen',    'alex@example.com',   '$2a$10$hashedpassword1', 'seller', 8420.00, 'ALEX2024', TRUE),
('Priya Sharma', 'priya@example.com',  '$2a$10$hashedpassword2', 'seller', 6100.00, 'PRIYA001', TRUE),
('Marcus Webb',  'marcus@example.com', '$2a$10$hashedpassword3', 'seller', 12200.00,'MARC001', TRUE),
('Yuki Tanaka',  'yuki@example.com',   '$2a$10$hashedpassword4', 'seller', 4900.00, 'YUKI001', TRUE),
('Sofia Reyes',  'sofia@example.com',  '$2a$10$hashedpassword5', 'seller', 7300.00, 'SOFI001', TRUE),
('James Miller', 'james@example.com',  '$2a$10$hashedpassword6', 'buyer',  25.00,   'JAME001', TRUE),
('Tom Lewis',    'tom@example.com',    '$2a$10$hashedpassword7', 'buyer',  10.00,   'TOML001', FALSE),
('Admin User',   'admin@promptvault.io','$2a$10$hashedpassword8','admin',  0.00,    'ADMN001', TRUE);

INSERT INTO prompts (title, description, content, category, ai_model, price, seller_id, approved, featured, sales_count, tags) VALUES
('Cold Email Generator',
 'Generate hyper-personalized cold emails that convert at 3x the industry average.',
 'You are an expert cold email copywriter. Write a personalized cold email to [NAME] at [COMPANY] about [PRODUCT] focusing on [PAIN_POINT]. Requirements: subject with curiosity gap, personalized opening, PAS structure, 1-line social proof, single CTA, under 120 words.',
 'Marketing', 'GPT-4', 9.99, 1, TRUE, TRUE, 247, 'cold email,b2b,sales,outreach,marketing'),

('Cinematic Portrait Master',
 'Create stunning cinematic portraits with Hollywood-grade lighting and depth.',
 'Cinematic portrait of [SUBJECT], golden hour lighting, f/1.4 bokeh, 8K ultra-realistic, award-winning photography, dramatic rim lighting, film grain, Kodak Portra 400 color grade, shallow depth of field, professional headshot --ar 3:4 --v 6',
 'Art', 'Midjourney', 14.99, 4, TRUE, TRUE, 189, 'midjourney,portrait,cinematic,photography,art'),

('Full-Stack Dev Assistant',
 'Architect, code, and debug full-stack apps 10x faster with structured system prompts.',
 'You are a senior full-stack engineer with 15+ years experience. For this task: 1) Analyze requirements, 2) Design DB schema, 3) Write clean [FRONTEND] + [BACKEND] code, 4) Add comprehensive error handling, 5) Write tests. Tech stack: [STACK]. Task: [TASK]',
 'Coding', 'Claude', 19.99, 1, TRUE, FALSE, 94, 'coding,full-stack,react,node,development'),

('Viral Twitter Thread Writer',
 'Turn any idea into a viral 12-tweet thread with irresistible hooks and CTAs.',
 'Write a viral 12-tweet Twitter thread about [TOPIC] for [AUDIENCE]. Thread structure: Tweet 1: Hook (curiosity gap + bold claim), Tweets 2-10: Value (story/data/insight per tweet), Tweet 11: Controversial take, Tweet 12: CTA + ask for RT. Voice: [TONE]. Use emojis, numbers, line breaks.',
 'Marketing', 'GPT-4', 7.99, 5, TRUE, TRUE, 312, 'twitter,viral,thread,content,social media'),

('Startup Pitch Deck Builder',
 'Generate investor-ready pitch decks with structured storytelling and compelling metrics.',
 'Create a compelling 12-slide investor pitch deck for [STARTUP_NAME] in [INDUSTRY]. Slides: 1-Problem, 2-Solution, 3-Market Size, 4-Product Demo, 5-Business Model, 6-Traction, 7-Competition, 8-Go-to-Market, 9-Team, 10-Financials, 11-Ask, 12-Vision. Include: statistics, narrative hooks, investor objection handlers.',
 'Business', 'GPT-4', 12.99, 3, TRUE, TRUE, 156, 'startup,pitch deck,investor,business,fundraising'),

('SEO Blog Writer Pro',
 'Write SEO-optimized blog posts that rank on Google page 1 in 30 days.',
 'Write a comprehensive, SEO-optimized blog post about [KEYWORD]. Requirements: H1 includes keyword, 1500+ words, H2/H3 structure, meta description (155 chars), LSI keywords naturally placed, E-E-A-T signals, FAQ section targeting "People Also Ask", internal linking suggestions, compelling CTA. Tone: [TONE]. Audience: [AUDIENCE].',
 'Writing', 'ChatGPT', 8.99, 2, TRUE, FALSE, 203, 'seo,blog,content,writing,google'),

('YouTube Script Creator',
 'Create engaging YouTube scripts with viral hooks, retention loops, and subscriber CTAs.',
 'Write a compelling YouTube script for a [LENGTH]-minute video about [TOPIC] targeting [AUDIENCE]. Structure: Hook (0-30s: pattern interrupt), Intro (30-60s: promise + credibility), Main Content (chapters with retention hooks every 2 min), Midroll CTA, Outro CTA. Include: [B-roll suggestions], thumbnail text idea, title options (CTR-optimized).',
 'Marketing', 'GPT-4', 11.99, 3, TRUE, FALSE, 118, 'youtube,script,video,content creation'),

('Product Description Wizard',
 'Convert-ready product descriptions for e-commerce that boost add-to-cart by 40%.',
 'Write 3 product description variants for [PRODUCT_NAME] (Price: [PRICE]) targeting [CUSTOMER_AVATAR]. Include: A) Short (80 words, punchy benefits), B) Long (200 words, storytelling + features + benefits + social proof), C) Bullet format (5 key features + 2 guarantees). Emotional triggers: [TRIGGERS]. SEO keyword: [KEYWORD].',
 'Business', 'ChatGPT', 6.99, 2, TRUE, FALSE, 164, 'ecommerce,product,shopify,amazon,copy'),

('Anime Character Designer',
 'Professional anime character sheets with multiple poses and consistent art style.',
 'Create an anime character design sheet for [CHARACTER_NAME], [AGE], [PERSONALITY]. Art style: [STYLE] (e.g., Makoto Shinkai, Studio Ghibli). Poses: front view, 3/4 view, back view, action pose, expression sheet (happy, sad, angry, surprised). Color palette: [COLORS]. Outfit: [OUTFIT]. --ar 1:1 --niji 6 --style expressive',
 'Art', 'Midjourney', 9.99, 4, TRUE, FALSE, 231, 'midjourney,anime,character design,art'),

('LinkedIn Post Optimizer',
 'Craft LinkedIn posts that triple your engagement rate with proven formats.',
 'Rewrite this LinkedIn post for maximum engagement: [ORIGINAL_POST]. Optimization rules: 1) Hook: Bold statement or question (no emojis first line), 2) Body: Short paragraphs (1-2 lines), 3) White space: Every 2-3 lines, 4) Story arc: Problem→Journey→Lesson, 5) CTA: Specific question to drive comments, 6) Max 1300 chars. Tone: [TONE]. Niche: [NICHE].',
 'Marketing', 'ChatGPT', 5.99, 5, TRUE, FALSE, 87, 'linkedin,social media,b2b,personal brand');

-- Sample Transactions
INSERT INTO transactions (buyer_id, prompt_id, seller_id, amount, seller_earnings, platform_fee, payment_method, payment_status) VALUES
(6, 1, 1, 9.99, 7.99, 2.00, 'stripe', 'completed'),
(6, 4, 5, 7.99, 6.39, 1.60, 'paypal', 'completed'),
(7, 2, 4, 14.99, 11.99, 3.00, 'stripe', 'completed'),
(6, 5, 3, 12.99, 10.39, 2.60, 'wallet', 'completed');

-- Sample Reviews
INSERT INTO reviews (user_id, prompt_id, rating, comment) VALUES
(6, 1, 5, 'This prompt is insane! My cold email reply rate jumped from 3% to 12%.'),
(7, 2, 5, 'Stunning results every time. The lighting suggestions are spot on.'),
(6, 4, 5, 'Booked 3 meetings in my first day using this. Highly recommend.'),
(7, 5, 4, 'Really solid pitch deck framework. Would love more financial templates.');

-- Sample Wishlist
INSERT INTO wishlists (user_id, prompt_id) VALUES (6, 3), (6, 7), (7, 1);
  
