package com.Lifam_Ragor.ScreenRobot;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public abstract class MyFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1299537692952385206L;
    // define the specific sleep time gap, so the main frame have enough time to be
    // totally invisible
    static final float CurtainOpacity = 0.7f;
    static final int InvisibleTimeGap = 700;
    // define the curtain opacity, the curtain is used when we select a sub image
    public static final Dimension MinimumSize = new Dimension(900, 600);

    Rectangle screenCaptureRect = ScreenCapture.getSystemMaximumScreenSizeRect();

    public MyFrame() {

    }

    public MyFrame(String title) {
        this();
        setTitle(title);
    }

    abstract public void setDisplayPanel(BufferedImage image);

    public void frameBeforeAdjustment() {
        setVisible(false);
    }

    public void frameAfterAdjustment() { setVisible(true); }

    public void setScreenCaptureRect(Rectangle screenCaptureRect) {
        this.screenCaptureRect = screenCaptureRect;
    }

    public Rectangle getScreenCaptureRect() {
        return screenCaptureRect;
    }

    protected void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static void setSystemLookAndFeel() {
//		JFrame.setDefaultLookAndFeelDecorated(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
