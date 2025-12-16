package client_store.dao;

import common.db.DBConnection;
import common.dto.StoreDTO;
import common.dto.IngredientDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 매장 관련 데이터베이스 접근 객체 (DAO)
 * 
 * <p><b>담당 테이블:</b> store, store_inventory, ingredient, store_material_request</p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>매장 로그인 인증</li>
 *   <li>매장 정보 조회 및 등록</li>
 *   <li>재고 조회</li>
 *   <li>발주 요청</li>
 * </ul>
 * 
 * @author Franchise Management System
 */
public class StoreDAO {

    /** 신규 매장 생성 시 기본 적재할 재고 수량 */
    private static final int DEFAULT_INITIAL_STOCK = 30;
    /** 재고 부족 안내 기준선 */
    private static final int DEFAULT_MIN_THRESHOLD = 10;

    /**
     * 매장 로그인 인증
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store 테이블에서 store_code와 phone_number로 매장 정보 조회</li>
     *   <li>일치하는 매장이 있으면 StoreDTO 반환, 없으면 null 반환</li>
     * </ol>
     * 
     * @param id 매장 코드 (store_code)
     * @param pw 전화번호 (phone_number, 숫자만)
     * @return 로그인 성공 시 StoreDTO, 실패 시 null
     */
    public StoreDTO login(String id, String pw) {
        String sql = "SELECT * FROM store WHERE store_code = ? AND phone_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    StoreDTO store = new StoreDTO();
                    store.setStoreId(rs.getInt("store_id"));
                    store.setStoreName(rs.getString("store_name"));
                    store.setStoreCode(rs.getString("store_code"));
                    store.setPhone(rs.getString("phone_number"));
                    store.setAddress(rs.getString("address"));
                    store.setManagerName(rs.getString("manager_name"));
                    return store;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 매장 코드로 매장 정보 조회
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store 테이블에서 store_code로 매장 정보 조회</li>
     *   <li>매장 정보를 StoreDTO로 변환하여 반환</li>
     * </ol>
     * 
     * @param id 매장 코드
     * @return 매장 정보 DTO, 없으면 null
     */
    public StoreDTO getStore(String id) {
        StoreDTO store = null;
        String sql = "SELECT * FROM store WHERE store_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    store = new StoreDTO();
                    store.setStoreId(rs.getInt("store_id"));
                    store.setStoreName(rs.getString("store_name"));
                    store.setStoreCode(rs.getString("store_code"));
                    store.setPhone(rs.getString("phone_number"));
                    store.setAddress(rs.getString("address"));
                    store.setManagerName(rs.getString("manager_name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return store;
    }

    /**
     * 신규 매장 등록
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store 테이블에 새로운 매장 정보 INSERT</li>
     *   <li>성공 시 true, 실패 시 false 반환</li>
     * </ol>
     * 
     * <p><b>주의사항:</b> store_code는 UNIQUE 제약조건이 있어야 합니다.</p>
     * 
     * @param dto 등록할 매장 정보
     * @return 등록 성공 여부
     */
    public boolean registerStore(StoreDTO dto) {
        String sqlStore = "INSERT INTO store (store_name, store_code, phone_number, address, manager_name) VALUES (?, ?, ?, ?, ?)";
        String sqlIngredients = "SELECT ingredient_id FROM ingredient";
        String sqlInitInventory = "INSERT INTO store_inventory (store_id, ingredient_id, quantity, min_threshold) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) 매장 기본 정보 저장
            try (PreparedStatement pstmtStore = conn.prepareStatement(sqlStore, Statement.RETURN_GENERATED_KEYS)) {
                pstmtStore.setString(1, dto.getStoreName());
                pstmtStore.setString(2, dto.getStoreCode());
                pstmtStore.setString(3, dto.getPhone());
                pstmtStore.setString(4, dto.getAddress());
                pstmtStore.setString(5, dto.getManagerName());

                int affected = pstmtStore.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                // 생성된 store_id 획득
                int storeId = -1;
                try (ResultSet rs = pstmtStore.getGeneratedKeys()) {
                    if (rs.next()) {
                        storeId = rs.getInt(1);
                    }
                }
                if (storeId == -1) {
                    conn.rollback();
                    return false;
                }

                // 2) 모든 재료를 기본 수량으로 재고 초기화
                try (PreparedStatement pstmtIngredients = conn.prepareStatement(sqlIngredients);
                     PreparedStatement pstmtInit = conn.prepareStatement(sqlInitInventory);
                     ResultSet rsIng = pstmtIngredients.executeQuery()) {

                    while (rsIng.next()) {
                        int ingredientId = rsIng.getInt("ingredient_id");
                        pstmtInit.setInt(1, storeId);
                        pstmtInit.setInt(2, ingredientId);
                        pstmtInit.setInt(3, DEFAULT_INITIAL_STOCK);
                        pstmtInit.setInt(4, DEFAULT_MIN_THRESHOLD);
                        pstmtInit.addBatch();
                    }
                    pstmtInit.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception e) { }
        }
    }

    /**
     * 모든 매장 목록 조회
     * 
     * <p><b>사용처:</b> 키오스크 시작 화면에서 매장 선택</p>
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store 테이블에서 모든 매장 정보 조회</li>
     *   <li>StoreDTO 리스트로 반환</li>
     * </ol>
     * 
     * @return 모든 매장 정보 리스트
     */
    public List<StoreDTO> getAllStores() {
        List<StoreDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM store";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                StoreDTO store = new StoreDTO();
                store.setStoreId(rs.getInt("store_id"));
                store.setStoreName(rs.getString("store_name"));
                store.setStoreCode(rs.getString("store_code"));
                list.add(store);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 특정 매장의 재고 목록 조회 (JTable 표시용)
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store_inventory와 ingredient 테이블을 JOIN</li>
     *   <li>해당 매장의 재료명, 수량, 단위, 카테고리 조회</li>
     *   <li>카테고리와 재료명 순으로 정렬</li>
     * </ol>
     * 
     * <p><b>반환 형식:</b> JTable에 직접 사용 가능한 Vector<Vector<Object>></p>
     * 
     * @param storeId 매장 ID
     * @return 재고 정보 테이블 데이터
     */
    public Vector<Vector<Object>> getStoreInventory(int storeId) {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT i.ingredient_name, si.quantity, i.unit, i.category_name " +
                "FROM store_inventory si " +
                "JOIN ingredient i ON si.ingredient_id = i.ingredient_id " +
                "WHERE si.store_id = ? " +
                "ORDER BY i.category_name, i.ingredient_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, storeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("ingredient_name"));
                    row.add(rs.getInt("quantity"));
                    row.add(rs.getString("unit"));
                    row.add(rs.getString("category_name"));
                    data.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 모든 재료 목록 조회
     * 
     * <p><b>사용처:</b> 발주 다이얼로그의 재료 선택 콤보박스</p>
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>ingredient 테이블에서 모든 재료 정보 조회</li>
     *   <li>재료명 순으로 정렬</li>
     *   <li>IngredientDTO 리스트로 반환</li>
     * </ol>
     * 
     * @return 모든 재료 정보 리스트
     */
    public List<IngredientDTO> getAllIngredients() {
        List<IngredientDTO> list = new ArrayList<>();
        String sql = "SELECT ingredient_id, ingredient_name, unit FROM ingredient ORDER BY ingredient_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new IngredientDTO(
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

    /**
     * 발주 요청 등록
     * 
     * <p><b>DB 흐름:</b></p>
     * <ol>
     *   <li>store_material_request 테이블에 발주 요청 INSERT</li>
     *   <li>status는 'PENDING'으로 설정 (본사 승인 대기 상태)</li>
     *   <li>request_date는 자동으로 현재 시간 설정</li>
     * </ol>
     * 
     * <p><b>다음 단계:</b> 본사 관리자가 HQMainView에서 승인/반려 처리</p>
     * 
     * @param storeId 매장 ID
     * @param ingredientId 재료 ID
     * @param count 발주 수량
     * @return 등록 성공 여부
     */
    public boolean requestSupply(int storeId, int ingredientId, int count) {
        String sql = "INSERT INTO store_material_request (store_id, ingredient_id, quantity, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, storeId);
            pstmt.setInt(2, ingredientId);
            pstmt.setInt(3, count);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}