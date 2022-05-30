package model.detector;

import gui.form.model.SelectedAreaResult;
import jip.JIP;
import jip.JIPBitmap;
import jip.JIPImage;
import model.DecisionAlgorithm;
import model.FastBackground;
import model.FastBlobFinder;
import model.SlowBackground;
import util.ImageUtil;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 20/08/2009
 * Time: 09:55:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class FireDetectorModel {
    private FastBackground fastBackground = new FastBackground();
    private SlowBackground slowBackground = new SlowBackground();
    private boolean running = false;
    private boolean learning = false;
    private DecisionAlgorithm dA = new DecisionAlgorithm();
    private FastBlobFinder finder;
    private Decision decision = new Decision();


    private double[] weightVector;
    public final static double MU = 1;
    private FireDetectorResult lastResult;

    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String MODEL_CHANGED = "modelChanged";

    public FireDetectorModel() {
        initializeWeightVector();
    }

    public void initializeWeightVector() {
        weightVector = new double[dA.getDecisionAlgorithmsCount()];
        for (int i = 0; i < dA.getDecisionAlgorithmsCount(); i++) {
            weightVector[i] = 1D / dA.getDecisionAlgorithmsCount();
        }
    }

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

    public boolean isRunning() {
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

    public void saveLearnedSequences() {
        dA.saveLearnedSequences();
    }

    public void deleteLastLearnedSequence() {
        dA.deleteLastLearnedSequence();
    }

    public FireDetectorResult getDecisionAlgorithmResult() {
        float[] currentImage = fastBackground.getCurrentFloatImage().clone();
        //float[] currentImage = fastBackground.getIn().clone();
        float[] slowImage = slowBackground.getCurrentFloatImage().clone();
        float[] d1, d2, d3, d4;

        JIPImage currentRGB = new JIPImage(fastBackground.getCurrentRGB());
        JIPImage currentRGBBackground = new JIPImage(fastBackground.getLastBackground());
        JIPImage currentYUV = new JIPImage(fastBackground.getCurrentYUV());

        int width = currentRGB.getWidth();
        int height = currentRGB.getHeight();

        //d1 = new float[currentImage.length];
        d2 = new float[currentImage.length];
        d3 = new float[currentImage.length];
        d4 = new float[currentImage.length];

        d1 = dA.subAlgorithm1(currentImage, slowImage);

        // determine non-stationary pixels
        JIPImage binaryImage = binarizeImage(d1, width, height);
        boolean[] binaryFloatImage = binaryImage.getBand(0).getAllBitPixel();

        List<FastBlobFinder.Blob> blobList = finder.detectBlobs(binaryFloatImage, 30, -1, true);
        List<FastBlobFinder.Blob> blobs = finder.simplifyBlobs(blobList);


        if (isLearning()) {
            dA.learn(blobs);
        } else {
            d2 = dA.subAlgorithm2(binaryFloatImage, currentYUV, blobList);
            d3 = dA.subAlgorithm3(binaryFloatImage, width, height, blobs);
            d4 = dA.subAlgorithm4(binaryFloatImage, currentRGB, currentRGBBackground, blobList);
        }

        float[][] decisionVector = new float[][]{d1, d2, d3, d4};

        Decision result = calculateDecision(decisionVector);
        lastResult = new FireDetectorResult(result, decisionVector, blobList, binaryImage);

        return lastResult;
    }

    public Decision calculateDecision(float[][] d) {
        decision.setCounter(0);

        float[] result = new float[d[0].length];

        double dweights;
        for (int i = 0; i < d[0].length; i++) {
            dweights = 0;
            for (int j = 0; j < dA.getDecisionAlgorithmsCount(); j++) {
                dweights += d[j][i] * weightVector[j];
            }
            //if (dweights > 0.4D) {
            result[i] = (float) dweights;
            /*if (dweights >= 0D) {
                result[i] = 1;*/
            if (dweights > 0.4D) {
                decision.incrementCounter();
            }
            /*} else {
                result[i] = -1;
            }*/

        }

        decision.setResult(result);
        decision.setAlarmRaised(decision.getCounter() > 10);

        return decision;
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
        boolean[] binarized = ImageUtil.binarize2(currentImage, 0.80f);

        JIPImage img1 = new JIPImage(width, height, JIP.tBIT);
        img1.setBand(0, new JIPBitmap(width, height, binarized));

        img1 = ImageUtil.grow(img1);
        return img1;
    }

    public double[] getWeightVector() {
        return weightVector;
    }

    public void setWeightVector(double[] weightVector) {
        this.weightVector = weightVector;
    }

    public FireDetectorResult getLastResult() {
        return lastResult;
    }

    /**
     * Recalculates the weights based on user selection
     * LMS Algorithm aproach
     *
     * @param selectedAreaResult User selected window corresponding to fire or non-fire
     */
    public void reCalculateWeights(SelectedAreaResult selectedAreaResult) {
        double error = 0;
        double normOfDecisionVector;

        int width = fastBackground.getCurrentRGB().getWidth();

        Decision estimatedDecision = calculateDecision(lastResult.getDecisionVector());

        // estimated decision
        float[] yEstimated = estimatedDecision.getResult();

        Rectangle selectedArea = selectedAreaResult.getSelectedArea();


        // observed value (real value)
        double oracle = selectedAreaResult.getAreaType().getType();

        // calculate weights
        int maxX = selectedArea.x + selectedArea.width;
        int maxY = selectedArea.y + selectedArea.height;
        System.out.println("Selected area x: " + selectedArea.x + " y: " + selectedArea.y + " maxX: "+ maxX + " maxY: " + maxY);
        //System.out.println("Length = " + yEstimated.length);
        for (int y = selectedArea.y; y < maxY; y++) {
            for (int x = selectedArea.x; x < maxX; x++) {
                int xy = y * width + x;
                error = oracle - (yEstimated[xy] >= 0 ? 1 : -1);

                //System.out.println("error = " + oracle + " - " + yEstimated[xy] + " = " + error);

                normOfDecisionVector = 0;
                for (int i = 0; i < dA.getDecisionAlgorithmsCount(); i++) {
                    float v = lastResult.getDecisionVector()[i][xy];
                    normOfDecisionVector += v * v;
                }

                for (int i = 0; i < dA.getDecisionAlgorithmsCount(); i++) {
                    weightVector[i] = weightVector[i] + (MU * error * lastResult.getDecisionVector()[i][xy]) / normOfDecisionVector;
                }

            }
        }


        // normalizes weightVector
        double maxWeight = 0;
        for (int i = 0; i < dA.getDecisionAlgorithmsCount(); i++) {
            if (weightVector[i] < 0) {
                weightVector[i] = 0;
            }
            maxWeight = Math.max(maxWeight, weightVector[i]);

            System.out.printf("original weight %d: %s%n", i, weightVector[i]);
        }

        for (int i = 0; i < dA.getDecisionAlgorithmsCount(); i++) {
            weightVector[i] = weightVector[i] / maxWeight;
            System.out.printf("normalized weight %d: %s%n", i, weightVector[i]);
        }
        
        propertyChangeSupport.firePropertyChange(MODEL_CHANGED, 1, this);

    }

    public short getDecisionAlgorighmsCount() {
        return dA.getDecisionAlgorithmsCount();
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(MODEL_CHANGED, propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(MODEL_CHANGED, propertyChangeListener);
    }
    
}
