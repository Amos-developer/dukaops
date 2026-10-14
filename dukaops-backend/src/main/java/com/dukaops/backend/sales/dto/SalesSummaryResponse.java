package com.dukaops.backend.sales.dto; import java.math.BigDecimal; public record SalesSummaryResponse(BigDecimal revenue,long completedSales,long pendingOrders,BigDecimal averageSaleValue) {}
