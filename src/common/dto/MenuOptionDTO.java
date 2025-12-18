package common.dto;

public class MenuOptionDTO {
    private int optionId;
    private String optionName;
    private int deltaPrice;

    public MenuOptionDTO(int optionId, String optionName, int deltaPrice) {
        this.optionId = optionId;
        this.optionName = optionName;
        this.deltaPrice = deltaPrice;
    }

    public int getOptionId() { return optionId; }
    public String getOptionName() { return optionName; }
    public int getDeltaPrice() { return deltaPrice; }
}
