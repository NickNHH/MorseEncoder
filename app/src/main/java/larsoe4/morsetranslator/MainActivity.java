package larsoe4.morsetranslator;

import android.annotation.SuppressLint;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    MorseEncoder morseEncoder = new MorseEncoder();

    //length of one tick in milliseconds
    public static final int TICK_TIME = 500;

    //map of radio button IDs to morse output devices
    private Map<Integer, MorseDevice> morseDeviceMap;

    //true when app is currently playing a message, false otherwise
    private static boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //maps the morse code dictionary contained in a string array in strings.xml to a global HashMap
        //map of characters to their morse codes (as Strings)
        morseDeviceMap = new TreeMap<>();

        //gives the user's text input field to a global variable
        EditText editText = findViewById(R.id.inputText);

        View blackWhiteView = findViewById(R.id.blackWhiteView);
        blackWhiteView.setBackgroundColor(ContextCompat.getColor(blackWhiteView.getContext(), R.color.black));

        //adds a listener to the user's text input field to check for when they change the text
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //when the user changes the text in the field, let the program know that there is
                //no more valid morse string (because the user has edited their message)
                //try to enable the user to send their morse code
                setSendClickable();
                //reset the text display to the default (hint)
                TextView textView = findViewById(R.id.message_display);
                textView.setText(R.string.text_view_hint);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    //function to translate the text that the user has put into the text field
    //and output it to the text display
    //called when the user clicks the Translate button
    @SuppressLint("SetTextI18n")
    public void onClickTranslateInput(View view) {
        //gets the user's input text and translates it into morse code
        EditText editText = findViewById(R.id.inputText);

        //sets the text display to show the morse code
        TextView textView = findViewById(R.id.message_display);
        try {
            List<Primitive> primitiveList = morseEncoder.textToCode(editText.getText().toString().toUpperCase());
            textView.setText("");
            for (Primitive primitive : primitiveList) {
                textView.setText(textView.getText() + primitive.getTextRepresentation());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //enables the user to send their morse code
        setSendClickable();
    }

    //function to check which output device the user has selected
    private MorseDevice getSelectedDevice() {
        //finds the radio button group associated with the output device
        RadioGroup rg = findViewById(R.id.output_radio_group);

        return morseDeviceMap.get(rg.getCheckedRadioButtonId());
    }

    private boolean isOutputSelected() {
        RadioGroup rg = findViewById(R.id.output_radio_group);
        int checked = rg.getCheckedRadioButtonId();

        return (checked != -1);
    }

    private boolean isTextTranslated() {
        //find the message display and get its text
        TextView textView = findViewById(R.id.message_display);
        String contents = textView.getText().toString();
        String defaultText = textView.getHint().toString();

        return (!contents.equals(defaultText));
    }


    //function to output the user's message in morse code
    //called when the user clicks the Send button
    public void onClickOutputMorse(View view) {

        //checks which action (Play/Stop) the Send button is currently bound to and calls the relevant function
        if (isPlaying) {
            updateIsPlaying(false);
        } else if (isOutputSelected()) {
            outputMorse();
        }
    }

    //function to be called when the send button is set to PLAY
    public void outputMorse() {

        //create new thread to avoid interfering with other tasks
        //thread's run method contains all the code to play the morse message
        Thread playThread = new Thread(new Runnable() {

            //find which output device this message will play through
            MorseDevice morseDevice = getSelectedDevice();

            @Override
            public void run() {
                //plays the string through whatever output device was selected
                //continues as long as the repeat option is selected and the app is not paused
                do {
                    sendNextPrimitive(morseDevice);
                    //playMorseString(morseString, morseDevice);
                } while (repeatOn() && isPlaying);

                //toggles the Send button to change its function back to Play
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateIsPlaying(false);
                    }
                });
            }
        });

        updateIsPlaying();
        try {
            playThread.start();
        } catch (Exception e) {
            updateIsPlaying(false);
        }
    }

    private void updateIsPlaying(boolean newState) {
        isPlaying = newState;
        updateSendButton();
    }

    private void updateIsPlaying() {
        isPlaying = !isPlaying;
        updateSendButton();
    }

    private void updateSendButton() {
        int buttonStringID = (isPlaying) ? (R.string.button_send_stop) : (R.string.button_send_play);
        Button b = findViewById(R.id.send_button);
        b.setText(buttonStringID);
    }

    private void sendNextPrimitive(MorseDevice morseDevice) {
        EditText editText = findViewById(R.id.inputText);
        MorseEncoder morseEncoder = new MorseEncoder();
        List<Primitive> code = new ArrayList<>();

        try {
            code = morseEncoder.textToCode(editText.getText().toString().toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (code.size() != 0) {
            Primitive nextPrimitive = code.remove(0);

            if (nextPrimitive.isLightOn()) {
                lightOn(morseDevice);
            } else {
                lightOff(morseDevice);
            }

            setTimer(nextPrimitive.getSignalLengthInDits() * TICK_TIME);
        }
        // transmission finished?
        code.size();
        // lights should be off after the transmission
        lightOff(morseDevice);
    }

    private void setTimer(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.d("Exceptions", "waitForTime");
        }
    }

    private void lightOn(MorseDevice morseDevice) {
        morseDevice.activate();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View blackWhiteView = findViewById(R.id.blackWhiteView);
                blackWhiteView.setBackgroundColor(ContextCompat.getColor(blackWhiteView.getContext(), R.color.white));
            }
        });
    }

    private void lightOff(MorseDevice morseDevice) {
        morseDevice.deactivate();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View blackWhiteView = findViewById(R.id.blackWhiteView);
                blackWhiteView.setBackgroundColor(ContextCompat.getColor(blackWhiteView.getContext(), R.color.black));
            }
        });
    }

    //function checks if the program is ready to output a message in morse code
    //if it is ready, allows the user to click the Send button
    private void setSendClickable() {
        //gets the Send button
        Button button = findViewById(R.id.send_button);

        //checks to see if:
        // A. the user has selected a method to play the message
        // B. there is a string of morse code ready to transmit
        if (isOutputSelected() && isTextTranslated()) {
            button.setClickable(true);
        } else {
            button.setClickable(false);
        }
    }

    public void setSendClickable(View view) {
        setSendClickable();
    }

    //checks to see if the user has selected the repeat option
    //returns true if the repeat option is selected, false if it is not
    private boolean repeatOn() {
        CheckBox checkBox = findViewById(R.id.button_repeat);

        return checkBox.isChecked();
    }

    /*
     * Look at each of the output devices and determine if they are available
     * Make their respective buttons available to the user
     * Instantiate the MorseDevice accessing that device
     */
    private void discoverDevices() {

        //get each of the radio buttons to select the output device
        RadioButton rb_light = findViewById(R.id.output_radio_light);

        try {
            MorseDevice flashlightDevice = new FlashlightMorseDevice(getApplicationContext());
            morseDeviceMap.put(R.id.output_radio_light, flashlightDevice);

            rb_light.setClickable(true);
        } catch (Exception e) {
            Log.d("resourceAvailability", "MISSING:CAMERA");
            rb_light.setClickable(false);
        }
    }

    /*on pause, the program will change the global static variable
     * paused to true, allowing the sending loop to stop
     * changing this variable first in the onPause method ensures that when the hardware is released in the onStop method,
     * the program will not try to call any of the hardware functions, causing a crash
     */

    @Override
    protected void onPause() {
        super.onPause();

        //let process know they are paused
        //paused = true;
        updateIsPlaying(false);
    }


    //on stop, the program will release or otherwise delete its links to the output devices
    @Override
    protected void onStop() {
        super.onStop();

        for (MorseDevice morseDevice : morseDeviceMap.values()) {
            morseDevice.onStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onStart() {
        super.onStart();

        discoverDevices();
    }
}
