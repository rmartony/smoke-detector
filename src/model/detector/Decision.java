package model.detector;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 16/02/2010
 * Time: 02:42:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class Decision {
    private float[] result = null;
        private boolean alarmRaised = false;
        private int counter = 0;

        public float[] getResult() {
            return result;
        }

        public void setResult(float[] result) {
            this.result = result;
        }

        public boolean isAlarmRaised() {
            return alarmRaised;
        }

        public void setAlarmRaised(boolean alarmRaised) {
            this.alarmRaised = alarmRaised;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        public void incrementCounter() {
            this.counter++;
        }
}
