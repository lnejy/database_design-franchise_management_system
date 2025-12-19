package common.dto;

/**
 * 매장 재고 정보를 담는 데이터 전송 객체(DTO)입니다.
 *
 * DB 테이블 store_inventory (ingredient 와 조인) 의 한 행을 표현하며,
 * 매장 재고 관리 화면(StoreMainView)에서 재고 현황을 표시할 때 사용됩니다.
 * 또한 재고 부족 알림, 발주 권장, 주문 시 재고 차감 확인 등의 기능에 활용됩니다.
 *
 * DB 처리 흐름 요약
 * - 조회: store_inventory 와 ingredient 를 조인하여 현재 재고 현황을 조회합니다.
 * - 차감: 주문 발생 시 menu_recipe 정보를 기반으로 재고를 자동 차감합니다(OrderDAO).
 * - 증가: 본사에서 발주를 승인하면 해당 재료의 재고를 증가시킵니다(HQDAO).
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
     * 재고 상태를 판별하여 문자열로 반환합니다.
     *
     * 현재 재고가 최소 기준보다 낮으면 "부족 (발주필요)" 를,
     * 그렇지 않으면 "양호" 를 반환합니다.
     *
     * @return 재고 상태를 나타내는 문자열
     */
    public String getStatus() {
        if (quantity < minThreshold) {
            return "부족 (발주필요)";
        }
        return "양호";
    }
}