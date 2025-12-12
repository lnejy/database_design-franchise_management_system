package client_store.dao;

import common.db.DBConnection;
import common.dto.StoreDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class StoreDAO {

    // 로그인 확인 메서드: 성공 시 StoreDTO 반환, 실패 시 null 반환
    public StoreDTO login(String storeCode, String phoneNumber) throws SQLException {
        String sql = "SELECT * FROM store WHERE store_code = ? AND phone_number = ? AND is_active = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, storeCode);
            pstmt.setString(2, phoneNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new StoreDTO(
                            rs.getInt("store_id"),
                            rs.getString("store_name"),
                            rs.getString("store_code"),
                            rs.getString("address"),
                            rs.getString("phone_number"),
                            rs.getString("manager_name"),
                            rs.getBoolean("is_active"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null; // 일치하는 정보 없음
    }

    // 매장 등록 (회원가입) 메서드
    public boolean registerStore(String name, String code, String rawPhone, String address, String manager) {
        // 1. 하이픈 제거 로직
        String cleanPhone = rawPhone.replaceAll("[^0-9]", "");

        String sql = "INSERT INTO store (store_name, store_code, phone_number, address, manager_name) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, code);
            pstmt.setString(3, cleanPhone); // 정제된 번호(비밀번호) 저장
            pstmt.setString(4, address);
            pstmt.setString(5, manager);

            int result = pstmt.executeUpdate();
            return result > 0; // 1개 이상 등록되면 true 반환

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<StoreDTO> getAllStores() {
        List<StoreDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM store"; // 모든 매장 조회

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StoreDTO store = new StoreDTO();
                store.setStoreId(rs.getInt("store_id"));
                store.setStoreName(rs.getString("store_name"));
                store.setStoreCode(rs.getString("store_code"));
                // 필요한 필드만 세팅
                list.add(store);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}