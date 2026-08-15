package com.huadi.smm.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class GatewayController {

    private final RestTemplate restTemplate;
    private final Map<String, String> routes;

    public GatewayController(RestTemplate restTemplate, Map<String, String> routes) {
        this.restTemplate = restTemplate;
        this.routes = routes;
    }

    @RequestMapping("/api/{subsystem}/**")
    public ResponseEntity<String> proxy(
            @PathVariable String subsystem,
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String target = routes.get(subsystem);
        if (target == null) {
            return ResponseEntity.status(404)
                    .body("{\"success\":false,\"msg\":\"unknown subsystem: " + subsystem + "\"}");
        }

        String path = request.getRequestURI();
        String prefix = "/api/" + subsystem;
        String forwardPath = "/api" + path.substring(prefix.length());

        String url = target + forwardPath;
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, entity, String.class);
    }
}
