package client_store.controller;

import client_store.dao.InventoryDAO;
import client_store.view.StoreMainView;
import common.dto.InventoryDTO;
import common.dto.StoreDTO;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class StoreMainController {
    private StoreMainView view;
    private InventoryDAO dao;
    private StoreDTO currentStore; // 로그인한 매장 정보

    public StoreMainController(StoreMainView view, StoreDTO store) {
        this.view = view;
        this.currentStore = store;
        this.dao = new InventoryDAO();

        // 1. 초기 화면 설정
        view.setStoreName(currentStore.getStoreName());
        view.setVisible(true);

        // 2. 재고 데이터 로드
        loadInventoryData();

        // 3. 이벤트 연결
        view.setRefreshButtonListener(e -> loadInventoryData());
    }

    // DB에서 재고 가져와서 테이블에 채우기
    private void loadInventoryData() {
        try {
            // 기존 데이터 지우기
            view.getTableModel().setRowCount(0);

            // DAO 호출 (로그인한 매장 ID 사용)
            List<InventoryDTO> list = dao.getStoreInventory(currentStore.getStoreId());

            for (InventoryDTO dto : list) {
                Object[] rowData = {
                        dto.getIngredientId(),
                        dto.getIngredientName(),
                        dto.getQuantity(),
                        dto.getUnit(),
                        dto.getMinThreshold(),
                        dto.getStatus() // "부족" or "양호"
                };
                view.getTableModel().addRow(rowData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "재고 정보를 불러오는데 실패했습니다.\n" + e.getMessage(),
                    "DB 에러", JOptionPane.ERROR_MESSAGE);
        }
    }
}