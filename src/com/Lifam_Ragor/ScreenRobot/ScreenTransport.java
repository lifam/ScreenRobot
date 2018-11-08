package com.Lifam_Ragor.ScreenRobot;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

public class ScreenTransport implements Runnable {
    ScreenRobot topFrame;
    TransportControlPanel controlPanel;

    int frameRate;
    Rectangle screenCaptureRect;
    boolean working = false;
    float imageCompressQuality = 0.6f;
    boolean debuging = false;

    //    Here '61357' is some what meaningless, what I want is just a big enough prime number.
    int transportingPort = 61357;
    InetAddress multicastAddress;
    //    Here 'isServer = true' means it will send the screen captured, vice versa.
    boolean isServer;

    Rectangle SystemMaximumScreenSizeRect;

    int packingSize = 60 * 1024;

    public ScreenTransport(ScreenRobot topFrame, TransportControlPanel controlPanel, int frameRate, Rectangle screenCaptureRect, boolean isServer) {
        this.topFrame = topFrame;
        this.controlPanel = controlPanel;
        this.frameRate = frameRate;
        this.screenCaptureRect = screenCaptureRect;
        this.isServer = isServer;

        try {
            multicastAddress = InetAddress.getByName("224.0.0.197");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        SystemMaximumScreenSizeRect = ScreenCapture.getSystemMaximumScreenSizeRect();
    }

    @Override
    public void run() {
        if (isServer) {
            int captureFrameCount = 0;
            long millisTimeInit = System.currentTimeMillis();
            double captureFrameTimeGap = 1000 / frameRate;

            MulticastSocket serverSocket = null;
            try {
                serverSocket = new MulticastSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DatagramPacket serverPacket = null;

            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(imageCompressQuality);

            int imageCount = 0;
            while (working) {
//            System.out.println(screenCaptureRect);
                BufferedImage bfimg = ScreenCapture.getScreenCapture(screenCaptureRect);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        topFrame.setDisplayPanel(bfimg);
                    }
                });

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
                    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
// specifies where the jpg image has to be written
                    writer.setOutput(ios);
// writes the file with given compression level
// from your JPEGImageWriteParam instance
                    writer.write(null, new IIOImage(bfimg, null, null), jpegParams);
                    baos.flush();
                    byte imageBytes[] = baos.toByteArray();

                    imageCount++;
                    int fullPacketNum = imageBytes.length / packingSize;
                    byte packetCache[] = new byte[packingSize + Integer.BYTES * 3];

                    int i;
                    for (i = 1; i <= fullPacketNum; i++) {
                        writeInt(packetCache, 0, imageCount);
                        writeInt(packetCache, 4, fullPacketNum + 1);
                        writeInt(packetCache, 8, i);
                        System.arraycopy(imageBytes, packingSize * (i - 1), packetCache, 12, packingSize);
                        serverPacket = new DatagramPacket(packetCache, packetCache.length, multicastAddress, transportingPort);
                        serverSocket.send(serverPacket);
                    }
                    writeInt(packetCache, 0, imageCount);
                    writeInt(packetCache, 4, fullPacketNum + 1);
                    writeInt(packetCache, 8, i);
                    System.arraycopy(imageBytes, packingSize * (i - 1), packetCache, 12, imageBytes.length % packingSize);
                    serverPacket = new DatagramPacket(packetCache, packetCache.length, multicastAddress, transportingPort);
                    serverSocket.send(serverPacket);

                    if (debuging) System.out.println("screen sent : " + imageCount + ", full packet num : " + fullPacketNum);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                captureFrameCount++;
                try {
                    long timeUpperBound = millisTimeInit + (long) (captureFrameCount * captureFrameTimeGap);
                    long timeToSleep = timeUpperBound - System.currentTimeMillis();

                    if (debuging && captureFrameCount % frameRate == 0) {
                        System.out.println("captureFrameCount : " + captureFrameCount + " average frame rate : " + 1000 / ((System.currentTimeMillis() - millisTimeInit) / (double) captureFrameCount));
                    }

                    if (timeToSleep > 10) {
                        Thread.sleep(timeToSleep);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (serverSocket != null) serverSocket.close();
        } else {
            long formerMillisTime = -1;

            byte imageByteCache[] = new byte[packingSize + Integer.BYTES * 3];
            DatagramPacket clientPacket = new DatagramPacket(imageByteCache, imageByteCache.length);
            MulticastSocket clientSocket = null;
            try {
                clientSocket = new MulticastSocket(transportingPort);
                clientSocket.joinGroup(multicastAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int imageCount = 0;
            int fullPacketNum = -1;
            boolean dropPacket = false;
//            boolean readingFirstPacket = true;
            while (working) {
                dropPacket = false;
//                BufferedImage bfimg = null;
                try {
                    if (debuging) System.out.println("packet received!");

//                    if (readingFirstPacket) {
                        clientSocket.receive(clientPacket);
                        imageByteCache = clientPacket.getData();
//                        imageCount = readInt(imageByteCache, 0);
                        fullPacketNum = readInt(imageByteCache, 4);
                        int i = readInt(imageByteCache, 8);
//                        If this is not the first packet of the image, continue, we will give up this image
                        if (i != 1) {
//                            dropPacket = true;
                            if (debuging) {
                                System.out.println("packet drop! image : " + readInt(imageByteCache, 0) + ", segment : " + i);
                            }
                            continue;
                        }
//                    }

                    byte imageByteArray[] = new byte[fullPacketNum * (packingSize)];
                    System.arraycopy(imageByteCache, 12, imageByteArray, 0, packingSize);

                    int j;
                    for (j = 2; j < fullPacketNum; j++) {
                        clientSocket.receive(clientPacket);
                        imageByteCache = clientPacket.getData();
//                        if (imageCount != readInt(imageByteCache, 0)) continue;
                        if (fullPacketNum != readInt(imageByteCache, 4)) dropPacket = true;
                        if (j != readInt(imageByteCache, 8)) dropPacket = true;
                        if (dropPacket) {
                            if (debuging) {
                                System.out.println("packet drop! image : " + readInt(imageByteCache, 0) + ", segment : " + i);
                            }
                            continue;
                        }

                        System.arraycopy(imageByteCache, 12, imageByteArray, packingSize * (j - 1), packingSize);
                    }

                    clientSocket.receive(clientPacket);
                    imageByteCache = clientPacket.getData();
//                    if (imageCount != readInt(imageByteCache, 0)) continue;
                    if (fullPacketNum != readInt(imageByteCache, 4)) dropPacket = true;
                    if (j != readInt(imageByteCache, 8)) dropPacket = true;
                    if (dropPacket) {
                        if (debuging) {
                            System.out.println("packet drop! image : " + readInt(imageByteCache, 0) + ", segment : " + i);
                        }
                        continue;
                    }

                    System.arraycopy(imageByteCache, 12, imageByteArray, packingSize * (j - 1), clientPacket.getLength() - 12);
                    int totalByteLength = (fullPacketNum - 1) * packingSize + clientPacket.getLength();

//                    byte imageByteRaw[] = Arrays.copyOfRange(clientPacket.getData(), 0, clientPacket.getLength());
                    BufferedImage bfimg = ImageIO.read(new ByteArrayInputStream(imageByteArray, 0, totalByteLength));
                    if (bfimg != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (SystemMaximumScreenSizeRect.getWidth() == bfimg.getWidth() && SystemMaximumScreenSizeRect.getHeight() == bfimg.getHeight()) {
//                                    if (!topFrame.getDisplayPanel().isAutoResize()) topFrame.getDisplayPanel().setAutoResize(true);
                                    topFrame.getDisplayPanel().setAutoResize(true);
                                } else {
                                    topFrame.getDisplayPanel().setAutoResize(false);
                                }
                                topFrame.setDisplayPanel(bfimg);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageCount++;

                if (debuging) {
                    System.out.println("image received count : " + imageCount);

                    if (formerMillisTime == -1) formerMillisTime = System.currentTimeMillis();
                    else {
                        long currentMillisTime = System.currentTimeMillis();
                        double currentFrameRate = 1000 / (double) (currentMillisTime - formerMillisTime);
                        formerMillisTime = currentMillisTime;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                controlPanel.updateFrameRate(currentFrameRate);
                            }
                        });
                    }
                }
            }

            if (clientSocket != null) clientSocket.close();
        }
    }

    int readInt(byte data[], int offset) {
        return ((data[offset] & (0xff)) << 24) | ((data[offset + 1] & (0xff)) << 16) | (((data[offset + 2] & (0xff))) << 8) | ((data[offset + 3] & (0xff)));
    }

    void writeInt(byte data[], int offset, int i) {
        data[offset] = (byte) (i >> 24);
        data[offset + 1] = (byte) (i >> 16);
        data[offset + 2] = (byte) (i >> 8);
        data[offset + 3] = (byte) i;
    }

    void setWorking(boolean working) {
        this.working = working;
    }

    public void setDebuging(boolean debuging) {
        this.debuging = debuging;
    }

    public float getImageCompressQuality() {
        return imageCompressQuality;
    }

    public void setImageCompressQuality(float imageCompressQuality) {
        this.imageCompressQuality = imageCompressQuality;
    }
}
