package com.Lifam_Ragor.ScreenRobot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TransportControlPanel extends JPanel {
    ScreenRobot topFrame;
    JPanel controlPanel;
    JButton startBtn, stopBtn;
    JCheckBox clientServerCheckBox;
    JCheckBox captureFullScreenCheckBox;
    JLabel frameRateLabel;
    JSlider frameRateSlider;
    JLabel stateLabel;

    ScreenTransport screenTransport = null;

    float imageCompressQuality = 0.5f;

    boolean working = false;
    boolean clientServerSelected = true;
    boolean captureFullScreenSelected = true;
    boolean debuging = true;

    public TransportControlPanel(ScreenRobot frame) {
        this.topFrame = frame;
        dataInit();
        viewInit();
        actionInit();
    }

    void dataInit() {

    }

    void viewInit() {
        setLayout(new BorderLayout(0, 0));

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        add(controlPanel, BorderLayout.CENTER);

        startBtn = new JButton();
        startBtn.setText("开始");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(startBtn, gbc);

        stopBtn = new JButton();
        stopBtn.setText("停止");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(stopBtn, gbc);

        frameRateLabel = new JLabel();
        frameRateLabel.setText("帧率:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(frameRateLabel, gbc);

        frameRateSlider = new JSlider();
        frameRateSlider.setMinimum(1);
        frameRateSlider.setMaximum(10);
        frameRateSlider.setValue(10);
        frameRateSlider.setPaintTrack(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(frameRateSlider, gbc);

        clientServerCheckBox = new JCheckBox();
        clientServerCheckBox.setText("发送");
        clientServerCheckBox.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.CENTER;
        controlPanel.add(clientServerCheckBox, gbc);

        captureFullScreenCheckBox = new JCheckBox();
        captureFullScreenCheckBox.setText("全屏");
        captureFullScreenCheckBox.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.CENTER;
        controlPanel.add(captureFullScreenCheckBox, gbc);

        stateLabel = new JLabel();
        stateLabel.setText(String.format("帧率：%d /s",frameRateSlider.getValue()));

        add(stateLabel, BorderLayout.SOUTH);
    }

    void actionInit() {
        captureFullScreenCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (captureFullScreenCheckBox.isSelected() && !captureFullScreenSelected) {
                    topFrame.setScreenCaptureRect(ScreenCapture.getSystemMaximumScreenSizeRect());
                    captureFullScreenSelected = true;
                } else if (!captureFullScreenCheckBox.isSelected() && captureFullScreenSelected) {
                    topFrame.frameBeforeAdjustment();
                    topFrame.threadSleep(MyFrame.InvisibleTimeGap);
                    new ScreenCapture(topFrame, null);
                    captureFullScreenSelected = false;
                }
            }
        });

        clientServerCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!clientServerSelected) {
//                    screenTransport = new ScreenRobot(topFrame, TransportControlPanel.this, frameRateSlider.getValue(), topFrame.getScreenCaptureRect(), true);
                    clientServerCheckBox.setText("发送");
                    clientServerCheckBox.setSelected(true);
                    clientServerSelected = true;
                } else if (clientServerSelected) {
//                    screenTransport = new ScreenRobot(topFrame, TransportControlPanel.this, frameRateSlider.getValue(), topFrame.getScreenCaptureRect(), false);
                    clientServerCheckBox.setText("接收");
                    clientServerCheckBox.setSelected(true);
                    clientServerSelected = false;
                }
            }
        });

        startBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

//                System.out.println(topFrame.getScreenCaptureRect());

                if (!working) {
                    working = true;

                    Rectangle rectangle;
                    if (captureFullScreenCheckBox.isSelected()) {
                        topFrame.getDisplayPanel().setAutoResize(true);
                        rectangle = ScreenCapture.getSystemMaximumScreenSizeRect();
                    } else {
                        topFrame.getDisplayPanel().setAutoResize(false);
                        rectangle = topFrame.getScreenCaptureRect();
                    }

                    if (clientServerSelected) {
                        screenTransport = new ScreenTransport(topFrame, TransportControlPanel.this, frameRateSlider.getValue(), rectangle, true);
                        screenTransport.setImageCompressQuality(imageCompressQuality);
                        screenTransport.setDebuging(debuging);
                        screenTransport.setWorking(true);
                        new Thread(screenTransport).start();
                    } else if (!clientServerSelected) {
                        screenTransport = new ScreenTransport(topFrame, TransportControlPanel.this, frameRateSlider.getValue(), rectangle, false);
                        screenTransport.setImageCompressQuality(imageCompressQuality);
                        screenTransport.setDebuging(debuging);
                        screenTransport.setWorking(true);
                        new Thread(screenTransport).start();
                    } else {
                        System.err.println("start button pressed, but didn't start working because of some state err!");
                    }
                }
            }
        });

        stopBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (working) {
                    working = false;
                    screenTransport.setWorking(false);
                }
            }
        });

        frameRateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                stateLabel.setText(String.format("帧率：%d /s",frameRateSlider.getValue()));
            }
        });
    }

    public void updateFrameRate(double realTimeFrameRate) {
        stateLabel.setText(String.format("帧率： %.1f /s",realTimeFrameRate));
    }

    public float getImageCompressQuality() {
        return imageCompressQuality;
    }

    public void setImageCompressQuality(float imageCompressQuality) {
        this.imageCompressQuality = imageCompressQuality;
    }

    public boolean isDebuging() {
        return debuging;
    }

    public void setDebuging(boolean debuging) {
        this.debuging = debuging;
    }
}
