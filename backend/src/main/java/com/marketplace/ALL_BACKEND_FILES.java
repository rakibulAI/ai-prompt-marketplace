// ════════════════════════════════════════════════════════════
// FILE: MarketplaceApplication.java
// ════════════════════════════════════════════════════════════
package com.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarketplaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}

// ════════════════════════════════════════════════════════════
// FILE: model/User.java
// ════════════════════════════════════════════════════════════
package com.marketplace.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.BUYER;

    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    private String avatarUrl;

    @Column(unique = true, length = 20)
    private String referralCode;

    private boolean isVerified = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { BUYER, SELLER, ADMIN }
}

// ════════════════════════════════════════════════════════════
// FILE: model/Prompt.java
// ════════════════════════════════════════════════════════════
package com.marketplace.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "prompts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Prompt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String aiModel;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal price;

    private String exampleFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    private boolean approved = false;
    private boolean featured = false;
    private int salesCount = 0;
    private int viewCount = 0;
    private String tags;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}

// ════════════════════════════════════════════════════════════
// FILE: model/Transaction.java
// ════════════════════════════════════════════════════════════
package com.marketplace.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "transactions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "prompt_id")
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id")
    private User seller;

    @Column(precision = 5, scale = 2)
    private BigDecimal amount;

    @Column(precision = 5, scale = 2)
    private BigDecimal sellerEarnings;

    @Column(precision = 5, scale = 2)
    private BigDecimal platformFee;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.STRIPE;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String stripeSessionId;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMethod { STRIPE, PAYPAL, WALLET }
    public enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
}

// ════════════════════════════════════════════════════════════
// FILE: model/Review.java
// ════════════════════════════════════════════════════════════
package com.marketplace.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","prompt_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "prompt_id")
    private Prompt prompt;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

// ════════════════════════════════════════════════════════════
// FILE: repository/UserRepository.java
// ════════════════════════════════════════════════════════════
package com.marketplace.repository;

import com.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByReferralCode(String code);
}

// ════════════════════════════════════════════════════════════
// FILE: repository/PromptRepository.java
// ════════════════════════════════════════════════════════════
package com.marketplace.repository;

import com.marketplace.model.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    Page<Prompt> findByApprovedTrue(Pageable pageable);
    Page<Prompt> findByCategoryAndApprovedTrue(String category, Pageable pageable);
    Page<Prompt> findBySellerId(Long sellerId, Pageable pageable);
    List<Prompt> findByApprovedFalseOrderByCreatedAtDesc();

    @Query("SELECT p FROM Prompt p WHERE p.approved = true AND " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Prompt> searchPrompts(@Param("search") String search,
                               @Param("category") String category,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               Pageable pageable);

    @Query("SELECT p FROM Prompt p WHERE p.approved = true ORDER BY p.salesCount DESC")
    List<Prompt> findTrending(Pageable pageable);

    @Query("SELECT p FROM Prompt p WHERE p.featured = true AND p.approved = true ORDER BY p.createdAt DESC")
    List<Prompt> findFeatured();
}

// ════════════════════════════════════════════════════════════
// FILE: repository/TransactionRepository.java
// ════════════════════════════════════════════════════════════
package com.marketplace.repository;

import com.marketplace.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Transaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    Optional<Transaction> findByStripeSessionId(String sessionId);
    boolean existsByBuyerIdAndPromptId(Long buyerId, Long promptId);

    @Query("SELECT COALESCE(SUM(t.sellerEarnings),0) FROM Transaction t WHERE t.seller.id = :sellerId AND t.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalEarnings(@Param("sellerId") Long sellerId);
}

// ════════════════════════════════════════════════════════════
// FILE: repository/ReviewRepository.java
// ════════════════════════════════════════════════════════════
package com.marketplace.repository;

import com.marketplace.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPromptIdOrderByCreatedAtDesc(Long promptId);
    boolean existsByUserIdAndPromptId(Long userId, Long promptId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.prompt.id = :promptId")
    Double getAverageRating(@Param("promptId") Long promptId);
}

// ════════════════════════════════════════════════════════════
// FILE: security/JwtUtil.java
// ════════════════════════════════════════════════════════════
package com.marketplace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    private Key getKey() { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) { return false; }
    }
}

// ════════════════════════════════════════════════════════════
// FILE: security/SecurityConfig.java
// ════════════════════════════════════════════════════════════
package com.marketplace.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/prompts", "/api/prompts/**").permitAll()
                .requestMatchers("/api/payment/webhook").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/seller/**").hasAnyRole("SELLER", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

// ════════════════════════════════════════════════════════════
// FILE: security/JwtAuthFilter.java
// ════════════════════════════════════════════════════════════
package com.marketplace.security;

import com.marketplace.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component @RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                userRepository.findByEmail(email).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                        user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        chain.doFilter(req, res);
    }
}

// ════════════════════════════════════════════════════════════
// FILE: controller/AuthController.java
// ════════════════════════════════════════════════════════════
package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.repository.UserRepository;
import com.marketplace.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByEmail(req.email()))
            return ResponseEntity.badRequest().body(Map.of("error","Email already in use"));
        User user = User.builder()
            .name(req.name()).email(req.email())
            .password(encoder.encode(req.password()))
            .role(User.Role.valueOf(req.role().toUpperCase()))
            .referralCode(UUID.randomUUID().toString().substring(0,8).toUpperCase())
            .build();
        userRepo.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "user", Map.of("id",user.getId(),"name",user.getName(),"role",user.getRole())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return userRepo.findByEmail(req.email())
            .filter(u -> encoder.matches(req.password(), u.getPassword()))
            .map(u -> {
                String token = jwtUtil.generateToken(u.getEmail(), u.getRole().name());
                return ResponseEntity.ok(Map.of("token", token, "user",
                    Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"role",u.getRole(),"balance",u.getBalance())));
            })
            .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error","Invalid credentials")));
    }

    record RegisterRequest(@NotBlank String name, @Email @NotBlank String email,
                           @Size(min=8) String password, @NotBlank String role) {}
    record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
}

// ════════════════════════════════════════════════════════════
// FILE: controller/PromptController.java
// ════════════════════════════════════════════════════════════
package com.marketplace.controller;

import com.marketplace.model.*;
import com.marketplace.repository.*;
import com.marketplace.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.*;

@RestController @RequestMapping("/api/prompts") @RequiredArgsConstructor
public class PromptController {
    private final PromptRepository promptRepo;
    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<?> getPrompts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @Requ
