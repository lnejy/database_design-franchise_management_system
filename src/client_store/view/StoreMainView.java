package client_store.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class StoreMainView extends JFrame {
    private JLabel lblTitle;
    private JTable tableInventory;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;

    public StoreMainView() {
        setTitle("가맹점 관리 시스템 - 메인");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. 상단 (매장명 표시)
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTitle = new JLabel("매장 이름 로딩중...");
        lblTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        panelTop.add(lblTitle);

        // 새로고침 버튼
        btnRefresh = new JButton("새로고침");
        panelTop.add(btnRefresh);

        add(panelTop, BorderLayout.NORTH);

        // 2. 중앙 (재고 테이블)
        // 컬럼 정의
        String[] columnNames = {"ID", "재료명", "현재고", "단위", "안전재고", "상태"};

        // 데이터 수정 불가하도록 설정
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableInventory = new JTable(tableModel);
        tableInventory.setRowHeight(30); // 행 높이 조절
        JScrollPane scrollPane = new JScrollPane(tableInventory);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단 (기능 버튼 예시 - 추후 발주 기능 연결)
        JPanel panelBottom = new JPanel();
        panelBottom.add(new JLabel("※ 재고가 부족한 항목은 빨간색으로 표시됩니다 (구현 예정)"));
        add(panelBottom, BorderLayout.SOUTH);
    }

    // 데이터 셋팅 메서드
    public void setStoreName(String name) {
        lblTitle.setText("[" + name + "] 재고 관리 현황");
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public void setRefreshButtonListener(ActionListener listener) {
        btnRefresh.addActionListener(listener);
    }
}