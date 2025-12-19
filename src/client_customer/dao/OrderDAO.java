package client_customer.dao;

import common.db.DBConnection;
import common.dto.CartItemDTO;
import common.dto.MenuOptionDTO;
import java.sql.*;
import java.util.List;

/**
 * 주문 관련 데이터베이스 접근 객체 (DAO)
 * 
 * 담당 테이블: customer_order, order_detail, order_detail_option, store_inventory, store_sale
 * 
 * 주요 기능:
 * - 주문 처리 (트랜잭션 기반)
 * - 주문 번호 조회
 * - 주문 상태 조회 (폴링용)
 * 
 * @author Franchise Management System
 */
public class OrderDAO {

    /** 주문 실패 시 반환하는 값 */
    private static final int ORDER_FAILED = -1;
    
    /** 주문 상태: 대기 중 */
    private static final String ORDER_STATUS_WAITING = "WAITING";
    
    /** 주문 상태: 완료 */
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    
    /** 결제 방법: 카드 */
    private static final String PAYMENT_METHOD_CARD = "CARD";
    
    /** 옵션 수량 기본값 (현재는 1로 고정) */
    private static final int DEFAULT_OPTION_QUANTITY = 1;

    /**
     * 주문 처리 (트랜잭션)
     * 
     * 주문 처리의 전체 흐름:
     * 1. 주문 번호 생성
     * 2. 주문 헤더 저장 (customer_order 테이블)
     * 3. 주문 상세 저장 및 재고 차감 (각 메뉴별로 반복)
     *    - order_detail 테이블에 주문 상세 저장
     *    - menu_recipe를 기반으로 기본 재료 재고 차감
     *    - 선택된 옵션이 있으면 order_detail_option 저장 및 옵션 재고 차감
     * 4. 매출 기록 저장 (store_sale 테이블)
     * 5. 트랜잭션 커밋
     * 
     * 모든 단계가 성공해야 커밋되며, 하나라도 실패하면 롤백됩니다.
     * 
     * @param storeId 매장 ID
     * @param cartItems 장바구니 항목 리스트
     * @param totalAmount 총 결제 금액
     * @return 성공 시 주문 ID, 실패 시 -1
     */
    public int placeOrder(int storeId, List<CartItemDTO> cartItems, int totalAmount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1단계: 주문 번호 생성
            String orderNumber = generateOrderNumber();

            // 2단계: 주문 헤더 저장 및 주문 ID 획득
            int orderId = insertOrderHeader(conn, storeId, orderNumber, totalAmount);
            if (orderId == 0) {
                throw new SQLException("주문 헤더 저장 실패");
            }

            // 3단계: 주문 상세 저장 및 재고 차감
            processOrderItems(conn, orderId, storeId, cartItems);

            // 4단계: 매출 기록 저장
            insertSaleRecord(conn, orderId, totalAmount);

            // 모든 작업 성공 시 커밋
            conn.commit();
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();
            rollbackTransaction(conn);
            return ORDER_FAILED;
        } finally {
            restoreAutoCommit(conn);
        }
    }

    /**
     * 주문 번호 생성
     * 
     * 현재는 간단히 시간 기반으로 생성하지만,
     * 실제 운영 환경에서는 "ORDER-YYYYMMDD-XXXX" 형식 권장
     * 
     * @return 생성된 주문 번호
     */
    private String generateOrderNumber() {
        return String.valueOf(System.currentTimeMillis() % 10000);
    }

    /**
     * 주문 헤더 저장
     * 
     * customer_order 테이블에 주문 기본 정보를 저장하고
     * 생성된 주문 ID를 반환합니다.
     * 
     * @param conn 데이터베이스 연결
     * @param storeId 매장 ID
     * @param orderNumber 주문 번호
     * @param totalAmount 총 결제 금액
     * @return 생성된 주문 ID (실패 시 0)
     * @throws SQLException 데이터베이스 오류 시 발생
     */
    private int insertOrderHeader(Connection conn, int storeId, String orderNumber, int totalAmount) throws SQLException {
        String sql = "INSERT INTO customer_order (store_id, order_number, total_amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, storeId);
            pstmt.setString(2, orderNumber);
            pstmt.setInt(3, totalAmount);
            pstmt.setString(4, ORDER_STATUS_WAITING);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * 주문 상세 항목 처리
     * 
     * 각 장바구니 항목에 대해:
     * 1. order_detail 테이블에 주문 상세 저장
     * 2. menu_recipe를 기반으로 기본 재료 재고 차감
     * 3. 선택된 옵션이 있으면 order_detail_option 저장 및 옵션 재고 차감
     * 
     * @param conn 데이터베이스 연결
     * @param orderId 주문 ID
     * @param storeId 매장 ID
     * @param cartItems 장바구니 항목 리스트
     * @throws SQLException 데이터베이스 오류 시 발생
     */
    private void processOrderItems(Connection conn, int orderId, int storeId, List<CartItemDTO> cartItems) throws SQLException {
        String sqlDetail = "INSERT INTO order_detail (order_id, menu_id, quantity, is_set, unit_price, subtotal_price) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE store_inventory si " +
                         "JOIN menu_recipe mr ON si.ingredient_id = mr.ingredient_id " +
                         "SET si.quantity = si.quantity - (mr.required_quantity * ?) " +
                         "WHERE si.store_id = ? AND mr.menu_id = ?";
        String sqlDetailOption = "INSERT INTO order_detail_option (detail_id, option_id, option_qty) VALUES (?, ?, ?)";
        String sqlStockOption = "UPDATE store_inventory si " +
                               "JOIN menu_option mo ON si.ingredient_id = mo.ingredient_id " +
                               "SET si.quantity = si.quantity - (mo.delta_quantity * ? * ?) " +
                               "WHERE si.store_id = ? AND mo.option_id = ?";

        try (PreparedStatement pstmtDetail = conn.prepareStatement(sqlDetail, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtStock = conn.prepareStatement(sqlStock);
             PreparedStatement pstmtDetailOption = conn.prepareStatement(sqlDetailOption);
             PreparedStatement pstmtStockOption = conn.prepareStatement(sqlStockOption)) {

            for (CartItemDTO item : cartItems) {
                // 주문 상세 저장
                int detailId = insertOrderDetail(pstmtDetail, orderId, item);
                
                // 기본 재료 재고 차감
                deductBaseIngredientStock(pstmtStock, storeId, item);
                
                // 옵션이 있으면 옵션 처리
                if (item.getOptions() != null && !item.getOptions().isEmpty()) {
                    processOrderOptions(pstmtDetailOption, pstmtStockOption, detailId, storeId, item);
                }
            }
        }
    }

    /**
     * 주문 상세 저장
     * 
     * @param pstmt PreparedStatement
     * @param orderId 주문 ID
     * @param item 장바구니 항목
     * @return 생성된 상세 ID
     * @throws SQLException 데이터베이스 오류 시 발생
     */
    private int insertOrderDetail(PreparedStatement pstmt, int orderId, CartItemDTO item) throws SQLException {
        pstmt.setInt(1, orderId);
        pstmt.setInt(2, item.getMenu().getMenuId());
        pstmt.setInt(3, item.getQuantity());
        pstmt.setBoolean(4, item.isSet());
        pstmt.setInt(5, item.getUnitPrice());
        pstmt.setInt(6, item.getSubTotal());
        pstmt.executeUpdate();

        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * 기본 재료 재고 차감
     * 
     * menu_recipe 테이블을 참조하여 해당 메뉴에 필요한 재료의 재고를 차감합니다.
     * 차감량 = 메뉴당 필요량 × 주문 수량
     * 
     * @param pstmt PreparedStatement
     * @param storeId 매장 ID
     * @param item 장바구니 항목
     * @throws SQLException 데이터베이스 오류 시 발생
     */
    private void deductBaseIngredientStock(PreparedStatement pstmt, int storeId, CartItemDTO item) throws SQLException {
        pstmt.setInt(1, item.getQuantity());
        pstmt.setInt(2, storeId);
        pstmt.setInt(3, item.getMenu().getMenuId());
        pstmt.executeUpdate();
    }

    /**
     * 주문 옵션 처리
     * 
     * 선택된 옵션에 대해:
     * 1. order_detail_option 테이블에 옵션 정보 저장
     * 2. 옵션에 필요한 재료 재고 차감
     * 
     * @param pstmtDetailOption 주문 옵션 저장용 PreparedStatement
     * @param pstmtStockOption 옵션 재고 차감용 PreparedStatement
     * @param detailId 주문 상세 ID
     * @param storeId 매장 ID
     * @param item 장바구니 항목
     * @throws SQLException 데이터베이스 오류 시 발생
     */
    private void processOrderOptions(PreparedStatement pstmtDetailOption, PreparedStatement pstmtStockOption,
                                    int detailId, int storeId, CartItemDTO item) throws SQLException {
        for (MenuOptionDTO opt : item.getOptions()) {
            // 옵션 정보 저장
            pstmtDetailOption.setInt(1, detailId);
            pstmtDetailOption.setInt(2, opt.getOptionId());
            pstmtDetailOption.setInt(3, DEFAULT_OPTION_QUANTITY);
            pstmtDetailOption.executeUpdate();

            // 옵션 재료 재고 차감
            // 차감량 = 옵션당 필요량 × 주문 수량 × 옵션 수량
            pstmtStockOption.setInt(1, item.getQuantity());
            pstmtStockOption.setInt(2, DEFAULT_OPTION_QUANTITY);
            pstmtStockOption.setInt(3, storeId);
            pstmtStockOption.setInt(4, opt.getOptionId());
            pstmtStockOption.executeUpdate();
        }
    }

    /**
     * 매출 기록 저장
     * 
     * @param conn 데이터베이스 연결
     * @param orderId 주문 ID
     * @param totalAmount 총 결제 금액
     * @throws SQLException 데이터베이스 오류 시 발생
     */
    private void insertSaleRecord(Connection conn, int orderId, int totalAmount) throws SQLException {
        String sql = "INSERT INTO store_sale (order_id, payment_method, total_price) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setString(2, PAYMENT_METHOD_CARD);
            pstmt.setInt(3, totalAmount);
            pstmt.executeUpdate();
        }
    }

    /**
     * 트랜잭션 롤백
     * 
     * @param conn 데이터베이스 연결
     */
    private void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 자동 커밋 모드 복원
     * 
     * @param conn 데이터베이스 연결
     */
    private void restoreAutoCommit(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 주문 번호 조회
     * 
     * @param orderId 주문 ID
     * @return 주문 번호 (조회 실패 시 빈 문자열)
     */
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

    /**
     * 주문 상태 조회 (폴링용)
     * 
     * 고객이 주문 완료 후 주방에서 조리가 완료되었는지 확인하기 위해
     * 주기적으로 호출됩니다. (예: 3초마다)
     * 
     * @param orderId 주문 ID
     * @return 주문 상태 (WAITING, COMPLETED 등), 조회 실패 시 빈 문자열
     */
    public String checkOrderStatus(int orderId) {
        String sql = "SELECT status FROM customer_order WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}