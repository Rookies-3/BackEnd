package com.store.rookiesoneteam.component;

import com.store.rookiesoneteam.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // (1) 이 필터의 검사를 건너뛸 경로 목록을 정의합니다.
    private static final String[] EXCLUDE_PATHS = {"/ws-stomp/**"};

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException
    {
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        String email = null;
        String role = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            try {
                email = jwtTokenProvider.getEmailFromToken(token);
                username = jwtTokenProvider.getUsernameFromToken(token);
                role = jwtTokenProvider.getRoleFromToken(token);
            } catch (Exception e) {
                log.error("JWT Token parsing error: {}", e.getMessage());
            }
        }

        if(username != null && email != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if(jwtTokenProvider.validateToken(token)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    // (2) 요청이 들어올 때마다 이 필터를 적용할지 말지 결정하는 메소드입니다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // (3) 요청 경로가 우리가 정의한 예외 경로 목록에 포함되는지 확인합니다.
        boolean shouldNotFilter = Arrays.stream(EXCLUDE_PATHS)
                .anyMatch(p -> pathMatcher.match(p, path));

        if (shouldNotFilter) {
            log.info("JWT Filter is skipping path: {}", path);
        }

        return shouldNotFilter;
    }
}
