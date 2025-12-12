package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

// fragmentos de interfaz
import proyects.camachopichal.apps.anotherweatherapp.R;
import proyects.camachopichal.apps.anotherweatherapp.activities.fragments.HomeFragment;
import proyects.camachopichal.apps.anotherweatherapp.activities.fragments.NotificationsFragment;
import proyects.camachopichal.apps.anotherweatherapp.activities.fragments.ProfileFragment;
import proyects.camachopichal.apps.anotherweatherapp.activities.fragments.PublicationsFragment;

import com.google.android.material.navigation.NavigationBarView;
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Carga Home por default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Configura el listener
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Instanciamos el fragmento con 'new' cada vez.
                if (itemId == R.id.nav_home) {
                    loadFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.nav_publications){
                    loadFragment(new PublicationsFragment());
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    loadFragment(new NotificationsFragment());
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }

                return false;
            }
        });
    }

    // Metodo para cargar/reemplazar un fragmento en el contenedor
    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }
}