// Conteúdo para: com/example/projetoindividual/model/Tarefa.java
package com.example.projetoindividual.model;

import java.io.Serializable;

public class Tarefa implements Serializable {

    // 1. Adicionar os campos 'id' e 'projetoId'
    public String projetoId;
    public String id, titulo;
    public String dataConclusao;
    public boolean concluida;

    public Tarefa() {} //pode ser preciso
    // Criar um construtor principal que inclui todos os campos
    public Tarefa(String id, String titulo, String dataConclusao, boolean concluida, String projetoId) {
        this.id = id;
        this.titulo = titulo;
        this.dataConclusao = dataConclusao;
        this.concluida = concluida;
        this.projetoId = projetoId;
    }

    // Criar um construtor secundário para quando se cria uma tarefa nova (sem ID)
    public Tarefa(String titulo,String dataConclusao, boolean concluida) {
        this.titulo = titulo;
        this.dataConclusao = dataConclusao;
        this.concluida = concluida;

    }
}
