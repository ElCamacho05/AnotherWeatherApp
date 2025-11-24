package proyects.camachopichal.apps.anotherweatherapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;

// Importa el View Binding de Login
import proyects.camachopichal.apps.anotherweatherapp.R;
import proyects.camachopichal.apps.anotherweatherapp.databinding.ActivityLoginBinding;

// Importaciones de Google Sign-In
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

// Importaciones de Firebase
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    private static final String TAG = "GoogleAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- CONFIGURACIÓN CRÍTICA DE GSO ---
        String webClientId = getString(R.string.default_web_client_id);

        if (webClientId.isEmpty()) {
            // Este Toast se mostrará si default_web_client_id es nulo (indicando un fallo en google-services.json)
            Toast.makeText(this, "ERROR DE CONFIGURACIÓN: default_web_client_id es nulo.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "FATAL ERROR: default_web_client_id is empty. Check google-services.json and SHA keys.");
            // Impedimos que continúe la configuración de Google
            return;
        }

        // --- 1. Configurar Google Sign-In Options (GSO) ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId) // Usamos la clave web obtenida
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- 2. Listeners de Botones ---

        // A. Listener para el botón "Continuar con Google"
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // B. Listener para el botón "Crea una cuenta" (Registro)
        binding.btnIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // C. Listener para el botón "Continuar" (Login con Email/Password)
        binding.btnContinuar.setOnClickListener(v -> {
            String email = binding.etEmailLogin.getText().toString();
            String password = binding.etPasswordLogin.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Ingrese email y contraseña.", Toast.LENGTH_SHORT).show();
                return;
            }
            iniciarSesionEmail(email, password);
        });

        // Verifica si ya hay un usuario activo para omitir el login
        if (mAuth.getCurrentUser() != null) {
            navegarAHome();
        }
    }

    // --- MÉTODOS DE GOOGLE SIGN-IN ---

    // 1. Inicia el intento para abrir la ventana de Google
    private void signInWithGoogle() {
        // Asegúrate de que mGoogleSignInClient no sea nulo antes de usarlo (por la verificación de onCreate)
        if (mGoogleSignInClient != null) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        }
    }

    // 2. Maneja el resultado de la ventana de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    // Si la cuenta de Google se obtiene con éxito, extraemos el token
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "Auth de Google exitosa, token obtenido.");

                    // Llama a la autenticación de Firebase
                    firebaseAuthWithGoogle(account.getIdToken());

                } catch (ApiException e) {
                    // Maneja errores de Google Sign-In, incluido el 12500 (misconfiguration)
                    Log.e(TAG, "Fallo en Google Sign-In: Código " + e.getStatusCode() + " - Configuración de certificados.", e);
                    Toast.makeText(LoginActivity.this, "Fallo en Google Sign-In: Código " + e.getStatusCode() + " (Error de certificado SHA-1).", Toast.LENGTH_LONG).show();
                }
            });

    // 3. Usa el token de Google para crear/iniciar sesión en Firebase Auth
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Autenticación exitosa en Firebase
                        String uid = mAuth.getCurrentUser().getUid();
                        String email = mAuth.getCurrentUser().getEmail();
                        String nombre = mAuth.getCurrentUser().getDisplayName();

                        // Lógica para guardar datos de perfil en Firestore si es la primera vez
                        verificarYGuardarUsuario(uid, email, nombre);

                    } else {
                        Toast.makeText(LoginActivity.this, "Error de autenticación con Firebase.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Método para verificar si el usuario es nuevo y guardar el perfil en Firestore
    private void verificarYGuardarUsuario(String uid, String email, String nombre) {
        db.collection("Usuario").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            // El usuario NO existe en Firestore, lo creamos

                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("uid", uid);
                            usuario.put("Email", email);
                            usuario.put("Telefono", "");
                            usuario.put("fecha_registro", new Date());

                            // Estructura anidada para Nombre_Completo
                            Map<String, Object> nombreCompletoMap = new HashMap<>();
                            nombreCompletoMap.put("Nombre", nombre != null ? nombre : "");
                            nombreCompletoMap.put("ApPaterno", "");
                            nombreCompletoMap.put("ApMaterno", "");

                            usuario.put("Nombre_Completo", nombreCompletoMap);

                            // Guardamos con el UID como ID del documento
                            db.collection("Usuario").document(uid).set(usuario)
                                    .addOnCompleteListener(taskDB -> {
                                        if (taskDB.isSuccessful()) {
                                            Log.i(TAG, "Perfil de Google guardado en DB.");
                                            navegarAHome();
                                        } else {
                                            Log.e(TAG, "Fallo al guardar perfil de Google en DB", taskDB.getException());
                                            Toast.makeText(this, "Fallo al guardar perfil de Google.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            // El usuario ya existe en Firestore, solo navegamos
                            navegarAHome();
                        }
                    }
                });
    }

    // Método de navegación a la actividad principal
    private void navegarAHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Método para manejar el login de Email/Password
    private void iniciarSesionEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        navegarAHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de credenciales.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}