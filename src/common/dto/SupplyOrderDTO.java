package common.dto;

/**
 * 발주 요청 정보를 담는 데이터 전송 객체(DTO)입니다.
 *
 * 매장이 본사에 자재 발주를 신청할 때 사용되는 데이터 구조로,
 * DB 테이블 store_material_request (store, ingredient 와 조인) 의 한 행을 표현합니다.
 *
 * 주요 사용처
 * - 본사 발주 관리 화면(HQMainView)에서 발주 목록을 표시할 때 사용됩니다.
 * - 발주 승인, 반려 등 상태를 변경하는 로직에서 사용됩니다.
 *
 * DB 처리 흐름 요약
 * 1. 발주 요청: 매장에서 신청하면 store_material_request 에 INSERT 되고 status 는 'PENDING' 으로 저장됩니다.
 * 2. 발주 승인: 본사에서 승인 시 status 를 'APPROVED' 로 변경하고, store_inventory 재고를 증가시킵니다.
 * 3. 발주 반려: 본사에서 반려 시 status 를 'REJECTED' 로 변경합니다.
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
     * JTable에 표시하기 위한 Object 배열로 변환합니다.
     *
     * 테이블 모델에 직접 추가할 수 있도록
     * 이 DTO 의 주요 필드를 한 행(row)에 해당하는 배열 형태로 반환합니다.
     *
     * @return 테이블 행 데이터 배열
     */
    public Object[] toRow() {
        return new Object[] { orderId, storeName, ingredientName, quantity, status, requestDate };
    }
}