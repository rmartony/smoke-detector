package gui.form;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 20/08/2009
 * Time: 09:32:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImagePanel extends JPanel {
    public Image myImage = null;

    public ImagePanel() {
        //setLayout(null);
        setSize(320, 240);
        setMinimumSize(new Dimension(320, 240));
    }

    public void setImage(Image img) {
        this.myImage = img;
        repaint();
    }

    public Dimension getPreferredSize() { return new Dimension(320,240); }

    public void paint(Graphics g) {
        if (myImage != null) {
            g.drawImage(myImage, 0, 0, this);
            //g.drawImage(myImage, 0, 0, (int)g.getClipBounds().getWidth(), (int)g.getClipBounds().getHeight(), this);
        }
    }
}
