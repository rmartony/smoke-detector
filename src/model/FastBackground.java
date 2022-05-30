package model;

import jip.JIPImage;
import util.ImageUtil;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 13/08/2009
 * Time: 09:15:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class FastBackground {
    private JIPImage current;  // only brightness image
    private JIPImage currentRGB;
    private JIPImage currentYUV;
    private float[] bmpBackground;
    private float[] bmpIn;
    private JIPImage lastBackground;
    private boolean running = false;
    private BackgroundUpdater backgroundUpdater = new BackgroundUpdater();
    private volatile Thread thread = new Thread(backgroundUpdater);

    private float[] threshold;
    private boolean[] movingPixels;

    // constants
    private static final float INITIAL_THRESHOLD = 1f;
    private float alpha = 0.7f;
    private final float C_CONSTANT = 5.0f;
    private float timeConstant = 1 - alpha;

    private Capture capture = new Capture();

    public FastBackground(JIPImage initialBackground) {
        current = initialBackground;
        init();
    }

    public FastBackground() {
        currentRGB = capture.getNewFrame();
        capture.initAllFrames(currentRGB);
        current = capture.getCurrentYFrame();
        init();
    }

    public void reset() {
        capture.initAllFrames(currentRGB);
        current = capture.getCurrentYFrame();
        init();
    }

    public void init() {
        bmpBackground = current.getAllPixelFlo(0);
        lastBackground = currentRGB;
        int length = bmpBackground.length;
        movingPixels = new boolean[length];
        threshold = new float[length];
        for (int i = 0; i < length; i++) {
            threshold[i] = INITIAL_THRESHOLD;
        }
    }

    public JIPImage getCurrent() {
        return current;
    }

    public JIPImage getCurrentRGB() {
        return currentRGB;
    }

    public JIPImage getCurrentYUV() {
        return currentYUV;
    }

    public float[] getCurrentFloatImage() {
        return bmpBackground;
    }

    public JIPImage getLastBackground() {
        return lastBackground;
    }

    protected void update() {
        currentRGB = capture.getNewFrame();
        currentYUV = capture.getCurrentYUVFrame();
        JIPImage In = capture.getCurrentYFrame();
        JIPImage InMinus1 = capture.getLastYFrame();
        JIPImage InMinus2 = capture.getNextToLastYFrame();

        bmpIn = In.getAllPixelFlo(0);
        float[] bmpInMinus1 = InMinus1.getAllPixelFlo(0);
        float[] bmpInMinus2 = InMinus2.getAllPixelFlo(0);

        float diffInInMinus1, diffInInMinus2;
        boolean pixelMoving;

        lastBackground = currentRGB;

        for (int i = 0; i < movingPixels.length; i++) {
            diffInInMinus1 = ImageUtil.subtract(bmpIn[i], bmpInMinus1[i]);
            diffInInMinus2 = ImageUtil.subtract(bmpIn[i], bmpInMinus2[i]);

            pixelMoving = (diffInInMinus1 > threshold[i]) && (diffInInMinus2 > threshold[i]);

            float diffInMinusCurrent = ImageUtil.subtract(bmpIn[i], bmpBackground[i]);
            //blob[i] = diffInMinusCurrent > threshold[i];

            if (!pixelMoving) {
                threshold[i] = alpha * threshold[i] + timeConstant * (C_CONSTANT * diffInMinusCurrent);
                bmpBackground[i] = alpha * bmpBackground[i] + timeConstant * bmpIn[i];
            }

        }

        current = new JIPImage(current.getWidth(), current.getHeight(), bmpBackground);
    }

    public float[] getIn() {
        return bmpIn;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        timeConstant = 1 - alpha;
    }

    public void start() {
        running = true;
        if (thread == null) {
            thread = new Thread(backgroundUpdater);
        }
        thread.start();
    }

    public void stop() {
        running = false;
        thread = null;
    }

    /**
     * actualiza Background model y Threshold
     */
    public class BackgroundUpdater implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted() && running) {
                update();
            }
            running = false;
        }

    }
}


