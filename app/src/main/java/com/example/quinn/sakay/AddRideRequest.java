package com.example.quinn.sakay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddRideRequest extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Connect to the Firebase database
        database = FirebaseDatabase.getInstance();
        // Get a reference to the todoItems child items it the database
        myRef = database.getReference("todoItems");
        text = (EditText) findViewById(R.id.addRideRequestStart);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_ride_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add_ride) {
            // Create a new child with a auto-generated ID.
            DatabaseReference childRef = myRef.push();

            // Set the child's data to the value passed in from the text box.
            childRef.setValue(text.getText().toString());


            Toast.makeText(getApplicationContext(), "Ride Request Added", Toast.LENGTH_SHORT).show();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
