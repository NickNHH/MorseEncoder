package larsoe4.morsetranslator;

/**
 * Created by larsoe4 on 10/23/2018.
 */

public interface MorseDevice {
    // Turn on output for this device (e.g. turn on light)
    void activate();
    // Turn off output for this device
    void deactivate();

    void onStart();
    void onStop();
}
