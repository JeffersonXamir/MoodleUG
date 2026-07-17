package ec.edu.ug.moodleug.ui.courses;

import android.os.Bundle;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import ec.edu.ug.moodleug.models.Course;
import ec.edu.ug.moodleug.utils.Constants;
import retrofit2.Call;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CoursesActivity extends AppCompatActivity {

    private static final String TAG = "CoursesActivity";
    private TextView textWelcome;
    private RecyclerView recyclerCourses;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        // 1. Inicializar vistas
        textWelcome = findViewById(R.id.textWelcome);
        recyclerCourses = findViewById(R.id.recyclerCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));

        // 2. Recuperar los datos del Intent
        if (getIntent() != null) {
            userId = getIntent().getIntExtra("USER_ID", -1);
            userName = getIntent().getStringExtra("USER_NAME");
        }

        // 3. Validar y mostrar bienvenida
        if (userId != -1) {
            textWelcome.setText("¡Bienvenido, " + userName + "!");
            // Aquí llamaremos al método para cargar los cursos desde la API
            loadMoodleCourses(userId);
        } else {
            Toast.makeText(this, "Error al recuperar datos del usuario", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMoodleCourses(int userId) {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        // Llamamos a la función con el ID que recibimos del Login
        Call<List<Course>> call = api.getUserCourses(
                Constants.MOODLE_TEST_TOKEN,
                "core_enrol_get_users_courses",
                "json",
                userId
        );

        call.enqueue(new retrofit2.Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, retrofit2.Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();

                    // Imprimimos en consola para verificar
                    Log.d(TAG, "Cursos encontrados: " + courses.size());
                    for (Course course : courses) {
                        Log.d(TAG, "Materia: " + course.getFullname());
                    }

                    Toast.makeText(CoursesActivity.this,
                            "Se cargaron " + courses.size() + " cursos", Toast.LENGTH_SHORT).show();

                    // AQUÍ LUEGO CONECTAREMOS EL RECYCLERVIEW

                } else {
                    Toast.makeText(CoursesActivity.this, "Error al obtener cursos", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error en la respuesta de Moodle");
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(CoursesActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Retrofit Error: ", t);
            }
        });
    }
}