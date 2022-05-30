package gui.form.panel;

import gui.form.model.SelectedAreaResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 05/03/2010
 * Time: 01:13:39 AM
 * A JPanel that allows to select a rectangle area with the mouse
 */
public class SelectableAreaPanel extends JPanel implements MouseListener, MouseMotionListener {
    private boolean areaSelectable = false;
    private boolean isNewRect = true;
    private JPopupMenu popupMenu;
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String AREA_SELECTED = "areaSelected";

    private int x,y,h,w;
    private int x1, y1, x2, y2;

    private SelectedAreaResult oldSelectedAreaResult = new SelectedAreaResult(null, null);

    public SelectableAreaPanel() {
        super();
        createPopupMenu();
        addMouseListener(this); // listens for own mouse and
        addMouseMotionListener(this); // mouse-motion events
    }

    protected void paintSelectedArea(Graphics g) {
        if (isAreaSelectable()) {
            int width = this.x1 - this.x2;
            int height = this.y1 - this.y2;

            w = Math.abs(width);
            h = Math.abs(height);
            x = width < 0 ? this.x1
                    : this.x2;
            y = height < 0 ? this.y1
                    : this.y2;

            if (!this.isNewRect) {
                g.setColor(Color.ORANGE);
                g.drawRect(x, y, w, h);
                g.setPaintMode();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(int x, int y) {
        if (getInsets() == null) {
            return super.contains(x, y);
        } else {
            return !(x < getInsets().left || x >= this.getWidth() - getInsets().right || y < getInsets().top || y >= this.getHeight() - getInsets().bottom) && super.contains(x, y);
        }
    }

    public void mousePressed(final MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (contains(x, y) && isAreaSelectable()) {
            this.isNewRect = true;
            this.x1 = x;
            this.y1 = y;
            repaint();
        }
    }

    // handle event when mouse released after dragging

    public void mouseReleased(final MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (contains(x, y) && isAreaSelectable()) {
            this.x2 = x;
            this.y2 = y;
            repaint();

            if (!event.isPopupTrigger()) {
                showPopup(event);
            }
        } else {
            if (isAreaSelectable() && !event.isPopupTrigger()) {
                showPopup(event);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

// MouseMotionListener event handlers // handle event when user drags mouse with button pressed

    public void mouseDragged(final MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (contains(x, y) && isAreaSelectable()) {
            this.isNewRect = false;
            this.x2 = x;
            this.y2 = y;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAreaSelectable() {
        return areaSelectable;
    }

    public void setAreaSelectable(boolean areaSelectable) {
        this.areaSelectable = areaSelectable;
    }

    public void createPopupMenu() {
        JMenuItem menuItem;

        // Create the popup menu.
        popupMenu = new JPopupMenu();
        menuItem = new JMenuItem("Mark region as fire");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SelectedAreaResult selectedAreaResult = new SelectedAreaResult(getSelectedArea(), SelectedAreaResult.AreaType.FIRE);
                propertyChangeSupport.firePropertyChange(AREA_SELECTED, oldSelectedAreaResult, selectedAreaResult);
                oldSelectedAreaResult = selectedAreaResult;
            }
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem("Mark region as non-fire");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SelectedAreaResult selectedAreaResult = new SelectedAreaResult(getSelectedArea(), SelectedAreaResult.AreaType.NON_FIRE);
                propertyChangeSupport.firePropertyChange(AREA_SELECTED, oldSelectedAreaResult, selectedAreaResult);
                oldSelectedAreaResult = selectedAreaResult;
            }
        });
        popupMenu.add(menuItem);
        menuItem = new JMenuItem("Cancel selection");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // cancels selection
                x1 = y1 = x2 = y2 = 0;
                repaint();
            }
        });
        popupMenu.add(menuItem);
    }

    private void showPopup(MouseEvent e) {
        popupMenu.show(e.getComponent(),
                e.getX(), e.getY());
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(AREA_SELECTED, propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(AREA_SELECTED, propertyChangeListener);
    }

    public Rectangle getSelectedArea() {
        return new Rectangle(x - getInsets().left, y - getInsets().top, w, h);
    }
}
