package client_customer.dao;

import common.db.DBConnection;
import common.dto.MenuDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    // 판매 중인 메뉴 전체 가져오기
    public List<MenuDTO> getAllMenus() {
        List<MenuDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM menu WHERE is_sold_out = false ORDER BY category_name, menu_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new MenuDTO(
                        rs.getInt("menu_id"),
                        rs.getString("menu_name"),
                        rs.getInt("price"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getBoolean("is_sold_out")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}