/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package ds.edu.symptomcheckerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Activity for user input
 * This activity is used to get user input for the selected feature
 * The input can be either a number or a choice from a list of choices
 * The input is validated based on the feature type
 * The activity layout is determined by the feature type
 * If the feature type is integer or double, the layout includes an EditText and a Button
 * If the feature type is categorical, the layout includes a RadioGroup and a Button
 * The choices for the categorical feature are dynamically added to the RadioGroup
 * The user can select one of the choices and save the input
 * The input is then sent back to the calling activity
 */
public class InputActivity extends AppCompatActivity {

    private TextView selectedFeatureTextView;
    private EditText inputEditText;
    private Button saveButton;
    private RadioGroup choicesRadioGroup;
    private Button saveCategoricalButton;

    // Method to create the activity layout based on the feature type
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String featureText = getIntent().getStringExtra("featureText");
        String featureType = getIntent().getStringExtra("featureType");

        // create the layout for integer or double feature
        if (featureType.equals("integer") || featureType.equals("double")) {
            setContentView(R.layout.activity_input);
            selectedFeatureTextView = findViewById(R.id.selectedFeatureTextView);
            inputEditText = findViewById(R.id.inputEditText);
            saveButton = findViewById(R.id.saveButton);

            selectedFeatureTextView.setText(featureText);

            // save the input value and return to the calling activity
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputValue = inputEditText.getText().toString();
                    if (isValidInput(inputValue, featureType)) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("inputValue", inputValue);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        inputEditText.setError("Please enter a valid number.");
                    }
                }
            });
        } else if (featureType.equals("categorical")) {
            // create the layout for categorical feature
            setContentView(R.layout.activity_categorical_input);
            selectedFeatureTextView = findViewById(R.id.selectedFeatureTextView);
            choicesRadioGroup = findViewById(R.id.choicesRadioGroup);
            saveCategoricalButton = findViewById(R.id.saveCategoricalButton);

            selectedFeatureTextView.setText(featureText);

            String choicesJson = getIntent().getStringExtra("choices");
            Gson gson = new Gson();
            JsonArray choicesArray = gson.fromJson(choicesJson, JsonArray.class);

            for (JsonElement choice : choicesArray) {
                JsonObject choiceObject = choice.getAsJsonObject();
                String choiceText = choiceObject.get("text").getAsString();
                int choiceValue = choiceObject.get("value").getAsInt();

                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(choiceText);
                radioButton.setTag(choiceValue);
                choicesRadioGroup.addView(radioButton);
            }

            // save the selected choice value and return to the calling activity
            saveCategoricalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedChoiceId = choicesRadioGroup.getCheckedRadioButtonId();
                    if (selectedChoiceId != -1) {
                        RadioButton selectedRadioButton = findViewById(selectedChoiceId);
                        int selectedChoiceValue = (int) selectedRadioButton.getTag();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("inputValue", String.valueOf(selectedChoiceValue));
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });
        }
    }


    // Utility method to validate input based on feature type
    private boolean isValidInput(String input, String featureType) {
        try {
            if (featureType.equals("integer")) {
                Integer.parseInt(input);
            } else if (featureType.equals("double")) {
                Double.parseDouble(input);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
