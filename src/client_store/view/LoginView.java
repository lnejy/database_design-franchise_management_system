package client_store.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JTextField tfStoreCode;
    private JPasswordField pfPassword;
    private JButton btnLogin;
    private JButton btnRegister; // [추가] 회원가입 버튼

    public LoginView() {
        setTitle("가맹점 관리 시스템 - 로그인");
        setSize(350, 250); // 높이 약간 늘림
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelInput = new JPanel(new GridLayout(2, 2, 5, 5));
        panelInput.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelInput.add(new JLabel("매장 코드 (ID):"));
        tfStoreCode = new JTextField();
        panelInput.add(tfStoreCode);

        panelInput.add(new JLabel("전화번호 (PW):"));
        pfPassword = new JPasswordField();
        panelInput.add(pfPassword);

        // 버튼 패널 (로그인 + 등록)
        JPanel panelBtn = new JPanel(new FlowLayout());

        btnRegister = new JButton("매장 등록"); // [추가]
        btnLogin = new JButton("로그인");
        btnLogin.setPreferredSize(new Dimension(100, 40));

        panelBtn.add(btnRegister);
        panelBtn.add(btnLogin);

        add(panelInput, BorderLayout.CENTER);
        add(panelBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    public String getStoreCode() { return tfStoreCode.getText().trim(); }
    public String getPassword() { return new String(pfPassword.getPassword()).trim(); }

    // 로그인 버튼 리스너 등록
    public void setLoginButtonListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
        pfPassword.addActionListener(listener); // 엔터키 처리
    }

    // 등록 버튼 리스너 등록
    public void setRegisterButtonListener(ActionListener listener) {
        btnRegister.addActionListener(listener);
    }
}