package ec.edu.ug.moodleug.ui.courses;

import android.os.Bundle;
import android.text.Html;
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

public class ForumReplyActivity extends AppCompatActivity {

    private String userToken;
    private int postId;
    private String discussionTitle;

    private TextView textOriginalTitle;
    private TextView textOriginalMessage;
    private EditText editReplyText;
    private Button btnSubmitReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_reply);

        textOriginalTitle = findViewById(R.id.textOriginalTitle);
        textOriginalMessage = findViewById(R.id.textOriginalMessage);
        editReplyText = findViewById(R.id.editReplyText);
        btnSubmitReply = findViewById(R.id.btnSubmitReply);

        if (getIntent() != null) {
            postId = getIntent().getIntExtra("POST_ID", -1);
            discussionTitle = getIntent().getStringExtra("DISCUSSION_TITLE");
            userToken = getIntent().getStringExtra("USER_TOKEN");
            String originalMessage = getIntent().getStringExtra("DISCUSSION_MESSAGE");

            textOriginalTitle.setText("Re: " + discussionTitle);

            if (originalMessage != null) {
                textOriginalMessage.setText(Html.fromHtml(originalMessage, Html.FROM_HTML_MODE_LEGACY).toString().trim());
            }
        }

        btnSubmitReply.setOnClickListener(v -> {
            String replyText = editReplyText.getText().toString().trim();
            if (!replyText.isEmpty()) {
                sendReplyToMoodle(replyText);
            } else {
                Toast.makeText(this, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendReplyToMoodle(String replyText) {
        Toast.makeText(this, "Publicando...", Toast.LENGTH_SHORT).show();
        btnSubmitReply.setEnabled(false);

        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        // Agregamos "Re: " al título original como suele hacer Moodle
        String subject = "Re: " + discussionTitle;

        Call<ResponseBody> call = api.addForumPost(
                userToken,
                "mod_forum_add_discussion_post",
                "json",
                postId,
                subject,
                replyText
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSubmitReply.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String respuestaMoodle = response.body().string();

                        if (respuestaMoodle.contains("exception") || respuestaMoodle.contains("errorcode")) {
                            // ¡AQUÍ ESTÁ EL FIX! Mostramos el texto exacto del error en pantalla y en la consola
                            Log.e("MoodleError", "Fallo en Moodle: " + respuestaMoodle);
                            Toast.makeText(ForumReplyActivity.this, "Error: " + respuestaMoodle, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ForumReplyActivity.this, "¡Respuesta publicada con éxito!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ForumReplyActivity.this, "Error HTTP del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmitReply.setEnabled(true);
                Toast.makeText(ForumReplyActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e("ForumReply", "Error Retrofit: ", t);
            }
        });
    }
}