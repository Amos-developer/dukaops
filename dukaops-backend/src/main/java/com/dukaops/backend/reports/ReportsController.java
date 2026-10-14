package com.dukaops.backend.reports;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.dukaops.backend.auth.TenantUserPrincipal;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    private final ReportsService service;
    public ReportsController(ReportsService service) { this.service = service; }
    @GetMapping("/overview") public Map<String,Object> overview(@AuthenticationPrincipal TenantUserPrincipal principal, @RequestParam(required=false) UUID branchId, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to) { return service.overview(principal, branchId, from, to); }
    @GetMapping("/sales") public Map<String,Object> sales(@AuthenticationPrincipal TenantUserPrincipal principal, @RequestParam(required=false) UUID branchId, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to) { return service.sales(principal, branchId, from, to); }
    @GetMapping("/inventory") public Map<String,Object> inventory(@AuthenticationPrincipal TenantUserPrincipal principal, @RequestParam(required=false) UUID branchId) { return service.inventory(principal, branchId); }
    @GetMapping("/products") public Map<String,Object> products(@AuthenticationPrincipal TenantUserPrincipal principal, @RequestParam(required=false) UUID branchId, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to) { return service.products(principal, branchId, from, to); }
    @GetMapping("/customers") public Map<String,Object> customers(@AuthenticationPrincipal TenantUserPrincipal principal) { return service.customers(principal); }
    @GetMapping("/suppliers") public Map<String,Object> suppliers(@AuthenticationPrincipal TenantUserPrincipal principal) { return service.suppliers(principal); }
    @GetMapping(value="/export", produces="text/csv") public String export(@AuthenticationPrincipal TenantUserPrincipal principal, @RequestParam(defaultValue="sales") String type, @RequestParam(required=false) UUID branchId) { return service.export(principal, type, branchId); }
}
