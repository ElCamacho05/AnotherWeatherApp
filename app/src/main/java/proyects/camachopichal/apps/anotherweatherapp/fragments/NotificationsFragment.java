package proyects.camachopichal.apps.anotherweatherapp.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Importa el R de tu paquete principal
import proyects.camachopichal.apps.anotherweatherapp.R;

public class NotificationsFragment extends Fragment {

    public NotificationsFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // "Infla" (conecta) el layout XML con este archivo Java
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Aquí puedes encontrar los elementos de esta pantalla (como el CalendarView)
        // Ejemplo: CalendarView calendarView = view.findViewById(R.id.calendarView);

        return view;
    }
}
