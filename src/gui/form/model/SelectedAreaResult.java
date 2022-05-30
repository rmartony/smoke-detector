package gui.form.model;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 06/03/2010
 * Time: 12:04:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectedAreaResult {
    private Rectangle selectedArea;
    private AreaType areaType;
    public static enum AreaType { FIRE(1), NON_FIRE(-1);
        private final float type;
        AreaType(final float type) {
            this.type = type;
        }

        public float getType() {
            return type;
        }
    }

    public SelectedAreaResult(Rectangle selectedArea, AreaType areaType) {
        this.selectedArea = selectedArea;
        this.areaType = areaType;
    }

    public Rectangle getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(Rectangle selectedArea) {
        this.selectedArea = selectedArea;
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
    }
}
