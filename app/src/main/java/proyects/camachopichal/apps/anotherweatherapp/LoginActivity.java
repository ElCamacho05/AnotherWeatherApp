package proyects.camachopichal.apps.anotherweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    // Declaración del objeto Binding
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el layout y obtener la instancia de binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        // 2. Establecer el root view como el contenido de la actividad
        setContentView(binding.getRoot());

        // --- Lógica para ir a la pantalla de Registro ---
        // Acceso directo a la vista con "binding.idDeLaVista"
        binding.btnIrARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos un "Intent" para iniciar la nueva actividad
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Esto te mandará a la pantalla principal de la app
        binding.btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica de validación de email/contraseña

                // Creamos el Intent para ir a la MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                // Estas líneas evitan que el usuario pueda "regresar" a la pantalla de login
                // después de haber entrado.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            }
        });

        // Para la lógica para los botones de Google y Apple
    }
}