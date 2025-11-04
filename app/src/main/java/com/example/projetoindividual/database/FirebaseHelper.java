package com.example.projetoindividual.database;

import com.example.projetoindividual.model.Projeto;
import com.example.projetoindividual.model.Tarefa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // =========================
    // AUTENTICAÇÃO
    // =========================

    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public static void logout() {
        auth.signOut();
    }

    // =========================
    // PROJETOS
    // =========================

    // Criar um novo projeto
    public static void criarProjeto(Projeto projeto, FirebaseCallback<DocumentReference> callback) {
        db.collection("projetos")
                .add(projeto)
                .addOnSuccessListener(docRef -> callback.onComplete(docRef, null))
                .addOnFailureListener(e -> callback.onComplete(null, e.getMessage()));
    }

    // Remover um projeto
    public static void removerProjeto(String projetoId, FirebaseCallback<Boolean> callback) {
        db.collection("projetos")
                .document(projetoId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    // Buscar projeto por ID
    public static void getProjectById(String projetoId, FirebaseCallback<Projeto> callback) {
        db.collection("projetos")
                .document(projetoId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Projeto projeto = document.toObject(Projeto.class);
                        if (projeto != null)
                            projeto.id = document.getId(); // <--- ESSENCIAL
                        callback.onComplete(projeto, null);
                    } else {
                        callback.onComplete(null, "Projeto não encontrado");
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(null, e.getMessage()));
    }

    // Adicionar uma tarefa a um projeto
    public static void adicionarTarefa(String projetoId, Tarefa tarefa, FirebaseCallback<Boolean> callback) {
        db.collection("projetos")
                .document(projetoId)
                .collection("tarefas")
                .add(tarefa)
                .addOnSuccessListener(docRef -> {
                    // GUARDA O ID GERADO PELO FIREBASE
                    tarefa.id = docRef.getId();
                    tarefa.projetoId = projetoId;

                    // Atualiza o documento com o ID dentro dele
                    docRef.set(tarefa);

                    db.collection("projetos")
                            .document(projetoId)
                            .update("tarefas", FieldValue.arrayUnion(tarefa))
                            .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                            .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    // Remover uma tarefa de um projeto
    public static void removerTarefa(String projetoId, String tarefaId, Tarefa tarefa, FirebaseCallback<Boolean> callback) {
        db.collection("projetos")
                .document(projetoId)
                .collection("tarefas")
                .document(tarefaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("projetos")
                            .document(projetoId)
                            .update("tarefas", FieldValue.arrayRemove(tarefa))
                            .addOnSuccessListener(aVoid2 -> callback.onComplete(true, null))
                            .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    // =========================
    // USUÁRIOS
    // =========================

    // Adicionar um usuário a um projeto
    public static void adicionarUsuario(String projetoId, String email, FirebaseCallback<Boolean> callback) {
        db.collection("projetos")
                .document(projetoId)
                .update("usuarios", FieldValue.arrayUnion(email))
                .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    // Remover um usuário de um projeto
    public static void removerUsuario(String projetoId, String email, FirebaseCallback<Boolean> callback) {
        db.collection("projetos")
                .document(projetoId)
                .update("usuarios", FieldValue.arrayRemove(email))
                .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }


    // =========================
// ATUALIZAR TAREFA
// =========================
    public static void updateTask(Tarefa tarefa, FirebaseCallback<Boolean> callback) {
        if (tarefa.id == null || tarefa.projetoId == null) {
            callback.onComplete(false, "Tarefa ou ID do projeto inválidos");
            return;
        }

        db.collection("projetos")
                .document(tarefa.projetoId)
                .collection("tarefas")
                .document(tarefa.id)
                .set(tarefa)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    // =========================
// ATUALIZAR PROJETO
// =========================
    public static void updateProject(Projeto projeto, FirebaseCallback<Boolean> callback) {
        if (projeto.id == null) {
            callback.onComplete(false, "ID do projeto inválido");
            return;
        }

        db.collection("projetos")
                .document(projeto.id)
                .set(projeto)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }


    // =========================
    // LISTAR PROJETOS DO USUÁRIO
    // =========================

    public static void getAllProjectsForCurrentUser(FirebaseCallback<List<Projeto>> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onComplete(null, "Usuário não logado");
            return;
        }

        db.collection("projetos")
                .whereArrayContains("users", user.getEmail())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Projeto> projetos = new ArrayList<>();
                    if (querySnapshot.isEmpty()) {
                        callback.onComplete(projetos, null);
                        return;
                    }

                    List<com.google.android.gms.tasks.Task<QuerySnapshot>> tasks = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Projeto projeto = doc.toObject(Projeto.class);
                        if (projeto != null) {
                            projeto.id = doc.getId();
                            projetos.add(projeto);

                            // Adiciona a task de carregar tarefas
                            tasks.add(db.collection("projetos")
                                    .document(doc.getId())
                                    .collection("tarefas")
                                    .get()
                                    .addOnSuccessListener(tarefasSnapshot -> {
                                        List<Tarefa> tarefas = new ArrayList<>();
                                        for (DocumentSnapshot tDoc : tarefasSnapshot.getDocuments()) {
                                            Tarefa t = tDoc.toObject(Tarefa.class);
                                            if (t != null) t.id = tDoc.getId();
                                            tarefas.add(t);
                                        }
                                        projeto.tarefas = tarefas;
                                    }));
                        }
                    }

                    // Quando todas as tasks de tarefas terminarem
                    com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(result -> callback.onComplete(projetos, null))
                            .addOnFailureListener(e -> callback.onComplete(null, e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onComplete(null, e.getMessage()));
    }




    public interface FirebaseCallback<T> {
        void onComplete(T result, String errorMessage);
    }

}
