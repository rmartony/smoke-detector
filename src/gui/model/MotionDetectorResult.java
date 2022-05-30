package gui.model;

import jip.JIPImage;
import model.FastBlobFinder;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 22/12/2009
 * Time: 06:40:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class MotionDetectorResult {
    private float[] result, d1, d2, d3, d4;
    private JIPImage binaryImage;
    private List<FastBlobFinder.Blob> blobs;

    public MotionDetectorResult(float[] result, float[] d1, float[] d2, float[] d3, float[] d4, List<FastBlobFinder.Blob> blobs, JIPImage binaryImage) {
        this.d1 = d1;
        this.d2 = d2;
        this.d3 = d3;
        this.d4 = d4;
        this.binaryImage = binaryImage;

        this.blobs = blobs;
        this.result = result;
    }

    public float[] getD1() {
        return d1;
    }

    public void setD1(float[] d1) {
        this.d1 = d1;
    }

    public float[] getD2() {
        return d2;
    }

    public void setD2(float[] d2) {
        this.d2 = d2;
    }

    public float[] getD3() {
        return d3;
    }

    public void setD3(float[] d3) {
        this.d3 = d3;
    }

    public float[] getD4() {
        return d4;
    }

    public void setD4(float[] d4) {
        this.d4 = d4;
    }

    public List<FastBlobFinder.Blob> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<FastBlobFinder.Blob> blobs) {
        this.blobs = blobs;
    }

    public float[] getResult() {
        return result;
    }

    public void setResult(float[] result) {
        this.result = result;
    }

    public JIPImage getBinaryImage() {
        return binaryImage;
    }

    public void setBinaryImage(JIPImage binaryImage) {
        this.binaryImage = binaryImage;
    }
}
