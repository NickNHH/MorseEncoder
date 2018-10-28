package larsoe4.morsetranslator;

import android.media.ToneGenerator;
import android.util.Log;

/**
 * Created by larsoe4 on 10/23/2018.
 */

public class SpeakerMorseDevice implements MorseDevice {

    ToneGenerator tg = null;

    public SpeakerMorseDevice() throws Exception{
        tg = new ToneGenerator(ToneGenerator.TONE_DTMF_3, 100);
        Log.d("resourceAvailability", "FOUND:SPEAKER");
    }

    @Override
    public void activate() {
        tg.startTone(ToneGenerator.TONE_DTMF_3);
    }

    @Override
    public void deactivate() {
        tg.stopTone();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        deactivate();
    }
}
