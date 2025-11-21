package com.example.app_cumbe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.app_cumbe.databinding.ActivityHomeBinding;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    // Instancias de nuestros fragmentos
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Cargar el fragmento Home por defecto al iniciar
        loadFragment(homeFragment);

        // 2. Configurar el Listener de la Barra Inferior
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    loadFragment(homeFragment);
                    return true;
                } else if (item.getItemId() == R.id.navigation_profile) {
                    loadFragment(profileFragment);
                    return true;
                }
                return false;
            }
        });
    }

    // Método para reemplazar el fragmento visible
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}