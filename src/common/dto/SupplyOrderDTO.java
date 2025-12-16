package common.dto;

/**
 * 발주 요청 정보를 담는 데이터 전송 객체 (DTO)
 * 
 * <p><b>DB 테이블:</b> store_material_request (JOIN store, ingredient)</p>
 * <p><b>주요 사용처:</b></p>
 * <ul>
 *   <li>본사 발주 관리 화면 (HQMainView)</li>
 *   <li>발주 승인/반려 처리</li>
 * </ul>
 * 
 * <p><b>DB 흐름:</b></p>
 * <ol>
 *   <li><b>발주 요청:</b> 매장에서 발주 신청 → store_material_request INSERT (status='PENDING')</li>
 *   <li><b>발주 승인:</b> 본사에서 승인 → status='APPROVED', store_inventory 재고 증가</li>
 *   <li><b>발주 반려:</b> 본사에서 반려 → status='REJECTED'</li>
 * </ol>
 * 
 * @author Franchise Management System
 */
public class SupplyOrderDTO {
    /** 발주 ID (PK) */
    private int orderId;
    
    /** 매장명 (JOIN으로 가져옴) */
    private String storeName;
    
    /** 재료명 (JOIN으로 가져옴) */
    private String ingredientName;
    
    /** 발주 수량 */
    private int quantity;
    
    /** 발주 상태 (PENDING, APPROVED, REJECTED) */
    private String status;
    
    /** 요청 일시 */
    private String requestDate;

    /**
     * 발주 DTO 생성자
     * 
     * @param orderId 발주 ID
     * @param storeName 매장명
     * @param ingredientName 재료명
     * @param quantity 수량
     * @param status 상태
     * @param requestDate 요청 일시
     */
    public SupplyOrderDTO(int orderId, String storeName, String ingredientName, int quantity, String status, String requestDate) {
        this.orderId = orderId;
        this.storeName = storeName;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.status = status;
        this.requestDate = requestDate;
    }

    // ========== Getters ==========

    public int getOrderId() { 
        return orderId; 
    }
    
    public String getStoreName() { 
        return storeName; 
    }
    
    public String getIngredientName() { 
        return ingredientName; 
    }
    
    public int getQuantity() { 
        return quantity; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public String getRequestDate() { 
        return requestDate; 
    }

    /**
     * JTable에 표시하기 위한 Object 배열로 변환
     * 
     * <p>테이블 모델에 직접 추가할 수 있도록 모든 필드를 배열로 반환합니다.</p>
     * 
     * @return 테이블 행 데이터 배열
     */
    public Object[] toRow() {
        return new Object[] { orderId, storeName, ingredientName, quantity, status, requestDate };
    }
}