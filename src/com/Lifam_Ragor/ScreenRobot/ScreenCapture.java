package com.Lifam_Ragor.ScreenRobot;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScreenCapture extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -176559795192931530L;
    /**
     *
     */
    private JPanel contentPane;
    private MyFrame targetFrame;
    private BufferedImage targetImage, bufferedFullScreen, fullScreenInOpacity;
    private JLabel imageLabel;
    private JButton invisibleButton;
    private Rectangle fullScreenRect, targetRect;

    public final static int MinWidth = 5, MinHeight = 5;
    public final static Dimension SystemMaximumScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final static Rectangle SystemMaximumScreenSizeRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

    private static Robot robot;

//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    // Thread.sleep(1500);
//                    ScreenCapture frame = new ScreenCapture(null, getSystemMaximumScreenSizeRect());
////                    frame.setOpacity(0.9f);
//                    frame.setVisible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    public ScreenCapture(MyFrame targetFrame, Rectangle targetRect) {
        this.targetFrame = targetFrame;
        this.targetRect = targetRect;

        dataInit();

        viewInit();

        actionInit();

        setVisible(true);
    }

    private void dataInit() {
        fullScreenRect = new Rectangle(getSystemMaximumScreenSize());

        if (targetRect == null)
            targetRect = fullScreenRect;
        Image fullScreen = getScreenCapture(fullScreenRect);

        bufferedFullScreen = new BufferedImage(fullScreenRect.width, fullScreenRect.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedFullScreen.getGraphics();
        g.drawImage(fullScreen, 0, 0, null);
        g.dispose();

        // here I first define a tempImage to generate the screen with specific opacity,
        // after tempImage was generated, I then use it to generate fullScreenInOpacity,
        // the reason I do this is because I don't know much about the difference
        // between TYPE_INT_ARGB and TYPE_INT_RGB, and because they can't work well when
        // fix together, so I have to choose a uniform TYPE, and I choose TYPE_INT_RGB.
        BufferedImage tempImage = new BufferedImage(fullScreenRect.width, fullScreenRect.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tempImage.getGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, MyFrame.CurtainOpacity));
        g2d.drawImage(fullScreen, 0, 0, null);
        g2d.dispose();

        fullScreenInOpacity = new BufferedImage(fullScreenRect.width, fullScreenRect.height,
                BufferedImage.TYPE_INT_RGB);
        g = fullScreenInOpacity.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
        g.dispose();
    }

    private void viewInit() {
        contentPane = new JPanel();
        contentPane.setBorder(null);
        contentPane.setLayout(new BorderLayout(0, 0));

        invisibleButton = new JButton();
        // to make the button invisible, the following code commented out are not
        // working
        // invisibleButton.setVisible(false);
        // invisibleButton.setEnabled(true);

        invisibleButton.setContentAreaFilled(false);
        invisibleButton.setBorderPainted(false);
        contentPane.add(invisibleButton, BorderLayout.CENTER);
        getRootPane().setDefaultButton(invisibleButton);

        imageLabel = new JLabel();
        imageLabel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        imageLabel.setIcon(new ImageIcon(fullScreenInOpacity));
        contentPane.add(imageLabel, BorderLayout.CENTER);

        setContentPane(contentPane);
        setUndecorated(true);
        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void actionInit() {
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                imageLabel.setIcon(new ImageIcon(fullScreenInOpacity));
                targetRect.setLocation(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int tempX = e.getX(), tempY = e.getY();
                int x = targetRect.getLocation().x, y = targetRect.getLocation().y;
                targetRect = new Rectangle(Math.min(x, tempX), Math.min(y, tempY), Math.abs(x - tempX),
                        Math.abs(y - tempY));

                // to avoid the error below, we need to adjust the minimum width and height
                // java.awt.image.RasterFormatException: negative or zero width
                targetRect.width = targetRect.width < MinWidth ? MinWidth : targetRect.width;
                targetRect.height = targetRect.height < MinHeight ? MinHeight : targetRect.height;

                targetImage = getSubImage(targetRect);
            }
        });

        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int tempX = e.getX(), tempY = e.getY();
                int x = targetRect.getLocation().x, y = targetRect.getLocation().y;
                Rectangle rect = new Rectangle(Math.min(x, tempX), Math.min(y, tempY), Math.abs(x - tempX),
                        Math.abs(y - tempY));

                // to avoid the error below, we only paint when both the width and height are
                // satisfied
                // java.awt.image.RasterFormatException: negative or zero width
                if (rect.width > MinWidth && rect.height > MinHeight) {
                    BufferedImage bufferFullScreenCapture = new BufferedImage(fullScreenRect.width,
                            fullScreenRect.height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D image2d = (Graphics2D) bufferFullScreenCapture.getGraphics();

                    image2d.drawImage(fullScreenInOpacity, 0, 0, null);
//                    image2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
//                            MyFrame.CurtainOpacity));
                    image2d.drawImage(getSubImage(rect), rect.x, rect.y, null);
                    image2d.setColor(Color.red);
                    image2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                    image2d.setColor(Color.black);
                    image2d.drawString(
                            String.format("x: %d y: %d width: %d height: %d", rect.x, rect.y, rect.width, rect.height),
                            tempX + 10, tempY - 10);
                    image2d.drawString("按回车截取指定屏幕区域", tempX + 10, tempY + 15);

                    Graphics2D g = (Graphics2D) imageLabel.getGraphics();
                    g.drawImage(bufferFullScreenCapture, 0, 0, null);

                    image2d.dispose();
                    g.dispose();
                }
            }
        });

        invisibleButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                 System.out.println(e.getKeyCode());
                 System.out.println(e.getKeyCode());

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     System.out.println("enter key pressed : " + e.getKeyCode());
                    if (targetFrame != null && targetImage != null) {
                        targetFrame.setDisplayPanel(targetImage);
                        targetFrame.frameAfterAdjustment();
                        targetFrame.setScreenCaptureRect(targetRect);
                    }
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });
    }

    private BufferedImage getSubImage(Rectangle rect) {
        return bufferedFullScreen.getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    public static Dimension getSystemMaximumScreenSize() {
        return SystemMaximumScreenSize;
    }

    public static Rectangle getSystemMaximumScreenSizeRect() {
        return SystemMaximumScreenSizeRect;
    }

    public static BufferedImage getScreenCapture(Rectangle rect) {
        if (robot == null) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
        return robot.createScreenCapture(rect);
    }
}
