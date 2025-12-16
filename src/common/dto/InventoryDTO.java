package common.dto;

/**
 * 매장 재고 정보를 담는 데이터 전송 객체 (DTO)
 * 
 * <p><b>DB 테이블:</b> store_inventory (JOIN ingredient)</p>
 * <p><b>주요 사용처:</b></p>
 * <ul>
 *   <li>매장 재고 관리 화면 (StoreMainView)</li>
 *   <li>재고 부족 알림 및 발주 권장</li>
 *   <li>주문 시 재고 차감 확인</li>
 * </ul>
 * 
 * <p><b>DB 흐름:</b></p>
 * <ul>
 *   <li>조회: store_inventory JOIN ingredient → 재고 현황 표시</li>
 *   <li>차감: 주문 시 menu_recipe 기반으로 자동 차감 (OrderDAO)</li>
 *   <li>증가: 본사 발주 승인 시 재고 증가 (HQDAO)</li>
 * </ul>
 * 
 * @author Franchise Management System
 */
public class InventoryDTO {
    /** 재료 ID (FK → ingredient.ingredient_id) */
    private final int ingredientId;
    
    /** 재료명 (JOIN으로 가져옴) */
    private final String ingredientName;
    
    /** 현재 재고 수량 */
    private int quantity;
    
    /** 최소 재고 기준 (이 값보다 낮으면 발주 필요) */
    private int minThreshold;
    
    /** 단위 (kg, g, 개, L 등) */
    private String unit;

    /**
     * 재고 DTO 생성자
     * 
     * @param ingredientId 재료 ID
     * @param ingredientName 재료명
     * @param quantity 현재 재고
     * @param minThreshold 최소 재고 기준
     * @param unit 단위
     */
    public InventoryDTO(int ingredientId, String ingredientName, int quantity, int minThreshold, String unit) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.unit = unit;
    }

    // ========== Getters ==========

    public int getIngredientId() { 
        return ingredientId; 
    }
    
    public String getIngredientName() { 
        return ingredientName; 
    }
    
    public int getQuantity() { 
        return quantity; 
    }
    
    public int getMinThreshold() { 
        return minThreshold; 
    }
    
    public String getUnit() { 
        return unit; 
    }

    /**
     * 재고 상태를 판별하여 문자열로 반환
     * 
     * <p>현재 재고가 최소 기준보다 낮으면 "부족 (발주필요)"를 반환하고,
     * 그렇지 않으면 "양호"를 반환합니다.</p>
     * 
     * @return 재고 상태 문자열
     */
    public String getStatus() {
        if (quantity < minThreshold) {
            return "부족 (발주필요)";
        }
        return "양호";
    }
}