package com.Lifam_Ragor.ScreenRobot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class RecordControlPanel extends JPanel {
    ScreenRobot topFrame;
    JPanel controlPanel;
    JButton startBtn, stopBtn;
    JCheckBox saveFileCheckBox;
    JCheckBox captureFullScreenCheckBox;
    JLabel frameRateLabel;
    JSlider frameRateSlider;
    JLabel stateLabel;

    JFileChooser imageFileChooser;

    File saveDest = null;
    boolean working = false;

    boolean saveDestSelected = false;
    boolean captureFullScreenSelected = true;
    ScreenRecord screenRecord = null;

    float imageCompressQuality = 0.5f;

    boolean debuging = true;

    public RecordControlPanel(ScreenRobot frame) {
        this.topFrame = frame;
        dataInit();
        viewInit();
        actionInit();
    }

    void dataInit() {
        imageFileChooser = new JFileChooser();
        imageFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
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

        saveFileCheckBox = new JCheckBox();
        saveFileCheckBox.setText("保存");
        saveFileCheckBox.setSelected(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.CENTER;
        controlPanel.add(saveFileCheckBox, gbc);

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

        saveFileCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (saveFileCheckBox.isSelected() && !saveDestSelected) {
                    if (topFrame.getScreenCaptureRect() == null) {
                        if (captureFullScreenCheckBox.isSelected()) {
                            topFrame.setScreenCaptureRect(ScreenCapture.getSystemMaximumScreenSizeRect());
                        } else {
                            JOptionPane.showMessageDialog(topFrame, "请先选择！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }

                    int chooserState = imageFileChooser.showSaveDialog(topFrame);
                    if (chooserState == JFileChooser.APPROVE_OPTION) {
                        imageFileChooser
                                .setSelectedFile(new File(imageFileChooser.getSelectedFile().getAbsolutePath()));
                        File saveFile = imageFileChooser.getSelectedFile();
                        String fileName = saveFile.getName();
                        if (fileName.indexOf(".") == -1) {
                            fileName += ".mp4";
                            saveFile = new File(saveFile.getParent(), fileName);
                        }
//                        String extentionName = fileName.substring(fileName.lastIndexOf(".") + 1);
                        if (!saveFile.exists()) {
                            try {
                                saveFile.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        saveDest = saveFile;
//                        System.out.println(saveDest);
                        saveDestSelected = true;
                        saveFileCheckBox.setSelected(true);
                    }
                } else {
                    saveDestSelected = false;
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

                    if (saveFileCheckBox.isSelected() && saveDest != null) {
                        screenRecord = new ScreenRecord(topFrame, frameRateSlider.getValue(), rectangle, "file:\\" + saveDest.getAbsolutePath());
                    } else {
                        screenRecord = new ScreenRecord(topFrame, frameRateSlider.getValue(), rectangle, null);
                    }

                    screenRecord.setRecording(true);
                    screenRecord.setImageCompressQuality(imageCompressQuality);
                    screenRecord.setDebuging(debuging);
                    new Thread(screenRecord).start();
                }
            }
        });

        stopBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (working) {
                    working = false;
                    screenRecord.setRecording(false);
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
