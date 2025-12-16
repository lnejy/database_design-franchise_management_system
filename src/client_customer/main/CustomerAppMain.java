package client_customer.main;

import client_customer.view.CustomerMainView;
import client_store.dao.StoreDAO;
import common.dto.StoreDTO;

import javax.swing.*;
import java.util.List;

/**
 * 고객 키오스크 시스템 메인 클래스
 * 
 * <p>고객이 사용하는 키오스크 애플리케이션의 진입점입니다.</p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>매장 선택</li>
 *   <li>메뉴 선택 및 주문</li>
 *   <li>결제 처리 (재고 자동 차감 포함)</li>
 * </ul>
 * 
 * <p><b>실행 흐름:</b></p>
 * <ol>
 *   <li>DB에서 등록된 매장 목록 조회</li>
 *   <li>사용자가 매장 선택</li>
 *   <li>선택한 매장의 키오스크 화면 실행</li>
 * </ol>
 * 
 * @author Franchise Management System
 */
public class CustomerAppMain {
    /**
     * 키오스크 시스템 시작점
     * 
     * <p>매장 선택 다이얼로그를 띄우고, 선택한 매장의 키오스크 화면을 실행합니다.</p>
     * 
     * @param args 명령행 인수 (사용하지 않음)
     */
    public static void main(String[] args) {
        // 시스템 기본 Look and Feel 적용
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            System.err.println("Look and Feel 설정 실패: " + e.getMessage());
        }

        // Swing 스레드 안전성 보장 (EDT에서 실행)
        SwingUtilities.invokeLater(() -> {
            // 1. DB에서 매장 목록 조회
            StoreDAO dao = new StoreDAO();
            List<StoreDTO> stores = dao.getAllStores();

            if (stores.isEmpty()) {
                JOptionPane.showMessageDialog(
                    null, 
                    "등록된 매장이 없습니다.\n점주 프로그램에서 매장을 먼저 등록해주세요.", 
                    "매장 없음", 
                    JOptionPane.WARNING_MESSAGE
                );
                System.exit(0);
                return;
            }

            // 2. 매장 선택 다이얼로그 표시
            StoreDTO selectedStore = (StoreDTO) JOptionPane.showInputDialog(
                null,
                "주문하실 매장을 선택해주세요:",
                "매장 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                stores.toArray(),
                stores.get(0) // 기본 선택값
            );

            // 3. 선택 결과 처리
            if (selectedStore != null) {
                // 선택된 매장의 키오스크 화면 실행
                new CustomerMainView(selectedStore.getStoreId(), selectedStore.getStoreName());
            } else {
                // 취소 시 프로그램 종료
                System.exit(0);
            }
        });
    }
}