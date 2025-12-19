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

/**
 * 매장 관리 메인 화면 컨트롤러
 * 
 * 주요 기능:
 * - 재고 현황 로드 및 갱신
 * - 매출 내역 로드 및 갱신
 * - 주방 주문 현황 관리
 * - 발주 요청 다이얼로그 처리
 * - 주문 완료 처리
 * 
 * @author Franchise Management System
 */
public class StoreMainController {
    /** 뷰 객체 */
    private StoreMainView view;
    
    /** 매장 정보 */
    private StoreDTO store;
    
    /** 매장 DAO */
    private StoreDAO storeDAO;
    
    /** 매출 DAO */
    private SalesDAO salesDAO;
    
    /** 주방 DAO */
    private KitchenDAO kitchenDAO;

    /**
     * 컨트롤러 생성자
     * 
     * UI 초기화 후 초기 데이터를 로드하고 이벤트 리스너를 등록합니다.
     * 
     * @param view 매장 메인 화면 뷰
     * @param store 매장 정보
     */
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

    /**
     * 재고 현황 로드
     * 
     * 현재 매장의 재고 정보를 조회하여 테이블에 표시합니다.
     */
    private void loadInventory() {
        DefaultTableModel model = view.getInventoryModel();
        model.setRowCount(0);
        Vector<Vector<Object>> data = storeDAO.getStoreInventory(store.getStoreId());
        for (Vector<Object> row : data) {
            model.addRow(row);
        }
    }

    /**
     * 매출 내역 로드
     * 
     * 현재 매장의 주문 내역과 총 매출액을 조회하여 표시합니다.
     */
    private void loadSalesHistory() {
        DefaultTableModel model = view.getSalesModel();
        model.setRowCount(0);
        Vector<Vector<Object>> data = salesDAO.getOrderHistory(store.getStoreId());
        for (Vector<Object> row : data) {
            model.addRow(row);
        }
        
        // 총 매출액 계산 및 표시
        int total = salesDAO.getTotalSales(store.getStoreId());
        view.getLblTotalSales().setText("총 매출: " + String.format("%,d", total) + "원");
    }

    /**
     * 발주 요청 다이얼로그 열기
     * 
     * 재료 발주 요청 다이얼로그를 표시하고,
     * 발주 성공 시 재고 현황을 자동으로 갱신합니다.
     */
    private void openOrderDialog() {
        OrderIngredientDialog dialog = new OrderIngredientDialog(view, store.getStoreId());
        if (dialog.isSuccess()) {
            loadInventory(); // 발주 성공 시 재고 현황 갱신
        }
    }

    /**
     * 주방 주문 목록 로드
     * 
     * 현재 대기 중인 주문 목록을 조회하여 주방 화면에 표시합니다.
     * 데이터 로드 후 행 높이를 자동으로 조절합니다.
     */
    private void loadKitchen() {
        DefaultTableModel model = view.getKitchenModel();
        model.setRowCount(0);
        Vector<Vector<Object>> data = kitchenDAO.getPendingOrders(store.getStoreId());
        for (Vector<Object> row : data) {
            model.addRow(row);
        }
        
        // 주문 내역 길이에 따라 행 높이 자동 조절
        view.adjustKitchenRowHeights();
    }

    /**
     * 주문 완료 처리
     * 
     * 선택된 주문을 완료 상태로 변경합니다.
     * 완료 처리 시:
     * 1. 주문 상태를 'COMPLETED'로 변경
     * 2. 고객에게 알림 전송 (폴링을 통해 확인)
     * 3. 주방 목록 및 매출 내역 갱신
     */
    private void completeOrder() {
        JTable table = view.getKitchenTable();
        int row = table.getSelectedRow();
        
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "완료할 주문을 선택하세요.");
            return;
        }

        // 숨겨진 ID 컬럼에서 주문 ID 가져오기
        int orderId = (int) table.getValueAt(row, 0);
        
        if (kitchenDAO.completeOrder(orderId)) {
            JOptionPane.showMessageDialog(
                view, 
                "조리 완료 처리되었습니다.\n고객에게 알림이 전송됩니다."
            );
            
            // 화면 갱신
            loadKitchen();        // 주방 목록 갱신
            loadSalesHistory();   // 매출 탭 갱신 (상태 변경 반영)
        } else {
            JOptionPane.showMessageDialog(view, "처리 실패");
        }
    }
}