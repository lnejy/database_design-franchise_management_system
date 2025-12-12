package common.dto;

public class MenuDTO {
    private int menuId;
    private String menuName;
    private int price;
    private String category;
    private String description;
    private boolean isSoldOut;

    public MenuDTO(int menuId, String menuName, int price, String category, String description, boolean isSoldOut) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.category = category;
        this.description = description;
        this.isSoldOut = isSoldOut;
    }

    // Getters
    public int getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public boolean isSoldOut() { return isSoldOut; }

    @Override
    public String toString() {
        return menuName; // 리스트 등에 표시될 때 이름만 나오게
    }
}