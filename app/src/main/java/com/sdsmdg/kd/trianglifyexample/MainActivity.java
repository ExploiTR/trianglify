package com.sdsmdg.kd.trianglifyexample;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sdsmdg.kd.trianglify.models.Palette;
import com.sdsmdg.kd.trianglify.utilities.Utilities;
import com.sdsmdg.kd.trianglify.views.TrianglifyView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public TrianglifyView trianglifyView;
    private SeekBar varianceSeekBar;
    private SeekBar cellSizeSeekBar;
    private SeekBar paletteSeekBar;
    private CheckBox strokeCheckBox;
    private CheckBox fillCheckBox;
    private CheckBox randomColoringCheckbox;
    private CheckBox customPaletteCheckbox;
    private Palette customPalette;
    private final int PERMISSION_CODE = 123;

    private ActivityResultLauncher<Intent> customPalettePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customPalettePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra(getResources().getString(R.string.palette_color_array))) {
                            int[] colors = data.getIntArrayExtra(getResources().getString(R.string.palette_color_array));
                            if (colors != null) {
                                customPalette = new Palette(colors);
                                if (customPaletteCheckbox.isChecked()) {
                                    trianglifyView.setPalette(customPalette);
                                    trianglifyView.smartUpdate();
                                }
                            }
                        }
                    }
                }
        );

        trianglifyView = findViewById(R.id.trianglify_main_view);
        trianglifyView.setBitmapQuality(TrianglifyView.DRAWING_CACHE_QUALITY_HIGH);

        customPalette = trianglifyView.getPalette();

        varianceSeekBar = findViewById(R.id.variance_seekbar);
        varianceSeekBar.setMax(100);
        varianceSeekBar.setProgress(trianglifyView.getVariance());
        varianceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trianglifyView.setVariance(progress + 1);
                trianglifyView.smartUpdate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cellSizeSeekBar = findViewById(R.id.cell_size_seekbar);
        int maxCellSize = 150;

        cellSizeSeekBar.setMax(maxCellSize);
        cellSizeSeekBar.setProgress(trianglifyView.getCellSize() - 100);
        cellSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trianglifyView.setCellSize(progress + 100);
                trianglifyView.smartUpdate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        paletteSeekBar = findViewById(R.id.palette_seekbar);
        paletteSeekBar.setMax(Palette.DEFAULT_PALETTE_COUNT - 1);
        paletteSeekBar.setProgress(Palette.indexOf(trianglifyView.getPalette()));
        paletteSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trianglifyView.setPalette(Palette.getPalette(progress));
                customPaletteCheckbox.setChecked(false);
                trianglifyView.smartUpdate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        strokeCheckBox = findViewById(R.id.draw_stroke_checkbox);
        strokeCheckBox.setChecked(trianglifyView.isDrawStrokeEnabled());
        strokeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked || trianglifyView.isFillTriangle()) {
                trianglifyView.setDrawStrokeEnabled(isChecked);
                strokeCheckBox.setChecked(isChecked);
                trianglifyView.smartUpdate();
            } else {
                strokeCheckBox.setChecked(true);
                showColoringError();
            }
        });

        fillCheckBox = findViewById(R.id.draw_fill_checkbox);
        fillCheckBox.setChecked(trianglifyView.isFillTriangle());
        fillCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked || trianglifyView.isDrawStrokeEnabled()) {
                trianglifyView.setFillTriangle(isChecked);
                fillCheckBox.setChecked(isChecked);
                trianglifyView.smartUpdate();
            } else {
                fillCheckBox.setChecked(true);
                showColoringError();
            }
        });

        randomColoringCheckbox = findViewById(R.id.random_coloring_checkbox);
        randomColoringCheckbox.setChecked(trianglifyView.isRandomColoringEnabled());
        randomColoringCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            trianglifyView.setRandomColoring(isChecked);
            trianglifyView.smartUpdate();
        });

        customPaletteCheckbox = findViewById(R.id.custom_palette_checkbox);
        customPaletteCheckbox.setChecked(false);
        customPaletteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                trianglifyView.setPalette(customPalette);
                trianglifyView.smartUpdate();
            } else {
                trianglifyView.setPalette(Palette.getPalette(paletteSeekBar.getProgress()));
                trianglifyView.smartUpdate();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void updateUIElements(TrianglifyView trianglifyView) {
        fillCheckBox.setChecked(trianglifyView.isFillTriangle());
        strokeCheckBox.setChecked(trianglifyView.isDrawStrokeEnabled());
        randomColoringCheckbox.setChecked(trianglifyView.isRandomColoringEnabled());

        varianceSeekBar.setProgress(trianglifyView.getVariance());
        cellSizeSeekBar.setProgress(trianglifyView.getCellSize());
        paletteSeekBar.setProgress(Palette.indexOf(trianglifyView.getPalette()));
    }

    public void randomizeTrianglifyParameters(TrianglifyView trianglifyView) {
        Random rnd = new Random(System.currentTimeMillis());
        trianglifyView.
                setCellSize(Utilities.dpToPx(rnd.nextInt(10) + 35, this))
                .setPalette(Palette.getPalette(rnd.nextInt(28))).setRandomColoring(rnd.nextInt(2) == 0)
                .setFillTriangle(rnd.nextInt(2) == 0)
                .setDrawStrokeEnabled(rnd.nextInt(2) == 0)
                .setVariance(rnd.nextInt(60));

        if (!trianglifyView.isFillTriangle() && !trianglifyView.isDrawStrokeEnabled()) {
            trianglifyView.setDrawStrokeEnabled(true);
            trianglifyView.setFillTriangle(true);
        }

        updateUIElements(trianglifyView);
    }

    // Click handlers for action bar menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            try {
                exportImage();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Storage access failed!", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.action_about) {
            Intent aboutActivityIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutActivityIntent);
        } else if (id == R.id.action_refresh) {
            randomizeTrianglifyParameters(trianglifyView);
            trianglifyView.generateAndInvalidate();
        } else if (id == R.id.custom_palette_picker) {
            Intent customPalettePickerIntent = new Intent(this, CustomPalettePickerActivity.class);
            customPalettePickerIntent.putExtra(getResources().getString(R.string.palette_color_array), trianglifyView.getPalette().getColors());
            customPalettePickerLauncher.launch(customPalettePickerIntent);
            customPaletteCheckbox.setChecked(true);
        } else if (id == R.id.action_set_wall) {
            setWallpaper(MainActivity.this.trianglifyView);
        }

        return true;
    }

    public void showColoringError() {
        Toast.makeText(this, "View should at least be set to draw strokes or fill triangles or both.", Toast.LENGTH_LONG).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.hasExtra(CustomPalettePickerActivity.CUSTOM_PALETTE_COLOR_ARRAY)) {
            int[] colors = data.getIntArrayExtra(CustomPalettePickerActivity.CUSTOM_PALETTE_COLOR_ARRAY);
            if (colors == null) return;
            customPalette = new Palette(colors);
            if (customPaletteCheckbox.isChecked()) {
                trianglifyView.setPalette(customPalette);
                trianglifyView.smartUpdate();
            }
        }
    }

    private void exportImage() throws IOException {
        // Checks if permission is required for android version > 6
        boolean permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        if (permissionStatus) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            Bitmap bitmap = trianglifyView.getBitmap();
            if (bitmap != null)
                //addImageToGallery(bitmap, this);
                Toast.makeText(this, "Feature removed", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Unable to generate image, please try again", Toast.LENGTH_LONG).show();
        }
    }

    public static void addImageToGallery(Bitmap bitmap, Context context) throws IOException {
        String timeStamp = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";
        OutputStream os = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            os = Files.newOutputStream(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + timeStamp));
        }
        if (os != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    // Sets bitmap from trianglify view as wallpaper of device
    public void setWallpaper(final TrianglifyView view) {
        AlertDialog.Builder alertDgBuilder = new AlertDialog.Builder(this);
        alertDgBuilder.setMessage(getString(R.string.wall_alert_dg_text));
        alertDgBuilder.setPositiveButton("Yes", (dialog, which) -> {
            WallpaperManager trianglifyWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                trianglifyWallpaperManager.setBitmap(view.getBitmap());
                Toast.makeText(MainActivity.this, "Wallpaper set successfuly", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
            }
        });
        alertDgBuilder.setNegativeButton("No", (dialog, which) -> {
            // Perform inbuilt functions
        });

        alertDgBuilder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    exportImage();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Storage access failed!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Storage access failed, check permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}
