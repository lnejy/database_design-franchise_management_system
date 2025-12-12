package common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnection {

    // DB 설정
    private static final String URL = "jdbc:mysql://localhost:3306/fastfood_db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "0618";

    // 드라이버 로드 (static 블록으로 최초 1회 실행)
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "MySQL 드라이버를 찾을 수 없습니다.", "Critical Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 연결 객체 반환
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 연결 테스트용 (나중에 삭제 가능)
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("DB 연결 성공: " + conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}