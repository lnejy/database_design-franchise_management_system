package client_customer.dao;

import common.db.DBConnection;
import common.dto.MenuDTO;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class OrderDAO {

    // 주문 처리 (트랜잭션)
    // storeId: 주문이 발생한 매장, cart: 메뉴와 수량(Map)
    public boolean processOrder(int storeId, Map<MenuDTO, Integer> cart, int totalAmount) {
        Connection conn = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtDetail = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtStock = null;

        try {
            conn = DBConnection.getConnection();
            // 1. 트랜잭션 시작 (자동 커밋 끔)
            conn.setAutoCommit(false);

            // ---------------------------------------------------------
            // A. 주문 헤더 생성 (customer_order)
            // ---------------------------------------------------------
            String sqlOrder = "INSERT INTO customer_order (store_id, order_number, total_amount, status) VALUES (?, ?, ?, 'ORDERED')";
            pstmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            String orderNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // 간단한 주문번호 생성

            pstmtOrder.setInt(1, storeId);
            pstmtOrder.setString(2, orderNumber);
            pstmtOrder.setInt(3, totalAmount);
            pstmtOrder.executeUpdate();

            // 생성된 order_id 가져오기
            int orderId = 0;
            try (ResultSet rs = pstmtOrder.getGeneratedKeys()) {
                if (rs.next()) orderId = rs.getInt(1);
            }

            // ---------------------------------------------------------
            // B. 주문 상세 및 재고 차감 (order_detail & store_inventory)
            // ---------------------------------------------------------
            String sqlDetail = "INSERT INTO order_detail (order_id, menu_id, quantity, unit_price, subtotal_price) VALUES (?, ?, ?, ?, ?)";

            // [핵심 로직] 레시피 기반 재고 차감 쿼리
            // "이 메뉴(menu_id)에 연결된 재료들을 찾아서, 해당 매장(store_id)의 재고에서 (소모량 * 주문수량) 만큼 뺀다"
            String sqlStock = "UPDATE store_inventory si " +
                    "JOIN menu_recipe mr ON si.ingredient_id = mr.ingredient_id " +
                    "SET si.quantity = si.quantity - (mr.required_quantity * ?) " +
                    "WHERE si.store_id = ? AND mr.menu_id = ?";

            pstmtDetail = conn.prepareStatement(sqlDetail);
            pstmtStock = conn.prepareStatement(sqlStock);

            for (Map.Entry<MenuDTO, Integer> entry : cart.entrySet()) {
                MenuDTO menu = entry.getKey();
                int qty = entry.getValue();

                // 상세 내역 저장
                pstmtDetail.setInt(1, orderId);
                pstmtDetail.setInt(2, menu.getMenuId());
                pstmtDetail.setInt(3, qty);
                pstmtDetail.setInt(4, menu.getPrice());
                pstmtDetail.setInt(5, menu.getPrice() * qty);
                pstmtDetail.executeUpdate();

                // 재고 차감 실행
                pstmtStock.setInt(1, qty);      // 주문 수량만큼 곱해서 차감
                pstmtStock.setInt(2, storeId);  // 해당 매장에서
                pstmtStock.setInt(3, menu.getMenuId()); // 이 메뉴의 레시피대로
                int updatedRows = pstmtStock.executeUpdate();

            }

            // ---------------------------------------------------------
            // C. 매출 확정 (store_sale)
            // ---------------------------------------------------------
            String sqlSale = "INSERT INTO store_sale (order_id, payment_method, total_price) VALUES (?, 'CARD', ?)";
            pstmtSale = conn.prepareStatement(sqlSale);
            pstmtSale.setInt(1, orderId);
            pstmtSale.setInt(2, totalAmount);
            pstmtSale.executeUpdate();

            // ---------------------------------------------------------
            // D. 커밋 (모든 과정이 성공하면 DB 반영)
            // ---------------------------------------------------------
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // 실패 시 롤백 (원상복구)
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // 자원 해제 및 오토커밋 복구
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            // pstmt close 생략 (try-with-resources 사용 권장하지만 로직상 길어져서 finally 처리)
        }
    }
}