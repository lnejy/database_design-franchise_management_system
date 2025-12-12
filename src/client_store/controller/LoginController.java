package client_store.controller;

import client_store.dao.StoreDAO;
import client_store.view.LoginView;
import client_store.view.StoreMainView;
import client_store.view.StoreRegisterDialog; // [추가] 다이얼로그 임포트
import common.dto.StoreDTO;

import javax.swing.*;
import java.sql.SQLException;

public class LoginController {
    private LoginView view;
    private StoreDAO dao;

    public LoginController(LoginView view) {
        this.view = view;
        this.dao = new StoreDAO();

        // 1. 로그인 버튼 이벤트
        this.view.setLoginButtonListener(e -> handleLogin());

        // 2. 매장 등록 버튼 이벤트
        this.view.setRegisterButtonListener(e -> {
            // 다이얼로그 띄우기 (현재 뷰를 부모로 설정)
            new StoreRegisterDialog(view);
        });
    }

    private void handleLogin() {
        String code = view.getStoreCode();
        String rawPw = view.getPassword(); // 사용자가 입력한 원본 비밀번호

        if (code.isEmpty() || rawPw.isEmpty()) {
            JOptionPane.showMessageDialog(view, "ID와 PW를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 비밀번호에서 하이픈(-) 제거 후 DB 조회
        String cleanPw = rawPw.replaceAll("[^0-9]", "");

        try {
            StoreDTO store = dao.login(code, cleanPw);
            if (store != null) {
                JOptionPane.showMessageDialog(view, store.getStoreName() + " 점주님 환영합니다!");
                view.dispose();

                // 메인 화면으로 이동
                StoreMainView mainView = new StoreMainView();
                new StoreMainController(mainView, store);

            } else {
                JOptionPane.showMessageDialog(view, "정보가 일치하지 않습니다.\n(비밀번호는 숫자만 확인합니다)", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "DB 오류: " + ex.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
        }
    }
}