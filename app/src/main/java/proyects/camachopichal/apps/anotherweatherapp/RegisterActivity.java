package proyects.camachopichal.apps.anotherweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

public class RegisterActivity extends AppCompatActivity {

    Button btnCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de tener el layout "activity_register.xml" en res/layout
        setContentView(R.layout.activity_register);

        // Botón en activity_register.xml se llama "btnCrearCuenta"
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        // --- Lógica para crear cuenta y volver al Login ---
        btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
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