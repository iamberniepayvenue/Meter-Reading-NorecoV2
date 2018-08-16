package Model;

import android.util.Log;

/**
 * Created by andrewlaurienrsocia on 23/08/2017.
 */

public class Person {

    private String FirstName;
    private String MiddleName;
    private String LastName;
    private String Address;
    private String FullName;

    public Person() {

    }


    public Person(String firstName, String middleName, String lastName, String address) {


        this.FirstName = firstName;
        this.MiddleName = middleName;
        this.LastName = lastName;
        this.Address = address;

        if (!lastName.isEmpty()) {
            this.FullName = lastName;
        }

        if (!firstName.isEmpty()) {
            this.FullName = this.FullName + firstName;
        }

        if (!middleName.isEmpty()) {
            this.FullName = this.FullName + middleName;
        }

        Log.d("Person", this.FullName);

    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }
}
