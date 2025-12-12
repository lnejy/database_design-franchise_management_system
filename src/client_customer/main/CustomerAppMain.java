package client_customer.main;

import client_customer.view.CustomerMainView;
import client_store.dao.StoreDAO;
import common.dto.StoreDTO;

import javax.swing.*;
import java.util.List;

public class CustomerAppMain {
    public static void main(String[] args) {
        // UI 스타일 설정
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {

            // 1. DB에서 매장 목록 가져오기
            StoreDAO dao = new StoreDAO();
            List<StoreDTO> stores = dao.getAllStores();

            if (stores.isEmpty()) {
                JOptionPane.showMessageDialog(null, "등록된 매장이 없습니다.\n점주 프로그램에서 매장을 먼저 등록해주세요.");
                return; // 프로그램 종료
            }

            // 2. 매장 선택 팝업 띄우기 (ComboBox)
            // stores.toArray()를 통해 List를 배열로 변환하여 콤보박스에 넣음
            StoreDTO selectedStore = (StoreDTO) JOptionPane.showInputDialog(
                    null,
                    "주문하실 매장을 선택해주세요:",
                    "매장 선택 (키오스크 설정)",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    stores.toArray(),
                    stores.get(0) // 기본값
            );

            // 3. 선택 결과 처리
            if (selectedStore != null) {
                // 선택된 매장 정보를 가지고 키오스크 화면 실행
                new CustomerMainView(selectedStore.getStoreId(), selectedStore.getStoreName());
            } else {
                // 취소 누르면 종료
                System.exit(0);
            }
        });
    }
}