/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package ds.edu.symptomcheckerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The main activity of the Symptom Checker app.
 * This activity allows the user to select symptoms and get a diagnosis.
 * The user can select symptoms from a list of features and input their values.
 * The user can also clear the input and get a diagnosis based on the selected symptoms.
 * The diagnosis is displayed to the user.
 * The user needs to enter their age before getting the diagnosis.
 * The user can select multiple symptoms and get a diagnosis based on the selected symptoms.
 * The user can clear the input and start over.
 */

public class MainActivity extends AppCompatActivity {

    private Spinner featureSpinner;
    private Button submitButton;
    private Button clearButton;
    private TextView userInputTextView;
    private List<JsonObject> features;
    private TextView diagnosisTextView;
    private TextView ageMessageTextView;

    // The onCreate method is called when the activity is created.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI components
        featureSpinner = findViewById(R.id.featureSpinner);
        userInputTextView = findViewById(R.id.userInputTextView);
        ageMessageTextView = findViewById(R.id.ageMessageTextView);
        submitButton = findViewById(R.id.submitButton);
        clearButton = findViewById(R.id.clearButton);
        diagnosisTextView = findViewById(R.id.diagnosisTextView);

        features = parseJsonData();

        List<String> featureNames = new ArrayList<>();
        for (JsonObject feature : features) {
            featureNames.add(feature.get("text").getAsString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, featureNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        featureSpinner.setAdapter(adapter);

        // spinner item selected listener
        featureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Check if this is the initial selection
                if (parent.getSelectedItemPosition() == 0) {
                    // Do nothing for the initial selection
                    return;
                }

                String selectedFeatureText = featureSpinner.getSelectedItem().toString();
                String userInput = userInputTextView.getText().toString();

                // Check if the user has already entered the selected feature
                if (userInput.contains(selectedFeatureText + ":")) {
                    featureSpinner.setSelection(0);
                    return;
                }

                JsonObject selectedFeature = features.get(position);
                String featureText = selectedFeature.get("text").getAsString();
                String featureName = selectedFeature.get("name").getAsString();
                String featureType = selectedFeature.get("type").getAsString();

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra("featureText", featureText);
                intent.putExtra("featureName", featureName);
                intent.putExtra("featureType", featureType);

                if (featureType.equals("categorical")) {
                    JsonArray choices = selectedFeature.getAsJsonArray("choices");
                    Gson gson = new Gson();
                    String choicesJson = gson.toJson(choices);
                    intent.putExtra("choices", choicesJson);
                }

                startActivityForResult(intent, 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set the onClickListener for the clear button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user input
                userInputTextView.setText("");
                diagnosisTextView.setText("");

                // Reset spinner selection to the first item
                featureSpinner.setSelection(0);
            }
        });

        // Set the onClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = userInputTextView.getText().toString();
                if (!userInput.contains("What is the age?")) {
                    ageMessageTextView.setText("Please enter your age before getting the diagnosis.");
                    ageMessageTextView.setVisibility(View.VISIBLE);
                    return;
                } else {
                    ageMessageTextView.setVisibility(View.GONE);
                }

                JsonObject requestBody = new JsonObject();
                String[] lines = userInputTextView.getText().toString().split("\n");
                for (String line : lines) {
                    String[] parts = line.split(": ");
                    if (parts.length == 2) {
                        String featureName = getFeatureNameFromText(parts[0]);
                        String inputValue = parts[1];
                        requestBody.addProperty(featureName, inputValue);
                    }
                }

                // Send the JSON request to the web service
                SymptomCheckerWebService.sendJsonRequest(requestBody, new SymptomCheckerWebService.ResponseListener() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the web service
                        Log.d("MainActivity", "Response: " + response);

                        // Parse the JSON response
                        Gson gson = new Gson();
                        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

                        // Extract the diagnosis from the JSON response
                        String diagnosis = jsonResponse.get("diagnosis").getAsString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (diagnosis.isEmpty()) {
                                    diagnosisTextView.setText("Please select more symptoms to get a diagnosis.");
                                } else {
                                    String[] symptoms = diagnosis.split(",");
                                    String diseaseName = symptoms[0];
                                    String probability = symptoms[1];
                                    String diagnosisText = "You may have: " + diseaseName + "\n" + "Probability: " + probability;
                                    diagnosisTextView.setText(diagnosisText);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Handle the error
                        Log.e("MainActivity", "Error: " + error);
                    }
                });
            }
        });

    }

    // Get the feature name from the feature text
    private String getFeatureNameFromText(String featureText) {
        for (JsonObject feature : features) {
            if (feature.get("text").getAsString().equals(featureText)) {
                return feature.get("name").getAsString();
            }
        }
        return "";
    }

    // The onActivityResult method is called when an activity returns a result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String inputValue = data.getStringExtra("inputValue");
            String selectedFeatureText = featureSpinner.getSelectedItem().toString();

            userInputTextView.append(selectedFeatureText + ": " + inputValue + "\n");
        }
    }

    // Parse the JSON data from the assets folder
    private List<JsonObject> parseJsonData() {
        // Parse the JSON data from the assets folder
        String jsonString = loadJsonFromAsset("SymptomsOutput.json");

        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);

        List<JsonObject> features = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            features.add(element.getAsJsonObject());
        }

        return features;
    }

    // Load the JSON data from the assets folder
    private String loadJsonFromAsset(String fileName) {
        String jsonString = null;
        try {
            InputStream inputStream = getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}