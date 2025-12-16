package common.dto;

public class CartItemDTO {
    private MenuDTO menu;
    private int quantity;
    private boolean isSet; // 세트 여부 (true: 세트, false: 단품)

    public CartItemDTO(MenuDTO menu, int quantity, boolean isSet) {
        this.menu = menu;
        this.quantity = quantity;
        this.isSet = isSet;
    }

    public MenuDTO getMenu() { return menu; }
    public int getQuantity() { return quantity; }
    public boolean isSet() { return isSet; }

    // 단품/세트 여부에 따른 가격 반환
    public int getUnitPrice() {
        return isSet ? menu.getSetPrice() : menu.getPrice();
    }

    // 총합계 (가격 * 수량)
    public int getSubTotal() {
        return getUnitPrice() * quantity;
    }
}