package fun.amireux.chat.book.framework.mvc.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("token");

        log.info("Request received: {} {}, Auth present: {}", method, uri, authHeader != null ? "yes" : "no");

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("Request failed: {} {}, Error: {}", method, uri, e.getMessage());
            throw e;
        }
    }
}