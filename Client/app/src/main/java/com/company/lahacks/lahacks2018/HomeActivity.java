package com.company.lahacks.lahacks2018;

import android.util.Log;
import android.view.View;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private String[] mUrls = new String[6];
    private boolean isHost = false;
    private EditText lobby;

    public void updateImageUI() {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("mUrl", mUrls);
        intent.putExtra("isHost", isHost);
        startActivity(intent);
    }

    public void updateUploadUI() {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        lobby = (EditText) findViewById(R.id.et_lobby);
    }

    public boolean getLobbyName(){
        String partyName = lobby.getText().toString();
        if(partyName.equals(""))
            return false;
        return true;
    }

    public void createParty(View view) {

        if (!getLobbyName()) {
            Toast.makeText(HomeActivity.this, "Lobby name cannot be empty!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final String partyName = lobby.getText().toString();
        isHost = true;
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.child("parties").getValue();
                if(map.containsKey(partyName)) {
                    Toast.makeText(HomeActivity.this, "Sorry that lobby already exists!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    myRef.child("parties").child(partyName).setValue(partyName);
                    generateImages(partyName);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void joinParty(View view){
        final String partyName = lobby.getText().toString();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.child("parties").getValue();
                if(map.containsKey(partyName)) {
                    Log.d("asdf", map.toString());
                    ArrayList<String> urls = (ArrayList<String>) map.get(partyName);
                    for(int i = 0; i < urls.size(); i++) {
                        mUrls[i] = urls.get(i);
                    }
                    updateImageUI();
                }else {
                    Toast.makeText(HomeActivity.this, "That lobby doesn't exist!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void generateImages(final String s){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.child("parties").getValue();
                if(map.containsValue(s)) {
                    foodHelper((Map<String, Object>) dataSnapshot.getValue());
                    for(int i = 0; i < 6; i ++) {
                        String j = Integer.toString(i);
                        String l = mUrls[i];
                        myRef.child("parties").child(s).child(j).setValue(l);
                    }
                    updateImageUI();
                }else {
                    return;
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void foodHelper(Map<String, Object> map){
        Map<String, String> foodUrls = (Map<String, String>) map.get("food");
        int length = foodUrls.size();

        Object[] keys = foodUrls.keySet().toArray();
        int numPics = 6;
        int size = 0;
        int[] chosenPics = new int[length];
        while (numPics > 0) {
            int randInt = (int) (Math.random() * length);
            boolean contains = false;
            for (int i = 0; i < size; i++) {
                if (chosenPics[i] == randInt)
                    contains = true;
            }
            if(contains)
                continue;
            chosenPics[6 - numPics] = randInt;
            mUrls[6 - numPics] = foodUrls.get((String) keys[randInt]);
            size++;
            numPics--;
        }
    }

    public void uploadImage(View view) {
        updateUploadUI();
    }

}
