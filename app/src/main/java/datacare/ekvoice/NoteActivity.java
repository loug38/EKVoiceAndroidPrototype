package datacare.ekvoice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


/**
 * This is the activity called when creating some sort of note it has three main ways of being called.
 * As a blank empty note, editing an existing note, or a new note with a contact already selected.
 */
public class NoteActivity extends Activity {

    //*********** UI Variables ***********
    private TextView timerValue;
    private TextView contactName;
    private TextView contactPhone;
    private TextView contactEmail;
    private Button saveButton;
    private Button cancelButton;
    private Button record;
    private ImageButton callButton;
    private ImageButton addContactButton;
    private ToggleButton timerButton;
    private Switch toggleOfflineMode;
    private EditText editBox;

    //*********** Handlers ***********
    private Handler customHandler = new Handler();
    private Case storedCase;
    private Case.Note note;
    private Contact contact;

    //*********** Local Variables ***********
    private long startTime = 0L;
    private boolean shouldContinue = false;
    private boolean editingNote = false;
    long timeInSeconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    //*********** Constant Permission Variables ***********
    private final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 1;
    private final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 2;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        timerValue = (TextView) findViewById(R.id.timerValue);
        contactName = (TextView) findViewById(R.id.addNoteContactName);
        contactPhone = (TextView) findViewById(R.id.addNoteContactPhone);
        contactEmail = (TextView) findViewById(R.id.addNoteContactEmail);
        record = (Button) findViewById(R.id.recordButton);
        saveButton = (Button) findViewById(R.id.addNoteSave);
        cancelButton = (Button) findViewById(R.id.addNoteCancelButton);
        timerButton = (ToggleButton) findViewById(R.id.timerButton);
        callButton = (ImageButton) findViewById(R.id.addNoteCallButton);
        addContactButton = (ImageButton) findViewById(R.id.addNoteAddContact);
        editBox = (EditText) findViewById(R.id.addNoteEditText);
        toggleOfflineMode = (Switch) findViewById(R.id.offlineModeSwitch);

        record.setVisibility(View.INVISIBLE);

        //Get case information from previous Activity
        Intent i = getIntent();
        storedCase = (Case) i.getSerializableExtra("CASE_EXTRA");

        editBox.setText(storedCase.firstName);

        /*
         * Check from which activity we came in from so we know from where to pull information
         * on the case from
         */

        if (i.hasExtra("selectedNote")) {
            editingNote = true;
            note = (Case.Note) i.getSerializableExtra("selectedNote");
            populateFromNote();
        } else if (i.hasExtra("selectedContact")) {
            contact = (Contact) i.getSerializableExtra("selectedContact");
            populateFromContact();
        }

        //Timer
        timerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!shouldContinue) {
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    shouldContinue = true;
                } else {
                    startTime = SystemClock.uptimeMillis();
                    shouldContinue = false;
                }
            }
        });

        //If this is pressed, the new note is saved.
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editBox.getText().toString().isEmpty()) {
                    finish();
                } else if (editingNote) {
                    note.noteText = editBox.getText().toString();
                    if (contact == null || contact != note.contact) {
                        note.contact = contact;
                        note.email = contact.email;
                        //HERE
                    }
                    storedCase.notes.remove(note);
                    storedCase.notes.add(note);
                } else {
                    note = new Case.Note();
                    note.noteText = editBox.getText().toString();
                    if (contact != null) {
                        note.contact = contact;
                    }
                    storedCase.notes.add(note);
                    finish();
                }

            }
        });

        /*
         * Since Android 6.0 permissions need to be asked for 1 by 1 to the user, and below we
         * handle those permissions depending on what's needed. In this case phone and contacts.
         * On first time use asks user for permission to access the phone, after that it calls
         * the phone number associated with the case.
         */
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NoteActivity.this,
                                                      Manifest.permission.CALL_PHONE)
                                                      != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(NoteActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_PHONE_CALL);
                } else {
                    makeCall();
                }
            }
        });

        /*
         * On first time use asks user for permission to access contacts, after that it opens up
         * the contacts application as a new activity with the given case fields filled out.
         * Currently fills out Phone number, Email, and name.
         */
        addContactButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NoteActivity.this,
                                                      Manifest.permission.WRITE_CONTACTS)
                                                      != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(NoteActivity.this,
                            new String[]{Manifest.permission.WRITE_CONTACTS},
                            MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);
                } else {
                    addContact();
                }

            }
        });

        //Quick way to get out of app without pressing the back button.
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        /*
         * Switch to toggle on pocketSphinx for offline voice recognition. All the switch does
         * it enable the sphinx record button, like this we don't load sphinx unless completely
         * necessary since It's a total memory hog and takes a long time to load, so don't do it
         * unless necessary.
         */
        toggleOfflineMode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (toggleOfflineMode.isChecked()) {
                    record.setVisibility(View.VISIBLE);
                } else {
                    record.setVisibility(View.INVISIBLE);
                }
            }
        });

        /*
         * The button that actually enables pocketSphinx. Currently not tied in, having serious
         * issues with pocketSphinx dependancies
         */
        record.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (toggleOfflineMode.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Recording", Toast.LENGTH_SHORT).show();

                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /*
     * Assuming that we went through from the note field, the fields in this activity are populated
     * differently than if we just came in here via a new case.
     */
    private void populateFromNote() {
        editBox.setText(note.noteText);
        timerValue.setText(note.hours + ":" + note.minutes + String.format("%01d", note.seconds));

        //fill in the name field with as many names as possible or make invisible in any other case.
        if (storedCase.firstName != null || storedCase.lastName != null) {
            if (storedCase.firstName != null && storedCase.lastName != null)
                contactName.setText(storedCase.firstName + " " + storedCase.lastName);
            if (storedCase.lastName == null)
                contactName.setText(storedCase.firstName);
            if (storedCase.firstName == null)
                contactName.setText(storedCase.lastName);
            contactName.setVisibility(View.VISIBLE);
        } else {
            contactName.setText(View.INVISIBLE);
        }

        if (storedCase.email != null) {
            contactEmail.setText(storedCase.email);
            contactEmail.setVisibility(View.VISIBLE);
        } else {
            contactEmail.setVisibility(View.INVISIBLE);
        }

        //format the phone from 1234567890 to (123) 456 - 7890
        if (storedCase.phoneNumber != null) {
            String formattedPhoneNumber = "";
            for (int i = 0; i < storedCase.phoneNumber.length(); i++){
                if (i == 0) formattedPhoneNumber += "(";
                if (i == 3) formattedPhoneNumber += ") " ;
                if (i == 6) formattedPhoneNumber += " - ";
                if (i == 13) formattedPhoneNumber += " ";
                formattedPhoneNumber += storedCase.phoneNumber.charAt(i);
            }
            contactPhone.setText(formattedPhoneNumber);
            contactPhone.setVisibility(View.VISIBLE);
            callButton.setEnabled(true);
            callButton.setVisibility(View.VISIBLE);
        } else {
            contactPhone.setVisibility(View.INVISIBLE);
            callButton.setVisibility(View.INVISIBLE);
        }



    }

    /*
     * Assuming we came in from the addNote button at the top, so we populate differently than
     * coming in from an existing note.
     */
    private void populateFromContact() {
        if (contact.name != null) {
            contactName.setText(contact.name);
            contactName.setVisibility(View.VISIBLE);
        } else {
            contactName.setText(View.INVISIBLE);
        }

        if (contact.email != null) {
            contactEmail.setText(contact.email);
            contactEmail.setVisibility(View.VISIBLE);
        } else {
            contactEmail.setVisibility(View.INVISIBLE);
        }

        if (contact.phoneNumber != null) {
            contactPhone.setText(contact.phoneNumber);
            contactPhone.setVisibility(View.VISIBLE);
            callButton.setEnabled(true);
            callButton.setVisibility(View.VISIBLE);
        } else {
            contactPhone.setVisibility(View.INVISIBLE);
            callButton.setVisibility(View.INVISIBLE);
        }

    }

    /*
     * The little time function that keeps the timer running. Checked the timing on it and it's
     * very accurate.
     */
    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (shouldContinue) {
                timeInSeconds = SystemClock.uptimeMillis() - startTime;
                updatedTime = timeSwapBuff + timeInSeconds;

                int secs = (int) (updatedTime / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                timerValue.setText("" + mins + ":"
                        + String.format("%02d", secs));

                customHandler.postDelayed(this, 0);
            } else {
                return;
            }
        }
    };


    /*
     * Handler for all permission request. The app requires 2 so far, one to make a phonecall, and
     * one to access the contacts. The app asks for permission when the buttons are pressed, and
     * android returns if it got permission via this function so we check it. If it works we just
     * call the appropriate function.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                    return;
                }
            }

            case MY_PERMISSIONS_REQUEST_WRITE_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addContact();
                    return;
                }
            }
        }
    }


    /*
     * Function that's called once the user gives permission, it attempts to launch the phone
     * and dial the number in the contacts.
     */
    private void makeCall(){
        if (contactPhone.getText() != "") {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + contactPhone.getText()));

            try {
                startActivity(callIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), "Can't make call", Toast.LENGTH_SHORT).show();
            }
            return;
        } else {
            Toast.makeText(getApplicationContext(), "No phone number given for this case",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Function that's called once the user gives permission, it attempts to launch the contacts
     * Activity and fill out available parameters with current case's contact information
     */
    private void addContact(){
        Intent addContactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        addContactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, contactPhone.getText()).
                putExtra(ContactsContract.Intents.Insert.EMAIL, contactEmail.getText()).
                putExtra(ContactsContract.Intents.Insert.NAME, contactName.getText());
        try {
            startActivity(addContactIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Couldn't add contact", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Note Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://datacare.ekvoice/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Note Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://datacare.ekvoice/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
