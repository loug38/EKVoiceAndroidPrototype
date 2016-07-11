package datacare.ekvoice;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

    private ToggleButton timerButton;
    private Button stopButton;

    private TextView timerValue;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    private Case storedCase;
    private Case.Note note;
    private Contact contact;
    private Boolean emptyNoteWithContact = false;
    private Boolean editingNote = false;
    private EditText editBox;
    private TextView addContactLabel;
    private TextView contactName;
    private TextView contactPhone;
    private TextView contactEmail;
    private ImageButton callButton;
    private ImageButton addContactButton;
    private Button saveButton;
    private boolean isPressed = false;
    private boolean shouldContinue = false;
    private final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 1;
    private final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 2;
    long timeInSeconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    boolean stop = true;
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
        timerButton = (ToggleButton) findViewById(R.id.timerButton);
        contactName = (TextView) findViewById(R.id.addNoteContactName);
        contactPhone = (TextView) findViewById(R.id.addNoteContactPhone);
        contactEmail = (TextView) findViewById(R.id.addNoteContactEmail);
        callButton = (ImageButton) findViewById(R.id.addNoteCallButton);
        addContactButton = (ImageButton) findViewById(R.id.addNoteAddContact);
        saveButton = (Button) findViewById(R.id.addNoteSave);
        editBox = (EditText) findViewById(R.id.addNoteEditText);

        Intent i = getIntent();
        storedCase = (Case) i.getSerializableExtra("CASE_EXTRA");

        editBox.setText(storedCase.firstName);

        if (i.hasExtra("selectedNote")) {
            editingNote = true;
            note = (Case.Note) i.getSerializableExtra("selectedNote");
            populateFromNote();
        } else if (i.hasExtra("selectedContact")) {
            emptyNoteWithContact = true;
            contact = (Contact) i.getSerializableExtra("selectedContact");
            populateFromContact();
        }


        timerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!isPressed) {
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    stop = false;
                    shouldContinue = true;
                    isPressed = true;
                } else {
                    startTime = SystemClock.uptimeMillis();
                    shouldContinue = false;
                    isPressed = false;
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editBox.getText().toString().isEmpty()) {
                    finish();
                } else if (editingNote) {
                    note.noteText = editBox.getText().toString();
                    if (contact == null || contact != note.contact) {
                        note.contact = contact;
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

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NoteActivity.this,
                                                      Manifest.permission.CALL_PHONE)
                                                      != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(NoteActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_PHONE_CALL);
                }
            }
        });

        addContactButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NoteActivity.this,
                                                      Manifest.permission.WRITE_CONTACTS)
                                                      != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(NoteActivity.this,
                                                      new String[]{Manifest.permission.WRITE_CONTACTS},
                                                      MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void populateFromNote() {
        editBox.setText(note.noteText);
        timerValue.setText(note.hours + ":" + note.minutes + String.format("%01d", note.seconds));

        //addContactLabel.setText("Change Contact");
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

    private void populateFromContact() {
        addContactLabel.setText("Change Contact");
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
            //Needs to be implemented
            callButton.setEnabled(true);
            callButton.setVisibility(View.VISIBLE);
        } else {
            contactPhone.setVisibility(View.INVISIBLE);
            callButton.setVisibility(View.INVISIBLE);
        }

    }

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

    public void cancelButton(View v) {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults){
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + storedCase.phoneNumber.toString()));

                    try {
                        startActivity(callIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "Can't make call", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Must give phone call permission", Toast.LENGTH_SHORT);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent addContactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                    addContactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                    addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, contactPhone.getText()).
                            putExtra(ContactsContract.Intents.Insert.EMAIL, contactEmail.getText()).
                            putExtra(ContactsContract.Intents.Insert.NAME, contactName.getText());
                    try {
                        startActivity(addContactIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "Can't make call", Toast.LENGTH_SHORT);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Must give contact permission", Toast.LENGTH_SHORT);
                }
            }
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
