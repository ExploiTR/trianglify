package com.sdsmdg.kd.trianglifyexample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.sdsmdg.kd.trianglify.models.Palette;
import com.sdsmdg.kd.trianglify.views.TrianglifyView;

public class CustomPalettePickerActivity extends AppCompatActivity {
    public static final String CUSTOM_PALETTE_COLOR_ARRAY = "Custom Palette Color Array";
    private static final String TAG = "CustomPaletteActivity";
    private final ImageView[] imageViews = new ImageView[9];
    private Palette palette;
    private TrianglifyView trianglifyView;
    private int[] colors = {Color.BLACK, Color.BLUE, Color.BLACK, Color.CYAN, Color.DKGRAY, Color.GREEN, Color.RED, Color.MAGENTA, Color.LTGRAY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_palette_picker);

        setupActionBar();
        initializeColors();
        initializeTrianglifyView();
        setupImageViews();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Custom Palette Picker");
            } catch (NullPointerException e) {
                Log.e(TAG, "Error setting up action bar", e);
            }
        }
    }

    private void initializeColors() {
        int[] receivedColors = getIntent().getIntArrayExtra(getResources().getString(R.string.palette_color_array));
        if (receivedColors != null) {
            colors = receivedColors;
        }
    }

    private void initializeTrianglifyView() {
        trianglifyView = findViewById(R.id.trianglify_custom_palette_view);
        palette = new Palette(colors);
        trianglifyView.setPalette(palette);
        trianglifyView.smartUpdate();
    }

    private void setupImageViews() {
        for (int i = 0; i < imageViews.length; i++) {
            //I mean it works? lol
            @SuppressLint("DiscouragedApi") int resID = getResources().getIdentifier("custom_palette_c" + i, "id", getPackageName());
            imageViews[i] = findViewById(resID);
            imageViews[i].setBackgroundColor(colors[i] | 0xff000000);
            setupColorPickerForImageView(imageViews[i], i);
        }
    }

    private void setupColorPickerForImageView(ImageView imageView, int index) {
        imageView.setOnClickListener(v -> ColorPickerDialogBuilder
                .with(this)
                .initialColor(colors[index] | 0xff000000)
                .setTitle("Choose Color")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(9)
                .showColorEdit(true)
                .lightnessSliderOnly()
                .setColorEditTextColor(0xff000000)
                .showColorPreview(true)
                .setPositiveButton("ok", (dialog, color, allColors) -> updateColor(index, color))
                .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                .build()
                .show());
    }

    private void updateColor(int index, int color) {
        imageViews[index].setBackgroundColor(color);
        colors[index] = color & 0x00ffffff; // Remove alpha
        palette = new Palette(colors);
        trianglifyView.setPalette(palette);
        trianglifyView.smartUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_custom_palette_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.custom_palette_ok) {
            savePaletteAndExit();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void savePaletteAndExit() {
        Intent intent = new Intent();
        intent.putExtra(CUSTOM_PALETTE_COLOR_ARRAY, colors);
        setResult(RESULT_OK, intent);
        onBackPressed();
    }
}