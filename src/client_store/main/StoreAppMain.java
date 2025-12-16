package client_store.main;

import client_store.controller.LoginController;
import client_store.view.LoginView;

/**
 * 매장 관리 시스템 메인 클래스
 * 
 * <p>매장 관리자(점주)가 사용하는 애플리케이션의 진입점입니다.</p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>매장 로그인</li>
 *   <li>재고 관리</li>
 *   <li>발주 요청</li>
 *   <li>매출 조회</li>
 * </ul>
 * 
 * @author Franchise Management System
 */
public class StoreAppMain {
    /**
     * 매장 관리 시스템 시작점
     * 
     * <p>Swing 스레드 안전성을 보장하기 위해 Event Dispatch Thread에서 실행합니다.</p>
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