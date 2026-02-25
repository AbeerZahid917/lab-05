package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    private City selectedCity;

    private boolean deleteMode = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);


//        addDummyData();
        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }
            if (value != null) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name"); // no colon
                    String province = snapshot.getString("province"); // no colon
                    if (name != null && province != null) {
                        cityArrayList.add(new City(name, province));
                    }
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });


        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });


        // Delete City button listener
        deleteCityButton.setOnClickListener(view -> {
            deleteMode = true;
        });

        cityListView.setOnItemClickListener((parent, view, position, id) -> {
            City city = cityArrayAdapter.getItem(position);

            if (deleteMode) {
                delCity(city);   // delete instead of update
                deleteMode = false; // exit delete mode
            }
            else {
                // normal update behavior
                CityDialogFragment dialog = CityDialogFragment.newInstance(city);
                dialog.show(getSupportFragmentManager(), "City Details");
            }
        });
    }





    @Override
    public void addCity(City city){
        citiesRef.document(city.getName()) // use city name as document ID
                .set(city)
                .addOnSuccessListener(aVoid -> {   // note: aVoid because .set() returns Task<Void>
                    Log.d("Firestore", "City added: " + city.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding city", e);
                });
    }





    @Override
    public void delCity(City city){
        citiesRef.document(city.getName()) // use city name as document ID
                .delete()
                .addOnSuccessListener(aVoid -> {   // note: aVoid because .set() returns Task<Void>
                    Log.d("Firestore", "City deleted: " + city.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting city", e);
                });
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();
    }





    @Override
    public void updateCity(City city, String title, String year) {
        if (!city.getName().equals(title)) {
            citiesRef.document(city.getName()).delete();
        }

        city.setName(title);
        city.setProvince(year);

        // Save updated city
        citiesRef.document(city.getName())
                .set(city)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "City updated: " + city.getName()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating city", e));

        cityArrayAdapter.notifyDataSetChanged();
    }


//    public void addDummyData(){
//        City m1 = new City("Edmonton", "AB");
//        City m2 = new City("Vancouver", "BC");
//        cityArrayList.add(m1);
//        cityArrayList.add(m2);
//        cityArrayAdapter.notifyDataSetChanged();
//    }
}