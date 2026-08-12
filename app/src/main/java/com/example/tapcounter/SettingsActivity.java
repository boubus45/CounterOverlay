package com.example.tapcounter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "CounterPrefs";
    public static final String KEY_INCREMENT = "increment_amount";
    public static final int DEFAULT_INCREMENT = 1;

    private EditText incrementInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        incrementInput = findViewById(R.id.increment_input);
        Button saveBtn = findViewById(R.id.save_btn);

        // Load current value
        int current = prefs.getInt(KEY_INCREMENT, DEFAULT_INCREMENT);
        incrementInput.setText(String.valueOf(current));

        saveBtn.setOnClickListener(v -> {
            String val = incrementInput.getText().toString().trim();
            if (val.isEmpty()) {
                Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int amount = Integer.parseInt(val);
                if (amount < 1) {
                    Toast.makeText(this, "Must be at least 1", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.edit().putInt(KEY_INCREMENT, amount).apply();
                Toast.makeText(this, "Saved: +" + amount + " per tap", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
