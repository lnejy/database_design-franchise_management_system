package client_store.dao;

import common.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class KitchenDAO {

    // 대기 중인 주문 조회 (메뉴 내역 합쳐서 조회)
    public Vector<Vector<Object>> getPendingOrders(int storeId) {
        Vector<Vector<Object>> data = new Vector<>();
        
        String sql = "SELECT " +
                     "  co.order_id, " +
                     "  co.order_number, " +
                     "  co.total_amount, " +
                     "  co.order_time, " +
                     "  GROUP_CONCAT(CONCAT(m.menu_name, IF(od.is_set, '(세트)', ''), ' x', od.quantity) SEPARATOR ', ') AS menu_summary " +
                     "FROM customer_order co " +
                     "JOIN order_detail od ON co.order_id = od.order_id " +
                     "JOIN menu m ON od.menu_id = m.menu_id " +
                     "WHERE co.store_id = ? AND co.status = 'WAITING' " +
                     "GROUP BY co.order_id " +
                     "ORDER BY co.order_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, storeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("order_id"));       // Col 0: ID (숨김 예정)
                    row.add(rs.getString("order_number")); // Col 1: 주문번호
                    row.add(rs.getString("menu_summary")); // Col 2: 주문 메뉴 (추가됨!)
                    row.add(rs.getInt("total_amount"));    // Col 3: 금액
                    row.add(rs.getTimestamp("order_time"));// Col 4: 시간
                    data.add(row);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // 상태 변경 (동일)
    public boolean completeOrder(int orderId) {
        String sql = "UPDATE customer_order SET status = 'COMPLETED' WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}