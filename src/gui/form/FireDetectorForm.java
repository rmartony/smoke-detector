/*
 * Created by JFormDesigner on Sun Feb 14 15:57:27 UYST 2010
 */

package gui.form;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import gui.form.model.FireDetectorFormModel;
import gui.form.model.SelectedAreaResult;
import gui.form.panel.*;
import gui.form.panel.ImagePanel;
import jip.JIPImage;
import jip.JIPToolkit;
import model.FastBlobFinder;
import model.detector.FireDetectorModel;
import model.detector.FireDetectorResult;
import util.ImageUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

/**
 * @author rmartony
 */
public class FireDetectorForm extends JFrame {
    private FireDetectorModel fireDetectorModel = new FireDetectorModel();
    private Analyzer analyzer = new Analyzer();
    private volatile Thread thread = new Thread(analyzer);
    private boolean analyzing;
    private Dimension capturedImagesDimension;


    public FireDetectorForm() {
        initComponents();
        loadShowComboBoxOptions();
        fireDetectorModel.start();
        previewActionPerformed();
        initImagePanels();
        weightsPanel.initPanel(fireDetectorModel);
    }

    private void loadShowComboBoxOptions() {
        showComboBox.setModel(new javax.swing.DefaultComboBoxModel(FireDetectorFormModel.ShowComboBoxOptions.values()));
        showComboBox.setSelectedItem(FireDetectorFormModel.ShowComboBoxOptions.RESULT);
    }

    private void initImagePanels() {
        JIPImage jipImage = fireDetectorModel.getFastBackground();
        capturedImagesDimension = new Dimension(jipImage.getWidth(), jipImage.getHeight());

        imagePanel1.setPreferredSize(capturedImagesDimension);
        imagePanel1.setMinimumSize(capturedImagesDimension);
        imagePanel1.setMaximumSize(capturedImagesDimension);
        imagePanel1.setSize(capturedImagesDimension);

        imagePanel1.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fireDetectorModel.reCalculateWeights((SelectedAreaResult) evt.getNewValue());
            }
        });

        imagePanel1.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (!fireDetectorModel.isRunning() && imagePanel1.contains(e.getPoint())) {

                    int x = e.getX() - imagePanel1.getInsets().left;
                    int y = e.getY() - imagePanel1.getInsets().top;

                    if (fireDetectorModel.getLastResult() != null) {
                        float result = fireDetectorModel.getLastResult().getResult()[capturedImagesDimension.width * y + x];
                        statusBar.setText("x: " + x + " y: " + y + " result: " + result);
                    }

                }

            }
        });

        imagePanel2.setPreferredSize(capturedImagesDimension);
        imagePanel2.setMinimumSize(capturedImagesDimension);
        imagePanel2.setMaximumSize(capturedImagesDimension);
        imagePanel2.setSize(capturedImagesDimension);

        imagePanel2.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fireDetectorModel.reCalculateWeights((SelectedAreaResult) evt.getNewValue());
            }
        });
    }

    private void previewActionPerformed() {
        if (analyzeCheckBox.isSelected()) {
            if (thread == null) {
                thread = new Thread(analyzer);
            }
            analyzing = true;
            if (!thread.isAlive()) {
                thread.start();
            }
        } else {
            analyzing = false;
            thread = null;
        }
    }

    public void preview() {

        /*
        JIPImage jipImage = fireDetectorModel.getSlowBackground();
        //JIPImage jipImage = fireDetectorModel.getCurrentRGB();
        Image image = JIPToolkit.getAWTImage(jipImage);
        imagePanel1.setImage(image);
        */

        FireDetectorResult fireDetectorResult = fireDetectorModel.getDecisionAlgorithmResult();
        fireLabel.setVisible(fireDetectorResult.isAlarmRaised());

        if (previewCheckBox.isSelected()) {
            int ancho = capturedImagesDimension.width;
            int alto = capturedImagesDimension.height;


            /*
            boolean[] binarized = ImageUtil.binarize2(img, 0.9f);

            JIPImage img1 = new JIPImage(ancho, alto, JIP.tBIT);
            img1.setBand(0, new JIPBitmap(ancho, alto, binarized));

            Image jipImg1 = JIPToolkit.getAWTImage(img1);
            imagePanel1.setImage(jipImg1);


            boolean[] bin = img1.getBand(0).getAllBitPixel();
            */

            FireDetectorFormModel.ShowComboBoxOptions selectedOption = (FireDetectorFormModel.ShowComboBoxOptions) showComboBox.getSelectedItem();
            if (!selectedOption.equals(FireDetectorFormModel.ShowComboBoxOptions.NO_PREVIEW)) {
                float[] imagen;

                switch (selectedOption) {
                    case RESULT:
                        imagen = fireDetectorResult.getResult();
                        showResultImage(ancho, alto, imagen);
                        break;
                    case DA1:
                        imagen = fireDetectorResult.getDecisionVector()[0];
                        showResultImage(ancho, alto, imagen);
                        break;
                    case DA2:
                        imagen = fireDetectorResult.getDecisionVector()[1];
                        showResultImage(ancho, alto, imagen);
                        break;
                    case DA3:
                        imagen = fireDetectorResult.getDecisionVector()[2];
                        showResultImage(ancho, alto, imagen);
                        break;
                    case DA4:
                        imagen = fireDetectorResult.getDecisionVector()[3];
                        showResultImage(ancho, alto, imagen);
                        break;
                    case SLOW:
                        JIPImage jipImage = fireDetectorModel.getSlowBackground();
                        Image image = JIPToolkit.getAWTImage(jipImage);
                        imagePanel1.setImage(image);
                        break;
                    case FAST:
                        jipImage = fireDetectorModel.getFastBackground();
                        image = JIPToolkit.getAWTImage(jipImage);
                        imagePanel1.setImage(image);
                        break;
                    case BINARY:
                        jipImage = fireDetectorResult.getBinaryImage();
                        image = JIPToolkit.getAWTImage(jipImage);
                        imagePanel1.setImage(image);
                        break;
                }

            }

            /*
            //JIPImage img1 = new JIPImage(ancho, alto, ImageUtil.adjustImage(fireDetectorResult.getDecision()));
            JIPImage img1 = new JIPImage(ancho, alto, ImageUtil.adjustImage(fireDetectorResult.getD4()));
            //JIPImage img1 = fireDetectorResult.getBinaryImage();
            Image jipImg1 = JIPToolkit.getAWTImage(img1);
            //imagePanel1.setImage(jipImg1);
            */

            JIPImage currentRGB = fireDetectorModel.getCurrentRGB();
            //drawBlobs(currentRGB, fireDetectorResult);
            imagePanel2.setDetectorResult(fireDetectorResult);
            drawResult(currentRGB, fireDetectorResult);

            Image movmentImage = JIPToolkit.getAWTImage(currentRGB);
            imagePanel2.setImage(movmentImage);

            // List Blobs
            //System.out.printf("Found %d blobs:\n", blobList.size());
            //for (FastBlobFinder.Blob blob : blobList) System.out.println(blob);

/*
            JIPImage blob = new JIPImage(jipImage2.getWidth(), jipImage2.getHeight(), img);
            Image movmentImage = JIPToolkit.getAWTImage(blob);
            imagePanel3.setImage(movmentImage);
*/

            /*
            int[] redComponent = ImageUtil.getAllByteFromReal(img);
            //JIPImage movement = fireDetectorModel.getCurrentRGB();
            //movement.setAllPixelRed(redComponent);
            JIPImage movement = new JIPImage(jipImage2.getWidth(), jipImage2.getHeight(), redComponent, greenComponent, new int[redComponent.length]);

            Image movmentImage = JIPToolkit.getAWTImage(movement);
            imagePanel3.setImage(movmentImage);
            */
        }

    }

    private void showResultImage(int ancho, int alto, float[] imagen) {
        JIPImage img1 = new JIPImage(ancho, alto, ImageUtil.adjustImage(imagen));
        Image jipImg1 = JIPToolkit.getAWTImage(img1);
        imagePanel1.setImage(jipImg1);
    }

    private void drawResult(JIPImage currentRGB, FireDetectorResult fireDetectorResult) {
        int ancho = currentRGB.getWidth();
        int alto = currentRGB.getHeight();
        float[] result = fireDetectorResult.getResult();

        for (int y = 0; y < alto; y++) {
            int posy = y * ancho;
            for (int x = 0; x < ancho; x++) {
                int posxy = posy + x;
                if (result[posxy] >= 0.8F) {
                    currentRGB.setIndexPixel(0, posxy, ImageUtil.MAX_INTENSITY);
                }
            }
        }

    }

    private int isAlarmRaised(FireDetectorResult fireDetectorResult, int ancho, int alto, int alarmRaised) {
        float[] result = fireDetectorResult.getResult();

        for (int y = 0; y < alto; y++) {
            int posy = y * ancho;
            for (int x = 0; x < ancho; x++) {
                int posxy = posy + x;
                if (result[posxy] == 1) {
                    alarmRaised++;
                }
            }
        }
        return alarmRaised;
    }

    private void drawBlobs(JIPImage currentRGB, FireDetectorResult fireDetectorResult) {
        int ancho = currentRGB.getWidth();
        // dibuja rectángulo correspondiente por cada blob detectado

        for (FastBlobFinder.Blob blob : fireDetectorResult.getBlobs()) {
            int yMin = blob.yMin * ancho;
            int yMax = blob.yMax * ancho;
            // lados superior e inferior del rectángulo
            for (int x = blob.xMin; x < blob.xMax; x++) {
                int indexInf = x + yMax;
                int indexSup = x + yMin;
                currentRGB.setIndexPixel(0, indexSup, ImageUtil.MAX_INTENSITY);
                currentRGB.setIndexPixel(0, indexInf, ImageUtil.MAX_INTENSITY);
                currentRGB.setIndexPixel(1, indexSup, 0);
                currentRGB.setIndexPixel(1, indexInf, 0);
                currentRGB.setIndexPixel(2, indexSup, 0);
                currentRGB.setIndexPixel(2, indexInf, 0);
            }
            // lados izquierdo y derecho del rectángulo
            for (int yy = blob.yMin; yy < blob.yMax; yy++) {
                int y = yy * ancho;
                int indexIzq = blob.xMin + y;
                int indexDer = blob.xMax + y;
                currentRGB.setIndexPixel(0, indexIzq, ImageUtil.MAX_INTENSITY);
                currentRGB.setIndexPixel(0, indexDer, ImageUtil.MAX_INTENSITY);
                currentRGB.setIndexPixel(1, indexIzq, 0);
                currentRGB.setIndexPixel(1, indexDer, 0);
                currentRGB.setIndexPixel(2, indexIzq, 0);
                currentRGB.setIndexPixel(2, indexDer, 0);
            }

        }
    }

    private void startButtonActionPerformed(ActionEvent e) {
        boolean alreadyStarted = fireDetectorModel.isRunning();
        fireDetectorModel.start();
        imagePanel1.setAreaSelectable(false);
        imagePanel2.setAreaSelectable(false);
        if (!alreadyStarted) {
            thread = new Thread(analyzer);
            previewActionPerformed();
        }
    }

    private void stopButtonActionPerformed(ActionEvent e) {
        fireDetectorModel.stop();
        imagePanel1.setAreaSelectable(true);
        imagePanel2.setAreaSelectable(true);
    }

    private void reinitButtonActionPerformed(ActionEvent e) {
        fireDetectorModel.reset();
    }

    private class Analyzer implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            while (!Thread.interrupted() && analyzing && fireDetectorModel.isRunning()) {
                preview();
            }
            analyzing = false;
        }
    }

    public void close() {
        fireDetectorModel.stop();
    }

    private void exitMenuItemActionPerformed(ActionEvent e) {
        close();
        System.exit(0);
    }

    private void startLearningButtonActionPerformed(ActionEvent e) {
        fireDetectorModel.startLearning();
    }

    private void stopLearningButtonActionPerformed(ActionEvent e) {
        fireDetectorModel.stopLearning();
    }

    private void deleteLastLearnedSequenceActionPerformed(ActionEvent e) {
        fireDetectorModel.deleteLastLearnedSequence();
    }

    private void finishLearningButtonActionPerformed(ActionEvent e) {
        fireDetectorModel.finishLearning(smokeRadioButton.isSelected());
    }

    private void saveLearningButtonActionPerformed(ActionEvent e) {
        fireDetectorModel.saveLearnedSequences();
    }

    private void previewCheckBoxActionPerformed(ActionEvent e) {
        previewActionPerformed();
    }

    private void analyzeCheckBoxActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("gui.language");
        DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        exitMenuItem = new JMenuItem();
        menu2 = new JMenu();
        menuItem2 = new JMenuItem();
        mainPanel = new JPanel();
        detectorPanel = new JPanel();
        startButton = new JButton();
        stopButton = new JButton();
        reinitButton = new JButton();
        analyzeCheckBox = new JCheckBox();
        previewCheckBox = new JCheckBox();
        label1 = new JLabel();
        showComboBox = new JComboBox();
        label3 = new JLabel();
        weightsPanel = new WeightsPanel();
        label2 = new JLabel();
        fireLabel = new JLabel();
        learnPanel = new JPanel();
        smokeRadioButton = new JRadioButton();
        cloudsRadioButton = new JRadioButton();
        separator1 = compFactory.createSeparator(bundle.getString("FireDetectorForm.separator1.text"), SwingConstants.CENTER);
        startLearningButton = new JButton();
        stopLearningButton = new JButton();
        deleteLastLearnedSequence = new JButton();
        finishLearningButton = new JButton();
        saveLearningButton = new JButton();
        imagePanel1 = new ImagePanel();
        imagePanel2 = new ImagePanel();
        statusPanel = new JPanel();
        statusBar = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("FireDetectorForm.this.title"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== menuBar1 ========
        {
            menuBar1.setBorderPainted(false);

            //======== menu1 ========
            {
                menu1.setText(bundle.getString("FireDetectorForm.menu1.text"));
                menu1.setMnemonic(bundle.getString("FireDetectorForm.menu1.mnemonic").charAt(0));

                //---- exitMenuItem ----
                exitMenuItem.setText(bundle.getString("FireDetectorForm.exitMenuItem.text"));
                exitMenuItem.setMnemonic(bundle.getString("FireDetectorForm.exitMenuItem.mnemonic").charAt(0));
                exitMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        exitMenuItemActionPerformed(e);
                    }
                });
                menu1.add(exitMenuItem);
            }
            menuBar1.add(menu1);

            //======== menu2 ========
            {
                menu2.setText(bundle.getString("FireDetectorForm.menu2.text"));
                menu2.setMnemonic(bundle.getString("FireDetectorForm.menu2.mnemonic").charAt(0));

                //---- menuItem2 ----
                menuItem2.setText(bundle.getString("FireDetectorForm.menuItem2.text"));
                menuItem2.setMnemonic(bundle.getString("FireDetectorForm.menuItem2.mnemonic").charAt(0));
                menu2.add(menuItem2);
            }
            menuBar1.add(menu2);
        }
        setJMenuBar(menuBar1);

        //======== mainPanel ========
        {
            mainPanel.setLayout(new FormLayout(
                "default:grow, $lcgap, default:grow",
                "fill:default:grow, $lgap, default:grow"));

            //======== detectorPanel ========
            {
                detectorPanel.setBorder(new TitledBorder(bundle.getString("FireDetectorForm.detectorPanel.border")));
                detectorPanel.setLayout(new FormLayout(
                    "4*(default:grow, $lcgap), default:grow",
                    "2*(default, $lgap), fill:default, $lgap, fill:pref"));

                //---- startButton ----
                startButton.setText(bundle.getString("FireDetectorForm.startButton.text"));
                startButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        startButtonActionPerformed(e);
                    }
                });
                detectorPanel.add(startButton, cc.xy(1, 1));

                //---- stopButton ----
                stopButton.setText(bundle.getString("FireDetectorForm.stopButton.text"));
                stopButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        stopButtonActionPerformed(e);
                    }
                });
                detectorPanel.add(stopButton, cc.xy(3, 1));

                //---- reinitButton ----
                reinitButton.setText(bundle.getString("FireDetectorForm.reinitButton.text"));
                reinitButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        reinitButtonActionPerformed(e);
                    }
                });
                detectorPanel.add(reinitButton, cc.xy(5, 1));

                //---- analyzeCheckBox ----
                analyzeCheckBox.setText(bundle.getString("FireDetectorForm.analyzeCheckBox.text"));
                analyzeCheckBox.setSelected(true);
                detectorPanel.add(analyzeCheckBox, cc.xy(7, 1));

                //---- previewCheckBox ----
                previewCheckBox.setText(bundle.getString("FireDetectorForm.previewCheckBox.text"));
                previewCheckBox.setSelected(true);
                previewCheckBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        previewCheckBoxActionPerformed(e);
                    }
                });
                detectorPanel.add(previewCheckBox, cc.xy(9, 1));

                //---- label1 ----
                label1.setText(bundle.getString("FireDetectorForm.label1.text"));
                detectorPanel.add(label1, cc.xy(1, 3));
                detectorPanel.add(showComboBox, cc.xy(3, 3));

                //---- label3 ----
                label3.setText(bundle.getString("FireDetectorForm.label3.text"));
                detectorPanel.add(label3, cc.xy(1, 5));
                detectorPanel.add(weightsPanel, cc.xywh(3, 5, 7, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

                //---- label2 ----
                label2.setText(bundle.getString("FireDetectorForm.label2.text"));
                detectorPanel.add(label2, cc.xy(1, 7));

                //---- fireLabel ----
                fireLabel.setText(bundle.getString("FireDetectorForm.fireLabel.text"));
                fireLabel.setBackground(Color.red);
                fireLabel.setForeground(Color.white);
                fireLabel.setHorizontalAlignment(SwingConstants.CENTER);
                fireLabel.setFont(fireLabel.getFont().deriveFont(fireLabel.getFont().getStyle() | Font.BOLD, fireLabel.getFont().getSize() + 5f));
                fireLabel.setFocusable(false);
                fireLabel.setOpaque(true);
                fireLabel.setVisible(false);
                detectorPanel.add(fireLabel, cc.xywh(5, 7, 3, 1));
            }
            mainPanel.add(detectorPanel, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

            //======== learnPanel ========
            {
                learnPanel.setBorder(new TitledBorder(bundle.getString("FireDetectorForm.learnPanel.border")));
                learnPanel.setLayout(new FormLayout(
                    "pref:grow, $rgap, pref:grow",
                    "top:default, 4*($lgap, default)"));

                //---- smokeRadioButton ----
                smokeRadioButton.setText(bundle.getString("FireDetectorForm.smokeRadioButton.text"));
                smokeRadioButton.setSelected(true);
                smokeRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
                learnPanel.add(smokeRadioButton, cc.xy(1, 1));

                //---- cloudsRadioButton ----
                cloudsRadioButton.setText(bundle.getString("FireDetectorForm.cloudsRadioButton.text"));
                cloudsRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
                learnPanel.add(cloudsRadioButton, cc.xy(3, 1));
                learnPanel.add(separator1, cc.xywh(1, 3, 3, 1));

                //---- startLearningButton ----
                startLearningButton.setText(bundle.getString("FireDetectorForm.startLearningButton.text"));
                startLearningButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        startLearningButtonActionPerformed(e);
                    }
                });
                learnPanel.add(startLearningButton, cc.xy(1, 5));

                //---- stopLearningButton ----
                stopLearningButton.setText(bundle.getString("FireDetectorForm.stopLearningButton.text"));
                stopLearningButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        stopLearningButtonActionPerformed(e);
                    }
                });
                learnPanel.add(stopLearningButton, cc.xy(3, 5));

                //---- deleteLastLearnedSequence ----
                deleteLastLearnedSequence.setText(bundle.getString("FireDetectorForm.deleteLastLearnedSequence.text"));
                deleteLastLearnedSequence.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteLastLearnedSequenceActionPerformed(e);
                    }
                });
                learnPanel.add(deleteLastLearnedSequence, cc.xy(1, 7));

                //---- finishLearningButton ----
                finishLearningButton.setText(bundle.getString("FireDetectorForm.finishLearningButton.text"));
                finishLearningButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        finishLearningButtonActionPerformed(e);
                    }
                });
                learnPanel.add(finishLearningButton, cc.xy(1, 9));

                //---- saveLearningButton ----
                saveLearningButton.setText(bundle.getString("FireDetectorForm.saveLearningButton.text"));
                saveLearningButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveLearningButtonActionPerformed(e);
                    }
                });
                learnPanel.add(saveLearningButton, cc.xy(3, 9));
            }
            mainPanel.add(learnPanel, cc.xy(3, 1));

            //---- imagePanel1 ----
            imagePanel1.setBorder(new TitledBorder(bundle.getString("FireDetectorForm.imagePanel1.border")));
            mainPanel.add(imagePanel1, cc.xy(1, 3));

            //---- imagePanel2 ----
            imagePanel2.setBorder(new TitledBorder(bundle.getString("FireDetectorForm.imagePanel2.border")));
            mainPanel.add(imagePanel2, cc.xy(3, 3));
        }
        contentPane.add(mainPanel, BorderLayout.CENTER);

        //======== statusPanel ========
        {
            statusPanel.setBorder(UIManager.getBorder("ComboBox.border"));
            statusPanel.setLayout(new BorderLayout());

            //---- statusBar ----
            statusBar.setText(" ");
            statusPanel.add(statusBar, BorderLayout.CENTER);
        }
        contentPane.add(statusPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());

        //---- learnButtonGroup ----
        ButtonGroup learnButtonGroup = new ButtonGroup();
        learnButtonGroup.add(smokeRadioButton);
        learnButtonGroup.add(cloudsRadioButton);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem exitMenuItem;
    private JMenu menu2;
    private JMenuItem menuItem2;
    private JPanel mainPanel;
    private JPanel detectorPanel;
    private JButton startButton;
    private JButton stopButton;
    private JButton reinitButton;
    private JCheckBox analyzeCheckBox;
    private JCheckBox previewCheckBox;
    private JLabel label1;
    private JComboBox showComboBox;
    private JLabel label3;
    private WeightsPanel weightsPanel;
    private JLabel label2;
    private JLabel fireLabel;
    private JPanel learnPanel;
    private JRadioButton smokeRadioButton;
    private JRadioButton cloudsRadioButton;
    private JComponent separator1;
    private JButton startLearningButton;
    private JButton stopLearningButton;
    private JButton deleteLastLearnedSequence;
    private JButton finishLearningButton;
    private JButton saveLearningButton;
    private ImagePanel imagePanel1;
    private ImagePanel imagePanel2;
    private JPanel statusPanel;
    private JLabel statusBar;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
