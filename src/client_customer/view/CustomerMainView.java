package client_customer.view;

import client_customer.dao.MenuDAO;
import client_customer.dao.OrderDAO;
import common.dto.MenuDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMainView extends JFrame {
    private int storeId;
    private String storeName;
    private Map<MenuDTO, Integer> cart = new HashMap<>(); // 장바구니 (메뉴, 수량)
    private DefaultTableModel tableModel;
    private JLabel lblTotal;

    public CustomerMainView(int storeId, String storeName) {
        this.storeId = storeId;
        this.storeName = storeName;

        setTitle("키오스크 - " + storeName + " (지점코드: " + storeId + ")");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. 좌측: 메뉴 패널 (Grid)
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        menuPanel.setPreferredSize(new Dimension(600, 600));
        menuPanel.setBackground(Color.WHITE);

        loadMenus(menuPanel); // 메뉴 버튼 생성 메서드 호출

        add(new JScrollPane(menuPanel), BorderLayout.CENTER);

        // 2. 우측: 장바구니 패널
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setPreferredSize(new Dimension(350, 600));
        cartPanel.setBorder(BorderFactory.createTitledBorder("장바구니"));

        // 장바구니 테이블
        String[] cols = {"메뉴명", "수량", "금액"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        cartPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 하단 결제 버튼 영역
        JPanel payPanel = new JPanel(new GridLayout(2, 1));
        lblTotal = new JLabel("총 결제금액: 0원", SwingConstants.CENTER);
        lblTotal.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        JButton btnOrder = new JButton("결제하기");
        btnOrder.setBackground(new Color(255, 100, 100));
        btnOrder.setForeground(Color.WHITE);
        btnOrder.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        payPanel.add(lblTotal);
        payPanel.add(btnOrder);
        cartPanel.add(payPanel, BorderLayout.SOUTH);

        add(cartPanel, BorderLayout.EAST);

        // [결제 버튼 이벤트]
        btnOrder.addActionListener(e -> processPayment());

        setVisible(true);
    }

    // 메뉴 버튼 로드
    private void loadMenus(JPanel panel) {
        MenuDAO dao = new MenuDAO();
        List<MenuDTO> menus = dao.getAllMenus();

        for (MenuDTO menu : menus) {
            JButton btn = new JButton("<html><center>" + menu.getMenuName() + "<br>" + menu.getPrice() + "원</center></html>");
            btn.setPreferredSize(new Dimension(130, 100));

            btn.addActionListener(e -> addToCart(menu)); // 클릭 시 장바구니 추가
            panel.add(btn);
        }
    }

    // 장바구니 추가 로직
    private void addToCart(MenuDTO menu) {
        cart.put(menu, cart.getOrDefault(menu, 0) + 1);
        refreshCartTable();
    }

    // 테이블 새로고침
    private void refreshCartTable() {
        tableModel.setRowCount(0);
        int total = 0;

        for (Map.Entry<MenuDTO, Integer> entry : cart.entrySet()) {
            MenuDTO m = entry.getKey();
            int qty = entry.getValue();
            int sum = m.getPrice() * qty;

            tableModel.addRow(new Object[]{m.getMenuName(), qty, sum});
            total += sum;
        }
        lblTotal.setText("총 결제금액: " + total + "원");
    }

    // 결제 처리 로직
    private void processPayment() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어있습니다.");
            return;
        }

        int total = 0;
        for (Map.Entry<MenuDTO, Integer> e : cart.entrySet()) {
            total += e.getKey().getPrice() * e.getValue();
        }

        int confirm = JOptionPane.showConfirmDialog(this, "총 " + total + "원을 결제하시겠습니까?", "결제 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {

            OrderDAO dao = new OrderDAO();
            boolean success = dao.processOrder(storeId, cart, total); // ★ DB 트랜잭션 호출

            if (success) {
                JOptionPane.showMessageDialog(this, "주문이 완료되었습니다!\n(재고가 차감되었습니다)");
                cart.clear();
                refreshCartTable();
            } else {
                JOptionPane.showMessageDialog(this, "주문 처리에 실패했습니다.\n관리자에게 문의하세요.", "에러", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}