package com.github.dirkraft.dropwizard.fileassets;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class FileAssetServletTest {

    FileAssetServlet servlet = new FileAssetServlet("src/test/resources/web", "/", "index.html", Charset.forName("UTF-8"));

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);

    @Before
    public void setupRequest() throws IOException {
        when(request.getServletPath()).thenReturn("");
        when(request.getServletContext()).thenReturn(mock(ServletContext.class));
        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @Test
    public void testOk() throws ServletException, IOException, URISyntaxException {
        when(request.getPathInfo()).thenReturn("/index.html");

        servlet.doGet(request, response);

        byte[] expectedContent = Files.readAllBytes(Paths.get(getClass().getResource("/web/index.html").toURI()));
        verifyOkResponse(expectedContent);
    }

    @Test
    public void testOkIndex() throws ServletException, IOException, URISyntaxException {
        when(request.getPathInfo()).thenReturn("/");

        servlet.doGet(request, response);

        byte[] expectedContent = Files.readAllBytes(Paths.get(getClass().getResource("/web/index.html").toURI()));
        verifyOkResponse(expectedContent);
    }

    /**
     * https://github.com/dirkraft/dropwizard-file-assets/issues/1
     */
    @Test
    public void test404() throws ServletException, IOException {
        servlet.doGet(request, response);
        verifyErrorResponse(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void test404NoIndex() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/subdir/");

        servlet.doGet(request, response);
        verifyErrorResponse(HttpStatus.NOT_FOUND_404);
    }

    private void verifyOkResponse(byte[] expectedContent) throws IOException {
        verify(servletOutputStream).write(eq(expectedContent));
        verify(response, never()).sendError(anyInt());
        verify(response, never()).sendError(anyInt(), anyString());
        verify(response, never()).sendRedirect(anyString());
    }

    private void verifyErrorResponse(int statusCode) throws IOException {
        verify(response).sendError(eq(statusCode));
        verifyNoMoreInteractions(response);
    }
}
