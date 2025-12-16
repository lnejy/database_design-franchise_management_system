package client_hq.view;

import client_hq.dao.HQDAO;
import common.dto.SupplyOrderDTO;
import common.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class HQMainView extends JFrame {
    private HQDAO dao = new HQDAO();

    // 탭 패널
    private JTabbedPane tabbedPane;

    // 탭 1: 발주 관리용 컴포넌트
    private DefaultTableModel orderModel;
    private JTable orderTable;

    // 탭 2: 가맹점 순위용 컴포넌트
    private DefaultTableModel rankModel;
    private JTable rankTable;

    public HQMainView() {
        setTitle("본사 관리자 시스템 (HQ)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 화면 중앙
        getContentPane().setBackground(UITheme.BASE_BG);

        // 메인 탭 패널 생성
        tabbedPane = new JTabbedPane();

        // --------------------------------------------------------
        // 탭 1: 자재 발주 관리 (기존 기능)
        // --------------------------------------------------------
        JPanel orderPanel = UITheme.createSectionPanel(new BorderLayout(), "자재 발주 관리");

        // 테이블 설정
        String[] orderCols = {"No", "매장명", "재료명", "수량", "상태", "요청일시"};
        orderModel = new DefaultTableModel(orderCols, 0);
        orderTable = new JTable(orderModel);
        UITheme.styleTable(orderTable);
        orderPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        // 버튼 패널
        JPanel btnPanel = UITheme.createSectionPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10), null);
        JButton btnRefresh = new JButton("새로고침");
        btnRefresh.setBackground(Color.BLACK);
        btnRefresh.setBorderPainted(false);
        JButton btnApprove = new JButton("승인 (물류 배송지시)");
        btnApprove.setBackground(Color.BLACK);
        btnApprove.setBorderPainted(false);
        JButton btnReject = new JButton("반려");
        btnReject.setBackground(Color.black);
        btnReject.setBorderPainted(false);

        // 스타일링: 대비를 높인 공통 팔레트 적용
        UITheme.applyFilled(btnRefresh, UITheme.PRIMARY_LIGHT, Color.BLUE, false);
        UITheme.applyFilled(btnApprove, UITheme.SUCCESS, Color.BLUE, true);
        UITheme.applyFilled(btnReject, UITheme.DANGER, Color.BLUE, true);

        btnPanel.add(btnRefresh);
        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        orderPanel.add(btnPanel, BorderLayout.SOUTH);

        // 이벤트 연결
        btnRefresh.addActionListener(e -> loadOrderData());
        btnApprove.addActionListener(e -> processOrder(true));
        btnReject.addActionListener(e -> processOrder(false));

        tabbedPane.addTab("자재 발주 관리", orderPanel);

        // --------------------------------------------------------
        // 탭 2: 가맹점 매출 순위 (신규 기능)
        // --------------------------------------------------------
        JPanel rankPanel = UITheme.createSectionPanel(new BorderLayout(), "가맹점 매출 랭킹");

        String[] rankCols = {"순위", "매장명", "점주명", "총 매출액"};
        rankModel = new DefaultTableModel(rankCols, 0);
        rankTable = new JTable(rankModel);

        // 테이블 디자인 조정 (행 높이 등)
        UITheme.styleTable(rankTable);
        rankTable.setRowHeight(28);

        rankPanel.add(new JScrollPane(rankTable), BorderLayout.CENTER);

        JPanel rankBtnBox = UITheme.createSectionPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10), null);
        JButton btnRankRefresh = new JButton("순위 새로고침");
        btnRankRefresh.setFont(UITheme.FONT_BOLD);
        UITheme.applyFilled(btnRankRefresh, UITheme.PRIMARY, Color.WHITE, true);
        btnRankRefresh.addActionListener(e -> loadRankData());

        rankBtnBox.add(btnRankRefresh);
        rankPanel.add(rankBtnBox, BorderLayout.SOUTH);

        tabbedPane.addTab("가맹점 매출 랭킹", rankPanel);

        // --------------------------------------------------------

        add(tabbedPane);

        // 초기 데이터 로드
        loadOrderData();
        loadRankData();

        setVisible(true);
    }

    // 탭 1 데이터 로드: 발주 대기 목록
    private void loadOrderData() {
        orderModel.setRowCount(0);
        List<SupplyOrderDTO> list = dao.getPendingOrders();
        for (SupplyOrderDTO dto : list) {
            orderModel.addRow(dto.toRow());
        }
    }

    // 탭 2 데이터 로드: 가맹점 랭킹
    private void loadRankData() {
        rankModel.setRowCount(0);
        // HQDAO에 추가된 getStoreRankings() 메서드 호출
        Vector<Vector<Object>> data = dao.getStoreRankings();
        for(Vector<Object> row : data) {
            rankModel.addRow(row);
        }
    }

    // 발주 승인/반려 처리 로직
    private void processOrder(boolean isApprove) {
        int row = orderTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "처리할 항목을 선택하세요.");
            return;
        }

        int orderId = (int) orderModel.getValueAt(row, 0);
        String action = isApprove ? "승인" : "반려";

        int confirm = JOptionPane.showConfirmDialog(this,
                "선택한 발주를 " + action + " 하시겠습니까?",
                "확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success;
            if (isApprove) success = dao.approveOrder(orderId);
            else success = dao.rejectOrder(orderId);

            if (success) {
                JOptionPane.showMessageDialog(this, action + " 완료되었습니다.");
                loadOrderData(); // 목록 갱신
            } else {
                JOptionPane.showMessageDialog(this, "처리 중 오류 발생");
            }
        }
    }
}