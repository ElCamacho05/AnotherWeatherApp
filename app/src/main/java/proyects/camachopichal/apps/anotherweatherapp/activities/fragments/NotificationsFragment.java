package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import proyects.camachopichal.apps.anotherweatherapp.R;
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentNotificationsBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public NotificationsFragment() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 1. Mostrar la fecha de hoy al iniciar
        Calendar hoy = Calendar.getInstance();
        actualizarVistaFecha(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));

        // 2. Listener para el botón "+ Nuevo" (Abre el diálogo flotante)
        binding.btnNuevoEvento.setOnClickListener(v -> mostrarDialogoNuevoEvento());

        // 3. Listener para cambios en el Calendario
        binding.calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            actualizarVistaFecha(year, month, dayOfMonth);
        });

        return view;
    }

    /**
     * Actualiza los textos de la interfaz y consulta la base de datos
     */
    private void actualizarVistaFecha(int year, int month, int dayOfMonth) {
        // Actualizar el título de la fecha (Ej: "7 de Noviembre 2025")
        String fechaTexto = dayOfMonth + " de " + obtenerNombreMes(month) + " " + year;
        binding.tvFechaSeleccionada.setText(fechaTexto);

        // Poner mensaje de carga
        binding.tvListaEventos.setText("Cargando eventos...");

        // Consultar a Firebase
        consultarEventosPorFecha(year, month, dayOfMonth);
    }

    /**
     * Consulta Firestore y actualiza el TextView de abajo directamente.
     */
    private void consultarEventosPorFecha(int year, int month, int day) {
        if (mAuth.getCurrentUser() == null) {
            binding.tvListaEventos.setText("Debes iniciar sesión para ver tus eventos.");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        // Rango de fecha (00:00 a 23:59 del día seleccionado)
        Calendar inicioDia = Calendar.getInstance();
        inicioDia.set(year, month, day, 0, 0, 0);
        Date fechaInicio = inicioDia.getTime();

        Calendar finDia = Calendar.getInstance();
        finDia.set(year, month, day, 23, 59, 59);
        Date fechaFin = finDia.getTime();

        db.collection("usuario").document(uid).collection("Eventos")
                .whereGreaterThanOrEqualTo("Fecha_Evento", fechaInicio)
                .whereLessThanOrEqualTo("Fecha_Evento", fechaFin)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // CASO 1: No hay eventos
                            binding.tvListaEventos.setText("No hay eventos para este día");
                        } else {
                            // CASO 2: Sí hay eventos -> Construimos la lista
                            StringBuilder listaBuilder = new StringBuilder();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String titulo = document.getString("Titulo");
                                String ubicacion = document.getString("Ubicacion");
                                String nota = document.getString("Nota");

                                listaBuilder.append("📍 ").append(titulo);
                                if (ubicacion != null && !ubicacion.isEmpty()) {
                                    listaBuilder.append(" en ").append(ubicacion);
                                }
                                if (nota != null && !nota.isEmpty()) {
                                    listaBuilder.append("\n   Nota: ").append(nota);
                                }
                                listaBuilder.append("\n\n"); // Espacio entre eventos
                            }
                            binding.tvListaEventos.setText(listaBuilder.toString().trim());
                        }
                    } else {
                        Log.e("Firestore", "Error al consultar", task.getException());
                        binding.tvListaEventos.setText("Error al cargar eventos. Verifica tu conexión.");
                    }
                });
    }

    private String obtenerNombreMes(int month) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[month];
    }

    // --- MÉTODOS PARA AGREGAR EVENTO (Mantenidos igual) ---

    private void mostrarDialogoNuevoEvento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        EditText etTitulo = dialogView.findViewById(R.id.etTituloEvento);
        TextView tvFecha = dialogView.findViewById(R.id.tvFechaEvento);
        EditText etUbicacion = dialogView.findViewById(R.id.etUbicacionEvento);
        EditText etNota = dialogView.findViewById(R.id.etNotaEvento);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarEvento);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarEvento);

        final long[] fechaSeleccionadaMillis = {0};

        tvFecha.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        tvFecha.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                        Calendar calendarSeleccionado = Calendar.getInstance();
                        calendarSeleccionado.set(year1, monthOfYear, dayOfMonth);
                        fechaSeleccionadaMillis[0] = calendarSeleccionado.getTimeInMillis();
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnGuardar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString();
            String ubicacion = etUbicacion.getText().toString();
            String nota = etNota.getText().toString();

            if (titulo.isEmpty() || fechaSeleccionadaMillis[0] == 0) {
                Toast.makeText(getContext(), "El título y la fecha son obligatorios", Toast.LENGTH_SHORT).show();
            } else {
                guardarEventoEnFirebase(titulo, fechaSeleccionadaMillis[0], ubicacion, nota);
                dialog.dismiss();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void guardarEventoEnFirebase(String titulo, long fechaMillis, String ubicacion, String nota) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> evento = new HashMap<>();
        evento.put("Titulo", titulo);
        evento.put("Fecha_Evento", new Date(fechaMillis));
        evento.put("Ubicacion", ubicacion);
        evento.put("Nota", nota);
        evento.put("Fecha_Creacion", new Date());

        db.collection("Usuario").document(uid).collection("Eventos")
                .add(evento)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Evento guardado", Toast.LENGTH_SHORT).show();
                    // Opcional: Recargar la vista del día actual si coincide con la fecha guardada
                    // Pero como el usuario puede guardar una fecha futura, mejor dejar que él navegue.
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}