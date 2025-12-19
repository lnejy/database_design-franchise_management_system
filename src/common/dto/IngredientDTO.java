package common.dto;

/**
 * 재료 정보를 담는 데이터 전송 객체(DTO)입니다.
 *
 * DB 테이블 ingredient 의 한 행을 표현하며,
 * 발주 다이얼로그에서 재료를 선택하거나
 * 재고 관리 화면에서 재료 정보를 표시할 때 사용됩니다.
 * 또한 레시피 정보와 연계하여 주문 시 재고 차감 계산에 활용됩니다.
 *
 * @author Franchise Management System
 */
public class IngredientDTO {
    /** 재료 고유 ID (PK) */
    private int id;
    
    /** 재료명 (예: 빵, 패티, 양상추) */
    private String name;
    
    /** 단위 (예: 개, kg, g, L) */
    private String unit;

    /**
     * 재료 DTO 생성자
     * 
     * @param id 재료 ID
     * @param name 재료명
     * @param unit 단위
     */
    public IngredientDTO(int id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    // ========== Getters ==========

    public int getId() { 
        return id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public String getUnit() { 
        return unit; 
    }

    /**
     * 콤보박스 등에서 표시될 문자열 반환
     * 
     * @return "재료명 (단위)" 형식의 문자열
     */
    @Override
    public String toString() {
        return name + " (" + unit + ")";
    }
}