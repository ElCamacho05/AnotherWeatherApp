package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import proyects.camachopichal.apps.anotherweatherapp.activities.HourlyForecastActivity;
import proyects.camachopichal.apps.anotherweatherapp.activities.MapSelectionActivity;
import proyects.camachopichal.apps.anotherweatherapp.adapters.SavedLocationsAdapter;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.OpenWeatherAPIManager;
import proyects.camachopichal.apps.anotherweatherapp.database.Weather.WeatherObject;
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentProfileBinding;
// Importamos el binding generado para el diálogo (asegúrate de que el layout se llame dialog_save_location.xml)
import proyects.camachopichal.apps.anotherweatherapp.databinding.DialogSaveLocationBinding;
import proyects.camachopichal.apps.anotherweatherapp.activities.LoginActivity;
import proyects.camachopichal.apps.anotherweatherapp.models.SavedLocation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SavedLocationsAdapter adapter;
    private List<SavedLocation> locationList;

    // Launcher para recibir coordenadas del mapa
    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("selected_lat", 0.0);
                    double lon = result.getData().getDoubleExtra("selected_lon", 0.0);
                    mostrarDialogoGuardarUbicacion(lat, lon);
                }
            });

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cargarEmailUsuario();
        configurarRecyclerView();
        cargarUbicacionesGuardadas();

        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesionUsuario());
        binding.btnAddLocation.setOnClickListener(v -> abrirMapaParaNuevaUbicacion());
    }

    private void cargarEmailUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            binding.tvUserEmail.setText(email != null ? email : "Email no disponible");
        }
    }

    private void configurarRecyclerView() {
        locationList = new ArrayList<>();
        adapter = new SavedLocationsAdapter(getContext(), locationList, new SavedLocationsAdapter.OnLocationActionListener() {
            @Override
            public void onEdit(SavedLocation location) {
                mostrarDialogoEditar(location);
            }

            @Override
            public void onDelete(SavedLocation location) {
                eliminarUbicacion(location);
            }

            @Override
            public void onClick(SavedLocation location) {
                // mostrar detalles del clima (actual) en esa ubicacion
                Intent intent = new Intent(getContext(), HourlyForecastActivity.class);
                intent.putExtra(HourlyForecastActivity.EXTRA_LAT, location.getLatitud());
                intent.putExtra(HourlyForecastActivity.EXTRA_LON, location.getLongitud());
                intent.putExtra(HourlyForecastActivity.EXTRA_DAY_TITLE, "HOY");
                startActivity(intent);
            }
        });
        binding.rvSavedLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSavedLocations.setAdapter(adapter);
    }

    // CARGA DE DATOS

    private void cargarUbicacionesGuardadas() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuario").document(uid).collection("ubicaciones")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        locationList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            SavedLocation loc = doc.toObject(SavedLocation.class);
                            loc.setId(doc.getId());
                            locationList.add(loc);
                        }
                        // obtenemos el clima para cada una de las ubicaciones
                        actualizarClimaParaLaLista();
                    }
                });
    }

    /**
     * Itera sobre la lista y pide el clima a la API para cada ubicacion
     */
    private void actualizarClimaParaLaLista() {
        for (SavedLocation loc : locationList) {
            OpenWeatherAPIManager.getHourlyForecastReal(loc.getLatitud(), loc.getLongitud(), new OpenWeatherAPIManager.APIResponseCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    try {
                        List<WeatherObject> forecast = OpenWeatherAPIManager.parseHourlyForecast(jsonResponse);
                        if (!forecast.isEmpty()) {
                            WeatherObject current = forecast.get(0);
                            // Actualizamos el objeto en memoria
                            loc.setCurrentTemp(String.valueOf(current.getTempCelsius()));
                            loc.setCurrentIcon(current.getIconCode());

                            // Notificamos al adaptador (UI Thread)
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                @Override
                public void onFailure(String errorMessage) {}
            });
        }
        // Notificar cambio inicial para mostrar la lista aunque sea sin clima aun
        adapter.notifyDataSetChanged();
    }

    // AGREGAR / EDITAR / ELIMINAR

    private void abrirMapaParaNuevaUbicacion() {
        // Usa la misma actividad de mapa que el Home
        Intent intent = new Intent(getContext(), MapSelectionActivity.class);
        // Opcional: pasar ubicación actual inicial
        mapLauncher.launch(intent);
    }

    private void mostrarDialogoGuardarUbicacion(double lat, double lon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        DialogSaveLocationBinding dialogBinding = DialogSaveLocationBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Pre llenado de direccion con Geocoder
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (!addresses.isEmpty()) {
                String addressStr = addresses.get(0).getAddressLine(0);
                dialogBinding.etLocationDesc.setText(addressStr);
            }
        } catch (IOException e) { e.printStackTrace(); }

        dialogBinding.btnConfirmSave.setOnClickListener(v -> {
            String titulo = dialogBinding.etLocationTitle.getText().toString();
            String desc = dialogBinding.etLocationDesc.getText().toString();

            if (titulo.isEmpty()) {
                Toast.makeText(getContext(), "Ingresa un título", Toast.LENGTH_SHORT).show();
                return;
            }

            guardarEnFirestore(titulo, desc, lat, lon);
            dialog.dismiss();
        });

        dialogBinding.btnCancelSave.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoEditar(SavedLocation location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        DialogSaveLocationBinding dialogBinding = DialogSaveLocationBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogBinding.tvDialogTitle.setText("Editar Ubicación");
        dialogBinding.etLocationTitle.setText(location.getTitulo());
        dialogBinding.etLocationDesc.setText(location.getDescripcion());

        // Listeners
        dialogBinding.btnConfirmSave.setOnClickListener(v -> {
            String titulo = dialogBinding.etLocationTitle.getText().toString();
            String desc = dialogBinding.etLocationDesc.getText().toString();

            if (titulo.isEmpty()) {
                Toast.makeText(getContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            actualizarEnFirestore(location.getId(), titulo, desc);
            dialog.dismiss();
        });

        dialogBinding.btnCancelSave.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- OPERACIONES FIRESTORE ---

    private void guardarEnFirestore(String titulo, String desc, double lat, double lon) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        String id = UUID.randomUUID().toString();

        SavedLocation newLoc = new SavedLocation(id, titulo, desc, lat, lon);

        db.collection("usuario").document(uid).collection("ubicaciones").document(id)
                .set(newLoc)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Ubicación guardada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    private void actualizarEnFirestore(String docId, String titulo, String desc) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuario").document(uid).collection("ubicaciones").document(docId)
                .update("titulo", titulo, "descripcion", desc)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Actualizado", Toast.LENGTH_SHORT).show());
    }

    private void eliminarUbicacion(SavedLocation location) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Ubicación")
                .setMessage("¿Estás seguro de eliminar '" + location.getTitulo() + "'?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("usuario").document(uid).collection("ubicaciones").document(location.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Eliminado", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cerrarSesionUsuario() {
        mAuth.signOut();
        GoogleSignIn.getClient(requireActivity(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}