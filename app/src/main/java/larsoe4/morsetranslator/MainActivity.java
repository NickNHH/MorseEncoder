package larsoe4.morsetranslator;

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
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    //length of one tick in milliseconds
    public static final int TICK_TIME = 500;

    //map of characters to their morse codes (as Strings)
    private Map<Character, String> morseValues;

    //map of radio button IDs to morse output devices
    private Map<Integer, MorseDevice> morseDeviceMap;

    //true when app is currently playing a message, false otherwise
    private static boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //maps the morse code dictionary contained in a string array in strings.xml to a global HashMap
        morseValues = new HashMap<>();
        morseDeviceMap = new TreeMap<>();

        mapValues(R.array.morse_code, morseValues);
        //gives the user's text input field to a global variable
        EditText editText = findViewById(R.id.inputText);
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
    public void onClickTranslateInput(View view) {
        //gets the user's input text and translates it into morse code
        EditText editText = findViewById(R.id.inputText);
        String morseString = translateText(editText.getText().toString());

        //sets the text display to show the morse code
        TextView textView = findViewById(R.id.message_display);
        textView.setText(morseString);

        //enables the user to send their morse code
        setSendClickable();
    }

    //function to check which output device the user has selected
    private MorseDevice getSelectedDevice() {
        //finds the radio button group associated with the output device
        RadioGroup rg = findViewById(R.id.output_radio_group);

        return morseDeviceMap.get(rg.getCheckedRadioButtonId());
    }

    //get the string of morse code currently ready to be translated
    private String getMorseString() {
        //find the message display and get its text
        TextView textView = findViewById(R.id.message_display);

        return textView.getText().toString();
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
            String morseString = getMorseString();

            @Override
            public void run() {
                //plays the string through whatever output device was selected
                //continues as long as the repeat option is selected and the app is not paused
                do {
                    playMorseString(morseString, morseDevice);
                    sendWordBreak(morseDevice);
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

    //function that takes a string in morse code and plays it
    private void playMorseString(String morseString, MorseDevice morseDevice) {
        char[] morseChars = morseString.toCharArray();
        int i = 0;

        //goes through each char in the string, making sure that the thread is supposed to be running
        while (i < morseChars.length && isPlaying) {
            char c = morseChars[i];
            if (c == '.') {
                //a period (.) corresponds to a dot
                sendDot(morseDevice);
            } else if (c == '-') {
                //a dash (-) corresponds to a dash
                sendDash(morseDevice);
            } else if (c == ' ') {
                //a space ( ) corresponds to a space between letters
                sendSpace(morseDevice);
            } else if (c == '/') {
                //a linebreak (\n) corresponds to a space between words
                sendWordBreak(morseDevice);
            }

            //pause between letters
            waitForTime(1);
            i++;
        }

    }

    //function to send a dot in morse code
    private void sendDot(MorseDevice morseDevice) {
        //a dot is 1 tick ON

        morseDevice.activate();
        waitForTime(1);
        morseDevice.deactivate();
    }

    //function to send a dash in morse code
    private void sendDash(MorseDevice morseDevice) {
        //a dash is 3 ticks ON

        morseDevice.activate();
        waitForTime(3);
        morseDevice.deactivate();

    }

    //function to send a space between characters in morse
    private void sendSpace(MorseDevice morseDevice) {
        // a space is 3 ticks OFF
        // 2 spacers before and after the wordbreak
        // are 1 tick each, only needs to wait 1 tick here

        morseDevice.deactivate();
        waitForTime(1);
    }

    //function to send a break between words in morse
    private void sendWordBreak(MorseDevice morseDevice) {
        // a wordbreak is 7 ticks OFF
        // 2 spacers before and after the wordbreak
        // are 1 tick each, only needs to wait 5 ticks here
        morseDevice.deactivate();
        waitForTime(5);
    }

    private void waitForTime(int ticks) {
        int millis = ticks * TICK_TIME;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.d("Exceptions", "waitForTime");
        }
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

    //function to take a given String and convert it into morse code
    //returns a string of morse code
    private String translateText(String text) {
        StringBuilder morseBuilder = new StringBuilder();
        //morse code has no distinctions between upper and lower case, so for simplicity's sake all input text is set to lowercase
        char[] charArray = text.toLowerCase().toCharArray();

        boolean allCharactersTranslatable = true;

        //translates each character to morse code and adds it to the output string
        for (char c : charArray) {
            String morseForChar = morseValues.get(c);
            if (morseForChar == null) {
                allCharactersTranslatable = false;
                morseBuilder.append(" ");
            } else {
                morseBuilder.append(morseForChar);
            }

            morseBuilder.append(" ");
        }

        if (!allCharactersTranslatable) {
            Toast.makeText(getApplicationContext(), "Not all characters have Morse encodings", Toast.LENGTH_LONG).show();
        }

        return morseBuilder.toString();
    }

    //populate a HashMap with all the morse codes from a given stringArray in strings.xml
    private void mapValues(int stringArrayResourceID, Map<Character, String> myMap) {
        //creates a new String[] using a stringArray in strings.xml
        String[] stringArray = getResources().getStringArray(stringArrayResourceID);

        //goes through string array and adds each entry to the HashMap
        for (String entry : stringArray) {
            //uses a | as a divider between the character and its corresponding code
            String[] splitResult = entry.split("\\|", 2);
            //adds entry to HashMap

            Log.d("PARSED ENCODING", "<" + splitResult[0] + ">\t<" + splitResult[1] + ">");

            if (splitResult[0].length() > 0) {
                myMap.put(splitResult[0].charAt(0), splitResult[1]);
            }
        }

        Log.d("Space encoding", "<" + myMap.get(' ') + ">");
    }

    /*
     * Look at each of the output devices and determine if they are available
     * Make their respective buttons available to the user
     * Instantiate the MorseDevice accessing that device
     */
    private void discoverDevices() {

        //get each of the radio buttons to select the output device
        RadioButton rb_vib = findViewById(R.id.output_radio_vib);
        RadioButton rb_light = findViewById(R.id.output_radio_light);
        RadioButton rb_beep = findViewById(R.id.output_radio_beep);

        try {
            MorseDevice vibDevice = new VibratorMorseDevice(getApplicationContext());
            morseDeviceMap.put(R.id.output_radio_vib, vibDevice);

            rb_vib.setClickable(true);

        } catch (Exception e) {
            Log.d("resourceAvailability", "MISSING:VIBRATOR");
            rb_vib.setClickable(false);
        }


        try {
            MorseDevice speakerDevice = new SpeakerMorseDevice();
            morseDeviceMap.put(R.id.output_radio_beep, speakerDevice);

            rb_beep.setClickable(true);
        } catch (Exception e) {
            Log.d("resourceAvailability", "MISSING:SPEAKER");
            rb_beep.setClickable(false);
        }

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
