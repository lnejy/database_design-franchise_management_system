package client_customer.dao;

import common.db.DBConnection;
import common.dto.CartItemDTO;
import java.sql.*;
import java.util.List;

public class OrderDAO {

    // 주문 처리 (트랜잭션) -> 성공 시 orderId 반환, 실패 시 -1
    public int placeOrder(int storeId, List<CartItemDTO> cartItems, int totalAmount) {
        Connection conn = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtDetail = null;
        PreparedStatement pstmtStock = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtDetailOption = null;
        PreparedStatement pstmtStockOption = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. 주문 번호 생성 (간단히 시간 + 난수 대신, DB의 Auto Increment ID 활용하여 생성)
            // 실제 영수증 번호는 "ORDER-날짜-ID" 형식이 좋으나 여기선 간단히 처리
            String orderNumber = String.valueOf(System.currentTimeMillis() % 10000); // 예: 4자리 번호

            // 2. 주문 헤더 Insert
            String sqlOrder = "INSERT INTO customer_order (store_id, order_number, total_amount, status) VALUES (?, ?, ?, 'WAITING')";
            pstmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, storeId);
            pstmtOrder.setString(2, orderNumber);
            pstmtOrder.setInt(3, totalAmount);
            pstmtOrder.executeUpdate();

            rs = pstmtOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            // 3. 주문 상세 & 재고 차감 Loop
            String sqlDetail = "INSERT INTO order_detail (order_id, menu_id, quantity, is_set, unit_price, subtotal_price) VALUES (?, ?, ?, ?, ?, ?)";
            
            // 재고 차감 쿼리
            String sqlStock = "UPDATE store_inventory si " +
                              "JOIN menu_recipe mr ON si.ingredient_id = mr.ingredient_id " +
                              "SET si.quantity = si.quantity - (mr.required_quantity * ?) " +
                              "WHERE si.store_id = ? AND mr.menu_id = ?";

            String sqlDetailOption =
                    "INSERT INTO order_detail_option (detail_id, option_id, option_qty) VALUES (?, ?, ?)";

            String sqlStockOption =
                    "UPDATE store_inventory si " +
                            "JOIN menu_option mo ON si.ingredient_id = mo.ingredient_id " +
                            "SET si.quantity = si.quantity - (mo.delta_quantity * ? * ?) " +
                            "WHERE si.store_id = ? AND mo.option_id = ?";

            pstmtStockOption = conn.prepareStatement(sqlStockOption);


            pstmtDetailOption = conn.prepareStatement(sqlDetailOption);


            pstmtDetail = conn.prepareStatement(sqlDetail, Statement.RETURN_GENERATED_KEYS);
            pstmtStock = conn.prepareStatement(sqlStock);

            for (CartItemDTO item : cartItems) {
                // 상세 기록
                pstmtDetail.setInt(1, orderId);
                pstmtDetail.setInt(2, item.getMenu().getMenuId());
                pstmtDetail.setInt(3, item.getQuantity());
                pstmtDetail.setBoolean(4, item.isSet());
                pstmtDetail.setInt(5, item.getUnitPrice());
                pstmtDetail.setInt(6, item.getSubTotal());
                pstmtDetail.executeUpdate();

                //detail_id 얻기
                int detailId = 0;
                try (ResultSet drs = pstmtDetail.getGeneratedKeys()) {
                    if (drs.next()) detailId = drs.getInt(1);
                }

                // 재고 차감
                pstmtStock.setInt(1, item.getQuantity());
                pstmtStock.setInt(2, storeId);
                pstmtStock.setInt(3, item.getMenu().getMenuId());
                pstmtStock.executeUpdate();

                // 옵션 저장 + 옵션 재고 반영
                if (item.getOptions() != null) {
                    for (var opt : item.getOptions()) {

                        // (a) order_detail_option 저장
                        pstmtDetailOption.setInt(1, detailId);
                        pstmtDetailOption.setInt(2, opt.getOptionId());
                        pstmtDetailOption.setInt(3, 1); // option_qty (지금은 1 고정)
                        pstmtDetailOption.executeUpdate();

                        // (b) 옵션 재고 반영
                        pstmtStockOption.setInt(1, item.getQuantity()); // 주문 수량
                        pstmtStockOption.setInt(2, 1);                  // option_qty
                        pstmtStockOption.setInt(3, storeId);
                        pstmtStockOption.setInt(4, opt.getOptionId());
                        pstmtStockOption.executeUpdate();
                    }
                }
            }

            // 4. 매출 기록
            String sqlSale = "INSERT INTO store_sale (order_id, payment_method, total_price) VALUES (?, 'CARD', ?)";
            pstmtSale = conn.prepareStatement(sqlSale);
            pstmtSale.setInt(1, orderId);
            pstmtSale.setInt(2, totalAmount);
            pstmtSale.executeUpdate();

            conn.commit();
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return -1;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    // 주문 번호 조회
    public String getOrderNumber(int orderId) {
        String sql = "SELECT order_number FROM customer_order WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("order_number");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    // 주문 상태 조회 (폴링용)
    public String checkOrderStatus(int orderId) {
        String sql = "SELECT status FROM customer_order WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }
}