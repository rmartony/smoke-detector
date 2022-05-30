package gui.form.panel;

import model.FastBlobFinder;
import model.detector.FireDetectorResult;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 20/08/2009
 * Time: 09:32:11 AM
 * A panel that draws an image and selectable area
 */
public class ImagePanel extends SelectableAreaPanel {
    private Image myImage = null;
    private FireDetectorResult fireDetectorResult = null;

    public ImagePanel() {
        super();
    }

    public void setImage(Image img) {
        this.myImage = img;
        getGraphics().setClip(getInsets().left, getInsets().top, myImage.getWidth(this)+getInsets().left, myImage.getHeight(this)+getInsets().top);
        repaint();
    }


    @Override
    public void setMinimumSize(Dimension minimumSize) {
        super.setMinimumSize(drawableRegion(minimumSize));
    }

    @Override
    public void setMaximumSize(Dimension maximumSize) {
        super.setMaximumSize(drawableRegion(maximumSize));
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(drawableRegion(preferredSize));

    }

    @Override
    public void setSize(Dimension d) {
        Dimension dimension = drawableRegion(d);
        super.setSize(dimension);
    }

    private Dimension drawableRegion(Dimension d) {
        if (getInsets() == null) {
            return d;
        } else {
            return new Dimension(d.width + getInsets().left + getInsets().right, d.height + getInsets().top + getInsets().bottom);
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return myImage != null && new Rectangle(getInsets().left, getInsets().top, myImage.getWidth(this), myImage.getHeight(this)).contains(x, y);
    }

    public boolean contains(Point p) {
        return myImage != null && new Rectangle(getInsets().left, getInsets().top, myImage.getWidth(this), myImage.getHeight(this)).contains(p);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (myImage != null) {

            if (getInsets() == null) {
                g.drawImage(myImage, 0, 0, this);
            } else {
                g.drawImage(myImage, getInsets().left, getInsets().top, this);
                //g.drawImage(myImage, 0, 0, this);
            }
            //g.drawImage(myImage, 0, 0, (int)g.getClipBounds().getWidth(), (int)g.getClipBounds().getHeight(), this);
        }

        // draws blobs
        if (fireDetectorResult != null) { // && fireDetectorResult.isAlarmRaised()) {
            /*
            int width = this.getWidth();
            int height = this.getHeight();
            */
            for (FastBlobFinder.Blob blob : fireDetectorResult.getBlobs()) {
                g.setXORMode(Color.YELLOW);
                g.drawRect(blob.xMin + getInsets().left, blob.yMin + getInsets().top, blob.xMax - blob.xMin, blob.yMax - blob.yMin);
            }
        }

        g.setPaintMode();
        paintSelectedArea(g);

    }

    public void setDetectorResult(FireDetectorResult fireDetectorResult) {
        this.fireDetectorResult = fireDetectorResult;
    }


}


