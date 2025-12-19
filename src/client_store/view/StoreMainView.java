package client_store.view;

import common.dto.StoreDTO;
import common.ui.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn; 
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 매장 관리 메인 화면
 * 
 * 주요 기능:
 * - 재고 현황 조회 및 발주 요청
 * - 매출 내역 조회
 * - 주방 주문 현황 관리 (KDS - Kitchen Display System)
 * 
 * @author Franchise Management System
 */
public class StoreMainView extends JFrame {
    /** 탭 패널 */
    private JTabbedPane tabbedPane;

    // 탭1: 재고 관리
    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;
    private JButton btnRefreshInventory, btnOrderIngredient;

    // 탭2: 매출 관리
    private JTable salesTable;
    private DefaultTableModel salesModel;
    private JLabel lblTotalSales;
    private JButton btnRefreshSales;

    // 탭3: 주방 현황 (KDS)
    private JTable kitchenTable;
    private DefaultTableModel kitchenModel;
    private JButton btnKitchenRefresh, btnCompleteOrder;
    
    /** 주방 테이블의 주문 내역 컬럼 인덱스 */
    private static final int KITCHEN_MENU_COLUMN_INDEX = 2;
    
    /** 주방 테이블의 기본 행 높이 */
    private static final int KITCHEN_DEFAULT_ROW_HEIGHT = 55;
    
    /** 주방 테이블의 최소 행 높이 */
    private static final int KITCHEN_MIN_ROW_HEIGHT = 35;
    
    /** 주방 테이블 행 높이 여백 */
    private static final int KITCHEN_ROW_HEIGHT_PADDING = 8;

    /**
     * UI 초기화
     * 
     * 매장 정보를 기반으로 화면을 구성합니다.
     * 재고, 매출, 주방 현황 세 개의 탭으로 구성됩니다.
     * 
     * @param store 매장 정보
     */
    public void initUI(StoreDTO store) {
        setTitle("가맹점 관리 - " + store.getStoreName());
        setSize(950, 600); // 화면 너비 살짝 늘림
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BASE_BG);

        tabbedPane = new JTabbedPane();

        // --- 1. 재고 탭 ---
        JPanel invPanel = UITheme.createSectionPanel(new BorderLayout(), "재고 현황");
        inventoryModel = new DefaultTableModel(new String[]{"재료명", "수량", "단위", "카테고리"}, 0);
        inventoryTable = new JTable(inventoryModel);
        UITheme.styleTable(inventoryTable);
        invPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        
        JPanel invBtnBox = UITheme.createSectionPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10), null);
        btnOrderIngredient = new JButton("재료 발주");
        UITheme.applyFilled(btnOrderIngredient, UITheme.PRIMARY_LIGHT, Color.WHITE, true);
        btnRefreshInventory = new JButton("새로고침");
        UITheme.applyFilled(btnRefreshInventory, UITheme.PRIMARY, Color.WHITE, false);
        invBtnBox.add(btnOrderIngredient);
        invBtnBox.add(btnRefreshInventory);
        invPanel.add(invBtnBox, BorderLayout.SOUTH);
        tabbedPane.addTab("재고 관리", invPanel);

        // --- 2. 매출 탭 ---
        JPanel salePanel = UITheme.createSectionPanel(new BorderLayout(), "매출 내역");
        salesModel = new DefaultTableModel(new String[]{"주문번호", "금액", "상태", "시간"}, 0);
        salesTable = new JTable(salesModel);
        UITheme.styleTable(salesTable);
        salePanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        
        JPanel saleBtnBox = UITheme.createSectionPanel(new BorderLayout(), null);
        lblTotalSales = new JLabel("총 매출: 0원", SwingConstants.CENTER);
        btnRefreshSales = new JButton("새로고침");
        UITheme.applyFilled(btnRefreshSales, UITheme.PRIMARY, Color.WHITE, false);
        saleBtnBox.add(lblTotalSales, BorderLayout.CENTER);
        saleBtnBox.add(btnRefreshSales, BorderLayout.EAST);
        salePanel.add(saleBtnBox, BorderLayout.SOUTH);
        tabbedPane.addTab("매출 내역", salePanel);

        // --- 3. 주방 현황 탭 (KDS) ---
        JPanel kitchenPanel = setupKitchenTab();

        kitchenPanel.add(new JScrollPane(kitchenTable), BorderLayout.CENTER);

        JPanel kitchenBtnBox = UITheme.createSectionPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10), null);
        btnKitchenRefresh = new JButton("새로고침");
        btnCompleteOrder = new JButton("준비 완료 (호출)");
        UITheme.applyFilled(btnKitchenRefresh, UITheme.PRIMARY_LIGHT, Color.WHITE, false);
        UITheme.applyFilled(btnCompleteOrder, UITheme.SUCCESS, Color.WHITE, true);
        
        kitchenBtnBox.add(btnKitchenRefresh);
        kitchenBtnBox.add(btnCompleteOrder);
        kitchenPanel.add(kitchenBtnBox, BorderLayout.SOUTH);
        tabbedPane.addTab("주방 현황 (KDS)", kitchenPanel);


        add(tabbedPane);
        setVisible(true);
    }

    /**
     * 주방 현황 탭 설정
     * 
     * 주문 내역을 표시하는 테이블을 구성합니다.
     * 메뉴명과 옵션을 HTML 형식으로 표시하며, 옵션은 빨간색으로 강조됩니다.
     * 
     * @return 주방 현황 패널
     */
    private JPanel setupKitchenTab() {
        JPanel kitchenPanel = UITheme.createSectionPanel(new BorderLayout(), "주방 주문 흐름");
        
        // 테이블 컬럼 정의 (ID는 숨김 처리 예정)
        String[] kCols = {"ID", "주문번호", "주문 내역 (메뉴)", "금액", "주문시간"};
        
        // 테이블 내용 수정 불가 설정
        kitchenModel = new DefaultTableModel(kCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        kitchenTable = new JTable(kitchenModel);
        UITheme.styleTable(kitchenTable);
        
        // 주문 내역 컬럼에 커스텀 렌더러 설정 (HTML 형식 표시)
        setupKitchenCellRenderer();
        
        // 기본 행 높이 설정
        kitchenTable.setRowHeight(KITCHEN_DEFAULT_ROW_HEIGHT);
        
        // ID 컬럼 숨기기 (데이터는 필요하지만 화면에는 표시하지 않음)
        hideColumn(kitchenTable, 0);
        
        // 주문 내역 컬럼 너비 설정
        kitchenTable.getColumnModel().getColumn(KITCHEN_MENU_COLUMN_INDEX).setPreferredWidth(300);

        kitchenPanel.add(new JScrollPane(kitchenTable), BorderLayout.CENTER);

        // 버튼 패널
        JPanel kitchenBtnBox = UITheme.createSectionPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10), null);
        btnKitchenRefresh = new JButton("새로고침");
        btnCompleteOrder = new JButton("준비 완료 (호출)");
        UITheme.applyFilled(btnKitchenRefresh, UITheme.PRIMARY_LIGHT, Color.WHITE, false);
        UITheme.applyFilled(btnCompleteOrder, UITheme.SUCCESS, Color.WHITE, true);
        
        kitchenBtnBox.add(btnKitchenRefresh);
        kitchenBtnBox.add(btnCompleteOrder);
        kitchenPanel.add(kitchenBtnBox, BorderLayout.SOUTH);
        
        return kitchenPanel;
    }

    /**
     * 주방 테이블 셀 렌더러 설정
     * 
     * 주문 내역 컬럼에 HTML 형식으로 표시되도록 설정합니다.
     * 줄바꿈을 지원하고, 옵션 부분은 빨간색으로 강조 표시합니다.
     */
    private void setupKitchenCellRenderer() {
        kitchenTable.getColumnModel().getColumn(KITCHEN_MENU_COLUMN_INDEX).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column
                    );

                    String text = value == null ? "" : value.toString();

                    // 줄바꿈 처리 (\n을 <br>로 변환)
                    text = text.replace("\n", "<br>");

                    // 옵션 부분만 빨간색으로 표시 (예: [양파 추가, 치즈 추가])
                    text = text.replaceAll("\\[(.*?)\\]", "<font color='red'>[$1]</font>");

                    lbl.setText("<html>" + text + "</html>");
                    return lbl;
                }
            }
        );
    }

    /**
     * 특정 컬럼 숨기기
     * 
     * 테이블에서 특정 컬럼을 화면에 표시하지 않도록 설정합니다.
     * 데이터는 유지되지만 사용자에게는 보이지 않습니다.
     * 
     * @param table 대상 테이블
     * @param index 숨길 컬럼 인덱스
     */
    private void hideColumn(JTable table, int index) {
        TableColumn col = table.getColumnModel().getColumn(index);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setWidth(0);
        col.setPreferredWidth(0);
        col.setResizable(false); // 사용자 조절 불가
    }

    /**
     * 주방 테이블 행 높이 자동 조절
     * 
     * 주문 내역의 내용 길이에 따라 각 행의 높이를 자동으로 조절합니다.
     * 여러 메뉴가 포함된 주문의 경우 행 높이가 자동으로 늘어납니다.
     */
    public void adjustKitchenRowHeights() {
        for (int row = 0; row < kitchenTable.getRowCount(); row++) {
            // 현재 셀 렌더러로 컴포넌트 생성
            Component comp = kitchenTable.prepareRenderer(
                    kitchenTable.getCellRenderer(row, KITCHEN_MENU_COLUMN_INDEX),
                    row, KITCHEN_MENU_COLUMN_INDEX
            );

            int colWidth = kitchenTable.getColumnModel().getColumn(KITCHEN_MENU_COLUMN_INDEX).getWidth();

            // 렌더러 컴포넌트가 해당 폭에서 필요로 하는 높이를 계산
            comp.setBounds(0, 0, colWidth, Integer.MAX_VALUE);

            int preferredHeight = comp.getPreferredSize().height;

            // 최소 높이와 계산된 높이 중 큰 값을 사용 (여백 포함)
            int finalHeight = Math.max(KITCHEN_MIN_ROW_HEIGHT, preferredHeight + KITCHEN_ROW_HEIGHT_PADDING);
            kitchenTable.setRowHeight(row, finalHeight);
        }
    }


    // Getters
    public DefaultTableModel getInventoryModel() { return inventoryModel; }
    public DefaultTableModel getSalesModel() { return salesModel; }
    public DefaultTableModel getKitchenModel() { return kitchenModel; }

    public JButton getBtnRefreshInventory() { return btnRefreshInventory; }
    public JButton getBtnOrderIngredient() { return btnOrderIngredient; }
    public JButton getBtnRefreshSales() { return btnRefreshSales; }
    public JLabel getLblTotalSales() { return lblTotalSales; }
    
    public JTable getKitchenTable() { return kitchenTable; }
    public JButton getBtnKitchenRefresh() { return btnKitchenRefresh; }
    public JButton getBtnCompleteOrder() { return btnCompleteOrder; }


}