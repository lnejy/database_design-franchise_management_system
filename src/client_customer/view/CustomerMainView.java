package client_customer.view;

import client_customer.dao.MenuDAO;
import client_customer.dao.OrderDAO;
import common.dto.CartItemDTO;
import common.dto.MenuDTO;
import common.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerMainView extends JFrame {
    private int storeId;
    private List<CartItemDTO> cartList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private int myOrderId = -1;
    private Timer notificationTimer;
    private MenuDAO menuDAO = new MenuDAO();

    public CustomerMainView(int storeId, String storeName) {
        this.storeId = storeId;
        setTitle("키오스크 - " + storeName);
        setSize(850, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BASE_BG);

        // 상단 타이틀
        JLabel title = new JLabel("어서오세요! " + storeName + "입니다.", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(title, BorderLayout.NORTH);

        // 중앙: 카테고리 탭 패널
        setupCategoryTabbedPane();

        // 하단: 장바구니 및 결제
        setupBottomPanel();

        setVisible(true);
    }

    private void setupCategoryTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        List<MenuDTO> allMenus = menuDAO.getAllMenus();

        // 1. 전체보기
        tabbedPane.addTab(" 전체보기 ", createMenuGridPanel(allMenus));

        // 2. 버거 (이름에 '버거'가 들어간 메뉴)
        tabbedPane.addTab("버거 ", createMenuGridPanel(
                allMenus.stream().filter(m -> m.getMenuName().contains("버거")).collect(Collectors.toList())
        ));

        // 3. 음료
        tabbedPane.addTab("음료 ", createMenuGridPanel(
                allMenus.stream().filter(m ->
                        m.getMenuName().contains("콜라") || m.getMenuName().contains("사이다") ||
                                m.getMenuName().contains("환타") || m.getMenuName().contains("음료") ||
                                m.getMenuName().contains("커피") || m.getMenuName().contains("스프라이트") ||
                                m.getMenuName().contains("아메리카노")
                ).collect(Collectors.toList())
        ));

        // 4. 사이드
        tabbedPane.addTab("사이드 ", createMenuGridPanel(
                allMenus.stream().filter(m ->
                        !m.getMenuName().contains("버거") && !m.getMenuName().contains("콜라") &&
                                !m.getMenuName().contains("사이다") && !m.getMenuName().contains("환타") &&
                                !m.getMenuName().contains("스프라이트")
                ).collect(Collectors.toList())
        ));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JScrollPane createMenuGridPanel(List<MenuDTO> menus) {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (MenuDTO m : menus) {
            String labelHtml = "<html><center><b>" + m.getMenuName() + "</b><br>"
                    + "<font color='red'>" + String.format("%,d", m.getPrice()) + "원</font></center></html>";

            JButton btn = new JButton(labelHtml);
            btn.setPreferredSize(new Dimension(200, 220));
            btn.setBackground(Color.WHITE);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);

            String imagePath = getImagePath(m.getMenuName());
            if (imagePath != null) {
                btn.setIcon(loadResizedIcon(imagePath, 150, 110));
            }

            btn.addActionListener(e -> onMenuClick(m));
            panel.add(btn);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private String getImagePath(String menuName) {
        if (menuName.contains("더블")) return "/image/double.png";
        if (menuName.contains("치즈")) return "/image/cheese.png";
        if (menuName.contains("불고기")) return "/image/bulgogi.png";
        if (menuName.contains("새우")) return "/image/shrimp.png";
        if (menuName.contains("치킨")) return "/image/chicken.png";
        if (menuName.contains("데리")) return "/image/terri.png";
        if (menuName.contains("모짜렐라")) return "/image/mozza.png";
        if (menuName.contains("감자")) return "/image/fries.png";
        if (menuName.contains("콜라")) return "/image/cola.png";
        if (menuName.contains("스프라이트")) return "/image/sprite.png";
        if (menuName.contains("에그타르트")) return "/image/eggtart.png";
        if (menuName.contains("윙봉")) return "/image/wingbong.png";
        if (menuName.contains("소프트콘")) return "/image/soft.png";
        if (menuName.contains("환타")) return "/image/fanta.png";
        return null;
    }

    private void setupBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(800, 200));

        tableModel = new DefaultTableModel(new String[]{"메뉴명", "옵션", "수량", "금액"}, 0);
        JTable cartTable = new JTable(tableModel);
        UITheme.styleTable(cartTable);
        bottomPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("총 결제금액: 0원   ");
        lblTotal.setFont(UITheme.FONT_TITLE);

        // [추가] 선택 삭제 버튼
        JButton btnRemove = new JButton("선택 삭제");
        btnRemove.setFont(UITheme.FONT_BOLD);
        btnRemove.setPreferredSize(new Dimension(110, 40));
        btnRemove.addActionListener(e -> removeSelectedItem(cartTable));

        JButton btnPay = new JButton("결제하기");
        btnPay.setFont(UITheme.FONT_BOLD);
        btnPay.setBackground(Color.BLACK);
        btnPay.setForeground(Color.WHITE);
        btnPay.setOpaque(true);
        btnPay.setContentAreaFilled(true);
        btnPay.setBorderPainted(false);
        btnPay.setFocusPainted(false);

        btnPay.setPreferredSize(new Dimension(130, 40));
        btnPay.addActionListener(e -> processPayment());

        payPanel.add(lblTotal);
        payPanel.add(btnRemove);
        payPanel.add(btnPay);
        bottomPanel.add(payPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void onMenuClick(MenuDTO menu) {
        boolean isSet = false;

        if (menu.getSetPrice() != 0) {
            Object[] options = {"단품 (" + menu.getPrice() + ")", "세트 (" + menu.getSetPrice() + ")"};
            int choice = JOptionPane.showOptionDialog(this, "옵션을 선택하세요.", "주문",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == JOptionPane.CLOSED_OPTION) return;
            isSet = (choice == JOptionPane.NO_OPTION);
        }

        // 재료 옵션 선택
        List<common.dto.MenuOptionDTO> selectedOptions = showOptionDialog(menu);
        if (selectedOptions == null) return; // 취소

        addToCart(menu, isSet, selectedOptions);    }

    private void addToCart(MenuDTO menu, boolean isSet, List<common.dto.MenuOptionDTO> options) {
        cartList.add(new CartItemDTO(menu, 1, isSet, options));
        refreshCart();
    }

    //선택 메뉴 삭제
    private void removeSelectedItem(JTable cartTable) {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
            return;
        }
        cartList.remove(row);
        refreshCart();
    }


    private void refreshCart() {
        tableModel.setRowCount(0);
        int total = 0;

        for (CartItemDTO item : cartList) {
            String type = item.isSet() ? "세트" : "단품";
            String opt = item.getOptionSummary();
            String optionText = opt.isEmpty() ? type : (type + " | " + opt);

            tableModel.addRow(new Object[]{
                    item.getMenu().getMenuName(),
                    optionText,
                    item.getQuantity(),
                    String.format("%,d", item.getSubTotal())
            });

            total += item.getSubTotal();
        }
        lblTotal.setText("총 결제금액: " + String.format("%,d", total) + "원   ");
    }


    private void processPayment() {
        if (cartList.isEmpty()) return;
        int total = cartList.stream().mapToInt(CartItemDTO::getSubTotal).sum();
        if (JOptionPane.showConfirmDialog(this, String.format("%,d", total) + "원을 결제하시겠습니까?", "결제", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            OrderDAO dao = new OrderDAO();
            int orderId = dao.placeOrder(storeId, cartList, total);
            if (orderId != -1) {
                JOptionPane.showMessageDialog(this, "주문 완료!");
                cartList.clear(); refreshCart(); myOrderId = orderId; startPolling();
            }
        }
    }

    private void startPolling() {
        if (notificationTimer != null) notificationTimer.stop();
        notificationTimer = new Timer(3000, e -> {
            if ("COMPLETED".equals(new OrderDAO().checkOrderStatus(myOrderId))) {
                notificationTimer.stop();
                JOptionPane.showMessageDialog(null, "주문하신 메뉴가 준비되었습니다!");
            }
        });
        notificationTimer.start();
    }

    private ImageIcon loadResizedIcon(String path, int width, int height) {
        try {
            URL imgURL = getClass().getResource(path);
            if (imgURL == null) return null;
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    private List<common.dto.MenuOptionDTO> showOptionDialog(MenuDTO menu) {
        List<common.dto.MenuOptionDTO> optionList = menuDAO.getOptionsByMenuId(menu.getMenuId());
        if (optionList == null || optionList.isEmpty()) return new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        List<JCheckBox> checks = new ArrayList<>();
        for (var opt : optionList) {
            String text = opt.getOptionName();
            if (opt.getDeltaPrice() > 0) text += " (+" + opt.getDeltaPrice() + "원)";
            JCheckBox cb = new JCheckBox(text);
            checks.add(cb);
            panel.add(cb);
        }

        int result = JOptionPane.showConfirmDialog(
                this, panel, "옵션 선택 - " + menu.getMenuName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return null; // 취소면 null로 처리

        List<common.dto.MenuOptionDTO> selected = new ArrayList<>();
        for (int i = 0; i < checks.size(); i++) {
            if (checks.get(i).isSelected()) selected.add(optionList.get(i));
        }
        return selected;
    }

}