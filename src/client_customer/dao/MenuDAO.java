package client_customer.dao;

import common.db.DBConnection;
import common.dto.MenuDTO;
import common.dto.MenuOptionDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    // 1. 전체 메뉴 조회 (기본 메뉴판용)
    public List<MenuDTO> getAllMenus() {
        List<MenuDTO> list = new ArrayList<>();
        // is_sold_out(품절)이 아닌 것만 조회
        String sql = "SELECT * FROM menu WHERE is_sold_out = false ORDER BY category_name, menu_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new MenuDTO(
                        rs.getInt("menu_id"),
                        rs.getString("menu_name"),
                        rs.getInt("price"),
                        rs.getInt("set_price"), // 세트 가격
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

    // 2. [신규] 인기 메뉴 Top 3 조회 (상단 노출용)
    // 주문 상세(order_detail)에서 많이 팔린 순서대로 집계
    public List<MenuDTO> getTopMenus() {
        List<MenuDTO> list = new ArrayList<>();
        String sql = "SELECT m.*, SUM(od.quantity) as sold_cnt " +
                "FROM menu m " +
                "JOIN order_detail od ON m.menu_id = od.menu_id " +
                "JOIN customer_order co ON od.order_id = co.order_id " +
                "WHERE co.status IN ('WAITING', 'COMPLETED') " + // 유효한 주문만
                "GROUP BY m.menu_id " +
                "ORDER BY sold_cnt DESC " +
                "LIMIT 3";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new MenuDTO(
                        rs.getInt("menu_id"),
                        rs.getString("menu_name"),
                        rs.getInt("price"),
                        rs.getInt("set_price"),
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

    // 3. [추가] 메뉴 옵션 조회 메서드
    public List<MenuOptionDTO> getOptionsByMenuId(int menuId) {
        List<MenuOptionDTO> list = new ArrayList<>();

        String sql = "SELECT option_id, option_name, delta_price " +
                "FROM menu_option " +
                "WHERE menu_id = ? AND is_active = true " +
                "ORDER BY sort_order, option_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, menuId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new MenuOptionDTO(
                            rs.getInt("option_id"),
                            rs.getString("option_name"),
                            rs.getInt("delta_price")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}