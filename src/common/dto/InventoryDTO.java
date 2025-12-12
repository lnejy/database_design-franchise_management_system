package common.dto;

public class InventoryDTO {
    private final int ingredientId;
    private final String ingredientName;
    private int quantity;      // 현재 재고
    private int minThreshold;  // 안전 재고(발주 기준점)
    private String unit;       // 단위 (kg, g, 개 등)

    public InventoryDTO(int ingredientId, String ingredientName, int quantity, int minThreshold, String unit) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.unit = unit;
    }

    // Getters
    public int getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public int getQuantity() { return quantity; }
    public int getMinThreshold() { return minThreshold; }
    public String getUnit() { return unit; }

    // 상태 판별 로직 (View에서 사용하기 편하게)
    public String getStatus() {
        if (quantity < minThreshold) return "부족 (발주필요)";
        return "양호";
    }
}