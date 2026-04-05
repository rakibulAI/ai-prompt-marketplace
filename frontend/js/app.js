/**
 * PromptVault — Core Application JS
 * Handles: Auth state, Cart persistence, Theme, API calls, Utilities
 */

// ═══════════════════════════════════════════════════════════
// CONFIG
// ═══════════════════════════════════════════════════════════
const API_BASE = 'http://localhost:8080/api';
const STRIPE_KEY = 'pk_test_51OqXXXXXXXXXXXXXXXXXXXXX'; // Replace with real key

// ═══════════════════════════════════════════════════════════
// AUTH UTILITIES
// ═══════════════════════════════════════════════════════════
const Auth = {
  getToken: () => localStorage.getItem('pv_token'),
  getUser: () => JSON.parse(localStorage.getItem('pv_user') || 'null'),
  isLoggedIn: () => !!localStorage.getItem('pv_token'),
  logout: () => {
    localStorage.removeItem('pv_token');
    localStorage.removeItem('pv_user');
    window.location.href = 'login.html';
  },
  headers: () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${Auth.getToken()}`
  }),

  updateNavForUser() {
    const user = Auth.getUser();
    const authButtons = document.getElementById('authButtons');
    if (!authButtons) return;
    if (user && Auth.isLoggedIn()) {
      authButtons.innerHTML = `
        <a href="profile.html" class="flex items-center gap-2 glass-btn text-sm px-3 py-2 rounded-xl font-medium">
          <span class="w-6 h-6 rounded-lg bg-gradient-to-br from-[#6366F1] to-[#7C3AED] flex items-center justify-center text-xs">
            ${user.name ? user.name[0].toUpperCase() : '?'}
          </span>
          <span>${user.name || 'Account'}</span>
        </a>
        ${user.role === 'seller' || user.role === 'admin' ?
          `<a href="${user.role === 'admin' ? 'admin-dashboard.html' : 'seller-dashboard.html'}" class="btn-primary text-sm px-4 py-2 rounded-xl font-semibold">
            ${user.role === 'admin' ? '🛡️ Admin' : '📊 Dashboard'}
          </a>` :
          `<a href="seller-dashboard.html" class="btn-primary text-sm px-4 py-2 rounded-xl font-semibold">Sell Prompts</a>`
        }
      `;
    }
  }
};

// ═══════════════════════════════════════════════════════════
// API CLIENT
// ═══════════════════════════════════════════════════════════
const API = {
  async get(endpoint) {
    try {
      const res = await fetch(`${API_BASE}${endpoint}`, { headers: Auth.headers() });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (e) {
      console.warn(`API GET ${endpoint} failed (using mock data):`, e.message);
      return null;
    }
  },

  async post(endpoint, body) {
    try {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: Auth.headers(),
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (e) {
      console.warn(`API POST ${endpoint} failed:`, e.message);
      return null;
    }
  },

  // Auth endpoints
  async register(data) { return this.post('/auth/register', data); },
  async login(data) { return this.post('/auth/login', data); },

  // Prompt endpoints
  async getPrompts(params = {}) {
    const q = new URLSearchParams(params).toString();
    return this.get(`/prompts${q ? '?' + q : ''}`);
  },
  async getTrending() { return this.get('/prompts/trending'); },
  async getPrompt(id) { return this.get(`/prompts/${id}`); },
  async uploadPrompt(formData) {
    try {
      const res = await fetch(`${API_BASE}/prompts/upload`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${Auth.getToken()}` },
        body: formData
      });
      return await res.json();
    } catch (e) { return null; }
  },

  // Payment
  async createCheckout(promptId) { return this.post(`/payment/create-checkout/${promptId}`, {}); },
  async getUserStats() { return this.get('/user/stats'); },
};

// ═══════════════════════════════════════════════════════════
// CART MANAGER
// ═══════════════════════════════════════════════════════════
const Cart = {
  STORAGE_KEY: 'pv_cart',

  get() { return JSON.parse(localStorage.getItem(this.STORAGE_KEY) || '[]'); },
  save(items) { localStorage.setItem(this.STORAGE_KEY, JSON.stringify(items)); },

  add(item) {
    const cart = this.get();
    if (cart.find(i => i.id === item.id)) {
      showToast('Already in cart!');
      return false;
    }
    cart.push(item);
    this.save(cart);
    this.updateBadge();
    return true;
  },

  remove(id) {
    const cart = this.get().filter(i => i.id !== id);
    this.save(cart);
    this.updateBadge();
  },

  clear() { this.save([]); this.updateBadge(); },

  total() { return this.get().reduce((sum, i) => sum + parseFloat(i.price), 0).toFixed(2); },

  count() { return this.get().length; },

  updateBadge() {
    const badge = document.getElementById('cartCount');
    if (!badge) return;
    const count = this.count();
    badge.textContent = count;
    badge.classList.toggle('hidden', count === 0);
  }
};

// ═══════════════════════════════════════════════════════════
// THEME MANAGER
// ═══════════════════════════════════════════════════════════
function toggleTheme() {
  const html = document.documentElement;
  const isDark = html.getAttribute('data-theme') === 'dark';
  html.setAttribute('data-theme', isDark ? 'light' : 'dark');
  localStorage.setItem('pv_theme', isDark ? 'light' : 'dark');
}

function initTheme() {
  const saved = localStorage.getItem('pv_theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
}

// ═══════════════════════════════════════════════════════════
// TOAST
// ═══════════════════════════════════════════════════════════
function showToast(msg, type = 'success') {
  let toast = document.getElementById('toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toast';
    toast.className = 'toast hidden';
    toast.innerHTML = '<span id="toastMsg"></span>';
    document.body.appendChild(toast);
  }
  document.getElementById('toastMsg').textContent = msg;
  toast.className = `toast ${type}`;
  toast.classList.remove('hidden');
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.add('hidden'), 3500);
}

// ═══════════════════════════════════════════════════════════
// SMOOTH SCROLL
// ═══════════════════════════════════════════════════════════
function initSmoothScroll() {
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', (e) => {
      const target = document.querySelector(anchor.getAttribute('href'));
      if (target) {
        e.preventDefault();
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });
}

// ═══════════════════════════════════════════════════════════
// KEYBOARD SHORTCUTS
// ═══════════════════════════════════════════════════════════
function initKeyboardShortcuts() {
  document.addEventListener('keydown', (e) => {
    // Cmd/Ctrl+K → focus search
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
      e.preventDefault();
      const searchInput = document.getElementById('searchInput');
      if (searchInput) { searchInput.focus(); searchInput.select(); }
    }
    // Escape → close modals
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-overlay:not(.hidden)').forEach(m => m.classList.add('hidden'));
      const cartDrawer = document.getElementById('cartDrawer');
      if (cartDrawer?.classList.contains('open')) {
        cartDrawer.classList.remove('open');
        document.getElementById('cartOverlay')?.classList.add('hidden');
      }
    }
  });
}

// ═══════════════════════════════════════════════════════════
// INTERSECTION OBSERVER (lazy animations)
// ═══════════════════════════════════════════════════════════
function initScrollAnimations() {
  if (!('IntersectionObserver' in window)) return;
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.08, rootMargin: '0px 0px -40px 0px' });

  const selectors = ['.prompt-card', '.category-card', '.seller-card', '.how-step', '.stat-card', '.glass-card'];
  document.querySelectorAll(selectors.join(',')).forEach(el => {
    if (!el.classList.contains('visible')) observer.observe(el);
  });
}

// ═══════════════════════════════════════════════════════════
// PAYMENT HELPERS
// ═══════════════════════════════════════════════════════════
async function initiatePayment(promptId, price, title) {
  if (!Auth.isLoggedIn()) {
    showToast('Please log in to purchase', 'error');
    setTimeout(() => window.location.href = 'login.html', 1000);
    return;
  }

  // Try real API first
  const session = await API.createCheckout(promptId);
  if (session?.url) {
    window.location.href = session.url; // Redirect to Stripe
    return;
  }

  // Mock flow
  showToast(`🚀 Processing payment for "${ title }" — $${price}`);
  setTimeout(() => {
    showToast('✅ Purchase successful! Prompt unlocked.');
    // Track purchase
    const purchases = JSON.parse(localStorage.getItem('pv_purchases') || '[]');
    purchases.push({ id: promptId, title, price, date: new Date().toISOString() });
    localStorage.setItem('pv_purchases', JSON.stringify(purchases));
  }, 2000);
}

// ═══════════════════════════════════════════════════════════
// GLOBAL addToCart (used across pages)
// ═══════════════════════════════════════════════════════════
function addToCart(id, title, price) {
  const added = Cart.add({ id, title, price });
  if (added) {
    showToast(`✓ "${title}" added to cart`);
    // Open cart drawer if on index page
    const drawer = document.getElementById('cartDrawer');
    if (drawer) {
      drawer.classList.add('open');
      document.getElementById('cartOverlay')?.classList.remove('hidden');
      renderCartDrawer();
    }
  }
}

function renderCartDrawer() {
  const items = Cart.get();
  const cartItemsEl = document.getElementById('cartItems');
  const cartFooter = document.getElementById('cartFooter');
  if (!cartItemsEl) return;

  if (!items.length) {
    cartItemsEl.innerHTML = `
      <div class="text-center py-16 text-slate-500">
        <div class="text-5xl mb-4">🛒</div>
        <div class="font-semibold">Your cart is empty</div>
        <a href="marketplace.html" class="text-[#6366F1] text-sm mt-2 block">Browse prompts →</a>
      </div>`;
    if (cartFooter) cartFooter.style.display = 'none';
  } else {
    cartItemsEl.innerHTML = items.map((item, i) => `
      <div class="cart-item">
        <div class="w-9 h-9 rounded-xl bg-[#6366F1]/15 flex items-center justify-center flex-shrink-0">⚡</div>
        <div class="flex-1 min-w-0">
          <div class="font-medium text-sm truncate">${item.title}</div>
          <div class="text-[#6366F1] font-bold text-sm">$${item.price}</div>
        </div>
        <button onclick="removeCartItem(${item.id})" class="text-slate-500 hover:text-rose-400 transition-colors text-lg ml-2">✕</button>
      </div>`).join('');
    const total = Cart.total();
    const totalEl = document.getElementById('cartTotal');
    if (totalEl) totalEl.textContent = `$${total}`;
    if (cartFooter) cartFooter.style.display = 'block';
  }
}

function removeCartItem(id) {
  Cart.remove(id);
  renderCartDrawer();
  showToast('Item removed from cart');
}

// ═══════════════════════════════════════════════════════════
// SEARCH SUGGESTIONS
// ═══════════════════════════════════════════════════════════
function initSearchSuggestions() {
  const input = document.getElementById('searchInput');
  if (!input) return;
  const suggestions = ['cold email', 'midjourney portrait', 'react component', 'twitter thread', 'startup pitch', 'blog writer', 'youtube script', 'product description', 'linkedin post', 'business plan'];
  const box = document.createElement('div');
  box.className = 'absolute top-full left-0 right-0 mt-2 glass-card rounded-2xl p-2 z-50 hidden';
  box.id = 'searchSuggestions';
  input.parentElement.style.position = 'relative';
  input.parentElement.appendChild(box);

  input.addEventListener('focus', () => { if (!input.value) showSuggestions(suggestions, box, input); });
  input.addEventListener('input', () => {
    const val = input.value.toLowerCase();
    const filtered = suggestions.filter(s => s.includes(val));
    if (val && filtered.length) showSuggestions(filtered, box, input);
    else box.classList.add('hidden');
  });
  document.addEventListener('click', e => { if (!input.contains(e.target)) box.classList.add('hidden'); });
}

function showSuggestions(list, box, input) {
  box.innerHTML = list.slice(0, 5).map(s => `
    <button class="w-full text-left px-3 py-2 text-sm text-slate-300 hover:text-white hover:bg-white/05 rounded-lg transition-colors flex items-center gap-2"
      onclick="document.getElementById('searchInput').value='${s}';document.getElementById('searchSuggestions').classList.add('hidden');if(typeof applyFilters==='function')applyFilters()">
      🔍 ${s}
    </button>`).join('');
  box.classList.remove('hidden');
}

// ═══════════════════════════════════════════════════════════
// MOBILE SIDEBAR
// ═══════════════════════════════════════════════════════════
function toggleMobileSidebar() {
  const sidebar = document.querySelector('aside');
  if (sidebar) sidebar.classList.toggle('hidden');
}

function toggleMobileFilters() {
  showToast('Filter panel toggled (mobile)');
}

// ═══════════════════════════════════════════════════════════
// FORM VALIDATORS
// ═══════════════════════════════════════════════════════════
const Validate = {
  email: (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v),
  password: (v) => v.length >= 8,
  notEmpty: (v) => v.trim().length > 0,
  price: (v) => v >= 2 && v <= 20,
};

// ═══════════════════════════════════════════════════════════
// PERFORMANCE: Lazy Image Loading
// ═══════════════════════════════════════════════════════════
function initLazyImages() {
  if (!('IntersectionObserver' in window)) return;
  const imgObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        if (img.dataset.src) { img.src = img.dataset.src; img.removeAttribute('data-src'); }
        imgObserver.unobserve(img);
      }
    });
  });
  document.querySelectorAll('img[data-src]').forEach(img => imgObserver.observe(img));
}

// ═══════════════════════════════════════════════════════════
// GLOBAL INIT
// ═══════════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  Auth.updateNavForUser();
  Cart.updateBadge();
  initSmoothScroll();
  initKeyboardShortcuts();
  initScrollAnimations();
  initLazyImages();
  initSearchSuggestions();

  // Navbar scroll effect
  window.addEventListener('scroll', () => {
    const navbar = document.getElementById('navbar');
    if (navbar) navbar.classList.toggle('scrolled', window.scrollY > 50);
  });

  // Render cart drawer on open
  const cartDrawer = document.getElementById('cartDrawer');
  if (cartDrawer) {
    const mutObs = new MutationObserver(() => {
      if (cartDrawer.classList.contains('open')) renderCartDrawer();
    });
    mutObs.observe(cartDrawer, { attributes: true, attributeFilter: ['class'] });
  }

  console.log('%cPromptVault 🚀', 'font-size:16px;color:#6366F1;font-weight:bold');
  console.log('%cBuilt with ❤️ — AI Prompt Marketplace', 'color:#94A3B8');
});
