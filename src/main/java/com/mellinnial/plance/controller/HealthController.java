    package com.mellinnial.plance.controller;

    import com.mellinnial.plance.dto.response.ApiResponseDto;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import java.util.Map;

    @RestController
    @RequestMapping("/api/health")
    @Tag(name = "Health Check", description = "Monitor API health status")
    public class HealthController {

        @GetMapping
        public ResponseEntity<ApiResponseDto<Map<String, String>>> checkHealth() {
            return ResponseEntity.ok(ApiResponseDto.success("System is running", Map.of("status", "UP")));
        }
    }
