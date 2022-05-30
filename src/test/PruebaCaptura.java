package test;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.util.BufferToImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class PruebaCaptura extends Panel implements ActionListener {
    private volatile static Player player = null;
    private CaptureDeviceInfo di = null;
    private MediaLocator ml = null;
    private JButton capture = null;
    private Buffer buf = null;
    private Image img = null;
    private VideoFormat vf = null;
    private BufferToImage btoi = null;
    private ImagePanel imgpanel = null;
    private DataSource dataSource = null;
    private PushBufferStream pbs = null;

    private static final Logger logger = Logger.getLogger("");

    public PruebaCaptura() {
        initLogger();

        vf = new VideoFormat(null);


        //DataSource dataSource = CaptureUtil.getCaptureDS(vf, null);

        Vector captureDevices = getVideoCaptureDevices();

        displayCaptureDevices(captureDevices);
        if (captureDevices == null || captureDevices.size() == 0) {
            logger.severe("No capture devices found.");
            System.exit(0);
        }

        setLayout(new BorderLayout());
        setSize(320, 550);

        imgpanel = new ImagePanel();
        capture = new JButton("Capture");
        capture.addActionListener(this);


        CaptureDeviceInfo captureDeviceInfo = (CaptureDeviceInfo) captureDevices.get(0);
        di = CaptureDeviceManager.getDevice(captureDeviceInfo.getName());
        ml = di.getLocator();

        try {
            dataSource = Manager.createDataSource(ml);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }    // to let camera settle ahead of processing

            player = Manager.createRealizedPlayer(dataSource);
            player.start();

            createPBDSource();

            FrameGrabbingControl frameControl = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
            if (frameControl == null) {
                logger.severe("Could not connect to capture device " + captureDeviceInfo.getName());
                processExit();
                System.exit(0);
            }

            Component comp;

            if ((comp = player.getVisualComponent()) != null) {
                add(comp, BorderLayout.NORTH);
            }
            add(capture, BorderLayout.CENTER);
            add(imgpanel, BorderLayout.SOUTH);

        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error al crear reproductor", e);
        }
    }

    /**
     * Gets a stream from the camera and sets debug
     */
    void createPBDSource() {
        try {
            pbs = ((PushBufferDataSource) dataSource).getStreams()[0];
            ((com.sun.media.protocol.vfw.VFWSourceStream) pbs).DEBUG = true;
        } catch (Exception e) {
        }
    }

    /**
     * Tidy exit
     */
    void processExit() {
        try {
            dataSource.stop();
            dataSource.disconnect();
        } catch (Exception e) {
            logger.log(Level.FINE, "Datasource failed at stop", e);
        }
        playerclose();
    }

    private void initLogger() {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        try {
            LogManager.getLogManager().readConfiguration();
        } catch (Throwable t) {
            displayCurrentFolder();
            logger.log(Level.WARNING, "" + t, t);
        }
    }

    public static void onClose() {
        try {
            playerclose();
        } catch (Throwable t) {
            logger.log(Level.FINE, "", t);
        }
        System.exit(0);
    }

    public static void playerclose() {
        if (player != null) {
            player.close();
            player.deallocate();
        }
    }

    public void actionPerformed(ActionEvent e) {
        JComponent c = (JComponent) e.getSource();

        if (c == capture) {
            // Grab a frame
            FrameGrabbingControl fgc = (FrameGrabbingControl)
                    player.getControl("javax.media.control.FrameGrabbingControl");
            buf = fgc.grabFrame();

            // Convert it to an image
            btoi = new
                    BufferToImage((VideoFormat) buf.getFormat());
            img = btoi.createImage(buf);

            // show the image
            imgpanel.setImage(img);

            // save image
            saveJPG(img, "c:\\test.jpg");
        }
    }

    class ImagePanel extends Panel {
        public Image myimg = null;

        public ImagePanel() {
            setLayout(null);
            setSize(320, 240);
        }

        public void setImage(Image img) {
            this.myimg = img;
            repaint();
        }

        public void paint(Graphics g) {
            if (myimg != null) {
                g.drawImage(myimg, 0, 0, this);
            }
        }
    }

    public static void saveJPG(Image img, String s) {
        BufferedImage bi = new
                BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, null, null);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(s);
        }
        catch (java.io.FileNotFoundException io) {
            System.out.println("File Not Found");
        }

        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
        param.setQuality(0.5f, false);
        encoder.setJPEGEncodeParam(param);

        try {
            encoder.encode(bi);
            out.close();
        }
        catch (java.io.IOException io) {
            logger.severe("IOException");
        }
    }

    public void displayCurrentFolder() {
        File f = new File(".");
        logger.info("Current folder: " + f.getAbsolutePath());
    }

    public Vector getCaptureDevices(Format f) {
        return CaptureDeviceManager.getDeviceList(f);
    }

    public Vector getVideoCaptureDevices() {
        return getCaptureDevices(new RGBFormat());
    }

    public void displayCaptureDevices(Vector captureDevices) {
        if (captureDevices == null) {
            logger.info("No Capture devices known to JMF");
        } else {
            logger.info("The following " + captureDevices.size()
                    + " capture devices are known to the JMF");
            for (int i = 0; i < captureDevices.size(); i++) {
                logger.info("\t" + captureDevices.elementAt(i));
            }
        }


    }

    public static void main(String[] args) {
        Frame f = new Frame("PruebaCaptura");

        /*
        PackageUtility.addContentPrefix("net.sf.fmj", false);
		PackageUtility.addProtocolPrefix("net.sf.fmj", false);

    	PlugInUtility.registerPlugIn("net.sf.fmj.media.renderer.video.SimpleAWTRenderer");
          */
        PruebaCaptura cf = new PruebaCaptura();

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        f.add("Center", cf);
        f.pack();
        f.setSize(new Dimension(320, 550));
        f.setVisible(true);
    }

}
