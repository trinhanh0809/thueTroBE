package com.example.DATN.service.JWT;

import com.example.DATN.service.UserSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSecurityService userSecurityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            chain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userSecurityService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                // Ưu tiên roles từ JWT (["ADMIN","HOST",...]) -> map "ROLE_*"
                List<String> roles = jwtService.extractRoles(token);

                Collection<? extends GrantedAuthority> authorities =
                        (roles != null && !roles.isEmpty())
                                ? roles.stream()
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                                : userDetails.getAuthorities(); // fallback DB

                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    /** Bỏ qua một số path public để giảm work cho filter (tuỳ chọn) */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getServletPath();
        return p.startsWith("/user/authenticate")
                || p.startsWith("/user/register")
                || p.startsWith("/user/activate")
                || p.startsWith("/v3/api-docs")
                || p.startsWith("/swagger-ui");
    }
}
