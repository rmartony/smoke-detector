package model;

import jipfunc.FCapture2;
import jipfunc.FRGBToYCbCr;
import jip.JIPImage;
import util.ImageUtil;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 11/08/2009
 * Time: 09:12:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class Capture {
    //public static final short NUMBER_OF_FRAMES = 5*2;
    private static final short NUMBER_OF_FRAMES = 3;
    private static FCapture2 capture = new FCapture2();
    //private static final Capture instance = new Capture();

    // frame[0] = In = current frame
    // frame[1] = In-1, etc.
    private LinkedList<JIPImage> frames = new LinkedList<JIPImage>();
    private LinkedList<JIPImage> yuvFrames = new LinkedList<JIPImage>();
    private LinkedList<JIPImage> yFrames = new LinkedList<JIPImage>();

    /*
    private Capture() {
    }
    */

    /*
    public static Capture getInstance() {
        return instance;
    }
    */

    public JIPImage getNewFrame() {
        JIPImage frame =  capture.processImg(null);
        JIPImage yuvFrame = ImageUtil.convertToYUV(frame);

        if (frames.size() >= NUMBER_OF_FRAMES) {
            frames.removeLast();
            yuvFrames.removeLast();
            yFrames.removeLast();
        }

        frames.addFirst(frame);
        yuvFrames.addFirst(yuvFrame);
        yFrames.addFirst(ImageUtil.extractYFromYUVImage(yuvFrame));

        return frame;
    }

    public void initAllFrames(JIPImage jipImage) {
        JIPImage yuvFrame = ImageUtil.convertToYUV(jipImage);
        JIPImage yFrame = ImageUtil.extractYFromYUVImage(yuvFrame);
        frames.clear();
        yuvFrames.clear();
        yFrames.clear();
        for (int i=0; i < NUMBER_OF_FRAMES; i++) {
            frames.addFirst(jipImage);
            yuvFrames.addFirst(yuvFrame);
            yFrames.addFirst(yFrame);
        }
    }

    public JIPImage getCurrentYUVFrame() {
        return yuvFrames.getFirst();
    }

    public JIPImage getCurrentFrame() {
        return frames.getFirst();
    }

    public JIPImage getLastFrame() {
        return frames.get(1);
    }

    public JIPImage getNextToLastFrame() {
        return frames.get(2);
    }

    public JIPImage getCurrentYFrame() {
        return yFrames.getFirst();
    }

    public JIPImage getLastYFrame() {
        return yFrames.get(1);
    }

    public JIPImage getNextToLastYFrame() {
        return yFrames.get(2);
    }

    public JIPImage getFrame(short f) {
        return frames.get(f);
    }

    public JIPImage getYFrame(short f) {
        return yFrames.get(f);
    }

    public JIPImage getYUVFrame(short f) {
        return yuvFrames.get(f);
    }
    
    public void start() {
        JIPImage frame =  capture.processImg(null);
        initAllFrames(frame);
    }

    public void stop() {
        capture.stopCapture();
    }

}
