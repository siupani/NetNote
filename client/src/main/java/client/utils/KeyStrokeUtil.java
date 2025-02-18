package client.utils;

import java.util.Objects;

public class KeyStrokeUtil {
    private int keystrokeCount;
    private int triggerCount;

    /**
     * Constructor
     */
    public KeyStrokeUtil() {
        this.keystrokeCount = 0;
        this.triggerCount = 100; //this is just a handpicked number that can be changed
    }

    /**
     * Method for increasing the counter of the KeyStroke
     */
    public void increaseCounter() {
        keystrokeCount++;
        //System.out.println("Key counter increased"); This line is solely for debugging purpose
    }

    /**
     * Getter for the counter
     *
     * @return int with the number of keystrokes pressed
     */
    public int getCounter() {
        return keystrokeCount;
    }

    /**
     * Getter for the number of keystrokes needed for triggering an action
     *
     * @return int with the number of keystrokes needed for triggering
     */
    public int getTrigger() {
        return triggerCount;
    }

    /**
     * Resets the keystrokes counter
     */
    public void counterReset() {
        this.keystrokeCount = 0;
    }


    /**
     * Sets the number of keystrokes that makes a note be saved
     *
     * @param triggerCount new int with the number of keystrokes that the note should be saved upon
     */
    public void setTriggerCount(int triggerCount) {
        this.triggerCount = triggerCount;
    }

    /**
     * Equals method
     *
     * @param o other object to be compared with the KeyStroke
     * @return True/False if the object o is equal/same to the KeyStroke
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyStrokeUtil that = (KeyStrokeUtil) o;
        return keystrokeCount == that.keystrokeCount;
    }

    /**
     * HashCode method
     *
     * @return an integer with the HashCode of the KeyStroke object
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(keystrokeCount);
    }
}
