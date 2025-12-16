package client_store.view;

import client_store.dao.StoreDAO;
import common.ui.UITheme;
import common.dto.StoreDTO;
import javax.swing.*;
import java.awt.*;

/**
 * 매장 신규 등록 다이얼로그
 * 
 * <p>새로운 매장을 등록하는 다이얼로그입니다.</p>
 * 
 * <p><b>DB 흐름:</b></p>
 * <ol>
 *   <li>매장 정보 입력</li>
 *   <li>StoreDAO.registerStore() 호출 → store 테이블에 INSERT</li>
 * </ol>
 * 
 * @author Franchise Management System
 */
public class StoreRegisterDialog extends JDialog {

    /**
     * 매장 등록 다이얼로그 생성자
     * 
     * @param parent 부모 프레임
     */
    public StoreRegisterDialog(JFrame parent) {
        super(parent, "매장 신규 등록", true);
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BASE_BG);

        // 입력 폼 패널
        JPanel panelInput = new JPanel(new GridLayout(5, 2, 12, 15));
        panelInput.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        panelInput.setBackground(UITheme.BASE_BG);

        JTextField tfName = new JTextField();
        JTextField tfCode = new JTextField();
        JTextField tfPhone = new JTextField();
        JTextField tfAddress = new JTextField();
        JTextField tfManager = new JTextField();
        
        // 텍스트 필드 스타일 통일
        Font inputFont = new Font("맑은 고딕", Font.PLAIN, 12);
        for (JTextField tf : new JTextField[]{tfName, tfCode, tfPhone, tfAddress, tfManager}) {
            tf.setFont(inputFont);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        Font labelFont = new Font("맑은 고딕", Font.PLAIN, 13);
        
        JLabel lblName = new JLabel("매장명 (예: 강남점):");
        lblName.setFont(labelFont);
        panelInput.add(lblName);
        panelInput.add(tfName);

        JLabel lblCode = new JLabel("매장코드 (ID):");
        lblCode.setFont(labelFont);
        panelInput.add(lblCode);
        panelInput.add(tfCode);

        JLabel lblPhone = new JLabel("연락처 (PW):");
        lblPhone.setFont(labelFont);
        panelInput.add(lblPhone);
        panelInput.add(tfPhone);

        JLabel lblAddress = new JLabel("주소:");
        lblAddress.setFont(labelFont);
        panelInput.add(lblAddress);
        panelInput.add(tfAddress);

        JLabel lblManager = new JLabel("점장 이름:");
        lblManager.setFont(labelFont);
        panelInput.add(lblManager);
        panelInput.add(tfManager);

        // 버튼 패널
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panelBtn.setBackground(UITheme.BASE_BG);
        
        JButton btnSave = new JButton("등록 완료");
        btnSave.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(120, 35));
        UITheme.applyFilled(btnSave, UITheme.PRIMARY, Color.WHITE, true);
        
        JButton btnCancel = new JButton("취소");
        btnCancel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(80, 35));
        btnCancel.setBackground(new Color(200, 200, 200));
        btnCancel.setFocusPainted(false);

        panelBtn.add(btnSave);
        panelBtn.add(btnCancel);

        add(panelInput, BorderLayout.CENTER);
        add(panelBtn, BorderLayout.SOUTH);

        // 이벤트 리스너
        btnCancel.addActionListener(e -> dispose());
        
        btnSave.addActionListener(e -> {
            String name = tfName.getText().trim();
            String code = tfCode.getText().trim();
            String phone = tfPhone.getText().trim();
            String address = tfAddress.getText().trim();
            String manager = tfManager.getText().trim();

            if(name.isEmpty() || code.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "매장명, 코드, 연락처는 필수 입력값입니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StoreDAO dao = new StoreDAO();
            // 전화번호에서 하이픈(-) 제거
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            
            StoreDTO dto = new StoreDTO();
            dto.setStoreName(name);
            dto.setStoreCode(code);
            dto.setPhone(cleanPhone);
            dto.setAddress(address);
            dto.setManagerName(manager);
            
            boolean isSuccess = dao.registerStore(dto);

            if (isSuccess) {
                JOptionPane.showMessageDialog(
                    this, 
                    "매장 등록이 완료되었습니다.\n\nID: " + code + "\nPW: " + cleanPhone, 
                    "등록 성공", 
                    JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                    this, 
                    "등록 실패.\n이미 존재하는 매장 코드이거나 시스템 오류입니다.", 
                    "등록 실패", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        setVisible(true);
    }
}