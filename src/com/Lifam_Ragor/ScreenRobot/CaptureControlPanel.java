package com.Lifam_Ragor.ScreenRobot;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CaptureControlPanel extends JPanel {
    ScreenRobot topFrame;
//    ImageViewPanel imageViewPanel;
    JButton captureFullScreen, captureSelectedScreen, saveCapture;
    JFileChooser imageFileChooser;

    public CaptureControlPanel(ScreenRobot frame) {
        this.topFrame = frame;
//        this.imageViewPanel = imageViewPanel;
        dataInit();
        viewInit();
        actionInit();
    }

    void dataInit() {
        imageFileChooser = new JFileChooser();
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png", "png"));
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jpg", "jpeg"));
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jpeg", "jpeg"));
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("gif", "gif"));
        imageFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    void viewInit() {
//        setLayout(new FlowLayout());
        setLayout(new GridBagLayout());

        captureSelectedScreen = new JButton();
        captureSelectedScreen.setText("自定义截屏");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(captureSelectedScreen, gbc);
        captureFullScreen = new JButton();
        captureFullScreen.setText("截取全屏");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(captureFullScreen, gbc);
        saveCapture = new JButton();
        saveCapture.setText("保存");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(saveCapture, gbc);
    }

    void actionInit() {
        captureFullScreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                topFrame.frameBeforeAdjustment();
                // make it sleep for a while, so the main frame can totally be invisible
                topFrame.threadSleep(topFrame.InvisibleTimeGap);
                BufferedImage bfimg = ScreenCapture.getScreenCapture(ScreenCapture.getSystemMaximumScreenSizeRect());
//                imageViewPanel.setImage(bfimg);
                topFrame.setDisplayPanel(bfimg);
                topFrame.frameAfterAdjustment();
            }
        });

        captureSelectedScreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                topFrame.frameBeforeAdjustment();
                // make it sleep for a while, so the main frame can totally be invisible
                topFrame.threadSleep(topFrame.InvisibleTimeGap);
                new ScreenCapture(topFrame, null);
            }
        });

        saveCapture.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (topFrame.getDisplayPanel() == null || topFrame.getDisplayPanel().getImage() == null) {
                    JOptionPane.showMessageDialog(topFrame, "请先截图！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                // 以下没有设置 finally 块，因为暂时搞不清楚有没有资源需要手动释放！
                int chooserState = imageFileChooser.showSaveDialog(topFrame);
                if (chooserState == JFileChooser.APPROVE_OPTION) {
                    imageFileChooser
                            .setSelectedFile(new File(imageFileChooser.getSelectedFile().getAbsolutePath()));
                    File saveFile = imageFileChooser.getSelectedFile();
                    try {
                        String fileName = saveFile.getName();
                        if (fileName.indexOf(".") == -1) {
                            fileName += ".png";
                            saveFile = new File(saveFile.getParent(), fileName);
                        }
                        String extentionName = fileName.substring(fileName.lastIndexOf(".") + 1);
                        boolean saveSuccessfully = ImageIO
                                .write(topFrame.getDisplayPanel().getImage(), extentionName, saveFile);
                        if (!saveSuccessfully) {
                            JOptionPane.showMessageDialog(topFrame, "文件保存失败！", "提示", JOptionPane.PLAIN_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(topFrame, "文件保存成功！", "提示", JOptionPane.PLAIN_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}

