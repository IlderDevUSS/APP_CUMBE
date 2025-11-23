package com.example.app_cumbe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.app_cumbe.databinding.ActivityHomeBinding;
import com.google.android.material.navigation.NavigationBarView;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment ticketsFragment = new TicketsFragment();
    private final Fragment servicesFragment = new ServicesFragment();
    private final Fragment profileFragment = new ProfileFragment();

    // --- VARIABLES PARA EL TIMEOUT ---
    private Handler handler;
    private Runnable logoutRunnable;
    private static final long TIEMPO_INACTIVIDAD = 7 * 60 * 1000; // 7 minutos

    // Variable para guardar el momento de la última interacción
    private long lastInteractionTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializamos la última interacción al momento de crear
        lastInteractionTime = System.currentTimeMillis();
        initInactivityTimer();

        loadFragment(homeFragment);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    loadFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.navigation_tickets) {
                    loadFragment(ticketsFragment);
                    return true;
                } else if (itemId == R.id.navigation_services) {
                    loadFragment(servicesFragment);
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    loadFragment(profileFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void initInactivityTimer() {
        handler = new Handler(Looper.getMainLooper());
        logoutRunnable = () -> {
            Toast.makeText(HomeActivity.this, "Sesión cerrada por inactividad", Toast.LENGTH_LONG).show();
            cerrarSesion();
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Cada vez que tocas, actualizamos la hora y reiniciamos el timer visual
        lastInteractionTime = System.currentTimeMillis();
        resetInactivityTimer();
        return super.dispatchTouchEvent(ev);
    }

    private void startInactivityTimer() {
        handler.postDelayed(logoutRunnable, TIEMPO_INACTIVIDAD);
    }

    private void stopInactivityTimer() {
        handler.removeCallbacks(logoutRunnable);
    }

    private void resetInactivityTimer() {
        stopInactivityTimer();
        startInactivityTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // VERIFICACIÓN CRÍTICA AL VOLVER A LA APP
        long currentTime = System.currentTimeMillis();
        long tiempoTranscurrido = currentTime - lastInteractionTime;

        if (tiempoTranscurrido >= TIEMPO_INACTIVIDAD) {
            // Si pasó más de 7 min desde que la usaste por última vez (incluso en background)
            Toast.makeText(this, "Tu sesión ha expirado por inactividad", Toast.LENGTH_LONG).show();
            cerrarSesion();
        } else {
            // Si aún es válido, arrancamos el timer por el tiempo restante o reiniciamos
            startInactivityTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopInactivityTimer();
        // No borramos lastInteractionTime aquí, para que persista en memoria mientras la app vive en background
    }

    /**
     * Para cerrar sesión al matar la app, usaremos onDestroy.
     * Sin embargo, Android a veces mata el proceso sin llamar a onDestroy.
     * La forma más segura es NO guardar el estado de "Logueado" permanentemente si quieres
     * que al cerrar la app se vaya la sesión, PERO eso contradice el "Refresh Token" de 30 días.
     * * Si quieres que AL CERRAR LA APP (Matarla) se cierre la sesión:
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInactivityTimer();

        // Si el usuario está cerrando la actividad explícitamente (no rotación de pantalla)
        if (isFinishing()) {
            borrarDatosSesion();
        }
    }

    public void cerrarSesion() {
        borrarDatosSesion();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void borrarDatosSesion() {
        stopInactivityTimer();
        SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}