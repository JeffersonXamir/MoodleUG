package ec.edu.ug.moodleug.ui.courses;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import ec.edu.ug.moodleug.models.ForumDiscussion;
import ec.edu.ug.moodleug.models.ForumDiscussionsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumActivity extends AppCompatActivity {

    private int forumId;
    private String forumName;
    private String userToken;

    private TextView textForumName;
    private RecyclerView recyclerDiscussions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ¡Cambiamos al nuevo layout!
        setContentView(R.layout.activity_forum);

        textForumName = findViewById(R.id.textForumName);
        recyclerDiscussions = findViewById(R.id.recyclerDiscussions);
        recyclerDiscussions.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent() != null) {
            forumId = getIntent().getIntExtra("FORUM_ID", -1);
            forumName = getIntent().getStringExtra("FORUM_NAME");
            userToken = getIntent().getStringExtra("USER_TOKEN");
        }

        if (forumId != -1 && userToken != null) {
            textForumName.setText(forumName);
            loadDiscussions();
        } else {
            Toast.makeText(this, "Error al cargar el foro", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDiscussions() {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        Call<ForumDiscussionsResponse> call = api.getForumDiscussions(
                userToken,
                "mod_forum_get_forum_discussions",
                "json",
                forumId
        );

        call.enqueue(new Callback<ForumDiscussionsResponse>() {
            @Override
            public void onResponse(Call<ForumDiscussionsResponse> call, Response<ForumDiscussionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ForumDiscussion> discussions = response.body().getDiscussions();

                    if (discussions != null && !discussions.isEmpty()) {
                        // Inflamos la lista en pantalla
                        // Inflamos la lista y agregamos la acción del clic
                        ForumDiscussionAdapter adapter = new ForumDiscussionAdapter(discussions, new ForumDiscussionAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(ForumDiscussion discussion) {
                                android.content.Intent intent = new android.content.Intent(ForumActivity.this, ForumReplyActivity.class);
                                intent.putExtra("POST_ID", discussion.getId());
                                intent.putExtra("DISCUSSION_TITLE", discussion.getName());
                                intent.putExtra("DISCUSSION_MESSAGE", discussion.getMessage());
                                intent.putExtra("USER_TOKEN", userToken);
                                startActivity(intent);
                            }
                        });
                        recyclerDiscussions.setAdapter(adapter);
                    } else {
                        Toast.makeText(ForumActivity.this, "No hay debates en este foro", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ForumActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForumDiscussionsResponse> call, Throwable t) {
                Toast.makeText(ForumActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e("ForumActivity", "Error Retrofit: ", t);
            }
        });
    }
}