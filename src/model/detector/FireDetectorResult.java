package model.detector;

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
public class FireDetectorResult {
    private Decision decision;
    private float[][] decisionVector;
    private JIPImage binaryImage;
    private List<FastBlobFinder.Blob> blobs;

    public FireDetectorResult(Decision decision, float[][] decisionVector, List<FastBlobFinder.Blob> blobs, JIPImage binaryImage) {
        this.decisionVector = decisionVector;
        this.binaryImage = binaryImage;

        this.blobs = blobs;
        this.decision = decision;
    }

    public float[][] getDecisionVector() {
        return decisionVector;
    }

    public void setDecisionVector(float[][] decisionVector) {
        this.decisionVector = decisionVector;
    }

    public List<FastBlobFinder.Blob> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<FastBlobFinder.Blob> blobs) {
        this.blobs = blobs;
    }

    public Decision getDecision() {
        return decision;
    }

    public float[] getResult() {
        return decision.getResult();
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public JIPImage getBinaryImage() {
        return binaryImage;
    }

    public void setBinaryImage(JIPImage binaryImage) {
        this.binaryImage = binaryImage;
    }

    public boolean isAlarmRaised() {
        return decision.isAlarmRaised();
    }

}
