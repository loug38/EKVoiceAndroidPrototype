package datacare.ekvoice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Robin on 3/1/2016.
 * This is the adapter created to populate the list of cases in CaseList.java
 */
public class CaseAdapter extends ArrayAdapter<Case> {
    public CaseAdapter(Context context, ArrayList<Case> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Case customer = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.case_list_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.nameText);
        TextView tvHome = (TextView) convertView.findViewById(R.id.caseNumber);
        // Populate the data into the template view using the data object
        tvName.setText(customer.lastName);
        tvHome.setText(customer.claimNumber);
        // Return the completed view to render on screen


        return convertView;
    }
}
