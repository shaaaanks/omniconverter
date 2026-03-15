package com.omniconverter.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.omniconverter.app.ui.adapter.ConverterListAdapter;
import com.omniconverter.app.ui.fragment.ConverterDetailFragment;
import com.omniconverter.app.ui.fragment.ConverterListFragment;
import com.omniconverter.app.ui.fragment.HistoryFragment;

public class MainActivity extends AppCompatActivity implements ConverterListFragment.OnConverterSelectedListener {
    private ConverterListFragment converterListFragment;
    private HistoryFragment historyFragment;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            bottomNav = findViewById(R.id.bottom_navigation);

            if (savedInstanceState == null) {
                // Initialize fragments
                converterListFragment = new ConverterListFragment();
                converterListFragment.setListener(this);
                historyFragment = new HistoryFragment();

                // Show converter list by default
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, converterListFragment)
                    .commitNow();
            } else {
                converterListFragment = (ConverterListFragment) getSupportFragmentManager()
                    .findFragmentByTag("converter_list");
                historyFragment = (HistoryFragment) getSupportFragmentManager()
                    .findFragmentByTag("history");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error during onCreate: ", e);
            finish();
            return;
        }

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener(item -> {
            try {
                if (item.getItemId() == R.id.nav_converters) {
                    if (converterListFragment == null) {
                        converterListFragment = new ConverterListFragment();
                        converterListFragment.setListener(this);
                    }
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, converterListFragment, "converter_list")
                        .commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_history) {
                    if (historyFragment == null) {
                        historyFragment = new HistoryFragment();
                    }
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, historyFragment, "history")
                        .commit();
                    return true;
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error during navigation: ", e);
            }
            return false;
        });
    }

    @Override
    public void onConverterSelected(ConverterListAdapter.ConverterItem item) {
        ConverterDetailFragment detailFragment = ConverterDetailFragment.newInstance(item);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit();
    }
}
