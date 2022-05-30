package gui;

import gui.form.FireDetectorForm;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 20/08/2009
 * Time: 08:47:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args) {

        // Set cross-platform Java L&F
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        final FireDetectorForm fireDetectorForm = new FireDetectorForm();

        fireDetectorForm.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        fireDetectorForm.close();
                    }
                }
        );

        fireDetectorForm.pack();
        fireDetectorForm.setVisible(true);

    }

}
