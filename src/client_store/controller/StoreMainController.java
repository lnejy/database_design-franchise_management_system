package client_store.controller;

import client_store.dao.StoreDAO;
import client_store.dao.SalesDAO;
import client_store.dao.KitchenDAO; // 추가
import client_store.view.StoreMainView;
import client_store.view.OrderIngredientDialog;
import common.dto.StoreDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class StoreMainController {
    private StoreMainView view;
    private StoreDTO store;
    private StoreDAO storeDAO;
    private SalesDAO salesDAO;
    private KitchenDAO kitchenDAO;

    public StoreMainController(StoreMainView view, StoreDTO store) {
        this.view = view;
        this.store = store;
        this.storeDAO = new StoreDAO();
        this.salesDAO = new SalesDAO();
        this.kitchenDAO = new KitchenDAO();

        view.initUI(store);

        // 초기 로드
        loadInventory();
        loadSalesHistory();
        loadKitchen(); // 추가

        // 리스너 등록
        view.getBtnRefreshInventory().addActionListener(e -> loadInventory());
        view.getBtnOrderIngredient().addActionListener(e -> openOrderDialog());
        view.getBtnRefreshSales().addActionListener(e -> loadSalesHistory());
        
        // 주방 리스너 추가
        view.getBtnKitchenRefresh().addActionListener(e -> loadKitchen());
        view.getBtnCompleteOrder().addActionListener(e -> completeOrder());
    }

    private void loadInventory() {
        DefaultTableModel model = view.getInventoryModel();
        model.setRowCount(0);
        Vector<Vector<Object>> data = storeDAO.getStoreInventory(store.getStoreId());
        for (Vector<Object> row : data) model.addRow(row);
    }

    private void loadSalesHistory() {
        DefaultTableModel model = view.getSalesModel();
        model.setRowCount(0);
        Vector<Vector<Object>> data = salesDAO.getOrderHistory(store.getStoreId());
        for (Vector<Object> row : data) model.addRow(row);
        int total = salesDAO.getTotalSales(store.getStoreId());
        view.getLblTotalSales().setText("총 매출: " + String.format("%,d", total) + "원");
    }

    private void openOrderDialog() {
        OrderIngredientDialog dialog = new OrderIngredientDialog(view, store.getStoreId());
        if (dialog.isSuccess()) {
            loadInventory();
        }
    }

    // 주방 목록 로드
    private void loadKitchen() {
        DefaultTableModel model = view.getKitchenModel();
        model.setRowCount(0);
        Vector<Vector<Object>> data = kitchenDAO.getPendingOrders(store.getStoreId());
        for (Vector<Object> row : data) model.addRow(row);
        view.adjustKitchenRowHeights(); // 데이터 다 넣고 나서 행 높이 자동 조절
    }

    // 준비 완료 처리
    private void completeOrder() {
        JTable table = view.getKitchenTable();
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "완료할 주문을 선택하세요.");
            return;
        }

        int orderId = (int) table.getValueAt(row, 0); // 0번 컬럼(ID) 가져오기
        if (kitchenDAO.completeOrder(orderId)) {
            JOptionPane.showMessageDialog(view, "조리 완료 처리되었습니다.\n고객에게 알림이 전송됩니다.");
            loadKitchen(); // 목록 갱신
            loadSalesHistory(); // 매출 탭도 갱신(상태변경 반영)
        } else {
            JOptionPane.showMessageDialog(view, "처리 실패");
        }
    }
}