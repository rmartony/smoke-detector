package model;

import jip.JIPImage;
import util.ImageUtil;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 21/08/2009
 * Time: 09:52:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DecisionAlgorithm {
    private short decisionAlgorithmsCount = 4; // number of decision algorithms - 1
    //private float tLow = 0.01f, tHigh = 0.03f;
    private float tLow = 0.039f, tHigh = 0.117f; // low and high luminance threshold for subAlgorithm 1
    private float tI = 0.392f; // low luminance threshold for subAlgorithm 2
    private static final float HALF_LUMINANCE = 0.5f;
    private static final float LUMINANCE_THRESHOLD = 0.6f;
    private HMModel hmModel = new HMModel();
    private static final int MIN_SEQUENCE_LENGTH = 20;

    public short getDecisionAlgorithmsCount() {
        return decisionAlgorithmsCount;
    }

    /**
     * Subalgorithm 1 - Movement
     * @param bFast background updated every frame
     * @param bSlow background updated every 1 second
     * @return result matrix
     */
    public float[] subAlgorithm1(final float[] bFast, final float[] bSlow) {
        float[] result = new float[bFast.length];
        float tDiff = tHigh - tLow;
        float bFastMinusBSlow;

        for (int i = 0; i < bFast.length; i++) {
            bFastMinusBSlow = ImageUtil.subtract(bFast[i], bSlow[i]);

            if (bFastMinusBSlow <= tLow) {
                result[i] = -1f;
            } else if ((tLow < bFastMinusBSlow) && (bFastMinusBSlow < tHigh)) {
                result[i] = 2f * ((bFastMinusBSlow - tLow) / tDiff) - 1f;
            } else {
                result[i] = 1f;
            }
        }

        return result;
    }

    /**
     * Subalgorithm 2 - Color
     *
     * @param YUVFrame
     * @return
     */
    public float[] subAlgorithm2(final JIPImage YUVFrame) {
        float[] YFrame = YUVFrame.getAllPixelFlo(0);
        float[] UFrame = YUVFrame.getAllPixelFlo(1);
        float[] VFrame = YUVFrame.getAllPixelFlo(2);
        float[] result = new float[YFrame.length];

        for (int i = 0; i < YFrame.length; i++) {
            if (YFrame[i] > tI) {
                result[i] = 1F - (ImageUtil.subtract(UFrame[i], HALF_LUMINANCE) + ImageUtil.subtract(VFrame[i], HALF_LUMINANCE)) / HALF_LUMINANCE;
            } else {
                result[i] = -1F;
            }
        }

        return result;
    }

    /**
     * Subalgorithm 2 - Color
     *
     * @param binaryImage binary image containing non-stationary pixels
     * @param YUVFrame    imagen YUV
     * @param blobs       lista de blobs detectados
     * @return matriz resultado que contiene valores entre -1 y 1
     */
    public float[] subAlgorithm2(final boolean[] binaryImage, final JIPImage YUVFrame, final List<FastBlobFinder.Blob> blobs) {
        float[] YFrame = YUVFrame.getAllPixelFlo(0);
        float[] UFrame = YUVFrame.getAllPixelFlo(1);
        float[] VFrame = YUVFrame.getAllPixelFlo(2);
        float[] result = new float[YFrame.length];

        for (FastBlobFinder.Blob blob : blobs) {
            for (int y = blob.yMin; y < blob.yMax; y++) {
                int posy = y * YUVFrame.getWidth();
                for (int x = blob.xMin; x < blob.xMax; x++) {
                    int posxy = posy + x;
                    if (binaryImage[posxy]) {
                        if (YFrame[posxy] > tI) {
                            result[posxy] = 1F - (ImageUtil.subtract(UFrame[posxy], HALF_LUMINANCE) + ImageUtil.subtract(VFrame[posxy], HALF_LUMINANCE)) / HALF_LUMINANCE;
                        } else {
                            result[posxy] = -1F;
                        }
                    } else {
                        result[posxy] = 0F;
                    }
                }

            }
        }

        return result;
    }

    /**
     * Calculates upper-most pixel of slow moving region
     *
     * @param blobs
     * @return upper most pixel row of slow moving region
     */
    public int getUpperMostPixel(List<FastBlobFinder.Blob> blobs) {
        int upperMostPixelRow = 0;
        if (blobs != null && blobs.size() > 0) {
            upperMostPixelRow = blobs.get(0).yMin;
        }
        return upperMostPixelRow;
    }

    /**
     * Subalgorithm 3 - rising regions
     *
     * @param binaryFloatImage binary image containing non-stationary pixels
     * @param blobs       lista de blobs detectados
     * @return matriz resultado que contiene valores entre -1 y 1
     */
    public float[] subAlgorithm3(final boolean[] binaryFloatImage, final int width, final int height, List<FastBlobFinder.Blob> blobs) {
        int upperMostPixelRow = getUpperMostPixel(blobs);

        float d3 = -1;

        hmModel.addSignalToSequence(upperMostPixelRow);
        if (hmModel.sequenceLength() > MIN_SEQUENCE_LENGTH) {
            double p1 = hmModel.probabilitySmoke();
            double p2 = hmModel.probabilityCloud();

            d3 = (float) ((p1 - p2) / (p1 + p2));
        }

        float[] result = new float[width * height];
        for (FastBlobFinder.Blob blob : blobs) {
            for (int y = blob.yMin; y < blob.yMax; y++) {
                int posy = y * width;
                for (int x = blob.xMin; x < blob.xMax; x++) {
                    int posx = posy + x;
                    if (binaryFloatImage[posx]) {
                        result[posx] = d3;
                    } else {
                        result[posx] = 0;
                    }
                }
            }

        }
        return result;

    }

    /**
     * Subalgorithm 4 - Shadow removal
     *
     * @param binaryFloatImage binary image containing non-stationary pixels
     * @param RGBImage      RGB image
     * @param RGBBackground RGB background image
     * @param blobs         Collection of detected blobs
     * @return matriz resultado que contiene valores entre -1 y 1
     */
    public float[] subAlgorithm4(final boolean[] binaryFloatImage, final JIPImage RGBImage, final JIPImage RGBBackground, final List<FastBlobFinder.Blob> blobs) {
        int width = RGBImage.getWidth();
        float[] result = new float[width * RGBImage.getHeight()];

        int[] rI = RGBImage.getAllPixelRed();
        int[] gI = RGBImage.getAllPixelGreen();
        int[] bI = RGBImage.getAllPixelBlue();
        int[] rB = RGBBackground.getAllPixelRed();
        int[] gB = RGBBackground.getAllPixelGreen();
        int[] bB = RGBBackground.getAllPixelBlue();
        long SumRI = 0, SumGI = 0, SumBI = 0;
        long SumRB = 0, SumGB = 0, SumBB = 0;
        double magnitudeCI, magnitudeCB;

        for (FastBlobFinder.Blob blob : blobs) {
            for (int y = blob.yMin; y < blob.yMax; y++) {
                int posy = y * width;
                for (int x = blob.xMin; x < blob.xMax; x++) {
                    int posxy = posy + x;
                    if (binaryFloatImage[posxy]) {
                        SumRI += rI[posxy];
                        SumGI += gI[posxy];
                        SumBI += bI[posxy];
                        SumRB += rB[posxy];
                        SumGB += gB[posxy];
                        SumBB += bB[posxy];
                    }

                }
            }

            SumRI /= blob.mass;
            SumGI /= blob.mass;
            SumBI /= blob.mass;
            SumRB /= blob.mass;
            SumGB /= blob.mass;
            SumBB /= blob.mass;

            magnitudeCI = Math.sqrt(SumRI * SumRI + SumGI * SumGI + SumBI * SumBI);
            magnitudeCB = Math.sqrt(SumRB * SumRB + SumGB * SumGB + SumGB * SumGB);

            double dotIB = SumRI * SumRB + SumGI * SumGB + SumBI * SumBB;
            double angle = Math.acos(dotIB / (magnitudeCB * magnitudeCI));

            for (int y = blob.yMin; y < blob.yMax; y++) {
                int posy = y * width;
                for (int x = blob.xMin; x < blob.xMax; x++) {
                    int posxy = posy + x;
                    if (binaryFloatImage[posxy]) {
                        if (magnitudeCI < magnitudeCB) {
                            result[posxy] = (float) (4 * Math.abs(angle) / Math.PI);
                        } else {
                            result[posxy] = -1;
                        }
                    } else {
                        result[posxy] = 0;
                    }

                }
            }

        }

        return result;
    }

    public void startLearning() {
        hmModel.startLearning();
    }

    public void stopLearning() {
        hmModel.stopLearning();
    }

    public void learn(final List<FastBlobFinder.Blob> blobs) {
        int upperMostPixelRow = getUpperMostPixel(blobs);
        hmModel.addSignalToLearningSequence(upperMostPixelRow);
    }

    public void finishLearning(boolean smoke) {
        hmModel.finishLearning(smoke);
    }

    public void deleteLastLearnedSequence() {
        hmModel.deleteLastLearnedSequence();
    }

    public void saveLearnedSequences() {
        hmModel.saveLearnedSequences();
    }

}
