package com.pm1ucenm.tl01examen122630020_122630012;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.pm1ucenm.tl01examen122630020_122630012.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.FirstFragment) {
                binding.fab.setImageResource(R.drawable.ic_add);
            } else {
                binding.fab.setImageResource(R.drawable.ic_arrow_back);
            }
        });

        binding.fab.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            if (nav.getCurrentDestination() != null &&
                    nav.getCurrentDestination().getId() == R.id.FirstFragment) {
                nav.navigate(R.id.action_FirstFragment_to_SecondFragment);
            } else {
                nav.popBackStack();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
