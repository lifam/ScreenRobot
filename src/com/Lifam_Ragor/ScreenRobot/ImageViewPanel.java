package com.Lifam_Ragor.ScreenRobot;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageViewPanel extends JPanel {
    JScrollPane imageViewScrollPanel;
    JPanel imagePanel;
    JLabel imageLabel;

    BufferedImage plus, minus, image = null;
    Rectangle plusRect, minusRect;
//    boolean mouseInPanel = false;
    boolean autoResize = true;

    public static final double DEFAULT_SCALE = 1.0, DEFAULT_SCALE_INC = 0.2;
    double scale = DEFAULT_SCALE, scaleInc = DEFAULT_SCALE_INC;

    public ImageViewPanel() {
        dataInit();
        viewInit();
        actionInit();
    }

    void dataInit() {
        try {
            plus = ImageIO.read(getClass().getResource("/resource/image/plus.png"));
            minus = ImageIO.read(getClass().getResource("/resource/image/minus.png"));
            image = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void viewInit() {
        imageLabel = new JLabel();
        imageLabel.setDoubleBuffered(true);

        imagePanel = new JPanel();
        imagePanel.setDoubleBuffered(true);
        imagePanel.setBackground(Color.white);
        imagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        imagePanel.add(imageLabel);
//        imagePanel.setLayout(new BorderLayout());
//        imagePanel.add(imageLabel, BorderLayout.CENTER);

        imageViewScrollPanel = new JScrollPane(imagePanel);
        imageViewScrollPanel.setDoubleBuffered(true);
//        imageViewScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//        imageViewScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.setDoubleBuffered(true);
        this.setLayout(new BorderLayout());
        this.add(imageViewScrollPanel);
    }

    void actionInit() {
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (plusRect.contains(e.getX(), e.getY())) {
                    zoomIn();
                } else if (minusRect.contains(e.getX(), e.getY())) {
                    zoomOut();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);

                generatePlusMinusRect();

                paintPlusMinus();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);

                generatePlusMinusRect();

                repaint();
            }
        });

        AdjustmentListener adjl = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                generatePlusMinusRect();

//                repaint();
//                paintPlusMinus();

//                paintPlusMinus();
            }
        };

        imageViewScrollPanel.getHorizontalScrollBar().addAdjustmentListener(adjl);

        imageViewScrollPanel.getVerticalScrollBar().addAdjustmentListener(adjl);
    }

    void generatePlusMinusRect() {
        Dimension topSize = getSize();
        Point p = imageViewScrollPanel.getViewport().getViewPosition();

        plusRect = new Rectangle(p.x + topSize.width - 150, p.y + topSize.height - 75, 30, 30);
        minusRect = new Rectangle(p.x + topSize.width - 100, p.y + topSize.height - 75, 30, 30);
    }

    void zoomIn() {
        if (image == null) return;

        scale += scaleInc;
        BufferedImage bi = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        AffineTransform aftf = new AffineTransform();
        aftf.setToScale(scale, scale);
        g.drawImage(image, aftf, null);
        g.dispose();
        imageLabel.setIcon(new ImageIcon(bi));

        int gap = (imagePanel.getHeight() - bi.getHeight()) / 2;
        if (gap > 0) {
            imageLabel.setBorder(new EmptyBorder(gap, 0, 0, 0));
        } else {
            imageLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        paintPlusMinus();
    }

    void zoomOut() {
        if (image == null) return;

        scale -= scaleInc;
        if (scale <= 0.01) scale += scaleInc;

        BufferedImage bi = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        AffineTransform aftf = new AffineTransform();
        aftf.setToScale(scale, scale);
        g.drawImage(image, aftf, null);

        imageLabel.setIcon(new ImageIcon(bi));

        int gap = (imagePanel.getHeight() - bi.getHeight()) / 2;
        if (gap > 0) {
            imageLabel.setBorder(new EmptyBorder(gap, 0, 0, 0));
        } else {
            imageLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        paintPlusMinus();
    }

    void paintPlusMinus() {
        if (image == null) return;

        Graphics2D g = (Graphics2D) imagePanel.getGraphics();
        g.drawImage(plus, plusRect.x, plusRect.y, null);
        g.drawImage(minus, minusRect.x, minusRect.y, null);
        g.drawString(String.format("%d %%", (int) (scale * 100)), plusRect.x + plusRect.width, plusRect.y + plusRect.height + 25);
        g.dispose();
    }

    void setImage(BufferedImage image) {
        this.image = image;

        if (autoResize && (image.getWidth() > getWidth() || image.getHeight() > getHeight())) {
            double tempScale = Math.min(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
            tempScale = tempScale - 0.01 < 0.00001 ? tempScale : tempScale - 0.01;

            BufferedImage bi = new BufferedImage((int) (image.getWidth() * tempScale), (int) (image.getHeight() * tempScale), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) bi.getGraphics();
            AffineTransform aftf = new AffineTransform();
            aftf.setToScale(tempScale, tempScale);
            g.drawImage(image, aftf, null);
            g.dispose();
            imageLabel.setIcon(new ImageIcon(bi));
        } else {
            imageLabel.setIcon(new ImageIcon(image));
        }
//        imageLabel.setIcon(new ImageIcon(image));
        int gap = (getHeight() - image.getHeight()) / 2;
        if (gap > 0) {
            imageLabel.setBorder(new EmptyBorder(gap, 0, 0, 0));
        } else {
            imageLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }

    BufferedImage getImage() {
        return image;
    }

    void setScale(double scale) {
        this.scale = scale;
    }

    double getScale() {
        return scale;
    }

    void setScaleInc(double scaleInc) {
        this.scaleInc = scaleInc;
    }

    double getScaleInc() {
        return scaleInc;
    }

    void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }

    public boolean isAutoResize() {
        return autoResize;
    }
}