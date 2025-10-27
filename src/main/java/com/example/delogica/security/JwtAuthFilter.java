package com.example.delogica.security;

import com.example.delogica.config.errors.ErrorCode;
import com.example.delogica.config.exceptions.JwtAuthenticationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

   @Override
protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request,
                                @SuppressWarnings("null") HttpServletResponse response,
                                @SuppressWarnings("null") FilterChain filterChain)
        throws ServletException, IOException {

    final String header = request.getHeader("Authorization");

    try {
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new JwtAuthenticationException("Token inválido o expirado", ErrorCode.JWT_INVALID);
        }

        final String username = jwtUtil.extractUsername(token);

        var auth = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(username, "", Collections.emptyList()),
                null,
                Collections.emptyList()
        );

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);

    } catch (JwtAuthenticationException ex) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"status\":401,\"code\":\"%s\",\"message\":\"%s\"}",
                ex.getCode().name(),
                ex.getMessage()
        ));
    }
}

}
