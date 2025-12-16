package client_store.dao;

import common.db.DBConnection;
import common.dto.InventoryDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 재고 관련 데이터베이스 접근 객체 (DAO)
 * 
 * <p><b>담당 테이블:</b> store_inventory, ingredient</p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>매장별 재고 현황 조회</li>
 *   <li>재고 부족 여부 확인</li>
 * </ul>
 * 
 * @author Franchise Management System
 */
public class InventoryDAO {

    /**
     * 특정 매장의 재고 목록 조회
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store_inventory와 ingredient 테이블을 JOIN</li>
     *   <li>재료 ID, 재료명, 현재 수량, 최소 기준, 단위 조회</li>
     *   <li>재료명 순으로 정렬</li>
     *   <li>InventoryDTO 리스트로 반환 (재고 상태 판별 메서드 포함)</li>
     * </ol>
     * 
     * @param storeId 매장 ID
     * @return 재고 정보 리스트
     * @throws SQLException 데이터베이스 오류 시 발생
     */
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