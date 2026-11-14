package org.delcom.ifs25033springbootcashflow.framework;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashedVaadinAssetCacheFilterTests {
    private final HashedVaadinAssetCacheFilter filter = new HashedVaadinAssetCacheFilter();

    @Test
    void hashedBuildAssetsGetLongImmutableCaching() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/VAADIN/build/vaadin-password-field-Mu18Z-4H.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            HttpServletResponse wrapped = (HttpServletResponse) servletResponse;
            wrapped.setStatus(HttpServletResponse.SC_OK);
            wrapped.setHeader("Cache-Control", "max-age=3600");
            wrapped.addHeader("Cache-Control", "no-cache");
            wrapped.setHeader("Content-Type", "text/javascript");
            wrapped.addHeader("X-Asset", "vaadin");
        });

        assertEquals("public, max-age=31536000, immutable", response.getHeader("Cache-Control"));
        assertEquals("text/javascript", response.getHeader("Content-Type"));
        assertEquals("vaadin", response.getHeader("X-Asset"));
    }

    @Test
    void unhashedThemeStylesKeepTheExistingCachePolicy() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/VAADIN/themes/cashflow/styles.css");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse).setHeader("Cache-Control", "max-age=3600"));

        assertEquals("max-age=3600", response.getHeader("Cache-Control"));
    }

    @Test
    void hashedAssetErrorsAreNotCached() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/VAADIN/build/missing-12345678.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            HttpServletResponse wrapped = (HttpServletResponse) servletResponse;
            wrapped.setStatus(HttpServletResponse.SC_NOT_FOUND);
            wrapped.setHeader("Cache-Control", "max-age=3600");
        });

        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void contextPathIsExcludedWhenMatchingHashedBuildAssets() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/cashflow/VAADIN/build/generated-cJdqcch-.js");
        request.setContextPath("/cashflow");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse).setHeader("Cache-Control", "max-age=3600"));

        assertEquals("public, max-age=31536000, immutable", response.getHeader("Cache-Control"));
    }

    @Test
    void requestWithoutContextPrefixStillMatchesAssetPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/VAADIN/build/generated-cJdqcch-.js");
        request.setContextPath("/cashflow");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse).setHeader("Cache-Control", "max-age=3600"));

        assertEquals("public, max-age=31536000, immutable", response.getHeader("Cache-Control"));
    }

    @Test
    void notModifiedHashedAssetsKeepLongImmutableCaching() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/VAADIN/build/generated-cJdqcch-.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            HttpServletResponse wrapped = (HttpServletResponse) servletResponse;
            wrapped.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            wrapped.setHeader("Cache-Control", "max-age=3600");
        });

        assertEquals("public, max-age=31536000, immutable", response.getHeader("Cache-Control"));
    }

    @Test
    void errorsAndRedirectsAreNotCachedAsImmutableAssets() throws Exception {
        assertUncachedResponse(response -> response.sendError(HttpServletResponse.SC_NOT_FOUND));
        assertUncachedResponse(response ->
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Missing asset"));
        assertUncachedResponse(response -> response.sendRedirect("/login"));
    }

    private void assertUncachedResponse(ResponseAction action) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/VAADIN/build/missing-12345678.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                action.apply((HttpServletResponse) servletResponse));

        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @FunctionalInterface
    private interface ResponseAction {
        void apply(HttpServletResponse response) throws IOException;
    }
}
