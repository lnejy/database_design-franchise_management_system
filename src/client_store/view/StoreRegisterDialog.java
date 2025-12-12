package client_store.view;

import client_store.dao.StoreDAO;
import javax.swing.*;
import java.awt.*;

public class StoreRegisterDialog extends JDialog {

    public StoreRegisterDialog(JFrame parent) {
        super(parent, "매장 신규 등록", true); // 모달 창 설정
        setSize(350, 450); // 높이 조금 늘림
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 입력 폼 패널
        JPanel panelInput = new JPanel(new GridLayout(6, 2, 10, 10));
        panelInput.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField tfName = new JTextField();
        JTextField tfCode = new JTextField();
        JTextField tfPhone = new JTextField(); // 비밀번호 역할
        JTextField tfAddress = new JTextField();
        JTextField tfManager = new JTextField();

        panelInput.add(new JLabel("매장명 (예: 강남점):"));
        panelInput.add(tfName);

        panelInput.add(new JLabel("매장코드 (ID):"));
        panelInput.add(tfCode);

        panelInput.add(new JLabel("연락처 (PW):"));
        panelInput.add(tfPhone); // 하이픈(-) 입력해도 내부에서 제거 처리

        panelInput.add(new JLabel("주소:"));
        panelInput.add(tfAddress);

        panelInput.add(new JLabel("점장 이름:"));
        panelInput.add(tfManager);

        // 버튼 패널
        JPanel panelBtn = new JPanel();
        JButton btnSave = new JButton("등록 완료");
        JButton btnCancel = new JButton("취소");

        panelBtn.add(btnSave);
        panelBtn.add(btnCancel);

        add(panelInput, BorderLayout.CENTER);
        add(panelBtn, BorderLayout.SOUTH);

        // --- 이벤트 리스너

        // [저장 버튼]
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
            // DAO 내부에서 하이픈(-) 제거 로직 수행됨
            boolean isSuccess = dao.registerStore(name, code, phone, address, manager);

            if (isSuccess) {
                JOptionPane.showMessageDialog(this, "매장 등록이 완료되었습니다.\nID: " + code + "\nPW: " + phone.replaceAll("[^0-9]", ""), "등록 성공", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // 창 닫기
            } else {
                JOptionPane.showMessageDialog(this, "등록 실패.\n이미 존재하는 매장 코드이거나 시스템 오류입니다.", "등록 실패", JOptionPane.ERROR_MESSAGE);
            }
        });

        // [취소 버튼]
        btnCancel.addActionListener(e -> dispose());

        setVisible(true);
    }
}