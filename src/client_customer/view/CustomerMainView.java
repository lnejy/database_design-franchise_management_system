package client_customer.view;

import client_customer.dao.MenuDAO;
import client_customer.dao.OrderDAO;
import common.dto.CartItemDTO;
import common.dto.MenuDTO;
import common.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerMainView extends JFrame {
    private int storeId;
    private List<CartItemDTO> cartList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JLabel lblTotal;

    // 알림용 변수
    private int myOrderId = -1;
    private Timer notificationTimer;

    public CustomerMainView(int storeId, String storeName) {
        this.storeId = storeId;
        setTitle("키오스크 - " + storeName);
        setSize(800, 700); // 높이를 조금 늘림 (상단 인기 메뉴 때문)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BASE_BG);

        // ---------------------------------------------------------
        // 1. 상단: 타이틀 + 인기 메뉴 Top 3
        // ---------------------------------------------------------
        JPanel topPanel = UITheme.createSectionPanel(new BorderLayout(), null);

        // 매장명 타이틀
        JLabel title = new JLabel("어서오세요! " + storeName + "입니다.", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        // 인기 메뉴 패널
        JPanel bestMenuPanel = UITheme.createSectionPanel(new BorderLayout(), "🔥 우리 매장 인기 메뉴 Top 3 🔥");
        bestMenuPanel.setPreferredSize(new Dimension(800, 110));

        JPanel bestItemsBox = new JPanel(new GridLayout(1, 3, 15, 0)); // 3개 나열
        bestItemsBox.setBackground(UITheme.BASE_BG);
        MenuDAO menuDAO = new MenuDAO();
        List<MenuDTO> topMenus = menuDAO.getTopMenus(); // Top 3 가져오기

        if (topMenus.isEmpty()) {
            JLabel lblEmpty = new JLabel("아직 인기 메뉴 데이터가 없습니다.", SwingConstants.CENTER);
            lblEmpty.setFont(UITheme.FONT_REGULAR);
            bestMenuPanel.add(lblEmpty, BorderLayout.CENTER);
        } else {
            for (MenuDTO m : topMenus) {
                JButton btn = new JButton("<html><center><b>" + m.getMenuName() + "</b><br>🏆 BEST</center></html>");
                btn.setBackground(UITheme.ACCENT_ORANGE); // 연한 주황색 (강조)
                btn.setForeground(new Color(60, 40, 25));
                btn.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
                btn.setBorder(BorderFactory.createLineBorder(new Color(230, 180, 140), 1));
                btn.setFocusPainted(false);
                btn.addActionListener(e -> onMenuClick(m)); // 클릭 시 주문 처리 동일하게
                bestItemsBox.add(btn);
            }
            bestMenuPanel.add(bestItemsBox, BorderLayout.CENTER);
        }

        // 여백 좀 주기
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.add(bestMenuPanel, BorderLayout.CENTER);
        paddingPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        topPanel.add(paddingPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // ---------------------------------------------------------
        // 2. 중앙: 전체 메뉴판
        // ---------------------------------------------------------
        JPanel menuPanel = UITheme.createSectionPanel(new GridLayout(0, 3, 10, 10), "전체 메뉴");
        List<MenuDTO> allMenus = menuDAO.getAllMenus();

        for (MenuDTO m : allMenus) {
            String label = "<html><center><b>" + m.getMenuName() + "</b><br>" +
                    String.format("%,d", m.getPrice()) + "원</center></html>";
            JButton btn = new JButton(label);
            btn.setFont(UITheme.FONT_REGULAR);
            btn.addActionListener(e -> onMenuClick(m));
            menuPanel.add(btn);
        }
        add(new JScrollPane(menuPanel), BorderLayout.CENTER);

        // ---------------------------------------------------------
        // 3. 하단: 장바구니 및 결제
        // ---------------------------------------------------------
        JPanel bottomPanel = UITheme.createSectionPanel(new BorderLayout(), null);
        bottomPanel.setPreferredSize(new Dimension(800, 200));

        // 장바구니 테이블
        String[] cols = {"메뉴명", "옵션", "수량", "금액"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable cartTable = new JTable(tableModel);
        UITheme.styleTable(cartTable);
        bottomPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // 결제 버튼 영역
        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        payPanel.setBackground(UITheme.BASE_BG);
        lblTotal = new JLabel("총 결제금액: 0원   ");
        lblTotal.setFont(UITheme.FONT_TITLE);

        JButton btnPay = new JButton("결제하기");
        btnPay.setFont(UITheme.FONT_BOLD);
        UITheme.applyFilled(btnPay, UITheme.DANGER, Color.BLACK, true);
        btnPay.setPreferredSize(new Dimension(120, 40));
        btnPay.addActionListener(e -> processPayment());

        payPanel.add(lblTotal);
        payPanel.add(btnPay);
        bottomPanel.add(payPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // 메뉴 버튼 클릭 처리 (단품/세트 선택)
    private void onMenuClick(MenuDTO menu) {
        // 1. 세트 가격이 0이면(사이드, 음료 등) 바로 단품 담기
        if (menu.getSetPrice() == 0) {
            addToCart(menu, false);
            return;
        }

        // 2. 단품/세트 선택 팝업
        Object[] options = {
                "단품 (" + String.format("%,d", menu.getPrice()) + "원)",
                "세트 (" + String.format("%,d", menu.getSetPrice()) + "원)"
        };

        int choice = JOptionPane.showOptionDialog(this,
                menu.getMenuName() + " 주문 옵션을 선택하세요.",
                "메뉴 선택",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) { // 단품
            addToCart(menu, false);
        } else if (choice == JOptionPane.NO_OPTION) { // 세트
            addToCart(menu, true);
        }
    }

    // 장바구니 담기
    private void addToCart(MenuDTO menu, boolean isSet) {
        // 이미 있는 메뉴인지 확인하고 수량 늘리는 로직을 넣을 수도 있음 (여기선 단순 추가)
        cartList.add(new CartItemDTO(menu, 1, isSet));
        refreshCart();
    }

    // 장바구니 UI 갱신
    private void refreshCart() {
        tableModel.setRowCount(0);
        int total = 0;
        for (CartItemDTO item : cartList) {
            String option = item.isSet() ? "세트" : "단품";
            tableModel.addRow(new Object[]{
                    item.getMenu().getMenuName(),
                    option,
                    item.getQuantity(),
                    String.format("%,d", item.getSubTotal())
            });
            total += item.getSubTotal();
        }
        lblTotal.setText("총 결제금액: " + String.format("%,d", total) + "원   ");
    }

    // 결제 프로세스
    private void processPayment() {
        if (cartList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어있습니다.");
            return;
        }

        int total = 0;
        for (CartItemDTO item : cartList) total += item.getSubTotal();

        int confirm = JOptionPane.showConfirmDialog(this,
                "총 " + String.format("%,d", total) + "원을 결제하시겠습니까?",
                "결제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            OrderDAO dao = new OrderDAO();
            int orderId = dao.placeOrder(storeId, cartList, total);

            if (orderId != -1) {
                String orderNum = dao.getOrderNumber(orderId);

                JOptionPane.showMessageDialog(this,
                        "주문이 완료되었습니다!\n주문번호: [ " + orderNum + " ]\n잠시만 기다려주세요.",
                        "주문 성공", JOptionPane.INFORMATION_MESSAGE);

                cartList.clear();
                refreshCart();

                // 내 주문 알림 시작 (Polling)
                myOrderId = orderId;
                startPolling();

            } else {
                JOptionPane.showMessageDialog(this, "주문 처리에 실패했습니다.\n(재고 부족 등 오류)", "실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 3초마다 주문 상태 체크
    private void startPolling() {
        if (notificationTimer != null) notificationTimer.stop();

        notificationTimer = new Timer(3000, e -> {
            OrderDAO dao = new OrderDAO();
            String status = dao.checkOrderStatus(myOrderId);

            if ("COMPLETED".equals(status)) {
                notificationTimer.stop();
                JOptionPane.showMessageDialog(null,
                        "주문하신 메뉴가 준비되었습니다!\n카운터에서 픽업해주세요.",
                        "🔔 픽업 알림", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        notificationTimer.start();
    }
}