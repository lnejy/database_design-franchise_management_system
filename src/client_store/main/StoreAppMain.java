package client_store.main;

import client_store.controller.LoginController;
import client_store.view.LoginView;

/**
 * 매장 관리 시스템 메인 클래스
 * 
 * 매장 관리자(점주)가 사용하는 애플리케이션의 진입점입니다.
 * 
 * 주요 기능:
 * - 매장 로그인
 * - 재고 관리
 * - 발주 요청
 * - 매출 조회
 * 
 * @author Franchise Management System
 */
public class StoreAppMain {
    /**
     * 매장 관리 시스템 시작점
     * 
     * Swing 스레드 안전성을 보장하기 위해 Event Dispatch Thread에서 실행합니다.
     * 
     * @param args 명령행 인수 (사용하지 않음)
     */
    public static void main(String[] args) {
        // Swing 스레드 안전성 보장 (EDT에서 실행)
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginView view = new LoginView();
            new LoginController(view);
        });
    }
}