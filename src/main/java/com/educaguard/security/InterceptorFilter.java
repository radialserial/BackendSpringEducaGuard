package com.educaguard.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.educaguard.domain.services.UserService;
import com.educaguard.security.jwt.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class InterceptorFilter extends OncePerRequestFilter {

    @Autowired
    private UserService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = null;
        try {
            authentication = getAuthentication(request);

            if (authentication != null) {
                // Token válido. Se o meu token for válido, eu passo a requisição para frente indicando que a requisição esta autenticada.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // toke inválido
                response.setStatus(400);
                response.setContentType("application/json");
            }

        } catch (Exception e) {
            // toke inválido
            response.setStatus(400);
            response.setContentType("application/json");
        }

        // Passa a requisição para a frente
        filterChain.doFilter(request, response);

    }

    public Authentication getAuthentication(HttpServletRequest request) throws Exception {
        var token = request.getHeader("Authorization");

        if (token != null && !token.isEmpty()) {
            token = token.replace("Bearer ", "");

            DecodedJWT decode = JwtToken.decodeTokenJWT(token);

            if (decode != null) {
                // token verificado e descriptografado
                Date validate = decode.getExpiresAt();
                var username_subject = decode.getSubject();

                if (validate.after(new Date(System.currentTimeMillis()))) {
                    UserDetails user = service.findUserByUsername(username_subject);
                    // caso a requisição tenha o cabeçalho correto, gero um "token interno"
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username_subject, null, user.getAuthorities());
                    return authenticationToken;
                } else {
                    return null;
                }

            } else // decode null
            {
                return null;
            }

        } else // token null
        {
            return null;
        }

    }
}
