package com.Lifam_Ragor.ScreenRobot;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScreenRecord implements Runnable, ControllerListener, DataSinkListener {
    public static final int THREAD_SLEEP_TIME = 300;

    ScreenRobot topFrame;
    int frameRate;
    Rectangle screenCaptureRect;
    List<BufferedImage> bufferedImagesArrayList;
    String saveFileDest;
    boolean recording = false;
    boolean saveFile = false;

    float imageCompressQuality = 0.6f;

    boolean debuging = false;

    public ScreenRecord(ScreenRobot topFrame, int frameRate, Rectangle screenCaptureRect, String saveFileDest) {
        this.topFrame = topFrame;
        this.frameRate = frameRate;
        this.screenCaptureRect = screenCaptureRect;
        bufferedImagesArrayList = Collections.synchronizedList(new ArrayList<>());

        this.saveFileDest = saveFileDest;
        if (saveFileDest != null) saveFile = true;
    }

    @Override
    public void run() {
        if (saveFile) {
            processing = true;
            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    saveScreenRecord();
                }
            });
            thr.setPriority(Thread.MAX_PRIORITY);
            thr.start();
        }

        int captureFrameCount = 0;
        long millisTimeInit = System.currentTimeMillis();
        double captureFrameTimeGap = 1000 / frameRate;

        while (recording) {
//            System.out.println(screenCaptureRect);
            BufferedImage bfimg = ScreenCapture.getScreenCapture(screenCaptureRect);
            if (saveFile) bufferedImagesArrayList.add(bfimg);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    topFrame.setDisplayPanel(bfimg);
                }
            });

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

        processing = false;
    }

    void setRecording(boolean recording) {
        this.recording = recording;
    }


// The following code is modified based on 'Java Media Framework 2.1 - Sample Code' :
// 'https://www.oracle.com/technetwork/java/javase/documentation/jpegimagestomovie-176885.html'


    /*
     * @(#)JpegImagesToMovie.java   1.3 01/03/13
     * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
     * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
     * modify and redistribute this software in source and binary code form,
     * provided that i) this copyright notice and license appear on all copies of
     * the software; and ii) Licensee does not utilize the software in a manner
     * which is disparaging to Sun.
     * This software is provided "AS IS," without a warranty of any kind. ALL
     * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
     * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
     * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
     * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
     * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
     * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
     * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
     * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
     * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
     * POSSIBILITY OF SUCH DAMAGES.
     *
     * This software is not designed or intended for use in on-line control of
     * aircraft, air traffic, aircraft navigation or aircraft communications; or in
     * the design, construction, operation or maintenance of any nuclear
     * facility. Licensee represents and warrants that it will not use or
     * redistribute the Software for such purposes.
     */


    boolean processing = false;

    public boolean saveScreenRecord() {
        MediaLocator saveFileLocator = new MediaLocator(saveFileDest);

        ImageDataSource ids = new ImageDataSource(frameRate, screenCaptureRect);
        Processor p = null;
        try {
            p = Manager.createProcessor(ids);
        } catch (Exception e) {
            System.err
                    .println("Yikes!  Cannot create a processor from the data source.");
            return false;
        }

        p.addControllerListener(this);

        // Put the Processor into configured state so we can set
        // some processing options on the processor.
        p.configure();
        if (!waitForState(p, p.Configured)) {
            System.err.println("Failed to configure the processor.");
            return false;
        }

        // Set the output content descriptor to QuickTime.
        p.setContentDescriptor(new ContentDescriptor(
                FileTypeDescriptor.QUICKTIME));

        // Query for the processor for supported formats.
        // Then set it on the processor.
        TrackControl tcs[] = p.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.err.println("The mux does not support the input format: "
                    + tcs[0].getFormat());
            return false;
        }
        tcs[0].setFormat(f[0]);

        // We are done with programming the processor. Let's just
        // realize it.
        p.realize();
        if (!waitForState(p, p.Realized)) {
            System.err.println("Failed to realize the processor.");
            return false;
        }

        // Now, we'll need to create a DataSink.
        DataSink dsink;
        if ((dsink = createDataSink(p, saveFileLocator)) == null) {
            System.err
                    .println("Failed to create a DataSink for the given output MediaLocator: "
                            + saveFileLocator);
            return false;
        }

        dsink.addDataSinkListener(this);
        fileDone = false;

        try {
            System.out.println("Generating the video : " + saveFileLocator.getURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // OK, we can now start the actual transcoding.
        try {
            p.start();
            dsink.start();
        } catch (IOException e) {
            System.err.println("IO error during processing");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wait for EndOfStream event.
        waitForFileDone();

        // Cleanup.
        try {
            dsink.close();
        } catch (Exception e) {
        }
        p.removeControllerListener(this);

        System.out.println("Video creation completed!!!!!");
        return true;
    }

    /**
     * Create the DataSink.
     */
    DataSink createDataSink(Processor p, MediaLocator outML) {

        DataSource ds;

        if ((ds = p.getDataOutput()) == null) {
            System.err
                    .println("Something is really wrong: the processor does not have an output DataSource");
            return null;
        }

        DataSink dsink;

        try {
            //System.err.println("- create DataSink for: " + outML);
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        } catch (Exception e) {
            System.err.println("Cannot create the DataSink: " + e);
            return null;
        }

        return dsink;
    }

    Object waitSync = new Object();
    boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state. Return
     * false if the transition failed.
     */
    boolean waitForState(Processor p, int state) {
        synchronized (waitSync) {
            try {
                while (p.getState() < state && stateTransitionOK)
                    waitSync.wait();
            } catch (Exception e) {
            }
        }
        return stateTransitionOK;
    }

    /**
     * Controller Listener.
     */
    public void controllerUpdate(ControllerEvent evt) {

        if (evt instanceof ConfigureCompleteEvent
                || evt instanceof RealizeCompleteEvent
                || evt instanceof PrefetchCompleteEvent) {
            synchronized (waitSync) {
                stateTransitionOK = true;
                waitSync.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
                stateTransitionOK = false;
                waitSync.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            evt.getSourceController().stop();
            evt.getSourceController().close();
        }
    }

    Object waitFileSync = new Object();
    boolean fileDone = false;
    boolean fileSuccess = true;

    /**
     * Block until file writing is done.
     */
    boolean waitForFileDone() {
        synchronized (waitFileSync) {
            try {
                while (!fileDone)
                    waitFileSync.wait();
            } catch (Exception e) {
            }
        }
        return fileSuccess;
    }

    /**
     * Event handler for the file writer.
     */
    public void dataSinkUpdate(DataSinkEvent evt) {

        if (evt instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                waitFileSync.notifyAll();
            }
        } else if (evt instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                fileSuccess = false;
                waitFileSync.notifyAll();
            }
        }
    }

    public float getImageCompressQuality() {
        return imageCompressQuality;
    }

    public void setImageCompressQuality(float imageCompressQuality) {
        this.imageCompressQuality = imageCompressQuality;
    }

    public void setDebuging(boolean debuging) {
        this.debuging = debuging;
    }

    ///////////////////////////////////////////////
//
// Inner classes.
// /////////////////////////////////////////////

    /**
     * A DataSource to read from a list of JPEG image files and turn that into a
     * stream of JMF buffers. The DataSource is not seekable or positionable.
     */
    class ImageDataSource extends PullBufferDataSource {

        ImageSourceStream streams[];

        ImageDataSource(int frameRate, Rectangle screenCaptureRect) {
            streams = new ImageSourceStream[1];
            streams[0] = new ImageSourceStream(frameRate, screenCaptureRect);
        }

        public void setLocator(MediaLocator source) {
        }

        public MediaLocator getLocator() {
            return null;
        }

        /**
         * Content type is of RAW since we are sending buffers of video frames
         * without a container format.
         */
        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        public void connect() {
        }

        public void disconnect() {
        }

        public void start() {
        }

        public void stop() {
        }

        /**
         * Return the ImageSourceStreams.
         */
        public PullBufferStream[] getStreams() {
            return streams;
        }

        /**
         * We could have derived the duration from the number of frames and
         * frame rate. But for the purpose of this program, it's not necessary.
         */
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    }

    /**
     * The source stream to go along with ImageDataSource.
     */
    class ImageSourceStream implements PullBufferStream {
        VideoFormat format;
        Rectangle screenCaptureRect;

        int count = 0;
        int maxArrayLength = 0;
        long processingMillisTimeInit = System.currentTimeMillis();

        boolean ended = false;

        JPEGImageWriteParam jpegParams;

        public ImageSourceStream(int frameRate, Rectangle screenCaptureRect) {
            this.screenCaptureRect = screenCaptureRect;

            format = new VideoFormat(VideoFormat.JPEG, new Dimension(screenCaptureRect.width,
                    screenCaptureRect.height), Format.NOT_SPECIFIED, Format.byteArray,
                    (float) frameRate);

            jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(imageCompressQuality);
        }

        /**
         * We should never need to block assuming data are read from files.
         */
        public boolean willReadBlock() {
            return false;
        }

        /**
         * This is called from the Processor to read a frame worth of video
         * data.
         */
        public void read(Buffer buf) throws IOException {
            count++;
//            System.out.println("count : " + count);
            if (debuging && count % frameRate == 0) {
                System.out.println("average processing frame rate : " + 1000 / ((System.currentTimeMillis() - processingMillisTimeInit) / (double) count));
            }


            if (bufferedImagesArrayList.size() > maxArrayLength) maxArrayLength = bufferedImagesArrayList.size();

            if (bufferedImagesArrayList.isEmpty()) {
                // Check if we've finished all the frames.
                if (!processing) {
                    // We are done. Set EndOfMedia.
                    //System.err.println("Done reading all images.");
                    buf.setEOM(true);
                    buf.setOffset(0);
                    buf.setLength(0);
                    ended = true;
                    System.out.println("EOF end");
                    System.out.println("max array length : " + maxArrayLength);
                    return;
                } else {
                    try {
                        do {
                            Thread.sleep(THREAD_SLEEP_TIME);
                        } while (bufferedImagesArrayList.isEmpty());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            BufferedImage tempScreenCapture = bufferedImagesArrayList.remove(0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);

            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
// specifies where the jpg image has to be written
            writer.setOutput(ios);
// writes the file with given compression level
// from your JPEGImageWriteParam instance
            writer.write(null, new IIOImage(tempScreenCapture, null, null), jpegParams);

//            ImageIO.write(tempScreenCapture, "jpg", baos);
            byte imageBytes[] = baos.toByteArray();

            buf.setData(imageBytes);
            buf.setOffset(0);
            buf.setLength(imageBytes.length);
            buf.setFormat(format);
            buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
        }

        /**
         * Return the format of each video frame. That will be JPEG.
         */
        public Format getFormat() {
            return format;
        }

        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        public long getContentLength() {
            return 0;
        }

        public boolean endOfStream() {
            return ended;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    }
}