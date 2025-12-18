package common.dto;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CartItemDTO {
    private MenuDTO menu;
    private int quantity;
    private boolean isSet; // 세트 여부 (true: 세트, false: 단품)
    private List<MenuOptionDTO> options; // 옵션 선택

    public CartItemDTO(MenuDTO menu, int quantity, boolean isSet) {
        this.menu = menu;
        this.quantity = quantity;
        this.isSet = isSet;
    }

    public CartItemDTO(MenuDTO menu, int quantity, boolean isSet, List<MenuOptionDTO> options) {
        this.menu = menu;
        this.quantity = quantity;
        this.isSet = isSet;
        this.options = (options == null) ? new ArrayList<>() : options;
    }

    public MenuDTO getMenu() { return menu; }
    public int getQuantity() { return quantity; }
    public boolean isSet() { return isSet; }

    // [추가] 옵션 목록 반환
    public List<MenuOptionDTO> getOptions() { return options; }

    // [추가] 옵션 추가금 합계
    public int getOptionTotalPrice() {
        return options.stream().mapToInt(MenuOptionDTO::getDeltaPrice).sum();
    }

    // 단품/세트 여부에 따른 가격 반환
    public int getUnitPrice() {
        int base = isSet ? menu.getSetPrice() : menu.getPrice();
        return base + getOptionTotalPrice();
    }

    // 총합계 (가격 * 수량)
    public int getSubTotal() {
        return getUnitPrice() * quantity;
    }

    // [추가] 화면 표시용 옵션 요약 문자열
    public String getOptionSummary() {
        if (options == null || options.isEmpty()) return "";
        return options.stream()
                .map(MenuOptionDTO::getOptionName)
                .collect(Collectors.joining(", "));
    }
}