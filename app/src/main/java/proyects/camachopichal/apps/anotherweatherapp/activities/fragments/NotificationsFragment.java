package proyects.camachopichal.apps.anotherweatherapp.activities.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Importa la clase de View Binding generada
import proyects.camachopichal.apps.anotherweatherapp.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public NotificationsFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflar el layout usando el objeto Binding
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        // 2. Obtener la vista raíz (root view)
        View view = binding.getRoot();

        // Aquí puedes encontrar los elementos de esta pantalla (como el CalendarView)
        // Ejemplo: CalendarView calendarView = binding.calendarView;
        // Solo si tu layout fragment_notifications.xml tiene un CalendarView con ID 'calendarView'.

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // IMPORTANTE: Limpiar la referencia del binding para evitar fugas de memoria
        binding = null;
    }
}