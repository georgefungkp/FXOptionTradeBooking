package org.george.fxoptiontradebooking.controller;

import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TracingTestController {

    @Autowired(required = false)
    private Tracer tracer;

    @GetMapping("/tracing")
    public Map<String, Object> testTracing() {
        Map<String, Object> response = new HashMap<>();
        
        if (tracer != null) {
            response.put("tracing_enabled", true);
            response.put("tracer_class", tracer.getClass().getName());
            response.put("message", "Tracing is working! Check Jaeger UI.");
            
            // Just check if we can get the current span
            var currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                response.put("current_trace_id", currentSpan.context().traceId());
                response.put("current_span_id", currentSpan.context().spanId());
            } else {
                response.put("current_span", "No current span");
            }
            
        } else {
            response.put("tracing_enabled", false);
            response.put("message", "No tracer bean found - tracing not configured");
        }
        
        return response;
    }
}