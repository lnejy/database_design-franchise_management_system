package common.dto;

import java.sql.Timestamp;

public class StoreDTO {
    private int storeId;
    private String storeName;
    private String storeCode; // 로그인 ID로 사용
    private String address;
    private String phoneNumber; // 로그인 PW로 사용 (임시)
    private String managerName;
    private boolean isActive;
    private Timestamp createdAt;

    // 기본 생성자
    public StoreDTO() {}

    // 전체 생성자
    public StoreDTO(int storeId, String storeName, String storeCode, String address,
                    String phoneNumber, String managerName, boolean isActive, Timestamp createdAt) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeCode = storeCode;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.managerName = managerName;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return storeName;
    }

    // Getters and Setters
    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}