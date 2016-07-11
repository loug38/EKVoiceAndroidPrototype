package datacare.ekvoice;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ramotion.foldingcell.FoldingCell;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by george on 4/11/16.
 * This fragment holds and populates the list of contacts to be displayed inside the CaseDetailsAdapter
 */
public class ContactListFragment extends Fragment {
    private ArrayList<Contact> contacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {

        View v = inflater.inflate(R.layout.contacts_list, container, false);
        TextView name = (TextView) v.findViewById(R.id.content_name_view);

        //get our list view
        ListView cList = (ListView) v.findViewById(R.id.contactsList);

        //create custom adapater that holds elements and their state.
        final ContactListAdapter contactsAdapter = new ContactListAdapter(getActivity(), contacts);

        //set elements to adapter
        cList.setAdapter(contactsAdapter);

        // get our folding cell
        cList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               ((FoldingCell) view).toggle(false);
            }
        });

        return v;
    }

    public static Fragment newInstance(ArrayList<Contact> contactsParam){
        ContactListFragment frag = new ContactListFragment();
        if (contactsParam != null && contactsParam.size() != 0) {
            frag.contacts = contactsParam;
        }
        return frag;
    }

    //This is the adapter for populating the contacts list.
    private  class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter(Context context, ArrayList<Contact> users) {
            super(context, 0, users);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Contact aContact = getItem(position);
            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell, parent, false);
            }
            //Button addNote = (Button) convertView.findViewById(R.id.button11);
            //addNote.setOnClickListener(new View.OnClickListener() {
            //    @Override
            //    public void onClick(View v) {
            //        Intent i = new Intent(/*getContext()*/ ContactListFragment,NoteActivity.class);
            //        i.putExtra("SELECTED CONTACT", aContact);
            //        startActivity(i);
            //    }
            //});
            TextView contactName = (TextView) convertView.findViewById(R.id.contactName);
            TextView contactPosition = (TextView) convertView.findViewById(R.id.contactPosition);
            TextView contactNameExpanded = (TextView) convertView.findViewById(R.id.content_name_view);
            TextView contactAddress1Expanded = (TextView) convertView.findViewById(R.id.content_from_address_1);
            TextView contactAddress2Expanded = (TextView) convertView.findViewById(R.id.content_from_address_2);
            TextView contactEmailExpanded = (TextView) convertView.findViewById(R.id.textView10);
            TextView contactPhoneExpanded = (TextView) convertView.findViewById(R.id.content_phone_number);
            TextView contactCompanyExpanded = (TextView) convertView.findViewById(R.id.content_company);

            contactName.setText(aContact.name);
            contactPosition.setText(aContact.position);
            contactNameExpanded.setText(aContact.name);

            if (aContact.address1 != null)
                contactAddress1Expanded.setText(aContact.address1);
            else contactAddress1Expanded.setText(" ");

            if (aContact.address2 != null)
                contactAddress2Expanded.setText(aContact.address2 + " " + aContact.city);
            else contactAddress2Expanded.setText(" ");

            if (aContact.email != null)
                contactEmailExpanded.setText(aContact.email);
            else contactEmailExpanded.setText(" ");

            if (aContact.phoneNumber != null) {
                String formattedNumber = "";
                for (int i = 0; i < aContact.phoneNumber.length(); i++) {
                    if (i == 0) formattedNumber += "(";
                    if (i == 3) formattedNumber += ") " ;
                    if (i == 6) formattedNumber += " - ";
                    if (i == 13) formattedNumber += " ";
                    formattedNumber += aContact.phoneNumber.charAt(i);
                }

                contactPhoneExpanded.setText(formattedNumber);
            } else contactPhoneExpanded.setText(" ");

            if (aContact.company != null)
                contactCompanyExpanded.setText(aContact.company);
            else contactCompanyExpanded.setText(" ");

            return convertView;
        }

    }
}
