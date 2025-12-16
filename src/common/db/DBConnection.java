package common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * 데이터베이스 연결 관리 클래스
 * 
 * <p>프로젝트 전체에서 사용하는 MySQL 데이터베이스 연결을 관리합니다.
 * 싱글톤 패턴을 사용하여 연결 설정을 중앙에서 관리합니다.</p>
 * 
 * <p><b>DB 흐름:</b></p>
 * <ul>
 *   <li>애플리케이션 시작 시 MySQL JDBC 드라이버 자동 로드</li>
 *   <li>각 DAO 클래스에서 getConnection() 호출하여 DB 연결 획득</li>
 *   <li>try-with-resources 구문으로 자동 연결 해제</li>
 * </ul>
 * 
 * @author Franchise Management System
 * @version 1.0
 */
public class DBConnection {

    /** 데이터베이스 연결 URL */
    private static final String URL = "jdbc:mysql://localhost:3306/fastfood_db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    
    /** 데이터베이스 사용자명 */
    private static final String USER = "root";
    
    /** 데이터베이스 비밀번호 */
    private static final String PASSWORD = "0618";

    /**
     * 정적 초기화 블록: MySQL JDBC 드라이버 로드
     * 
     * <p>클래스가 최초 로드될 때 한 번만 실행되며,
     * 드라이버가 없을 경우 사용자에게 오류 메시지를 표시합니다.</p>
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                null, 
                "MySQL 드라이버를 찾을 수 없습니다.\n프로젝트의 lib 폴더에 mysql-connector-j-9.5.0.jar가 있는지 확인하세요.", 
                "Critical Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * 데이터베이스 연결 객체를 반환합니다.
     * 
     * <p><b>사용 예시:</b></p>
     * <pre>{@code
     * try (Connection conn = DBConnection.getConnection();
     *      PreparedStatement pstmt = conn.prepareStatement(sql)) {
     *     // DB 작업 수행
     * }
     * }</pre>
     * 
     * @return 데이터베이스 연결 객체
     * @throws SQLException 데이터베이스 연결 실패 시 발생
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 데이터베이스 연결 테스트용 메인 메서드
     * 
     * <p>개발 및 디버깅 목적으로 사용됩니다.</p>
     * 
     * @param args 명령행 인수 (사용하지 않음)
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✓ DB 연결 성공: " + conn);
            System.out.println("✓ 데이터베이스: fastfood_db");
        } catch (SQLException e) {
            System.err.println("✗ DB 연결 실패:");
            e.printStackTrace();
        }
    }
}