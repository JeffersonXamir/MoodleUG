package ec.edu.ug.moodleug.ui.grades;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import ec.edu.ug.moodleug.models.GradeItem;
import ec.edu.ug.moodleug.models.GradeReportResponse;
import ec.edu.ug.moodleug.models.UserGrade;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradesActivity extends AppCompatActivity {

    private int courseId;
    private int userId;
    private String userToken;
    private RecyclerView recyclerGrades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ¡Importante! Cambiamos al nuevo layout
        setContentView(R.layout.activity_grades);

        recyclerGrades = findViewById(R.id.recyclerGrades);
        recyclerGrades.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent() != null) {
            courseId = getIntent().getIntExtra("COURSE_ID", -1);
            userId = getIntent().getIntExtra("USER_ID", -1);
            userToken = getIntent().getStringExtra("USER_TOKEN");
        }

        if (courseId != -1 && userId != -1 && userToken != null) {
            loadGrades();
        } else {
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadGrades() {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        // Ya enviamos el Request pidiendo directamente nuestro modelo GradeReportResponse
        Call<GradeReportResponse> call = api.getCourseGrades(
                userToken,
                "gradereport_user_get_grade_items",
                "json",
                courseId,
                userId
        );

        call.enqueue(new Callback<GradeReportResponse>() {
            @Override
            public void onResponse(Call<GradeReportResponse> call, Response<GradeReportResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserGrade> userGrades = response.body().getUsergrades();

                    if (userGrades != null && !userGrades.isEmpty()) {
                        List<GradeItem> gradeItems = userGrades.get(0).getGradeitems();

                        if (gradeItems != null && !gradeItems.isEmpty()) {
                            // Inflamos las tarjetas de calificaciones
                            GradeAdapter adapter = new GradeAdapter(gradeItems);
                            recyclerGrades.setAdapter(adapter);
                        } else {
                            Toast.makeText(GradesActivity.this, "Aún no tienes calificaciones", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(GradesActivity.this, "No se encontró libreta de notas", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(GradesActivity.this, "Error al obtener las notas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GradeReportResponse> call, Throwable t) {
                Toast.makeText(GradesActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e("GradesActivity", "Error Retrofit: ", t);
            }
        });
    }
}