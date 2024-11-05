package com.ai.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private final Color grayBorderColor; // 添加灰色边框颜色

    public RoundedPanel(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.grayBorderColor = Color.GRAY; // 设置灰色边框颜色
        setLayout(new BorderLayout());
        setOpaque(false); // Keep background transparent
        setBorder(new EmptyBorder(10, 20, 10, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Set anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Define paddings
        int leftPadding = 10;
        int rightPadding = 10;
        int topPadding = 10; // 上填充
        int bottomPadding = 10; // 底部填充
        int width = getWidth() - leftPadding - rightPadding;

        // Draw transparent padding (left and right)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1)); // Set transparency (50%)
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, leftPadding, getHeight()); // Left padding
        g2d.fillRect(getWidth() - rightPadding, 0, rightPadding, getHeight()); // Right padding

        // Draw the top padding background (与左右填充背景相同)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1)); // 设置透明度为50%
        g2d.fillRect(leftPadding, 0, width, topPadding); // 上填充背景

        // Draw solid (opaque) background in the middle
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Set opacity to 100%
        g2d.setColor(getBackground()); // 其他区域的背景颜色
        g2d.fillRoundRect(leftPadding, topPadding, width, getHeight() - topPadding - bottomPadding, cornerRadius, cornerRadius); // 减去上、下填充

        // Draw transparent bottom padding
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1)); // Set transparency (50%)
        g2d.fillRect(leftPadding, getHeight() - bottomPadding, width, bottomPadding); // 底部填充

        // Draw gray border after filling
        g2d.setColor(grayBorderColor);
        g2d.setStroke(new BasicStroke(2)); // Set border thickness to 2
        g2d.drawRoundRect(leftPadding, topPadding, width, getHeight() - topPadding - bottomPadding, cornerRadius, cornerRadius); // 减去上、下填充
    }


    @Override
    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width, 60); // 返回最小高度
    }
}
