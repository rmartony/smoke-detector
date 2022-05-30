package gui.form.panel;

import model.detector.FireDetectorModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 21/03/2010
 * Time: 02:48:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WeightsPanel extends JPanel implements ChangeListener {
    private FireDetectorModel fireDetectorModel;
    private List<JSpinner> jSpinners;

    public WeightsPanel() {

    }

    public WeightsPanel(FireDetectorModel fireDetectorModel) {
        initPanel(fireDetectorModel);
    }

    public void initPanel(FireDetectorModel fireDetectorModel) {
        this.fireDetectorModel = fireDetectorModel;
        this.setLayout(new FlowLayout());
        initJSpinners();
        this.fireDetectorModel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updatePanel();
                System.out.println("Model changed");
            }
        });
        JButton jButton = new JButton();
        jButton.setText("reset");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtonActionPerformed();
            }
        });
        this.add(jButton);
    }

    private void resetButtonActionPerformed() {
        fireDetectorModel.initializeWeightVector();
        updatePanel();
    }

    private void initJSpinners() {
        jSpinners = new ArrayList<JSpinner>(fireDetectorModel.getDecisionAlgorighmsCount());

        for (int i = 0; i < fireDetectorModel.getDecisionAlgorighmsCount(); i++) {
            SpinnerNumberModel model = new SpinnerNumberModel(0D, 0D, 1D, 0.01D);
            JSpinner jSpinner = new JSpinner(model) {
                @Override
                protected JComponent createEditor(SpinnerModel model) {
                    return new NumberEditor(this, "0.00");
                }
            };
            this.add(jSpinner);
            jSpinners.add(jSpinner);
        }
        updatePanel();
    }

    public void updatePanel() {
        disableListeners();
        double[] weightVector = fireDetectorModel.getWeightVector();
        for (int i = 0; i < weightVector.length; i++) {
            jSpinners.get(i).setValue(weightVector[i]);
        }
        enableListeners();
    }

    public void Panel2Model() {
        for (int i = 0; i < jSpinners.size(); i++) {
            fireDetectorModel.getWeightVector()[i] = (Double) jSpinners.get(i).getValue();
        }
    }

    public void stateChanged(ChangeEvent e) {
        Panel2Model();
        System.out.println("User changed weights");
    }

    public void enableListeners() {
        for (JSpinner jSpinner : jSpinners) {
            jSpinner.addChangeListener(this);
        }
    }

    public void disableListeners() {
        for (JSpinner jSpinner : jSpinners) {
            jSpinner.removeChangeListener(this);
        }
    }

}
