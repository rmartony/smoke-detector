package gui.model;

import jip.JIP;
import jip.JIPBitmap;
import jip.JIPImage;
import model.DecisionAlgorithm;
import model.FastBackground;
import model.FastBlobFinder;
import model.SlowBackground;
import util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 20/08/2009
 * Time: 09:55:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class MotionDetectorModel {
    private FastBackground fastBackground = new FastBackground();
    private SlowBackground slowBackground = new SlowBackground();
    private boolean running = false;
    private boolean learning = false;
    private DecisionAlgorithm dA = new DecisionAlgorithm();
    private FastBlobFinder finder;

    public void start() {
        if (!running) {
            running = true;

            fastBackground.start();
            slowBackground.start();

            JIPImage i = getCurrentRGB();
            if (finder == null) {
                finder = new FastBlobFinder(i.getWidth(), i.getHeight());
            }
            System.out.println("Started");
        }
    }

    public void stop() {
        if (running) {
            running = false;

            fastBackground.stop();
            slowBackground.stop();
            System.out.println("Stopped");
        }
    }

    public boolean running() {
        return running;
    }

    public JIPImage getFastBackground() {
        return fastBackground.getCurrent();
    }

    public JIPImage getSlowBackground() {
        return slowBackground.getCurrent();
    }

    public JIPImage getCurrentRGB() {
        return fastBackground.getCurrentRGB();
    }

    public float[] getIn() {
        return fastBackground.getIn();
    }

    public float[] getBfastFloatImage() {
        return fastBackground.getCurrentFloatImage();
    }

    public float[] getBslowFloatImage() {
        return slowBackground.getCurrentFloatImage();
    }

    public void reset() {
        fastBackground.reset();
        slowBackground.reset();
    }

    public boolean isLearning() {
        return learning;
    }

    public void startLearning() {
        learning = true;
        dA.startLearning();
    }

    public void stopLearning() {
        learning = false;
        dA.stopLearning();
    }

    public void finishLearning(boolean smoke) {
        dA.finishLearning(smoke);
    }

    public void deleteLastLearnedSequence() {
        dA.deleteLastLearnedSequence();
    }


    public float[] showDA1() {
        float[] d1 = dA.subAlgorithm1(fastBackground.getCurrentFloatImage(), slowBackground.getCurrentFloatImage());
        // para movimientos muy lentos, descomentar la siguiente linea
        //float[] d1 = dA.subAlgorithm1(fastBackground.getIn(), slowBackground.getCurrentFloatImage());
        return d1;
    }

    public float[] showDA2() {
        float[] d2 = dA.subAlgorithm2(fastBackground.getCurrentYUV());
        //float[] d2 = dA.subAlgorithm2(Capture.getInstance().getCurrentYUVFrame());
/*
        for (int i=0; i < 50; i++) {
            System.out.print(d2[i] + " ");
        }
        System.out.println("");
*/
        return d2;
    }


    public int[] showDA3(float[] img) {
        int[] result = new int[img.length];

        /*
        double prob = dA.subAlgorithm3(img);
        if (prob > 0.3) {
            System.out.println("D3: " + prob);
        }

        for (int j = -7; j < 7; j++) {
            if ((i + j >= 0) && (i + j < img.length)) {
                result[i + j] = 255;
            }
        }
        */
        return result;
    }

    public MotionDetectorResult showDA() {
        float[] currentImage = fastBackground.getCurrentFloatImage().clone();
        float[] slowImage = slowBackground.getCurrentFloatImage().clone();
        float[] d1, d2, d3, d4;

        JIPImage currentRGB = new JIPImage(fastBackground.getCurrentRGB());
        JIPImage currentRGBBackground = new JIPImage(fastBackground.getLastBackground());
        JIPImage currentYUV = new JIPImage(fastBackground.getCurrentYUV());
        // Blob list
        ArrayList<FastBlobFinder.Blob> blobList = null;

        int width = currentRGB.getWidth();
        int height = currentRGB.getHeight();

        d1 = new float[currentImage.length];
        d2 = new float[currentImage.length];
        d3 = new float[currentImage.length];
        d4 = new float[currentImage.length];

        d1 = dA.subAlgorithm1(currentImage, slowImage);

        JIPImage binaryImage = binarizeImage(d1, width, height);
        boolean[] bin = binaryImage.getBand(0).getAllBitPixel();

        List<FastBlobFinder.Blob> blobs = finder.detectBlobs(bin, 30, -1, true, blobList);

        if (isLearning()) {
            dA.learn(blobs);
        } else {
            d2 = dA.subAlgorithm2(bin, currentYUV, blobs);
            d3 = dA.subAlgorithm3(binaryImage, blobs);
            d4 = dA.subAlgorithm4(binaryImage, currentRGB, currentRGBBackground, blobs);
        }

        float[] result = calculateDecision(d1, d2, d3, d4);

        return new MotionDetectorResult(result, d1, d2, d3, d4, blobs, binaryImage);

    }

    public float[] calculateDecision
            (
                    float[] d1,
                    float[] d2,
                    float[] d3,
                    float[] d4) {
        float[] result = new float[d1.length];
        float[] weightVector = {0.25F, 0.25F, 0.25F, 0.25F};
        for (int i = 0; i < d1.length; i++) {
            float d = d1[i] * weightVector[0] + d2[i] * weightVector[1] + d3[i] * weightVector[2] + d4[i] * weightVector[3];
            if (d > 0.3F) {
                result[i] = 1;
            } else {
                result[i] = -1;
            }

        }
        return result;
    }

    /*
    private boolean[] binarizeImage(float[] currentImage, int width, int height) {
        boolean[] binarized = ImageUtil.binarize2(currentImage, 0.9f);

        JIPImage img1 = new JIPImage(width, height, JIP.tBIT);
        img1.setBand(0, new JIPBitmap(width, height, binarized));

        img1 = ImageUtil.grow(img1);

        boolean[] bin = img1.getBand(0).getAllBitPixel();
        return bin;
    }
    */

    private JIPImage binarizeImage(float[] currentImage, int width, int height) {
        boolean[] binarized = ImageUtil.binarize2(currentImage, 0.9f);

        JIPImage img1 = new JIPImage(width, height, JIP.tBIT);
        img1.setBand(0, new JIPBitmap(width, height, binarized));

        img1 = ImageUtil.grow(img1);
        return img1;
    }


}
