package com.Lifam_Ragor.ScreenRobot;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.IOException;

public class ScreenShot extends MyFrame {

    /**
     *
     */
    private static final long serialVersionUID = 4043857324861957601L;
    private JPanel contentPane;
    private JPanel toolBarPane;
    private JButton createNormal;
    private JButton createFull;
    private JButton saveImage;
    private JFileChooser imageFileChooser;

    private ImageViewPanel imageViewPanel;
    // private int x, y;
    // private Dimension maxDimension;

    /**
     * Launch the application.
     */
//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                    JFrame.setDefaultLookAndFeelDecorated(true);
//                    ScreenShot frame = new ScreenShot();
//                    frame.setVisible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    /**
     * Create the frame.
     */
    public ScreenShot() {
        dataInit();
        showView();
        createAction();
    }

    private void dataInit() {
        imageFileChooser = new JFileChooser();
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png", "png"));
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jpg", "jpeg"));
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jpeg", "jpeg"));
        imageFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("gif", "gif"));
        imageFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    private void showView() {
        createNormal = new JButton("新建截图");
        createFull = new JButton("全屏截图");
        saveImage = new JButton("保存");
        toolBarPane = new JPanel();
        toolBarPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
        toolBarPane.add(createNormal);
        toolBarPane.add(createFull);
        toolBarPane.add(saveImage);

        imageViewPanel = new ImageViewPanel();
        imageViewPanel.setAutoResize(false);

        contentPane = new JPanel();
        contentPane.setBorder(null);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(toolBarPane, BorderLayout.NORTH);
        contentPane.add(imageViewPanel, BorderLayout.CENTER);
        setContentPane(contentPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setBounds(0, 0, 500, 300);
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }

    private void createAction() {
        createNormal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    frameBeforeAdjustment();
                    // make it sleep for a while, so the main frame can totally be invisible
                    threadSleep(InvisibleTimeGap);
                    new ScreenCapture(ScreenShot.this, null);
                }
            }
        });
        createFull.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    frameBeforeAdjustment();
                    // make it sleep for a while, so the main frame can totally be invisible
                    threadSleep(InvisibleTimeGap);
                    BufferedImage bfimg = ScreenCapture.getScreenCapture(ScreenCapture.getSystemMaximumScreenSizeRect());
                    setDisplayPanel(bfimg);
                    frameAfterAdjustment();
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            }
        });
        saveImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEmptyImage()) {
                    JOptionPane.showMessageDialog(ScreenShot.this, "请先截图！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                // 以下没有设置 finally 块，因为暂时搞不清楚有没有资源需要手动释放！
                int chooserState = imageFileChooser.showSaveDialog(ScreenShot.this);
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
                                .write(imageViewPanel.getImage(), extentionName, saveFile);
                        if (!saveSuccessfully) {
                            JOptionPane.showMessageDialog(ScreenShot.this, "文件保存失败！", "提示", JOptionPane.PLAIN_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(ScreenShot.this, "文件保存成功！", "提示", JOptionPane.PLAIN_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean isEmptyImage() {
        return imageViewPanel.getImage() == null || imageViewPanel.getImage().getWidth() == 0 || imageViewPanel.getImage().getHeight() == 0;
    }

    @Override
    public void frameAfterAdjustment() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void setDisplayPanel(BufferedImage image) {
        imageViewPanel.setImage(image);
    }

}
