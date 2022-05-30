package gui.form.model;

/**
 * Created by IntelliJ IDEA.
 * User: rafa
 * Date: 04/03/2010
 * Time: 10:55:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FireDetectorFormModel {
    public static enum ShowComboBoxOptions {
        NO_PREVIEW("No preview"), RESULT("Result"), DA1("DA 1"), DA2("DA 2"), DA3("DA 3"), DA4("DA 4"), SLOW("Slow"), FAST("Fast"), BINARY("Binary");

        private final String displayName;

        ShowComboBoxOptions(final String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
    }
}

