package client_store.dao;

import common.db.DBConnection;
import common.dto.InventoryDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    // 특정 매장의 재고 리스트 가져오기 (JOIN 쿼리 사용)
    public List<InventoryDTO> getStoreInventory(int storeId) throws SQLException {
        List<InventoryDTO> list = new ArrayList<>();

        // store_inventory(수량)와 ingredient(이름)를 합침
        String sql = "SELECT " +
                "  i.ingredient_id, " +
                "  i.ingredient_name, " +
                "  si.quantity, " +
                "  si.min_threshold, " +
                "  i.unit " +
                "FROM store_inventory si " +
                "JOIN ingredient i ON si.ingredient_id = i.ingredient_id " +
                "WHERE si.store_id = ? " +
                "ORDER BY i.ingredient_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, storeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    InventoryDTO dto = new InventoryDTO(
                            rs.getInt("ingredient_id"),
                            rs.getString("ingredient_name"),
                            rs.getInt("quantity"),
                            rs.getInt("min_threshold"),
                            rs.getString("unit")
                    );
                    list.add(dto);
                }
            }
        }
        return list;
    }
}