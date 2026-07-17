package ec.edu.ug.moodleug.ui.courses;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentActivity extends AppCompatActivity {

    private String userToken;
    private int assignmentId;
    private String assignmentName;

    private TextView textAssignmentName;
    private EditText editSubmissionText;
    private Button btnSubmitAssignment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        // 1. Enlazar las vistas
        textAssignmentName = findViewById(R.id.textAssignmentName);
        editSubmissionText = findViewById(R.id.editSubmissionText);
        btnSubmitAssignment = findViewById(R.id.btnSubmitAssignment);

        // 2. Recuperar datos del Intent
        if (getIntent() != null) {
            assignmentId = getIntent().getIntExtra("ASSIGNMENT_ID", -1);
            assignmentName = getIntent().getStringExtra("ASSIGNMENT_NAME");
            userToken = getIntent().getStringExtra("USER_TOKEN");
        }

        // 3. Validar y configurar título
        if (assignmentId != -1 && userToken != null) {
            textAssignmentName.setText(assignmentName);
        } else {
            Toast.makeText(this, "Error al cargar los datos de la tarea", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 4. Acción del botón azul
        btnSubmitAssignment.setOnClickListener(v -> {
            String submissionText = editSubmissionText.getText().toString().trim();

            if (!submissionText.isEmpty()) {
                sendSubmissionToMoodle(submissionText);
            } else {
                Toast.makeText(this, "Por favor, escribe tu respuesta antes de enviar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSubmissionToMoodle(String text) {
        Toast.makeText(this, "Enviando a Moodle...", Toast.LENGTH_SHORT).show();
        btnSubmitAssignment.setEnabled(false); // Bloqueamos el botón para evitar que el usuario lo presione 2 veces

        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        // Enviamos los datos obligatorios con el formato de Moodle
        Call<ResponseBody> call = api.saveAssignmentSubmission(
                userToken,
                "mod_assign_save_submission",
                "json",
                assignmentId,
                text,
                1, // El 1 le dice a Moodle que es formato HTML/Texto
                0  // El 0 le dice a Moodle que no estamos enviando archivos adjuntos
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSubmitAssignment.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Convertimos la respuesta cruda de Moodle a texto
                        String respuestaMoodle = response.body().string();
                        Log.d("MoodleAPI", "Respuesta exacta de Moodle: " + respuestaMoodle);

                        // Moodle esconde los errores dentro de respuestas exitosas.
                        // Verificamos si la respuesta contiene la palabra "exception" o "error"
                        if (respuestaMoodle.contains("exception") || respuestaMoodle.contains("errorcode")) {
                            Toast.makeText(AssignmentActivity.this, "Moodle rechazó el envío (revisa Logcat)", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AssignmentActivity.this, "¡Tarea enviada con éxito!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AssignmentActivity.this, "Error HTTP del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmitAssignment.setEnabled(true);
                Toast.makeText(AssignmentActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e("AssignmentActivity", "Error Retrofit: ", t);
            }
        });
    }
}