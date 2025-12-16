package client_warehouse.view;

import client_warehouse.dao.WarehouseDAO;
import common.ui.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class WarehouseMainView extends JFrame {
    private WarehouseDAO dao = new WarehouseDAO();
    private DefaultTableModel model;

    public WarehouseMainView() {
        setTitle("물류창고 관리 시스템 (WMS)");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BASE_BG);

        // 상단 타이틀
        JLabel title = new JLabel("물류 센터 출고(배송) 현황", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        add(title, BorderLayout.NORTH);

        // 중앙 테이블
        String[] cols = {"배송ID", "가맹점명", "재료명", "수량", "출고일시"};
        model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 하단 버튼
        JPanel bottomPanel = UITheme.createSectionPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10), null);
        JButton btnRefresh = new JButton("새로고침");
        UITheme.applyFilled(btnRefresh, UITheme.PRIMARY, Color.WHITE, false);
        btnRefresh.addActionListener(e -> loadData());
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        // 데이터 로드
        loadData();
        setVisible(true);
    }

    private void loadData() {
        model.setRowCount(0);
        Vector<Vector<Object>> data = dao.getShipmentHistory();
        for (Vector<Object> row : data) {
            model.addRow(row);
        }
    }
}