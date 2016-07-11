package datacare.ekvoice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by george on 4/19/16.
 * This fragment serves to take the list of notes pulled from the data and populates a listview with
 * each note sorted from newest to oldest.
 */

public class NotesHistoryFragment extends Fragment{
    private ArrayList<Case.Note> notes;
    private Case storedCase;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle state) {
        View v = inflater.inflate(R.layout.notes_history, container, false);
        final NoteListAdapter notesAdapter = new NoteListAdapter(getActivity(), notes);
        final ListView cList = (ListView) v.findViewById(R.id.notesList);
        cList.setAdapter(notesAdapter);
        cList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                Case.Note itemValue = (Case.Note) cList.getItemAtPosition(itemPosition);

                Intent intent = new Intent(getActivity(), NoteActivity.class);
                intent.putExtra("CASE_EXTRA", storedCase);
                intent.putExtra("selectedNote", itemValue);
                intent.putExtra("selectedNoteData", storedCase);

                startActivity(intent);


            }

        });
        return v;
    }
    public static Fragment newInstance(ArrayList<Case.Note> notesParam, Case caseParam){
        NotesHistoryFragment frag = new NotesHistoryFragment();
        frag.storedCase = caseParam;
        if (notesParam != null && notesParam.size() != 0) {
            Collections.reverse(notesParam);
            frag.notes = notesParam;
        }
        return frag;
    }


    private class NoteListAdapter extends ArrayAdapter<Case.Note> {

        public NoteListAdapter(Context context, ArrayList<Case.Note> users) {
            super(context, 0, users);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Case.Note aNote = getItem(position);
            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.notes_list_item, parent, false);
            }
            TextView noteContent = (TextView) convertView.findViewById(R.id.noteContent);
            TextView noteUserName = (TextView) convertView.findViewById(R.id.noteUserName);

            noteContent.setText(aNote.noteText);
            noteUserName.setText(aNote.user);
            return convertView;
        }

    }

}

