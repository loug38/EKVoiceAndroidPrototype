package datacare.ekvoice;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Robin on 3/5/2016.
 * This is the Case object which serves as a simple plain-old data object to hold the data we pull
 * from the JSON. A case primarily consists of three sets of data: The main client information, a set
 * of contacts for that client, and a list of notes for that case.
 */
public class Case implements Serializable{

    public Case() {
        notes = new ArrayList<Note>();
    }
    public String lastName;
    public String firstName;
    public String position;
    public String claimNumber;
    public String address1;
    public String address2;
    public String city;
    public String state;
    public String zip;
    public String phoneNumber;
    public String email;
    public Contact MD;
    public Contact manager;
    public Contact carrier_contact;
    public Contact employer;
    public Contact attorney;
    public Contact serviceProvider;
    public ArrayList<Note> notes;
    public static class Note implements Serializable{
        public String user;
        public String phone;
        public String email;
        public String date;
        public String noteText;
        public Contact contact;
        public int hours = 0;
        public int minutes = 0;
        public int seconds = 0;
    }
}
