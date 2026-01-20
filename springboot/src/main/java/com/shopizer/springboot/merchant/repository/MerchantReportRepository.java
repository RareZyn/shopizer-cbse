package com.shopizer.springboot.merchant.repository;

import com.shopizer.springboot.merchant.dto.SalesReportResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class MerchantReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public MerchantReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Orders count = number of distinct orders that contain products belonging to this merchant/store.
     */
    public long countOrders(Long merchantId, Long storeId, LocalDateTime startTs, LocalDateTime endTsExclusive) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(DISTINCT oi.order_id) AS total_orders
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            JOIN merchant_stores ms ON ms.id = p.store_id
            WHERE ms.merchant_id = ?
        """);

        List<Object> args = new ArrayList<>();
        args.add(merchantId);

        if (storeId != null) {
            sql.append(" AND p.store_id = ?");
            args.add(storeId);
        }
        if (startTs != null) {
            sql.append(" AND o.created_at >= ?");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            sql.append(" AND o.created_at < ?");
            args.add(endTsExclusive);
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
        return result == null ? 0L : result;
    }

    /**
     * Revenue = sum of order_items.total_price for products belonging to this merchant/store.
     * (This avoids double-counting order totals and works even when orders have items from multiple stores.)
     */
    public BigDecimal sumRevenue(Long merchantId, Long storeId, LocalDateTime startTs, LocalDateTime endTsExclusive) {
        StringBuilder sql = new StringBuilder("""
            SELECT COALESCE(SUM(oi.total_price), 0) AS total_revenue
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            JOIN merchant_stores ms ON ms.id = p.store_id
            WHERE ms.merchant_id = ?
        """);

        List<Object> args = new ArrayList<>();
        args.add(merchantId);

        if (storeId != null) {
            sql.append(" AND p.store_id = ?");
            args.add(storeId);
        }
        if (startTs != null) {
            sql.append(" AND o.created_at >= ?");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            sql.append(" AND o.created_at < ?");
            args.add(endTsExclusive);
        }

        BigDecimal result = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, args.toArray());
        return result == null ? BigDecimal.ZERO : result;
    }

    public List<SalesReportResponse.TopSellingProduct> topSellingProducts(
            Long merchantId,
            Long storeId,
            LocalDateTime startTs,
            LocalDateTime endTsExclusive,
            int limit
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
              oi.product_id,
              oi.product_name,
              oi.product_sku,
              COALESCE(SUM(oi.quantity), 0) AS units_sold,
              COALESCE(SUM(oi.total_price), 0) AS total_sales
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            JOIN merchant_stores ms ON ms.id = p.store_id
            WHERE ms.merchant_id = ?
        """);

        List<Object> args = new ArrayList<>();
        args.add(merchantId);

        if (storeId != null) {
            sql.append(" AND p.store_id = ?");
            args.add(storeId);
        }
        if (startTs != null) {
            sql.append(" AND o.created_at >= ?");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            sql.append(" AND o.created_at < ?");
            args.add(endTsExclusive);
        }

        sql.append("""
            GROUP BY oi.product_id, oi.product_name, oi.product_sku
            ORDER BY total_sales DESC
            LIMIT ?
        """);
        args.add(limit);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new SalesReportResponse.TopSellingProduct(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getString("product_sku"),
                        rs.getLong("units_sold"),
                        rs.getBigDecimal("total_sales")
                ),
                args.toArray()
        );
    }

    public Map<String, BigDecimal> salesByDay(Long merchantId, Long storeId, LocalDateTime startTs, LocalDateTime endTsExclusive) {
        StringBuilder sql = new StringBuilder("""
            SELECT
              CAST(o.created_at AS date) AS day,
              COALESCE(SUM(oi.total_price), 0) AS total_sales
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            JOIN merchant_stores ms ON ms.id = p.store_id
            WHERE ms.merchant_id = ?
        """);

        List<Object> args = new ArrayList<>();
        args.add(merchantId);

        if (storeId != null) {
            sql.append(" AND p.store_id = ?");
            args.add(storeId);
        }
        if (startTs != null) {
            sql.append(" AND o.created_at >= ?");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            sql.append(" AND o.created_at < ?");
            args.add(endTsExclusive);
        }

        sql.append("""
            GROUP BY CAST(o.created_at AS date)
            ORDER BY day ASC
        """);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            Date day = (Date) r.get("day");
            BigDecimal total = (BigDecimal) r.get("total_sales");
            result.put(day.toLocalDate().toString(), total);
        }
        return result;
    }

    /**
     * Per-product sales aggregation with optional filters (store, category, product, date).
     */
    public List<ProductPerformanceRow> productPerformance(
            Long merchantId,
            Long storeId,
            Long categoryId,
            Long productId,
            LocalDateTime startTs,
            LocalDateTime endTsExclusive
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
              p.id AS product_id,
              p.name AS product_name,
              p.sku AS product_sku,
              p.stock_quantity,
              p.low_stock_threshold,
              p.store_id,
              c.name AS category_name,
              COALESCE(SUM(oi.quantity), 0) AS units_sold,
              COUNT(DISTINCT oi.order_id) AS orders_count,
              COALESCE(SUM(oi.total_price), 0) AS total_revenue
            FROM products p
            JOIN merchant_stores ms ON ms.id = p.store_id
            LEFT JOIN categories c ON c.id = p.category_id
            LEFT JOIN order_items oi ON oi.product_id = p.id
            LEFT JOIN orders o ON o.id = oi.order_id
            WHERE ms.merchant_id = ?
        """);

        List<Object> args = new ArrayList<>();
        args.add(merchantId);

        if (storeId != null) {
            sql.append(" AND p.store_id = ?");
            args.add(storeId);
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            args.add(categoryId);
        }
        if (productId != null) {
            sql.append(" AND p.id = ?");
            args.add(productId);
        }
        if (startTs != null) {
            sql.append(" AND (o.created_at >= ? OR o.created_at IS NULL)");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            sql.append(" AND (o.created_at < ? OR o.created_at IS NULL)");
            args.add(endTsExclusive);
        }

        sql.append("""
            GROUP BY p.id, p.name, p.sku, p.stock_quantity, p.low_stock_threshold, p.store_id, c.name
            ORDER BY total_revenue DESC
        """);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new ProductPerformanceRow(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getString("product_sku"),
                        rs.getLong("store_id"),
                        rs.getString("category_name"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("low_stock_threshold"),
                        rs.getLong("orders_count"),
                        rs.getLong("units_sold"),
                        rs.getBigDecimal("total_revenue")
                ),
                args.toArray()
        );
    }

    /**
     * View counts for products to support conversion calculations.
     */
    public Map<Long, Long> productViews(
            Long merchantId,
            Long storeId,
            Long categoryId,
            Long productId,
            LocalDateTime startTs,
            LocalDateTime endTsExclusive
    ) {
        StringBuilder join = new StringBuilder(" LEFT JOIN product_view_events pve ON pve.product_id = p.id");
        List<Object> args = new ArrayList<>();

        if (startTs != null) {
            join.append(" AND pve.viewed_at >= ?");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            join.append(" AND pve.viewed_at < ?");
            args.add(endTsExclusive);
        }

        StringBuilder sql = new StringBuilder("""
            SELECT p.id AS product_id, COUNT(pve.id) AS views
            FROM products p
            JOIN merchant_stores ms ON ms.id = p.store_id
        """);
        sql.append(join);
        sql.append(" WHERE ms.merchant_id = ?");
        args.add(merchantId);

        if (storeId != null) {
            sql.append(" AND p.store_id = ?");
            args.add(storeId);
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            args.add(categoryId);
        }
        if (productId != null) {
            sql.append(" AND p.id = ?");
            args.add(productId);
        }

        sql.append(" GROUP BY p.id");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> r : rows) {
            result.put((Long) r.get("product_id"), (Long) r.get("views"));
        }
        return result;
    }

    public Map<String, BigDecimal> productSalesByDay(
            Long merchantId,
            Long productId,
            LocalDateTime startTs,
            LocalDateTime endTsExclusive
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
              CAST(o.created_at AS date) AS day,
              COALESCE(SUM(oi.total_price), 0) AS total_sales
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            JOIN merchant_stores ms ON ms.id = p.store_id
            WHERE ms.merchant_id = ? AND p.id = ?
        """);

        List<Object> args = new ArrayList<>();
        args.add(merchantId);
        args.add(productId);

        if (startTs != null) {
            sql.append(" AND o.created_at >= ?");
            args.add(startTs);
        }
        if (endTsExclusive != null) {
            sql.append(" AND o.created_at < ?");
            args.add(endTsExclusive);
        }

        sql.append("""
            GROUP BY CAST(o.created_at AS date)
            ORDER BY day ASC
        """);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            Date day = (Date) r.get("day");
            BigDecimal total = (BigDecimal) r.get("total_sales");
            result.put(day.toLocalDate().toString(), total);
        }
        return result;
    }

    public record ProductPerformanceRow(
            Long productId,
            String productName,
            String productSku,
            Long storeId,
            String categoryName,
            Integer stockQuantity,
            Integer lowStockThreshold,
            Long ordersCount,
            Long unitsSold,
            BigDecimal totalRevenue
    ) {}
}
