package com.enesb.travelbook.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.enesb.travelbook.R;
import com.enesb.travelbook.adapter.CustomAdapter;
import com.enesb.travelbook.model.Place;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ArrayList<Place> placeList = new ArrayList<>();
    ListView listView;
    CustomAdapter customAdapter;


    // MENU ICIN GEREKLI METODLAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_place) { // Kontrol ediyoruz ID ler ayni mi diye
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("info", "new"); // Bir sonraki aktiviteden kontrol icin yaptik
            startActivity(intent); // Haritalara gitme

        }

        return super.onOptionsItemSelected(item);
    }
    // ---------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        getData();
    }

    public void getData() {

        customAdapter = new CustomAdapter(this, placeList);

        try {

            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM places",null);

            int nameIx = cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx = cursor.getColumnIndex("longitude");

            while (cursor.moveToNext()) {

                String nameFromDatabase = cursor.getString(nameIx);
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIx);

                Double latitude = Double.parseDouble(latitudeFromDatabase);
                Double longitude = Double.parseDouble(longitudeFromDatabase);

                Place place = new Place(nameFromDatabase,latitude,longitude);

                System.out.println(place.name);

                placeList.add(place);

            }
            customAdapter.notifyDataSetChanged(); // Veri degisikligi oldugunu bildirme
            cursor.close();

        } catch (Exception e){
            e.printStackTrace();
        }

        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("info", "old"); // Baska bir yerden geldigimiz icin old yazdik
                intent.putExtra("place", placeList.get(position));
                startActivity(intent);
            }
        });


    }


}