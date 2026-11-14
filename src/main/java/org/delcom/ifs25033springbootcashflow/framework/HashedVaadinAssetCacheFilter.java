package org.delcom.ifs25033springbootcashflow.framework;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class HashedVaadinAssetCacheFilter extends OncePerRequestFilter {
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String IMMUTABLE_CACHE_POLICY = "public, max-age=31536000, immutable";
    private static final String NO_STORE_CACHE_POLICY = "no-store";
    private static final Pattern HASHED_VAADIN_BUILD_ASSET =
            Pattern.compile("/VAADIN/build/.+-[A-Za-z0-9_-]{8,}\\.(?:js|css)");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        return !HASHED_VAADIN_BUILD_ASSET.matcher(requestPath).matches();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                if (CACHE_CONTROL.equalsIgnoreCase(name)) {
                    super.setHeader(name, cachePolicy());
                } else {
                    super.setHeader(name, value);
                }
            }

            @Override
            public void addHeader(String name, String value) {
                if (CACHE_CONTROL.equalsIgnoreCase(name)) {
                    super.setHeader(name, cachePolicy());
                } else {
                    super.addHeader(name, value);
                }
            }

            @Override
            public void setStatus(int status) {
                super.setStatus(status);
                if (status != HttpServletResponse.SC_OK && status != HttpServletResponse.SC_NOT_MODIFIED) {
                    super.setHeader(CACHE_CONTROL, NO_STORE_CACHE_POLICY);
                }
            }

            @Override
            public void sendError(int status) throws IOException {
                super.setHeader(CACHE_CONTROL, NO_STORE_CACHE_POLICY);
                super.sendError(status);
            }

            @Override
            public void sendError(int status, String message) throws IOException {
                super.setHeader(CACHE_CONTROL, NO_STORE_CACHE_POLICY);
                super.sendError(status, message);
            }

            @Override
            public void sendRedirect(String location) throws IOException {
                super.setHeader(CACHE_CONTROL, NO_STORE_CACHE_POLICY);
                super.sendRedirect(location);
            }

            private String cachePolicy() {
                int status = getStatus();
                return status == HttpServletResponse.SC_OK || status == HttpServletResponse.SC_NOT_MODIFIED
                        ? IMMUTABLE_CACHE_POLICY : NO_STORE_CACHE_POLICY;
            }
        };
        filterChain.doFilter(request, wrappedResponse);
    }
}
