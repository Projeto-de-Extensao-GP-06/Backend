package br.com.sgpj.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting client IP addresses from HTTP requests.
 * Handles proxy servers, load balancers, and various forwarding headers.
 */
public class IpAddressUtil {

    /**
     * Extracts the client IP address from the HTTP request.
     * Handles proxy servers and load balancers by checking common forwarding headers.
     * 
     * @param request HTTP request
     * @return Client IP address, or "unknown" if cannot be determined
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For (take the first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * Creates a unique identifier for rate limiting based on client IP and optional user identifier.
     * 
     * @param request HTTP request
     * @param userIdentifier Optional user identifier (email, username, etc.)
     * @return Unique identifier for rate limiting
     */
    public static String createRateLimitIdentifier(HttpServletRequest request, String userIdentifier) {
        String clientIp = getClientIpAddress(request);
        if (userIdentifier != null && !userIdentifier.isBlank()) {
            return clientIp + ":" + userIdentifier.toLowerCase().trim();
        }
        return clientIp;
    }
}