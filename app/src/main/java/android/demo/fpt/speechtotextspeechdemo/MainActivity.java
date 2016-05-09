package android.demo.fpt.speechtotextspeechdemo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView tvMgsOutput, tvStatus;
    ImageButton imgBtnMic;
    ImageView ivLight, ivTv, ivFan;
    Firebase fireBaseRef;
    MediaPlayer mp;
    Switch swLight, swTv, swFan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMgsOutput = (TextView) findViewById(R.id.tvMgsOutput);
        imgBtnMic = (ImageButton) findViewById(R.id.imgBtnMic);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        ivLight = (ImageView) findViewById(R.id.imageView_Light);
        ivTv = (ImageView) findViewById(R.id.imageView_Tv);
        ivFan = (ImageView) findViewById(R.id.imageView_Fan);
        swLight = (Switch) findViewById(R.id.switch_Light);
        swTv = (Switch) findViewById(R.id.switch_Tv);
        swFan = (Switch) findViewById(R.id.switch_Fan);

        // Setup Firebase
        Firebase.setAndroidContext(this);
        fireBaseRef = new Firebase("https://smart-home-rasp.firebaseio.com/");

        // Listening event
        fireBaseRef.child("light").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int value = Integer.parseInt(dataSnapshot.getValue().toString());
                changeIcon("light",value);
                changeSwitch("light",value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(MainActivity.this, firebaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        fireBaseRef.child("tv").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int value = Integer.parseInt(dataSnapshot.getValue().toString());
                changeIcon("tv", value);
                changeSwitch("tv", value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(MainActivity.this, firebaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        fireBaseRef.child("fan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int value = Integer.parseInt(dataSnapshot.getValue().toString());
                changeIcon("fan", value);
                changeSwitch("fan", value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        imgBtnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRegconition();
            }
        });

        sendEventToServer_bySwitch();

    }

    private void speechRegconition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something !");

        try {
            startActivityForResult(intent, 100);
        }catch (ActivityNotFoundException a){
            Toast.makeText(MainActivity.this, "Sorry! Your device doesn't support speech input! ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 100:{
                if(resultCode == RESULT_OK && data!= null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String value = result.get(0);

                    if(!value.equalsIgnoreCase("light on") && !value.equalsIgnoreCase("light off") && !value.equalsIgnoreCase("tv on") && !value.equalsIgnoreCase("tv off") && !value.equalsIgnoreCase("fan on") && !value.equalsIgnoreCase("fan off")){
                        soundAlert("try again");
                    }

                    tvMgsOutput.setText(value);
                    sendEventToServer_byVoice(value); // call function
                }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
                    Toast.makeText(MainActivity.this, "Audio error!", Toast.LENGTH_SHORT).show();
                }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
                    Toast.makeText(MainActivity.this, "Client error!", Toast.LENGTH_SHORT).show();
                }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
                    Toast.makeText(MainActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
                }
            }

        }

    }

    private void sendEventToServer_byVoice(String event){   // Through switches
        if(event.equalsIgnoreCase("light on")){
            swLight.setChecked(true);
        }
        if(event.equalsIgnoreCase("light off")){
            swLight.setChecked(false);
        }
        if(event.equalsIgnoreCase("tv on")){
            swTv.setChecked(true);
        }
        if(event.equalsIgnoreCase("tv off")){
            swTv.setChecked(false);
        }
        if(event.equalsIgnoreCase("fan on")){
            swFan.setChecked(true);
        }
        if(event.equalsIgnoreCase("fan off")){
            swFan.setChecked(false);
        }
    }

    private void soundAlert(String event){  // need release resource for mediaPlayer
        if(event.equalsIgnoreCase("try again")){
            mp = MediaPlayer.create(this,R.raw.iamsorrytryagain);
            mp.start();
        }
    }



    private void changeIcon(String device, int value){
        if(value == 0){
            if(device.equals("light")){
                ivLight.setImageResource(R.drawable.ic_light_off);
            }
            if(device.equals("tv")){
                ivTv.setImageResource(R.drawable.ic_tv_off);
            }
            if (device.equals("fan")){
                ivFan.setImageResource(R.drawable.ic_fan_off);
            }
        }
        if(value == 1){
            if(device.equals("light")){
                ivLight.setImageResource(R.drawable.ic_light_on);
            }
            if(device.equals("tv")){
                ivTv.setImageResource(R.drawable.ic_tv_on);
            }
            if (device.equals("fan")){
                ivFan.setImageResource(R.drawable.ic_fan_on);
            }
        }
    }

    private void changeSwitch(String device, int value){
        if(value == 0){
            if(device.equals("light")){
                swLight.setChecked(false);
            }
            if(device.equals("tv")){
                swTv.setChecked(false);
            }
            if(device.equals("fan")){
                swFan.setChecked(false);
            }
        }
        if(value == 1){
            if(device.equals("light")){
                swLight.setChecked(true);
            }
            if(device.equals("tv")){
                swTv.setChecked(true);
            }
            if(device.equals("fan")){
                swFan.setChecked(true);
            }
        }
    }

    // If you only using switch, The speech execution is faster than the voice
    // Because when you use speech recognition. It triggers switch. So switch continues pass value to server
    // ==> Has resolved! Transformation all event to Switch (Key event)
    private void sendEventToServer_bySwitch(){
        swLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fireBaseRef.child("light").setValue("1");
                } else {
                    fireBaseRef.child("light").setValue("0");
                }
            }
        });

        swTv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fireBaseRef.child("tv").setValue("1");
                } else {
                    fireBaseRef.child("tv").setValue("0");
                }
            }
        });

        swFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    fireBaseRef.child("fan").setValue("1");
                }else{
                    fireBaseRef.child("fan").setValue("0");
                }
            }
        });
    }


}

// first upload
