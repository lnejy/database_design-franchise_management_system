package client_store.view;

import client_store.dao.StoreDAO;
import common.ui.UITheme;
import common.dto.IngredientDTO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 재료 발주 요청 다이얼로그
 * 
 * 매장 관리자가 재료를 발주 요청하는 다이얼로그입니다.
 * 
 * DB 흐름:
 * 1. 재료 선택 및 수량 입력
 * 2. StoreDAO.requestSupply() 호출 → store_material_request INSERT (status='PENDING')
 * 3. 본사 승인 대기 상태로 등록
 * 
 * @author Franchise Management System
 */
public class OrderIngredientDialog extends JDialog {
    /** 발주 성공 여부 */
    private boolean isSuccess = false;

    /**
     * 발주 다이얼로그 생성자
     * 
     * @param parent 부모 프레임
     * @param storeId 매장 ID
     */
    public OrderIngredientDialog(JFrame parent, int storeId) {
        super(parent, "재료 발주 요청", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BASE_BG);

        StoreDAO dao = new StoreDAO();
        List<IngredientDTO> ingredients = dao.getAllIngredients();

        // 입력 패널
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));
        inputPanel.setBackground(UITheme.BASE_BG);

        // 재료 선택
        JLabel lblIngredient = new JLabel("재료 선택:");
        lblIngredient.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        inputPanel.add(lblIngredient);
        
        JComboBox<IngredientDTO> cbIngredients = new JComboBox<>(ingredients.toArray(new IngredientDTO[0]));
        cbIngredients.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        inputPanel.add(cbIngredients);

        // 수량 입력
        JLabel lblQuantity = new JLabel("발주 수량:");
        lblQuantity.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        inputPanel.add(lblQuantity);
        
        JTextField tfQuantity = new JTextField();
        tfQuantity.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        tfQuantity.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        inputPanel.add(tfQuantity);

        // 안내 메시지
        JLabel lblInfo = new JLabel("※ 본사 승인 후 재고에 반영됩니다.", SwingConstants.CENTER);
        lblInfo.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lblInfo.setForeground(new Color(100, 100, 100));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        // 버튼 패널
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(UITheme.BASE_BG);
        
        JButton btnOrder = new JButton("발주 신청");
        btnOrder.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnOrder.setPreferredSize(new Dimension(120, 35));
        UITheme.applyFilled(btnOrder, UITheme.SUCCESS, Color.WHITE, true);

        JButton btnCancel = new JButton("취소");
        btnCancel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(80, 35));
        btnCancel.setBackground(new Color(200, 200, 200));
        btnCancel.setFocusPainted(false);

        btnPanel.add(btnOrder);
        btnPanel.add(btnCancel);

        add(inputPanel, BorderLayout.CENTER);
        add(lblInfo, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.SOUTH);

        // 이벤트 처리
        btnCancel.addActionListener(e -> dispose());
        
        btnOrder.addActionListener(e -> {
            IngredientDTO selected = (IngredientDTO) cbIngredients.getSelectedItem();
            String qtyStr = tfQuantity.getText().trim();

            if (selected == null || qtyStr.isEmpty()) return;

            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) throw new NumberFormatException();

                boolean result = dao.requestSupply(storeId, selected.getId(), qty);
                if (result) {
                    JOptionPane.showMessageDialog(
                        this, 
                        "발주 요청이 완료되었습니다.\n본사 승인 후 재고에 반영됩니다.", 
                        "발주 요청 완료", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    isSuccess = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                        this, 
                        "발주 요청에 실패했습니다.\n다시 시도해주세요.", 
                        "요청 실패", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this, 
                    "수량은 1 이상의 숫자만 입력하세요.", 
                    "입력 오류", 
                    JOptionPane.WARNING_MESSAGE
                );
            }
        });

        setVisible(true);
    }

    /**
     * 발주 성공 여부 반환
     * 
     * @return 발주 성공 시 true
     */
    public boolean isSuccess() {
        return isSuccess;
    }
}