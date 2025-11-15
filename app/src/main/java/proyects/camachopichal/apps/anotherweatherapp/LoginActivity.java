package proyects.camachopichal.apps.anotherweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    // Declaramos los botones
    Button btnContinuar;
    Button btnIrARegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Conectamos los botones del layout con el código
        btnIrARegistro = findViewById(R.id.btnIrARegistro);
        btnContinuar = findViewById(R.id.btnContinuar);

        // --- Lógica para ir a la pantalla de Registro ---
        btnIrARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos un "Intent" para iniciar la nueva actividad
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Esto te mandará a la pantalla principal de la app
        btnContinuar.setOnClickListener(new View.OnClickListener() {
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