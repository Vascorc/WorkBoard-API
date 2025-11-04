package com.example.projetoindividual.ui.projetos;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.projetoindividual.ui.ProjetoDetalheActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjetosFragment extends Fragment {

    private List<Projeto> listaProjetos;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projetos, container, false);

        // Botão para adicionar projeto (igual ao da Home)
        view.findViewById(R.id.btnAddProjeto).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProjetoDetalheActivity.class);
            startActivity(intent);
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        LinearLayout containerProjetos = getView().findViewById(R.id.containerProjetos);
        containerProjetos.removeAllViews();

        // Buscar projetos do Firebase
        FirebaseHelper.getAllProjectsForCurrentUser((projetos, error) -> {
            if (error != null) {
                // Aqui você pode mostrar uma mensagem de erro se quiser
                return;
            }

            listaProjetos = projetos;
            ordenarProjetos(listaProjetos, containerProjetos);
        });

    }

    private void ordenarProjetos(List<Projeto> projetos, LinearLayout layout) {
        // Ordem: Em andamento -> Por começar -> Concluído
        for (String estado : new String[]{"Em andamento", "Por começar", "Concluído"}) {
            for (Projeto projeto : projetos) {
                if (projeto.getEstado().equals(estado)) {
                    adicionarCardProjeto(layout, projeto);
                }
            }
        }
    }

    private void adicionarCardProjeto(LinearLayout layout, Projeto projeto) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_projeto, layout, false);

        TextView nome = card.findViewById(R.id.textNomeProjeto);
        TextView status = card.findViewById(R.id.textStatusProjeto);
        TextView dataProjeto = card.findViewById(R.id.textDataProjeto);

        nome.setText(projeto.nome);
        status.setText(projeto.getEstado());

        // Pegar a menor data entre as tarefas
        String primeiraData = "";
        if (projeto.tarefas != null) {
            for (Tarefa t : projeto.tarefas) {
                if (primeiraData.isEmpty() || t.dataConclusao.compareTo(primeiraData) < 0) {
                    primeiraData = t.dataConclusao;
                }
            }
        }

        // Formatar data
        try {
            Date data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(primeiraData);
            primeiraData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(data);
        } catch (Exception e) {
            // ignorar se não tiver data
        }

        dataProjeto.setText("Primeira tarefa até: " + primeiraData);

        // Abrir Activity de detalhe
        card.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProjetoDetalheActivity.class);
            intent.putExtra(ProjetoDetalheActivity.EXTRA_PROJETO, projeto);
            startActivity(intent);
        });

        layout.addView(card);
    }
}
