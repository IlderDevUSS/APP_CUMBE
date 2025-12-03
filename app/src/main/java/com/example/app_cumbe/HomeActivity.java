package com.example.app_cumbe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.databinding.ActivityHomeBinding;
import com.example.app_cumbe.model.db.AppDatabase;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    // Instancias de los fragmentos
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment ticketsFragment = new TicketsFragment();
    private final Fragment servicesFragment = new ServicesFragment();
    private final Fragment profileFragment = new ProfileFragment();

    private final Fragment notificationsFragment = new NotificationsFragment();
    // [CORRECCIÓN 1] Instanciamos el fragmento de conductor aquí
    private final Fragment driverRoutesFragment = new DriverRoutesFragment();
    private Handler handler;
    private Runnable logoutRunnable;
    private static final long TIEMPO_INACTIVIDAD = 7 * 60 * 1000;
    private long lastInteractionTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // [CORRECCIÓN 2] Inicializar ApiClient por seguridad si la app se restaura aquí
        ApiClient.init(this);
        programarNotificaciones();
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

                    // Verificamos el modo conductor
                    SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
                    boolean esConductorActivo = prefs.getBoolean("MODO_CONDUCTOR_ACTIVO", false);

                    if (esConductorActivo) {
                        // [CORRECCIÓN PRINCIPAL] Usar loadFragment, NO startActivity
                        loadFragment(driverRoutesFragment);

                    } else {
                        // Modo Cliente
                        loadFragment(ticketsFragment);
                    }
                    return true; // Retornamos true para que el icono se marque como seleccionado

                } else if (itemId == R.id.navigation_notifications) {
                    loadFragment(notificationsFragment);
                    limpiarBadgeNotificaciones();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    loadFragment(profileFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void programarNotificaciones() {
        // Configura el worker para que se ejecute cada 15 minutos (mínimo permitido por Android)
        // o cada hora para ahorrar batería.
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationsWorker.class,
                1, TimeUnit.HOURS) // Ejecutar cada 1 hora
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CheckViajes",
                ExistingPeriodicWorkPolicy.KEEP, // Si ya existe, no lo reemplaza (KEEP)
                workRequest
        );

        // TRUCO PARA PROBAR YA MISMO: Ejecutar una vez inmediatamente
        androidx.work.OneTimeWorkRequest oneTimeRequest =
                new androidx.work.OneTimeWorkRequest.Builder(NotificationsWorker.class).build();
        WorkManager.getInstance(this).enqueue(oneTimeRequest);
    }

    public void actualizarBadgeNotificaciones() {
        AppDatabase db = AppDatabase.getDatabase(this);
        int noLeidas = db.notificacionDao().contarNoLeidas();

        // Cuidado: getOrCreateBadge usa el ID del menú
        BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.navigation_notifications);

        if (noLeidas > 0) {
            badge.setVisible(true);
            badge.setNumber(noLeidas);
            badge.setBackgroundColor(getColor(R.color.color_cancelado)); // Rojo
        } else {
            badge.setVisible(false);
        }
    }

    private void limpiarBadgeNotificaciones() {
        BadgeDrawable badge = binding.bottomNavigation.getBadge(R.id.navigation_notifications);
        if (badge != null) {
            badge.setVisible(false);
            // badge.clearNumber(); // Opcional
        }
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
        lastInteractionTime = System.currentTimeMillis();
        resetInactivityTimer();
        return super.dispatchTouchEvent(ev);
    }

    private void startInactivityTimer() {
        if (handler != null && logoutRunnable != null) {
            handler.postDelayed(logoutRunnable, TIEMPO_INACTIVIDAD);
        }
    }

    private void stopInactivityTimer() {
        if (handler != null && logoutRunnable != null) {
            handler.removeCallbacks(logoutRunnable);
        }
    }

    private void resetInactivityTimer() {
        stopInactivityTimer();
        startInactivityTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        long currentTime = System.currentTimeMillis();
        long tiempoTranscurrido = currentTime - lastInteractionTime;
        actualizarBadgeNotificaciones();
        if (tiempoTranscurrido >= TIEMPO_INACTIVIDAD) {
            Toast.makeText(this, "Tu sesión ha expirado por inactividad", Toast.LENGTH_LONG).show();
            cerrarSesion();
        } else {
            startInactivityTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopInactivityTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInactivityTimer();
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