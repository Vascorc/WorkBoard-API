package com.example.projetoindividual;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class NotificacaoHelper {

    public static void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "canal_tarefas";
            CharSequence nome = "Notificações de Tarefas";
            String descricao = "Notificações para lembrar das tarefas";
            int importancia = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel canal = new android.app.NotificationChannel(id, nome, importancia);
            canal.setDescription(descricao);
            android.app.NotificationManager manager = context.getSystemService(android.app.NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(canal);
        }
    }

    public static void agendarNotificacao(Context context, String tituloTarefa, String dataConclusao) {
        try {
            String[] parts = dataConclusao.split("-"); // formato yyyy-MM-dd
            int ano = Integer.parseInt(parts[0]);
            int mes = Integer.parseInt(parts[1]) - 1; // Calendar 0-based
            int dia = Integer.parseInt(parts[2]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, ano);
            cal.set(Calendar.MONTH, mes);
            cal.set(Calendar.DAY_OF_MONTH, dia);
            cal.set(Calendar.HOUR_OF_DAY, 9); // horário da notificação
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            // Subtrair 1 dia
            cal.add(Calendar.DAY_OF_MONTH, -1);

            Intent intent = new Intent(context, NotificacaoReceiver.class);
            intent.putExtra("titulo", "Tarefa Amanhã!");
            intent.putExtra("mensagem", "A tarefa \"" + tituloTarefa + "\" vence amanhã.");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) System.currentTimeMillis(), // ID único
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
