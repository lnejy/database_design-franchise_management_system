package client_store.controller;

import client_store.dao.StoreDAO;
import client_store.view.LoginView;
import client_store.view.StoreMainView;
import client_store.view.StoreRegisterDialog;
import common.dto.StoreDTO;

import javax.swing.*;

/**
 * 매장 로그인 화면 컨트롤러
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>매장 로그인 인증 처리</li>
 *   <li>매장 등록 다이얼로그 호출</li>
 *   <li>로그인 성공 시 메인 화면으로 전환</li>
 * </ul>
 * 
 * <p><b>DB 흐름:</b></p>
 * <ol>
 *   <li>사용자 입력 (매장 코드, 전화번호) → StoreDAO.login() 호출</li>
 *   <li>DB에서 매장 정보 조회 → 로그인 성공 시 StoreMainView로 이동</li>
 * </ol>
 * 
 * @author Franchise Management System
 */
public class LoginController {
    private LoginView view;
    private StoreDAO dao;

    /**
     * 로그인 컨트롤러 생성자
     * 
     * @param view 로그인 화면 뷰
     */
    public LoginController(LoginView view) {
        this.view = view;
        this.dao = new StoreDAO();

        // 로그인 버튼 이벤트 리스너 등록
        this.view.setLoginButtonListener(e -> handleLogin());

        // 매장 등록 버튼 이벤트 리스너 등록
        this.view.setRegisterButtonListener(e -> {
            new StoreRegisterDialog(view);
        });
    }

    /**
     * 로그인 처리 메서드
     * 
     * <p>매장 코드와 전화번호를 입력받아 DB에서 인증하고,
     * 성공 시 메인 화면으로 전환합니다.</p>
     */
    private void handleLogin() {
        String code = view.getStoreCode();
        String rawPw = view.getPassword(); // 사용자가 입력한 원본 비밀번호

        if (code.isEmpty() || rawPw.isEmpty()) {
            JOptionPane.showMessageDialog(view, "ID와 PW를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 전화번호에서 하이픈(-) 등 특수문자 제거 (숫자만 추출)
        String cleanPw = rawPw.replaceAll("[^0-9]", "");

        try {
            // DB에서 매장 정보 조회
            StoreDTO store = dao.login(code, cleanPw);
            
            if (store != null) {
                // 로그인 성공
                JOptionPane.showMessageDialog(
                    view, 
                    store.getStoreName() + " 점주님 환영합니다!", 
                    "로그인 성공", 
                    JOptionPane.INFORMATION_MESSAGE
                );
                view.dispose();

                // 메인 화면으로 이동
                StoreMainView mainView = new StoreMainView();
                new StoreMainController(mainView, store);

            } else {
                // 로그인 실패
                JOptionPane.showMessageDialog(
                    view, 
                    "정보가 일치하지 않습니다.\n(비밀번호는 숫자만 확인합니다)", 
                    "로그인 실패", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                view, 
                "DB 오류: " + ex.getMessage(), 
                "에러", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}