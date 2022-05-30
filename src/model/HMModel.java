package model;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.io.*;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 26/09/2009
 * Time: 01:10:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class HMModel {
    private Hmm<ObservationInteger> learntSmokeHmm;
    private Hmm<ObservationInteger> learntCloudHmm;
    private List<List<ObservationInteger>> learningSequences = new ArrayList<List<ObservationInteger>>();
    private List<ObservationInteger> learningSequence;

    private static final int MAX_SEQUENCE_LENGTH = 200;
    private LinkedList<ObservationInteger> sequence = new LinkedList<ObservationInteger>(); // actual observations
    private int lastUpperMostPixelRow = 0;
    private static final String CLOUDS_HMM_TXT_FILE = "cloudsHmm.txt";
    private static final String SMOKE_HMM_TXT_FILE = "smokeHmm.txt";


    public HMModel() {
        learntCloudHmm = loadCloudsHmm();
        learntSmokeHmm = loadSmokeHmm();
    }

    /* Initial guess for the Baum-Welch algorithm */
/*
    static Hmm<ObservationInteger> buildDefaultSmokeHmm() {
        OpdfIntegerFactory factory = new OpdfIntegerFactory(2);

        Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(3, factory);

        hmm.setPi(0, 0.4);
        hmm.setPi(1, 0.3);
        hmm.setPi(2, 0.3);

        hmm.setOpdf(0, new OpdfInteger(new double[]{0.6, 0.2, 0.2}));
        hmm.setOpdf(1, new OpdfInteger(new double[]{0.2, 0.6, 0.2}));
        hmm.setOpdf(2, new OpdfInteger(new double[]{0.2, 0.2, 0.6}));

        hmm.setAij(0, 0, 0.5);
        hmm.setAij(0, 1, 0.2);
        hmm.setAij(0, 2, 0.3);

        hmm.setAij(1, 0, 0.5);
        hmm.setAij(1, 1, 0.2);
        hmm.setAij(1, 2, 0.3);

        hmm.setAij(2, 0, 0.5);
        hmm.setAij(2, 1, 0.2);
        hmm.setAij(2, 2, 0.3);

        return hmm;
    }
*/

    private Hmm<ObservationInteger> loadHmm(String filename) throws IOException, FileFormatException {
        BufferedReader bufferedReader = null;

        System.out.println("Loading " + filename);
        try {
            if (new File(filename).exists()) {

                bufferedReader = new BufferedReader(new FileReader(filename));
                return HmmReader.read(bufferedReader, new OpdfIntegerReader());
            } else {
                throw new IOException("File " + filename + " not found");
            }
        } finally {
            //Close the BufferedWriter
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Hmm<ObservationInteger> loadSmokeHmm() {

        try {
            return loadHmm(SMOKE_HMM_TXT_FILE);
        } catch (FileFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return buildDefaultSmokeHmm();
        } catch (IOException e) {
            return buildDefaultSmokeHmm();
        }

    }

    public Hmm<ObservationInteger> loadCloudsHmm() {

        try {
            return loadHmm(CLOUDS_HMM_TXT_FILE);
        } catch (FileFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return buildDefaultCloudsHmm();
        } catch (IOException e) {
            return buildDefaultCloudsHmm();
        }

    }

    public void writeSmokeHmm() {
        writeHmm(learntSmokeHmm, SMOKE_HMM_TXT_FILE);
    }

    public void writeCloudsHmm() {
        writeHmm(learntCloudHmm, CLOUDS_HMM_TXT_FILE);
    }

    private void writeHmm(Hmm<ObservationInteger> hmm, String filename) {
        BufferedWriter bufferedWriter = null;

        System.out.println("Writing " + filename);
        try {

            bufferedWriter = new BufferedWriter(new FileWriter(filename));
            HmmWriter.write(bufferedWriter, new OpdfIntegerWriter(), hmm);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            //Close the BufferedWriter
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Crea HMM inicial para HUMO
     *
     * @return HMM inicial
     */
    private Hmm<ObservationInteger> buildDefaultSmokeHmm() {
        OpdfIntegerFactory factory = new OpdfIntegerFactory(2);

        Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(3, factory);

        hmm.setPi(0, 0.1);
        hmm.setPi(1, 0.36);
        hmm.setPi(2, 0.54);

        hmm.setOpdf(0, new OpdfInteger(new double[]{0.129, 0.065, 0.807}));
        hmm.setOpdf(1, new OpdfInteger(new double[]{0.313, 0.152, 0.535}));
        hmm.setOpdf(2, new OpdfInteger(new double[]{0.12, 0.071, 0.808}));

        hmm.setAij(0, 0, 0.337);
        hmm.setAij(0, 1, 0.021);
        hmm.setAij(0, 2, 0.642);

        hmm.setAij(1, 0, 0.367);
        hmm.setAij(1, 1, 0.129);
        hmm.setAij(1, 2, 0.504);

        hmm.setAij(2, 0, 0.307);
        hmm.setAij(2, 1, 0.027);
        hmm.setAij(2, 2, 0.666);


        return hmm;
    }

    /**
     * Crea HMM inicial para NUBES
     *
     * @return HMM inicial
     */
    private Hmm<ObservationInteger> buildDefaultCloudsHmm() {
        OpdfIntegerFactory factory = new OpdfIntegerFactory(3);

        Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(3, factory);

        hmm.setPi(0, 0.839);
        hmm.setPi(1, 0.160);
        hmm.setPi(2, 0.001);

        hmm.setOpdf(0, new OpdfInteger(new double[]{0.233, 0.107, 0.666}));
        hmm.setOpdf(1, new OpdfInteger(new double[]{0.006, 0.049, 0.945}));
        hmm.setOpdf(2, new OpdfInteger(new double[]{0, 0, 1}));

        hmm.setAij(0, 0, 0.439);
        hmm.setAij(0, 1, 0.52);
        hmm.setAij(0, 2, 0.041);

        hmm.setAij(1, 0, 0.422);
        hmm.setAij(1, 1, 0.572);
        hmm.setAij(1, 2, 0.007);

        hmm.setAij(2, 0, 0);
        hmm.setAij(2, 1, 0.001);
        hmm.setAij(2, 2, 0.999);

        return hmm;
    }

    protected int getFeatureSignal(int upperMostPixel) {
        int result;
        if (upperMostPixel < lastUpperMostPixelRow) {
            result = 0;
        } else if (upperMostPixel > lastUpperMostPixelRow) {
            result = 1;
        } else {
            result = 2;
        }
        lastUpperMostPixelRow = upperMostPixel;
        return result;
    }


    public void learnSmokeHmm(List<List<ObservationInteger>> sequences) {
        /* Baum-Welch learning */
        BaumWelchLearner bwl = new BaumWelchLearner();

        // Incrementally improve the solution
        for (int i = 0; i < 10; i++) {
            learntSmokeHmm = bwl.iterate(learntSmokeHmm, sequences);
        }
        System.out.println("Resulting smoke HMM:\n" + learntSmokeHmm);
    }

    public void learnCloudHmm(List<List<ObservationInteger>> sequences) {
        /* Baum-Welch learning */
        BaumWelchLearner bwl = new BaumWelchLearner();
        //learntCloudHmm = buildDefaultCloudsHmm();

        // Incrementally improve the solution
        for (int i = 0; i < 10; i++) {
            learntCloudHmm = bwl.iterate(learntCloudHmm, sequences);
        }
        System.out.println("Resulting cloud HMM:\n" + learntCloudHmm);
    }

    public void startLearning() {
        learningSequence = new ArrayList<ObservationInteger>();
    }

    public void stopLearning() {
        learningSequences.add(learningSequence);
        System.out.println("Learned sequences: " + learningSequences.size());
    }

    public void addSignalToLearningSequence(int upperMostPixelRow) {
        int signal = getFeatureSignal(upperMostPixelRow);
        learningSequence.add(new ObservationInteger(signal));
    }

    public void finishLearning(boolean smoke) {
        if (smoke) {
            learnSmokeHmm(learningSequences);
        } else {
            learnCloudHmm(learningSequences);
        }
    }

    public void deleteLastLearnedSequence() {
        if (learningSequences != null && learningSequences.size() > 0) {
            learningSequences.remove(learningSequences.size() - 1);
        }
    }

    public void saveLearnedSequences() {
        writeSmokeHmm();
        writeCloudsHmm();
    }

    public double probabilitySmoke() {
        return learntSmokeHmm.probability(sequence);
    }

    public double probabilityCloud() {
        return learntCloudHmm.probability(sequence);
    }

    public void addSignalToSequence(int upperMostPixelRow) {
        int signal = getFeatureSignal(upperMostPixelRow);
        if (sequence.size() >= MAX_SEQUENCE_LENGTH) {
            sequence.removeFirst();
        }
        sequence.addLast(new ObservationInteger(signal));
    }

    public int sequenceLength() {
        return sequence.size();
    }


}
