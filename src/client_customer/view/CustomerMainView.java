package client_customer.view;

import client_customer.dao.MenuDAO;
import client_customer.dao.OrderDAO;
import common.dto.CartItemDTO;
import common.dto.MenuDTO;
import common.dto.MenuOptionDTO;
import common.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 고객 키오스크 메인 화면
 * 
 * 주요 기능:
 * - 메뉴 카테고리별 조회 (전체보기, 버거, 음료, 사이드)
 * - 장바구니 관리 (추가, 삭제)
 * - 주문 처리 및 결제
 * - 주문 완료 알림 (폴링 방식)
 * 
 * @author Franchise Management System
 */
public class CustomerMainView extends JFrame {
    /** 매장 ID */
    private int storeId;
    
    /** 장바구니 항목 리스트 */
    private List<CartItemDTO> cartList = new ArrayList<>();
    
    /** 장바구니 테이블 모델 */
    private DefaultTableModel tableModel;
    
    /** 총 결제 금액 레이블 */
    private JLabel lblTotal;
    
    /** 현재 주문 ID (폴링용) */
    private int myOrderId = -1;
    
    /** 주문 상태 확인용 타이머 */
    private Timer notificationTimer;
    
    /** 메뉴 DAO */
    private MenuDAO menuDAO = new MenuDAO();
    
    /** 주문 상태 확인 폴링 간격 (밀리초) */
    private static final int POLLING_INTERVAL_MS = 3000;
    
    /** 주문 완료 상태 */
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    
    /** 음료 카테고리 키워드 목록 */
    private static final String[] DRINK_KEYWORDS = {
        "콜라", "사이다", "환타", "음료", "커피", "스프라이트", 
        "아메리카노", "마운틴듀"
    };

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

    /**
     * 카테고리별 탭 패널 설정
     * 
     * 전체보기, 버거, 음료, 사이드 카테고리로 메뉴를 분류하여
     * 각각의 탭으로 표시합니다.
     */
    private void setupCategoryTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        List<MenuDTO> allMenus = menuDAO.getAllMenus();

        // 전체보기 탭
        tabbedPane.addTab(" 전체보기 ", createMenuGridPanel(allMenus));

        // 버거 탭 (메뉴명에 '버거'가 포함된 메뉴)
        tabbedPane.addTab("버거 ", createMenuGridPanel(
                filterMenusByCategory(allMenus, "버거")
        ));

        // 음료 탭
        tabbedPane.addTab("음료 ", createMenuGridPanel(
                filterMenusByCategory(allMenus, DRINK_KEYWORDS)
        ));

        // 사이드 탭 (버거와 음료가 아닌 메뉴)
        tabbedPane.addTab("사이드 ", createMenuGridPanel(
                filterSideMenus(allMenus)
        ));

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 카테고리별 메뉴 필터링
     * 
     * @param menus 전체 메뉴 리스트
     * @param keyword 필터링할 키워드
     * @return 필터링된 메뉴 리스트
     */
    private List<MenuDTO> filterMenusByCategory(List<MenuDTO> menus, String keyword) {
        return menus.stream()
                .filter(m -> m.getMenuName().contains(keyword))
                .collect(Collectors.toList());
    }

    /**
     * 여러 키워드로 메뉴 필터링
     * 
     * @param menus 전체 메뉴 리스트
     * @param keywords 필터링할 키워드 배열
     * @return 필터링된 메뉴 리스트
     */
    private List<MenuDTO> filterMenusByCategory(List<MenuDTO> menus, String[] keywords) {
        return menus.stream()
                .filter(m -> {
                    String menuName = m.getMenuName();
                    for (String keyword : keywords) {
                        if (menuName.contains(keyword)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * 사이드 메뉴 필터링
     * 
     * 버거와 음료 카테고리에 속하지 않는 메뉴를 반환합니다.
     * 
     * @param menus 전체 메뉴 리스트
     * @return 사이드 메뉴 리스트
     */
    private List<MenuDTO> filterSideMenus(List<MenuDTO> menus) {
        return menus.stream()
                .filter(m -> {
                    String menuName = m.getMenuName();
                    // 버거가 아니고
                    if (menuName.contains("버거")) {
                        return false;
                    }
                    // 음료 키워드에도 해당하지 않는 메뉴
                    for (String keyword : DRINK_KEYWORDS) {
                        if (menuName.contains(keyword)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 메뉴 그리드 패널 생성
     * 
     * 메뉴 리스트를 3열 그리드 레이아웃으로 표시합니다.
     * 각 메뉴는 이미지와 함께 버튼으로 표시되며, 클릭 시 장바구니에 추가됩니다.
     * 
     * @param menus 표시할 메뉴 리스트
     * @return 스크롤 가능한 메뉴 그리드 패널
     */
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

    /**
     * 메뉴명에 따른 이미지 경로 반환
     * 
     * 메뉴명에 포함된 키워드를 기반으로 해당하는 이미지 경로를 반환합니다.
     * 
     * @param menuName 메뉴명
     * @return 이미지 경로 (해당 이미지가 없으면 null)
     */
    private String getImagePath(String menuName) {
        if (menuName.contains("더블")) return "/image/double.png";
        if (menuName.contains("치즈버거")) return "/image/cheese.png";
        if (menuName.contains("불고기")) return "/image/bulgogi.png";
        if (menuName.contains("새우")) return "/image/shrimp.png";
        if (menuName.contains("치킨")) return "/image/chicken.png";
        if (menuName.contains("데리")) return "/image/terri.png";
        if (menuName.contains("모짜렐라")) return "/image/mozza.png";
        if (menuName.contains("감자")) return "/image/fries.png";
        if (menuName.contains("제로콜라")) return "/image/zerocoke.png";
        if (menuName.contains("콜라")) return "/image/cola.png";
        if (menuName.contains("스프라이트")) return "/image/sprite.png";
        if (menuName.contains("에그타르트")) return "/image/eggtart.png";
        if (menuName.contains("윙봉")) return "/image/wingbong.png";
        if (menuName.contains("소프트콘")) return "/image/soft.png";
        if (menuName.contains("환타")) return "/image/fanta.png";
        if (menuName.contains("아메리카노")) return "/image/americano.png";
        if (menuName.contains("마운틴듀")) return "/image/mountaindew.png";
        if (menuName.contains("시그니처")) return "/image/signatureBurger.png";
        if (menuName.contains("치즈스틱")) return "/image/cheesestick.png";
        if (menuName.contains("코울슬로")) return "/image/coleslaw.png";
        
        
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
        btnPay.setOpaque(true);
        btnPay.setBorderPainted(false);
        btnPay.setBackground(Color.BLUE);
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

    /**
     * 메뉴 클릭 이벤트 처리
     * 
     * 메뉴 버튼 클릭 시:
     * 1. 세트 메뉴가 있으면 단품/세트 선택 다이얼로그 표시
     * 2. 추가 옵션 선택 다이얼로그 표시
     * 3. 선택 완료 시 장바구니에 추가
     * 
     * @param menu 선택된 메뉴
     */
    private void onMenuClick(MenuDTO menu) {
        boolean isSet = false;

        // 세트 메뉴가 있으면 단품/세트 선택
        if (menu.getSetPrice() != 0) {
            Object[] options = {
                "단품 (" + String.format("%,d", menu.getPrice()) + "원)", 
                "세트 (" + String.format("%,d", menu.getSetPrice()) + "원)"
            };
            int choice = JOptionPane.showOptionDialog(
                this, 
                "옵션을 선택하세요.", 
                "주문",
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]
            );

            if (choice == JOptionPane.CLOSED_OPTION) {
                return; // 사용자가 취소
            }
            isSet = (choice == JOptionPane.NO_OPTION);
        }

        // 추가 옵션 선택 (예: 양파 추가, 치즈 추가 등)
        List<MenuOptionDTO> selectedOptions = showOptionDialog(menu);
        if (selectedOptions == null) {
            return; // 사용자가 취소
        }

        addToCart(menu, isSet, selectedOptions);
    }

    /**
     * 장바구니에 메뉴 추가
     * 
     * @param menu 메뉴 정보
     * @param isSet 세트 여부
     * @param options 선택된 옵션 리스트
     */
    private void addToCart(MenuDTO menu, boolean isSet, List<MenuOptionDTO> options) {
        cartList.add(new CartItemDTO(menu, 1, isSet, options));
        refreshCart();
    }

    /**
     * 장바구니에서 선택된 항목 삭제
     * 
     * @param cartTable 장바구니 테이블
     */
    private void removeSelectedItem(JTable cartTable) {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
            return;
        }
        cartList.remove(row);
        refreshCart();
    }


    /**
     * 장바구니 화면 갱신
     * 
     * 장바구니 리스트를 기반으로 테이블을 업데이트하고
     * 총 결제 금액을 계산하여 표시합니다.
     */
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


    /**
     * 결제 처리
     * 
     * 결제 확인 후 주문을 처리하고, 성공 시 장바구니를 비우고
     * 주문 완료 알림 폴링을 시작합니다.
     */
    private void processPayment() {
        if (cartList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어있습니다.");
            return;
        }
        
        int total = cartList.stream().mapToInt(CartItemDTO::getSubTotal).sum();
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            String.format("%,d", total) + "원을 결제하시겠습니까?", 
            "결제", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            OrderDAO dao = new OrderDAO();
            int orderId = dao.placeOrder(storeId, cartList, total);
            
            if (orderId != -1) {
                JOptionPane.showMessageDialog(this, "주문 완료!");
                cartList.clear();
                refreshCart();
                myOrderId = orderId;
                startPolling();
            } else {
                JOptionPane.showMessageDialog(this, "주문 처리 중 오류가 발생했습니다.");
            }
        }
    }

    /**
     * 주문 완료 알림 폴링 시작
     * 
     * 주기적으로 주문 상태를 확인하여, 주방에서 조리가 완료되면
     * 고객에게 알림을 표시합니다.
     */
    private void startPolling() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
        
        notificationTimer = new Timer(POLLING_INTERVAL_MS, e -> {
            OrderDAO dao = new OrderDAO();
            String status = dao.checkOrderStatus(myOrderId);
            
            if (ORDER_STATUS_COMPLETED.equals(status)) {
                notificationTimer.stop();
                JOptionPane.showMessageDialog(
                    null, 
                    "주문하신 메뉴가 준비되었습니다!"
                );
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

    /**
     * 메뉴 옵션 선택 다이얼로그 표시
     * 
     * 해당 메뉴에 사용 가능한 옵션들을 체크박스로 표시하고,
     * 사용자가 선택한 옵션 리스트를 반환합니다.
     * 
     * @param menu 메뉴 정보
     * @return 선택된 옵션 리스트 (취소 시 null)
     */
    private List<MenuOptionDTO> showOptionDialog(MenuDTO menu) {
        List<MenuOptionDTO> optionList = menuDAO.getOptionsByMenuId(menu.getMenuId());
        if (optionList == null || optionList.isEmpty()) {
            return new ArrayList<>();
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        List<JCheckBox> checks = new ArrayList<>();
        for (MenuOptionDTO opt : optionList) {
            String text = opt.getOptionName();
            if (opt.getDeltaPrice() > 0) {
                text += " (+" + opt.getDeltaPrice() + "원)";
            }
            JCheckBox cb = new JCheckBox(text);
            checks.add(cb);
            panel.add(cb);
        }

        int result = JOptionPane.showConfirmDialog(
                this, panel, "옵션 선택 - " + menu.getMenuName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null; // 취소면 null로 처리
        }

        List<MenuOptionDTO> selected = new ArrayList<>();
        for (int i = 0; i < checks.size(); i++) {
            if (checks.get(i).isSelected()) {
                selected.add(optionList.get(i));
            }
        }
        return selected;
    }

}