package ec.edu.ug.moodleug.ui.courses;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import ec.edu.ug.moodleug.models.CourseModule;
import ec.edu.ug.moodleug.models.CourseSection;
import ec.edu.ug.moodleug.ui.grades.GradesActivity;
import retrofit2.Call;

public class CourseDetailActivity extends AppCompatActivity {

    private static final String TAG = "CourseDetailActivity";
    private String userToken;
    private int courseId;
    private String courseName;
    private RecyclerView recyclerModules;
    private TextView textCourseName;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ¡Importante! Asegúrate de enlazar el nuevo layout
        setContentView(R.layout.activity_course_detail);

        textCourseName = findViewById(R.id.textCourseName);
        recyclerModules = findViewById(R.id.recyclerModules);
        recyclerModules.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent() != null) {
            courseId = getIntent().getIntExtra("COURSE_ID", -1);
            courseName = getIntent().getStringExtra("COURSE_NAME");
            userToken = getIntent().getStringExtra("USER_TOKEN");
            userId = getIntent().getIntExtra("USER_ID", -1);
        }

        if (courseId != -1 && userToken != null) {
            textCourseName.setText(courseName);
            loadCourseContents(courseId, userToken);
        } else {
            Toast.makeText(this, "Error al cargar el curso", Toast.LENGTH_SHORT).show();
            finish();
        }

        android.widget.Button btnViewGrades = findViewById(R.id.btnViewGrades);
        btnViewGrades.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(CourseDetailActivity.this, GradesActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            intent.putExtra("USER_TOKEN", userToken);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void loadCourseContents(int courseId, String token) {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        Call<List<CourseSection>> call = api.getCourseContents(
                token,
                "core_course_get_contents",
                "json",
                courseId
        );

        call.enqueue(new retrofit2.Callback<List<CourseSection>>() {
            @Override
            public void onResponse(Call<List<CourseSection>> call, retrofit2.Response<List<CourseSection>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CourseSection> sections = response.body();
                    List<CourseModule> allModules = new ArrayList<>();

                    // Extraemos todas las actividades de todas las secciones en una sola lista
                    for (CourseSection section : sections) {
                        if (section.getModules() != null && !section.getModules().isEmpty()) {
                            allModules.addAll(section.getModules());
                        }
                    }

                    if (allModules.isEmpty()) {
                        Toast.makeText(CourseDetailActivity.this, "El curso aún no tiene actividades", Toast.LENGTH_LONG).show();
                    } else {
                        // Inflamos las tarjetas en pantalla
                        // Instanciamos el adaptador con el listener de clics
                        CourseModuleAdapter adapter = new CourseModuleAdapter(allModules, new CourseModuleAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(CourseModule module) {

                                if ("assign".equals(module.getModname())) {
                                    // Si es una TAREA, viajamos a la pantalla de envío
                                    android.content.Intent intent = new android.content.Intent(CourseDetailActivity.this, AssignmentActivity.class);
                                    intent.putExtra("ASSIGNMENT_ID", module.getInstance());
                                    intent.putExtra("ASSIGNMENT_NAME", module.getName());
                                    intent.putExtra("USER_TOKEN", userToken);
                                    startActivity(intent);

                                } else if ("forum".equals(module.getModname())) {
                                    // Si es un FORO, viajamos a la pantalla de discusiones
                                    android.content.Intent intent = new android.content.Intent(CourseDetailActivity.this, ForumActivity.class);

                                    // Enviamos el instance (que para los foros es el forumId)
                                    intent.putExtra("FORUM_ID", module.getInstance());
                                    intent.putExtra("FORUM_NAME", module.getName());
                                    intent.putExtra("USER_TOKEN", userToken);
                                    startActivity(intent);

                                } else if ("resource".equals(module.getModname()) || "folder".equals(module.getModname())) {
                                    if (module.getContents() != null && !module.getContents().isEmpty()) {

                                        String fileUrl = module.getContents().get(0).getFileurl();

                                        // FIX: Validamos si la URL ya tiene el símbolo '?' para saber cómo pegar el token
                                        String urlConPermiso = fileUrl;
                                        if (fileUrl.contains("?")) {
                                            urlConPermiso += "&token=" + userToken;
                                        } else {
                                            urlConPermiso += "?token=" + userToken;
                                        }

                                        android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlConPermiso));
                                        startActivity(browserIntent);

                                        Toast.makeText(CourseDetailActivity.this, "Descargando recurso...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CourseDetailActivity.this, "Este recurso está vacío", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(CourseDetailActivity.this, "Actividad no interactiva", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        recyclerModules.setAdapter(adapter);
                    }

                } else {
                    Toast.makeText(CourseDetailActivity.this, "Error al obtener el contenido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CourseSection>> call, Throwable t) {
                Toast.makeText(CourseDetailActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Retrofit Error: ", t);
            }
        });
    }
}