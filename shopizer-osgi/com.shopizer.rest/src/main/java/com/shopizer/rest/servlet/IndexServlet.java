package com.shopizer.rest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Servlet to serve the static index.html page
 */
public class IndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Set content type
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        // Load index.html from classpath
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("static/index.html")) {
            if (in == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "index.html not found");
                return;
            }

            // Copy content to response
            try (OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
