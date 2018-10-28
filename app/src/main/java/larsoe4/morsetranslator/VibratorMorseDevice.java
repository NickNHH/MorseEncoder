package larsoe4.morsetranslator;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by larsoe4 on 10/23/2018.
 */

public class VibratorMorseDevice implements MorseDevice {
    private Vibrator vib = null;

    public VibratorMorseDevice(Context context) throws Exception{
        //get the vibrator object from the system
        vib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);

        if (vib.hasVibrator()){
            Log.d("resourceAvailability", "FOUND:VIBRATOR");

        }else{
            throw new Exception();
        }
    }

    @Override
    public void activate() {
        // should always be cancelled before finishing a full second
        vib.vibrate(1000);
    }

    @Override
    public void deactivate() {
        vib.cancel();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        if (vib != null){
            //stop vibrator operations and sets the global variable to null
            vib.cancel();
        }
    }
}
