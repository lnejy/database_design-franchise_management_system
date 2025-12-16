package common.dto;

public class MenuDTO {
    private int menuId;
    private String menuName;
    private int price;
    private int setPrice;
    private String category;
    private String description;
    private boolean isSoldOut;

    public MenuDTO(int menuId, String menuName, int price, int setPrice, String category, String description, boolean isSoldOut) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.setPrice = setPrice;
        this.category = category;
        this.description = description;
        this.isSoldOut = isSoldOut;
    }

    public int getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public int getPrice() { return price; }
    public int getSetPrice() { return setPrice; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public boolean isSoldOut() { return isSoldOut; }

    @Override
    public String toString() { return menuName; }
}