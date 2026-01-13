package com.shopizer.rest.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Base servlet providing common functionality for all REST endpoints
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Configure Jackson ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Send JSON response
     */
    protected void sendJsonResponse(HttpServletResponse response, Object data, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, data);
        out.flush();
    }

    /**
     * Send success response
     */
    protected void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendJsonResponse(response, data, HttpServletResponse.SC_OK);
    }

    /**
     * Send created response (201)
     */
    protected void sendCreated(HttpServletResponse response, Object data) throws IOException {
        sendJsonResponse(response, data, HttpServletResponse.SC_CREATED);
    }

    /**
     * Send no content response (204)
     */
    protected void sendNoContent(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Send error response
     */
    protected void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", statusCode);
        error.put("timestamp", java.time.Instant.now().toString());

        sendJsonResponse(response, error, statusCode);
    }

    /**
     * Send bad request error (400)
     */
    protected void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Send not found error (404)
     */
    protected void sendNotFound(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Send internal server error (500)
     */
    protected void sendInternalError(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Read JSON request body
     */
    protected <T> T readJsonBody(HttpServletRequest request, Class<T> valueType) throws IOException {
        return objectMapper.readValue(request.getReader(), valueType);
    }

    /**
     * Extract path parameter (e.g., /products/123 -> 123)
     */
    protected Long extractIdFromPath(HttpServletRequest request, String prefix) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }

        try {
            String idStr = pathInfo.substring(1); // Remove leading '/'
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get query parameter as Long
     */
    protected Long getQueryParamAsLong(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get query parameter as Integer
     */
    protected Integer getQueryParamAsInt(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Enable CORS for testing
     */
    protected void enableCors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
