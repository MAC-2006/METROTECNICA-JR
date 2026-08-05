package com.metrotecnica.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                String email = jwtService.extractEmail(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtService.isTokenValid(token, email)) {
                        // Carrega o usuário real (confirma que existe/está ativo)...
                        UserDetails baseDetails = userDetailsService.loadUserByUsername(email);

                        // ...mas monta o principal com role/tenant_id do TOKEN, não do banco.
                        // Isso é o que faz o impersonate funcionar: o superadmin autentica
                        // com o e-mail dele, porém "vestindo" o tenant_id da empresa acessada.
                        Long tenantIdDoToken = jwtService.extractTenantId(token);
                        String roleDoToken = jwtService.extractRole(token);



                        UserDetails userDetails = baseDetails;
                        if (baseDetails instanceof UserPrincipal existing) {
                            boolean precisaOverride = !java.util.Objects.equals(existing.getTenantId(), tenantIdDoToken)
                                    || !java.util.Objects.equals(existing.getRole(), roleDoToken);
                            if (precisaOverride) {
                                userDetails = new UserPrincipal(userDetailsService.carregarEntidade(email), roleDoToken, tenantIdDoToken);
                            }
                        }

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Tenta pelo header Authorization: Bearer <token>
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // 2. Fallback: query param ?jwt=... (equivalente ao JWT_TOKEN_LOCATION do Flask, usado nos links de PDF)
        return request.getParameter("jwt");
    }
}