package client_warehouse.dao;

import common.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class WarehouseDAO {

    // 출고(배송) 이력 조회 (본사가 승인한 건들)
    public Vector<Vector<Object>> getShipmentHistory() {
        Vector<Vector<Object>> data = new Vector<>();

        // processed_date(승인일)가 있는 건들이 배송된 건으로 간주
        String sql = "SELECT smr.request_id, s.store_name, i.ingredient_name, smr.quantity, smr.processed_date " +
                "FROM store_material_request smr " +
                "JOIN store s ON smr.store_id = s.store_id " +
                "JOIN ingredient i ON smr.ingredient_id = i.ingredient_id " +
                "WHERE smr.status = 'APPROVED' " +
                "ORDER BY smr.processed_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("request_id"));
                row.add(rs.getString("store_name"));     // 어느 지점
                row.add(rs.getString("ingredient_name")); // 어떤 재료
                row.add(rs.getInt("quantity"));          // 얼마나
                row.add(rs.getTimestamp("processed_date")); // 언제 (출고일)
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
}