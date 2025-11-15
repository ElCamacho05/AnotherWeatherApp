package proyects.camachopichal.apps.anotherweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    // Declaración del objeto Binding
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el layout y obtener la instancia de binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());

        // 2. Establecer el root view como el contenido de la actividad
        setContentView(binding.getRoot());

        // --- Lógica para crear cuenta y volver al Login ---
        // Acceso directo a la vista con "binding.idDeLaVista"
        binding.btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Lógica para guardar los datos del usuario en la base de datos )


                // 2. Después de guardar, cerramos esta actividad.
                //    Llamar a finish() simplemente cierra la pantalla actual
                //    y regresa al usuario a la pantalla anterior (LoginActivity).

                finish();
            }
        });
    }
}