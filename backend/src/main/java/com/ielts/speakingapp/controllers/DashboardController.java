package com.ielts.speakingapp.controllers;

import com.ielts.speakingapp.services.DashboardService;
import com.ielts.speakingapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }

            String username = jwtUtil.getUsernameFromToken(token);
            Map<String, Object> stats = dashboardService.getUserDashboardStats(username);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
