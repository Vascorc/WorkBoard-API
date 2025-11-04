package com.example.projetoindividual.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projetoindividual.AddProjetoActivity;
import com.example.projetoindividual.R;
import com.example.projetoindividual.database.FirebaseHelper;
import com.example.projetoindividual.databinding.FragmentHomeBinding;
import com.example.projetoindividual.model.Projeto;
import com.example.projetoindividual.model.Tarefa;
import com.example.projetoindividual.ui.ProjetoDetalheActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<Projeto> listaProjetos = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Botão de ir para o calendário
        binding.btnIrCalendario.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_calendario);
        });


        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Limpar cards antigos
        binding.containerProjetos.removeAllViews();

        // Buscar projetos do Firebase
        FirebaseHelper.getAllProjectsForCurrentUser((projetos, error) -> {
            if (error != null) {
                // mostrar mensagem de erro, se quiser
                return;
            }

            listaProjetos = projetos;
            // Mostrar cards na home
            ordenarProjetos(listaProjetos, binding.containerProjetos);
        });

        // Configura o botão para abrir a AddProjetoActivity
        binding.btnAddProjeto.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddProjetoActivity.class);
            startActivity(intent);
        });

    }


    private void ordenarProjetos(List<Projeto> projetos, LinearLayout layout) {
        for (Projeto projeto : projetos) {
            if (projeto.getEstado().equals("Em andamento") || projeto.getEstado().equals("Por começar")) {
                adicionarCardProjeto(layout, projeto);
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

        String primeiraData = "";
        for (Tarefa t : projeto.tarefas) {
            if (primeiraData.isEmpty() || t.dataConclusao.compareTo(primeiraData) < 0) {
                primeiraData = t.dataConclusao;
            }
        }
        dataProjeto.setText("Primeira tarefa até: " + primeiraData);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProjetoDetalheActivity.class);
            intent.putExtra(ProjetoDetalheActivity.EXTRA_PROJETO, projeto);
            startActivity(intent);
        });

        layout.addView(card);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
