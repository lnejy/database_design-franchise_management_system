package common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * 데이터베이스 연결을 관리하는 유틸리티 클래스입니다.
 *
 * 프로젝트 전체에서 사용하는 MySQL 데이터베이스 연결을 한 곳에서 설정하고,
 * 각 DAO 가 공통으로 사용할 수 있도록 제공합니다.
 * 애플리케이션 시작 시 JDBC 드라이버를 한 번만 로드하고,
 * try-with-resources 구문을 통해 연결이 자동으로 해제되도록 사용하는 것을 권장합니다.
 *
 * 주요 흐름
 * - 애플리케이션 시작 시 MySQL JDBC 드라이버가 로드됩니다.
 * - 각 DAO 클래스에서는 {@link #getConnection()} 을 호출하여 DB 연결을 획득합니다.
 * - try-with-resources 구문을 사용하여 사용이 끝난 연결은 자동으로 해제됩니다.
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
     * 정적 초기화 블록.
     *
     * 클래스가 최초로 로드될 때 한 번만 실행되며,
     * MySQL JDBC 드라이버를 로드합니다.
     * 드라이버를 찾을 수 없는 경우 사용자에게 오류 메시지를 표시합니다.
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
     * 데이터베이스 연결 객체를 생성하여 반환합니다.
     *
     * 예시
     * try (Connection conn = DBConnection.getConnection();
     *      PreparedStatement pstmt = conn.prepareStatement(sql)) {
     *     // DB 작업 수행
     * }
     *
     * @return 데이터베이스 연결 객체
     * @throws SQLException 데이터베이스 연결에 실패한 경우
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 데이터베이스 연결을 테스트하기 위한 메인 메서드입니다.
     *
     * 개발 및 디버깅 목적으로 사용되며,
     * 실제 서비스 코드에서는 사용하지 않습니다.
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