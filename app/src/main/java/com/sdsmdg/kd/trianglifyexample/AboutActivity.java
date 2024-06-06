package com.sdsmdg.kd.trianglifyexample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    ImageView githubLinkBtn, reviewLinkBtn, shareLink, backBtn;
    TextView fragTitle, openSourceLicense, versiontTextView;
    View bottomMarginLayout;
    PackageInfo pInfo;
    String versionName;
    int versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        // To add underline effect in open source license textView
        SpannableString content = new SpannableString("view license");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

        // Gets version and build number from package manager
        try {
            pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.about_activity_title));
        }

        try {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (java.lang.NullPointerException e) {
            Log.e(TAG, "Null pointer exception in generating back action button");
        }

        versiontTextView = this.findViewById(R.id.about_version_text);

        openSourceLicense = this.findViewById(R.id.about_license_text);
        openSourceLicense.setText(content);

        githubLinkBtn = this.findViewById(R.id.about_github_link);
        reviewLinkBtn = this.findViewById(R.id.about_rate_link);
        shareLink = this.findViewById(R.id.about_share_link);


        versiontTextView.setText(getString(R.string.about_activity_version) + versionName);
        openSourceLicense.setOnClickListener(v -> displayOpenSourceLicenses());

        githubLinkBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://github.com/sdsmdg/trianglify");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        reviewLinkBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=" + v.getContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + v.getContext().getPackageName())));
            }
        });

        //TODO: Update link when app releases to marketplace

        shareLink.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_application_text) +
                    " " + getResources().getString(R.string.trianglify_store_short_link));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });
    }

    public void displayOpenSourceLicenses() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(this.getApplicationInfo().name);
        alertDialog.setMessage(getResources().getString(R.string.license_text));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "view license", (dialog, which) -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(getResources().getString(R.string.license_link)))));
        alertDialog.show();
    }

    // Sets action for Action Bar Items
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
