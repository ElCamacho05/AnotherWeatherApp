package proyects.camachopichal.apps.anotherweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

// Importa tus fragmentos
import proyects.camachopichal.apps.anotherweatherapp.fragments.HomeFragment;
import proyects.camachopichal.apps.anotherweatherapp.fragments.NotificationsFragment;
import proyects.camachopichal.apps.anotherweatherapp.fragments.ProfileFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    // Crea instancias de tus tres fragmentos
    HomeFragment homeFragment = new HomeFragment();
    NotificationsFragment notificationsFragment = new NotificationsFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Conecta la barra de navegación del layout
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Carga el fragmento "Home" por defecto al abrir la app
        loadFragment(homeFragment);

        // Configura el listener para saber a qué ícono se le da clic
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Compara el ID del ítem seleccionado (definido en bottom_nav_menu.xml)
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    loadFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    loadFragment(notificationsFragment);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    loadFragment(profileFragment);
                    return true;
                }

                return false;
            }
        });
    }

    // Método para cargar (reemplazar) un fragmento en el contenedor
    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // R.id.fragment_container es el ID del FrameLayout en activity_main.xml
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }
}
