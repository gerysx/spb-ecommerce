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

/**
 * Filtro de autenticación JWT que se ejecuta una vez por cada solicitud HTTP.
 * <p>
 * Valida el token JWT presente en el encabezado <code>Authorization</code>,
 * establece el contexto de seguridad si el token es válido,
 * o devuelve un error 401 en caso de token inválido o ausente.
 * </p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Constructor que inyecta la utilidad JWT.
     *
     * @param jwtUtil Componente de utilidad para validación y análisis de tokens JWT.
     */
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Intercepta cada solicitud entrante para validar el token JWT.
     * <p>
     * Si el encabezado <code>Authorization</code> contiene un token válido,
     * se establece la autenticación en el contexto de seguridad.
     * En caso contrario, se responde con un estado HTTP 401.
     * </p>
     *
     * @param request      Petición HTTP entrante.
     * @param response     Respuesta HTTP saliente.
     * @param filterChain  Cadena de filtros que continúa el procesamiento.
     * @throws ServletException Si ocurre un error en el procesamiento del filtro.
     * @throws IOException      Si ocurre un error de lectura o escritura en la respuesta.
     */
    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request,
                                   @SuppressWarnings("null") HttpServletResponse response,
                                   @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");

        try {
            // Si no hay encabezado o no empieza con "Bearer ", continúa sin autenticar.
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = header.substring(7);

            // Valida el token JWT.
            if (!jwtUtil.isTokenValid(token)) {
                throw new JwtAuthenticationException("Token inválido o expirado", ErrorCode.JWT_INVALID);
            }

            // Extrae el nombre de usuario del token.
            final String username = jwtUtil.extractUsername(token);

            // Crea una autenticación basada en el usuario extraído del token.
            var auth = new UsernamePasswordAuthenticationToken(
                    new org.springframework.security.core.userdetails.User(username, "", Collections.emptyList()),
                    null,
                    Collections.emptyList());

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Establece la autenticación en el contexto de seguridad.
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Continúa la cadena de filtros.
            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException ex) {
            // Responde con error 401 y cuerpo JSON en caso de token inválido.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"status\":401,\"code\":\"%s\",\"message\":\"%s\"}",
                    ex.getCode().name(),
                    ex.getMessage()));
        }
    }
}
