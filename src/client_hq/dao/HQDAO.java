package client_hq.dao;

import common.db.DBConnection;
import common.dto.SupplyOrderDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 본사 관리 관련 데이터베이스 접근 객체 (DAO)
 * 
 * 담당 테이블: store_material_request, store_inventory
 * 
 * 주요 기능:
 * - 발주 요청 목록 조회
 * - 발주 승인 처리 (재고 증가 포함)
 * - 발주 반려 처리
 * 
 * @author Franchise Management System
 */
public class HQDAO {

    /**
     * 대기 중인 발주 요청 목록 조회
     * 
     * DB 흐름:
     * 1. store_material_request, store, ingredient 테이블을 JOIN
     * 2. status가 'PENDING'인 발주만 조회
     * 3. 요청일시 내림차순 정렬 (최신 요청이 위로)
     * 
     * @return 대기 중인 발주 요청 리스트
     */
    public List<SupplyOrderDTO> getPendingOrders() {
        List<SupplyOrderDTO> list = new ArrayList<>();
        String sql = "SELECT smr.request_id, s.store_name, i.ingredient_name, smr.quantity, smr.status, smr.request_date " +
                "FROM store_material_request smr " +
                "JOIN store s ON smr.store_id = s.store_id " +
                "JOIN ingredient i ON smr.ingredient_id = i.ingredient_id " +
                "WHERE smr.status = 'PENDING' " +
                "ORDER BY smr.request_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new SupplyOrderDTO(
                        rs.getInt("request_id"),
                        rs.getString("store_name"),
                        rs.getString("ingredient_name"),
                        rs.getInt("quantity"),
                        rs.getString("status"),
                        rs.getString("request_date")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * 발주 승인 처리 (트랜잭션)
     * 
     * DB 흐름 (트랜잭션):
     * 1. 발주 상태 변경: store_material_request의 status를 'APPROVED'로 변경, processed_date 설정
     * 2. 재고 증가: 해당 매장의 store_inventory에서 발주 수량만큼 재고 증가
     * 3. 커밋: 모든 작업 성공 시 커밋, 실패 시 롤백
     * 
     * 주의사항: 재고 테이블에 해당 재료가 없으면 업데이트 실패합니다.
     * 
     * @param requestId 발주 요청 ID
     * @return 승인 처리 성공 여부
     */
    public boolean approveOrder(int requestId) {
        Connection conn = null;
        PreparedStatement pstmtStatus = null;
        PreparedStatement pstmtStock = null;

        // 발주 승인: 상태 변경 및 재고 증가를 하나의 트랜잭션으로 처리
        String sqlUpdateStatus = "UPDATE store_material_request SET status = 'APPROVED', processed_date = NOW() WHERE request_id = ?";

        // JOIN을 사용하여 발주 정보를 기반으로 해당 매장의 재고를 직접 증가
        String sqlUpdateStock = "UPDATE store_inventory si " +
                "JOIN store_material_request smr ON si.store_id = smr.store_id AND si.ingredient_id = smr.ingredient_id " +
                "SET si.quantity = si.quantity + smr.quantity " +
                "WHERE smr.request_id = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // A. 상태 변경
            pstmtStatus = conn.prepareStatement(sqlUpdateStatus);
            pstmtStatus.setInt(1, requestId);
            pstmtStatus.executeUpdate();

            // B. 재고 증가
            pstmtStock = conn.prepareStatement(sqlUpdateStock);
            pstmtStock.setInt(1, requestId);
            int result = pstmtStock.executeUpdate();

            if(result == 0) {
                // 재고 테이블에 해당 재료가 없으면 업데이트 실패
                // 실제 운영 환경에서는 INSERT로 처리하거나, 기초 데이터 초기화 필요
                throw new Exception("재고 테이블 업데이트 실패 (매장에 해당 재료 데이터가 없음)");
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try { if(conn != null) conn.rollback(); } catch(Exception ex){}
            e.printStackTrace();
            return false;
        } finally {
            try { if(conn != null) conn.setAutoCommit(true); } catch(Exception ex){}
        }
    }

    /**
     * 발주 반려 처리
     * 
     * DB 흐름:
     * 1. store_material_request 테이블의 status를 'REJECTED'로 변경
     * 2. 재고는 변경하지 않음
     * 
     * @param requestId 발주 요청 ID
     * @return 반려 처리 성공 여부
     */
    public boolean rejectOrder(int requestId) {
        String sql = "UPDATE store_material_request SET status = 'REJECTED' WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // 가맹점 매출 순위 조회 (가맹점 정보/매출/순위 관리)
    public Vector<Vector<Object>> getStoreRankings() {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT s.store_name, s.manager_name, SUM(co.total_amount) as total_sales " +
                "FROM store s " +
                "LEFT JOIN customer_order co ON s.store_id = co.store_id AND co.status IN ('WAITING', 'COMPLETED') " +
                "GROUP BY s.store_id " +
                "ORDER BY total_sales DESC"; // 매출 높은 순

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rank++); // 순위
                row.add(rs.getString("store_name"));
                row.add(rs.getString("manager_name"));
                int total = rs.getInt("total_sales"); // null이면 0
                row.add(String.format("%,d원", total));
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

}