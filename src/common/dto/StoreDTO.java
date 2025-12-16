package common.dto;

/**
 * 매장 정보를 담는 데이터 전송 객체 (DTO)
 * 
 * <p><b>DB 테이블:</b> store</p>
 * <p><b>주요 사용처:</b></p>
 * <ul>
 *   <li>매장 로그인 인증 (LoginController)</li>
 *   <li>매장 정보 조회 및 등록 (StoreDAO)</li>
 *   <li>키오스크 매장 선택 (CustomerAppMain)</li>
 * </ul>
 * 
 * @author Franchise Management System
 */
public class StoreDTO {
    /** 매장 고유 ID (PK) */
    private int storeId;
    
    /** 매장명 (예: 강남점, 홍대점) */
    private String storeName;
    
    /** 매장 코드 (로그인 ID로 사용) */
    private String storeCode;
    
    /** 매장 연락처 (로그인 비밀번호로 사용, 숫자만 저장) */
    private String phone;
    
    /** 매장 주소 */
    private String address;
    
    /** 점장 이름 */
    private String managerName;

    // ========== Getters and Setters ==========

    public int getStoreId() { 
        return storeId; 
    }
    
    public void setStoreId(int storeId) { 
        this.storeId = storeId; 
    }

    public String getStoreName() { 
        return storeName; 
    }
    
    public void setStoreName(String storeName) { 
        this.storeName = storeName; 
    }

    public String getStoreCode() { 
        return storeCode; 
    }
    
    public void setStoreCode(String storeCode) { 
        this.storeCode = storeCode; 
    }

    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }

    public String getAddress() { 
        return address; 
    }
    
    public void setAddress(String address) { 
        this.address = address; 
    }

    public String getManagerName() { 
        return managerName; 
    }
    
    public void setManagerName(String managerName) { 
        this.managerName = managerName; 
    }

    /**
     * 콤보박스 등에서 표시될 문자열 반환
     * 
     * @return 매장명
     */
    @Override
    public String toString() {
        return storeName;
    }
}