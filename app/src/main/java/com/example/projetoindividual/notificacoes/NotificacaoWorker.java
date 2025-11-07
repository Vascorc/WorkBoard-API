package com.example.projetoindividual.notificacoes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class NotificacaoWorker extends Worker {

    private static final String TAG = "NotificacaoWorker";

    public NotificacaoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    // Metodo que dispara a notificação
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            String tituloTarefa = getInputData().getString("titulo_tarefa");
            if (tituloTarefa == null) {
                Log.w(TAG, "Título da tarefa é nulo");
                return Result.failure();
            }

            enviarNotificacao(context, tituloTarefa);
            Log.d(TAG, "Notificação enviada: " + tituloTarefa);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    // Função que cria e dispara a notificação
    private void enviarNotificacao(Context context, String titulo) {
        String channelId = "canal_notificacao";
        String channelName = "Lembretes";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Lembrete de Tarefa")
                .setContentText(titulo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // Metodo estático para agendar a notificação
    public static void agendar(Context context, String titulo, String dataConclusao) {
        try {
            Log.d(TAG, "Agendando notificação para: " + titulo + ", data: " + dataConclusao);

            // Lê preferências do usuário
            SharedPreferences prefs = context.getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
            String jsonConfig = prefs.getString("notificacao_config", null);
            Log.d(TAG, "Configurações lidas: " + jsonConfig);

            int diasAntes = 0;
            String horaConfig = "09:00"; // padrão

            if (jsonConfig != null) {
                JSONObject obj = new JSONObject(jsonConfig);
                horaConfig = obj.optString("hora", "09:00");

                JSONArray diasArray = obj.optJSONArray("dias");
                if (diasArray != null && diasArray.length() > 0) {
                    diasAntes = diasArray.getInt(0);
                }
            }

            // Calcula a data/hora da notificação
            LocalDate data = LocalDate.parse(dataConclusao).minusDays(diasAntes);
            LocalTime hora = LocalTime.parse(horaConfig);
            LocalDateTime dataHoraNotif = LocalDateTime.of(data, hora);

            long millisParaAgendar = dataHoraNotif.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long delay = millisParaAgendar - System.currentTimeMillis();
            if (delay < 0) delay = 0; // caso já tenha passado

            Log.d(TAG, "Notificação será enviada em: " + dataHoraNotif + " (delay = " + delay / 1000 + "s)");

            // Prepara os dados para o Worker
            Data inputData = new Data.Builder()
                    .putString("titulo_tarefa", titulo)
                    .build();

            // Cria o trabalho agendado
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificacaoWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build();

            // Envia para o WorkManager
            WorkManager.getInstance(context).enqueue(workRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Erro ao agendar notificação: " + e.getMessage());
        }
    }
}
