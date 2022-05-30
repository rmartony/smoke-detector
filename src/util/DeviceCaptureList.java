package util;

import javax.media.*;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.swing.*;
import java.awt.*;


public class DeviceCaptureList {
    private static CaptureDeviceInfo captureVideoDevice = null;
    JFrame frame = new JFrame("Test Web Cam");
    Player p = null;
    FrameGrabbingControl frameGrabber;

    public DeviceCaptureList() {

//get all the capture devices
// add capture devices
        net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger.addCaptureDevices();
        java.util.Vector deviceListVector = CaptureDeviceManager.getDeviceList(null);
        System.out.println("Number of capture device: " + deviceListVector.size());

        for (int i = 0; i < deviceListVector.size(); i++) {
//display device name
            CaptureDeviceInfo deviceInfo = (CaptureDeviceInfo) deviceListVector.elementAt(i);
            System.out.println("deviceInfo: " + deviceInfo);

        }

        CaptureDeviceInfo webCamInfo = (CaptureDeviceInfo) deviceListVector.elementAt(0);
        System.out.println("Web Camera Info: " + webCamInfo);
        try {
            System.out.println("ready to create realized player");
            p = Manager.createRealizedPlayer(webCamInfo.getLocator());
            System.out.println("visual component:" + p.getVisualComponent());
            frameGrabber = (FrameGrabbingControl) p.getControl("javax.media.control.FrameGrabbingControl");
            System.out.println("Frame Grabbing Control: " + frameGrabber);

        }
        catch (Exception e) {
            System.out.println("Unable to create the web cam Player: " + e);
        }

        System.out.println("done with creating the realized player");


        Component displayWebCam = p.getVisualComponent();
        frame.getContentPane().add(displayWebCam, "Center");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        p.start();


        Buffer frameBuffer = frameGrabber.grabFrame();
        System.out.println("Frame Buffer: " + frameBuffer);
        FormatControl formatCon = (FormatControl) p.getControl("javax.media.control.FormatControl");
        VideoFormat vF = (VideoFormat) formatCon.getFormat();
        BufferToImage bufToImage = new BufferToImage(vF);
        Image img = bufToImage.createImage(frameBuffer);


        if (frameBuffer == null)
            System.out.println("no frame");
    }

    public static void main(String args[]) {
        DeviceCaptureList deviceCaptureList = new DeviceCaptureList();


    }
}