package com.example.projetoindividual.ui.calendario;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projetoindividual.R;
import com.example.projetoindividual.database.FirebaseHelper;
import com.example.projetoindividual.model.Projeto;
import com.example.projetoindividual.model.Tarefa;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class CalendarioFragment extends Fragment {

    private MaterialCalendarView calendar;
    private LinearLayout containerTarefas;
    private List<Projeto> listaProjetos = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendario, container, false);

        calendar = root.findViewById(R.id.calendarView);
        containerTarefas = root.findViewById(R.id.containerTarefas);

        calendar.setCurrentDate(CalendarDay.today());
        calendar.setSelectedDate(CalendarDay.today());

        FirebaseHelper.getAllProjectsForCurrentUser((projetos, error) -> {
            if (error != null) return;

            listaProjetos = projetos;

            // Depuração
            for (Projeto projeto : listaProjetos) {
                int qtdTarefas = projeto.tarefas != null ? projeto.tarefas.size() : 0;
                Log.d("Calendario", "Projeto: " + projeto.nome + ", tarefas: " + qtdTarefas);
            }

            marcarDiasComTarefas();
        });

        calendar.setOnDateChangedListener((widget, date, selected) -> mostrarTarefasDoDia(date));

        return root;
    }

    private void mostrarTarefasDoDia(CalendarDay date) {
        containerTarefas.removeAllViews();

        for (Projeto projeto : listaProjetos) {
            if (projeto.tarefas == null) continue;

            for (Tarefa tarefa : projeto.tarefas) {
                if (tarefa.concluida) continue;
                if (tarefa.dataConclusao == null || tarefa.dataConclusao.isEmpty()) continue;

                String[] parts = tarefa.dataConclusao.split("-");
                if (parts.length != 3) continue;

                int ano, mes, dia;
                try {
                    ano = Integer.parseInt(parts[0]);
                    mes = Integer.parseInt(parts[1]); // 1-based do Firebase
                    dia = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    continue;
                }

                // Corrige mês: date.getMonth() é 0-based
                if (ano == date.getYear() && mes == (date.getMonth() + 1) && dia == date.getDay()) {
                    View card = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_tarefa, containerTarefas, false);

                    TextView tituloTarefa = card.findViewById(R.id.textTituloTarefa);
                    TextView nomeProjeto = card.findViewById(R.id.textNomeProjeto);

                    tituloTarefa.setText(tarefa.titulo);
                    nomeProjeto.setText(projeto.nome);

                    containerTarefas.addView(card);
                }
            }
        }
    }
    private void marcarDiasComTarefas() {
        int corAzulHevy = getResources().getColor(R.color.hevy_blue);

        for (Projeto projeto : listaProjetos) {
            if (projeto.tarefas == null) continue;

            for (Tarefa tarefa : projeto.tarefas) {
                // Ignora tarefas sem data ou já concluídas
                if (tarefa.dataConclusao == null || tarefa.dataConclusao.isEmpty()) continue;
                if (tarefa.concluida) continue;

                String[] parts = tarefa.dataConclusao.split("-");
                if (parts.length != 3) continue;

                int ano, mes, dia;
                try {
                    ano = Integer.parseInt(parts[0]);
                    mes = Integer.parseInt(parts[1]); // 1-based do Firebase
                    dia = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    continue;
                }

                // Subtrair 1 do mês para CalendarDay (0-based)
                CalendarDay day = CalendarDay.from(ano, mes - 1, dia);
                calendar.addDecorator(new DiaComTarefaDecorator(day, corAzulHevy));
            }
        }
    }


}
