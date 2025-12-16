package client_hq.main;

import client_hq.view.HQMainView;
import javax.swing.*;

/**
 * 본사 관리 시스템 메인 클래스
 * 
 * <p>본사 관리자가 사용하는 애플리케이션의 진입점입니다.</p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>발주 요청 목록 조회</li>
 *   <li>발주 승인 처리 (재고 증가 포함)</li>
 *   <li>발주 반려 처리</li>
 * </ul>
 * 
 * @author Franchise Management System
 */
public class HQAppMain {
    /**
     * 본사 관리 시스템 시작점
     * 
     * <p>시스템 기본 Look and Feel을 적용하고, Swing 스레드 안전성을 보장합니다.</p>
     * 
     * @param args 명령행 인수 (사용하지 않음)
     */
    public static void main(String[] args) {
        // 시스템 기본 Look and Feel 적용
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            // Look and Feel 설정 실패 시 기본값 사용
            System.err.println("Look and Feel 설정 실패: " + e.getMessage());
        }

        // Swing 스레드 안전성 보장 (EDT에서 실행)
        SwingUtilities.invokeLater(() -> new HQMainView());
    }
}