package client_store.view;

import common.ui.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 매장 로그인 화면.
 * 매장 코드와 전화번호를 입력받아 로그인하는 UI를 제공합니다.
 */
public class LoginView extends JFrame {
    private JTextField tfStoreCode;
    private JPasswordField pfPassword;
    private JButton btnLogin;
    private JButton btnRegister;

    public LoginView() {
        setTitle("가맹점 관리 시스템 - 로그인");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // --- 공통 테마 적용 ---
        getContentPane().setBackground(UITheme.BASE_BG);

        // --- 입력 필드 영역 ---
        JPanel panelInput = UITheme.createSectionPanel(new GridLayout(2, 2, 10, 15), "매장 정보");
        panelInput.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        // 매장 코드 입력
        JLabel lblCode = new JLabel("매장 코드 (ID):");
        lblCode.setFont(UITheme.FONT_REGULAR);
        panelInput.add(lblCode);
        tfStoreCode = new JTextField();
        UITheme.styleField(tfStoreCode);
        panelInput.add(tfStoreCode);

        // 전화번호 입력
        JLabel lblPhone = new JLabel("전화번호 (PW):");
        lblPhone.setFont(UITheme.FONT_REGULAR);
        panelInput.add(lblPhone);
        pfPassword = new JPasswordField();
        UITheme.styleField(pfPassword);
        panelInput.add(pfPassword);

        // --- 버튼 영역 ---
        JPanel panelBtn = UITheme.createSectionPanel(new FlowLayout(FlowLayout.CENTER, 12, 15), null);

        btnRegister = new JButton("매장 등록");
        btnRegister.setFont(UITheme.FONT_REGULAR);
        btnRegister.setPreferredSize(new Dimension(100, 35));
        UITheme.applyFilled(btnRegister, UITheme.PRIMARY_LIGHT, Color.WHITE, false);
        
        btnLogin = new JButton("로그인");
        btnLogin.setFont(UITheme.FONT_BOLD);
        btnLogin.setPreferredSize(new Dimension(120, 40));
        UITheme.applyFilled(btnLogin, UITheme.PRIMARY, Color.WHITE, true);

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