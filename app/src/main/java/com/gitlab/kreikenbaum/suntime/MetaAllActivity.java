package com.gitlab.kreikenbaum.suntime;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/** sets up menu */
public class MetaAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.all_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.plus:
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                try {
                    startActivity(new Intent(intent)
                                  .setPackage("com.android.vending"));
                } catch (android.content.ActivityNotFoundException exception) {
                    startActivity(intent);
                }
                return true;
            case R.id.better:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/kreikenbaum/suntime-app/issues")));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
