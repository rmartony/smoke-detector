package model;

import jip.JIPImage;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 17/08/2009
 * Time: 11:44:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class SlowBackground {
    private FastBackground fastBackground = new FastBackground();
    private BackgroundUpdater backgroundUpdater = new BackgroundUpdater();
    private Thread thread = new Thread(backgroundUpdater);
    private float alpha = 0.9f;

    private boolean running = false;

    public SlowBackground() {
        fastBackground.setAlpha(alpha);
    }

    public JIPImage getCurrent() {
        return fastBackground.getCurrent();
    }

    public void start() {
        running = true;
        if (thread == null) {
            thread = new Thread(backgroundUpdater);
        }
        thread.start();
    }

    public void stop() {
        running = false;
        thread = null;
    }

    public float[] getCurrentFloatImage() {
        return fastBackground.getCurrentFloatImage();
    }

    public void init() {
        fastBackground.init();
    }

    public void reset() {
        fastBackground.reset();
    }

    public class BackgroundUpdater implements Runnable {
        private static final int INTERVAL = 1000;  // one second interval

        @Override
        public void run() {
            while (!Thread.interrupted() && running) {
                try {
                    Thread.sleep(INTERVAL);
                    fastBackground.update();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                    running = false;
                }
            }

        }
    }

}
