package client_store.view;

import common.dto.StoreDTO;
import common.ui.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn; 
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;

public class StoreMainView extends JFrame {
    private JTabbedPane tabbedPane;

    // 탭1: 재고
    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;
    private JButton btnRefreshInventory, btnOrderIngredient;

    // 탭2: 매출
    private JTable salesTable;
    private DefaultTableModel salesModel;
    private JLabel lblTotalSales;
    private JButton btnRefreshSales;

    // 탭3: 주방 (KDS)
    private JTable kitchenTable;
    private DefaultTableModel kitchenModel;
    private JButton btnKitchenRefresh, btnCompleteOrder;

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

        // --- 3. 주방 현황 탭 ---
        JPanel kitchenPanel = UITheme.createSectionPanel(new BorderLayout(), "주방 주문 흐름");
        
        // 컬럼 변경: "주문 내역" 추가
        String[] kCols = {"ID", "주문번호", "주문 내역 (메뉴)", "금액", "주문시간"};
        
        // 테이블 내용 수정 불가하게 설정 (Override isCellEditable)
        kitchenModel = new DefaultTableModel(kCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        kitchenTable = new JTable(kitchenModel);
        kitchenTable.setRowHeight(25); // 행 높이 살짝 키움
        UITheme.styleTable(kitchenTable);

        this.kitchenTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                String text = value == null ? "" : value.toString();

                // 줄바꿈 처리
                text = text.replace("\n", "<br>");

                // 옵션 부분만 빨간색
                text = text.replaceAll("\\[(.*?)\\]", "<font color='red'>[$1]</font>");

                lbl.setText("<html>" + text + "</html>");
                return lbl;
            }
        });

        // 옵션 줄 때문에 행 높이 조금 키우기
        this.kitchenTable.setRowHeight(55);

        // 0번 컬럼(ID) 숨기기
        hideColumn(kitchenTable, 0);
        
        // 주문 내역 컬럼 너비 늘리기 (잘 보이라고)
        kitchenTable.getColumnModel().getColumn(2).setPreferredWidth(300);

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

    // 특정 컬럼 숨기기 메서드
    private void hideColumn(JTable table, int index) {
        TableColumn col = table.getColumnModel().getColumn(index);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setWidth(0);
        col.setPreferredWidth(0);
        col.setResizable(false); // 사용자 조절 불가
    }

    //행 높이 자동 조절 메서드
    public void adjustKitchenRowHeights() {
        int col = 2; // 주문 내역(메뉴) 컬럼

        for (int row = 0; row < kitchenTable.getRowCount(); row++) {
            // 현재 셀 렌더러로 컴포넌트 생성
            Component comp = kitchenTable.prepareRenderer(
                    kitchenTable.getCellRenderer(row, col),
                    row, col
            );

            int colWidth = kitchenTable.getColumnModel().getColumn(col).getWidth();

            // 렌더러 컴포넌트가 해당 폭에서 필요로 하는 높이를 계산하게 함
            comp.setBounds(0, 0, colWidth, Integer.MAX_VALUE);

            int prefH = comp.getPreferredSize().height;

            // 최소 높이 + 약간 여백
            kitchenTable.setRowHeight(row, Math.max(35, prefH + 8));
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