package com.example.projetoindividual.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projetoindividual.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    private TimePicker timePicker;
    private LinearLayout containerDays;
    private Button buttonAddDay;
    private SharedPreferences prefs;
    private ArrayList<Integer> diasAntes = new ArrayList<>();

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireContext().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        timePicker = view.findViewById(R.id.time_picker);
        containerDays = view.findViewById(R.id.container_days);
        buttonAddDay = view.findViewById(R.id.button_add_day);


        // Carregar preferências
        carregarPreferencias();

        buttonAddDay.setOnClickListener(v -> adicionarCampoDia(0));

        return view;
    }


    private void adicionarCampoDia(int valorInicial) {
        EditText editDia = new EditText(getContext());
        editDia.setHint("Dias antes (ex: 1)");
        editDia.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editDia.setTextColor(getResources().getColor(android.R.color.black));
        editDia.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        editDia.setPadding(16, 16, 16, 16);
        editDia.setBackgroundResource(android.R.drawable.edit_text);
        if (valorInicial > 0) editDia.setText(String.valueOf(valorInicial));

        containerDays.addView(editDia);

        // Guardar sempre que algo muda
        editDia.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) guardarPreferencias();
        });
    }

    private void guardarPreferencias() {
        try {
            JSONObject json = new JSONObject();

            // Guardar hora
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            json.put("hora", String.format("%02d:%02d", hour, minute));

            // Guardar dias antes
            JSONArray arrayDias = new JSONArray();
            diasAntes.clear();
            for (int i = 0; i < containerDays.getChildCount(); i++) {
                View v = containerDays.getChildAt(i);
                if (v instanceof EditText) {
                    String text = ((EditText) v).getText().toString().trim();
                    if (!text.isEmpty()) {
                        arrayDias.put(Integer.parseInt(text));
                    }
                }
            }
            json.put("dias", arrayDias);

            // Salvar em SharedPreferences
            prefs.edit().putString("notificacao_config", json.toString()).apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void carregarPreferencias() {
        String jsonStr = prefs.getString("notificacao_config", null);
        if (jsonStr != null) {
            try {
                JSONObject json = new JSONObject(jsonStr);
                String hora = json.getString("hora");
                JSONArray dias = json.getJSONArray("dias");

                String[] partes = hora.split(":");
                int hour = Integer.parseInt(partes[0]);
                int minute = Integer.parseInt(partes[1]);
                timePicker.setHour(hour);
                timePicker.setMinute(minute);

                for (int i = 0; i < dias.length(); i++) {
                    adicionarCampoDia(dias.getInt(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // valor padrão
            timePicker.setHour(9);
            timePicker.setMinute(0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        guardarPreferencias();
    }
}
