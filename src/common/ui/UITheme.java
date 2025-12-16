package common.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 공통 UI 색상 및 버튼 스타일 유틸리티.
 * 버튼 배경/글자색 대비를 충분히 확보하도록 일관된 팔레트를 제공합니다.
 */
public final class UITheme {
    /** 기본 본문 폰트 */
    public static final Font FONT_REGULAR = new Font("맑은 고딕", Font.PLAIN, 13);
    /** 기본 볼드 폰트 */
    public static final Font FONT_BOLD = FONT_REGULAR.deriveFont(Font.BOLD);
    /** 섹션 타이틀용 폰트 */
    public static final Font FONT_TITLE = FONT_REGULAR.deriveFont(Font.BOLD, 18f);

    /** 기본 배경 톤 */
    public static final Color BASE_BG = new Color(245, 245, 250);
    /** 주요 액션용 진한 파랑 (화이트 텍스트 대비 6:1 이상) */
    public static final Color PRIMARY = new Color(30, 90, 160);
    /** 보조 액션용 파랑 (화이트 텍스트 대비 확보) */
    public static final Color PRIMARY_LIGHT = new Color(48, 120, 190);
    /** 성공/승인용 녹색 */
    public static final Color SUCCESS = new Color(27, 94, 32);
    /** 오류/위험 알림용 레드 */
    public static final Color DANGER = new Color(176, 0, 32);
    /** 강조 박스용 연한 오렌지 */
    public static final Color ACCENT_ORANGE = new Color(255, 230, 200);

    private UITheme() {
    }

    /**
     * 일관된 대비와 테두리를 가진 채운 버튼 스타일을 적용합니다.
     *
     * @param button     대상 버튼
     * @param background 배경색
     * @param foreground 글자색
     * @param bold       true면 글자를 볼드로 지정
     */
    public static void applyFilled(JButton button, Color background, Color foreground, boolean bold) {
        button.setBackground(background);
        button.setForeground(pickHighContrastForeground(background, foreground));
        if (bold) {
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        }
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(background.darker(), 1));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    /**
     * 주어진 배경색에 대해 4.5:1 이상의 대비를 갖는 글자색을 선택한다.
     * 필요 시 검정/흰색 중 더 대비가 높은 색으로 대체한다.
     */
    private static Color pickHighContrastForeground(Color background, Color desired) {
        Color darkText = new Color(30, 30, 30);
        Color lightText = Color.WHITE;

        Color[] candidates = {desired, lightText, darkText};
        Color best = desired;
        double bestContrast = 0;

        for (Color c : candidates) {
            double contrast = contrastRatio(background, c);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                best = c;
            }
        }
        return best;
    }

    // WCAG 대비 비율 계산
    private static double contrastRatio(Color c1, Color c2) {
        double l1 = relativeLuminance(c1);
        double l2 = relativeLuminance(c2);
        double light = Math.max(l1, l2);
        double dark = Math.min(l1, l2);
        return (light + 0.05) / (dark + 0.05);
    }

    private static double relativeLuminance(Color c) {
        return 0.2126 * toLinear(c.getRed()) +
                0.7152 * toLinear(c.getGreen()) +
                0.0722 * toLinear(c.getBlue());
    }

    private static double toLinear(int channel) {
        double srgb = channel / 255.0;
        return srgb <= 0.03928 ? srgb / 12.92 : Math.pow((srgb + 0.055) / 1.055, 2.4);
    }

    /**
     * 입력 필드에 일관된 패딩과 테두리를 설정합니다.
     */
    public static void styleField(JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    /**
     * 테이블 기본 스타일 (행 높이, 폰트, 헤더 정렬) 적용.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(26);
        table.setGridColor(new Color(225, 225, 230));
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * 공통 배경 + 패딩이 포함된 섹션 패널을 생성합니다.
     */
    public static JPanel createSectionPanel(LayoutManager layout, String title) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BASE_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                title == null ? BorderFactory.createEmptyBorder() : BorderFactory.createTitledBorder(title)
        ));
        return panel;
    }
}

