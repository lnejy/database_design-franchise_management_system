package client_store.dao;

import common.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * 매출 관련 데이터베이스 접근 객체 (DAO)
 * 
 * 담당 테이블: customer_order, ingredient
 * 
 * 주요 기능:
 * - 매장별 주문 내역 조회
 * - 총 매출액 계산
 * 
 * @author Franchise Management System
 */
public class SalesDAO {

    /**
     * 특정 매장의 주문 내역 조회 (JTable 표시용)
     * 
     * DB 흐름:
     * 1. customer_order 테이블에서 해당 매장의 주문 조회
     * 2. order_time 컬럼 기준 내림차순 정렬 (최신 주문이 위로)
     * 3. 주문번호, 결제금액, 상태, 주문일시 반환
     * 
     * @param storeId 매장 ID
     * @return 주문 내역 테이블 데이터
     */
    public Vector<Vector<Object>> getOrderHistory(int storeId) {
        Vector<Vector<Object>> data = new Vector<>();

        // 최신 주문이 위로 오게 정렬 (ORDER BY order_time DESC)
        String sql = "SELECT order_number, total_amount, order_time, status " +
                "FROM customer_order " +
                "WHERE store_id = ? " +
                "ORDER BY order_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, storeId);

            try(ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("order_number"));
                    row.add(rs.getInt("total_amount"));
                    row.add(rs.getString("status"));
                    row.add(rs.getTimestamp("order_time")); // 날짜 및 시간
                    data.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 특정 매장의 총 누적 매출액 조회
     * 
     * DB 흐름:
     * 1. customer_order 테이블에서 해당 매장의 모든 주문 조회
     * 2. total_amount 컬럼의 합계 계산 (SUM)
     * 
     * @param storeId 매장 ID
     * @return 총 누적 매출액 (원)
     */
    public int getTotalSales(int storeId) {
        int total = 0;
        String sql = "SELECT SUM(total_amount) FROM customer_order WHERE store_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, storeId);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) {
                    total = rs.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    /**
     * 모든 재료 목록 조회
     * 
     * 참고: StoreDAO에도 동일한 메서드가 있습니다.
     * 중복 제거를 위해 공통 DAO로 이동 고려 필요.
     * 
     * @return 모든 재료 정보 리스트
     */
    public java.util.List<common.dto.IngredientDTO> getAllIngredients() {
        java.util.List<common.dto.IngredientDTO> list = new java.util.ArrayList<>();

        String sql = "SELECT ingredient_id, ingredient_name, unit FROM ingredient ORDER BY ingredient_name";

        try (java.sql.Connection conn = common.db.DBConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // IngredientDTO 객체 생성 후 리스트에 추가
                list.add(new common.dto.IngredientDTO(
                        rs.getInt("ingredient_id"),
                        rs.getString("ingredient_name"),
                        rs.getString("unit")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}