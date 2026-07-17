package ec.edu.ug.moodleug.ui.courses;

import android.os.Bundle;
import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import ec.edu.ug.moodleug.models.Course;
import ec.edu.ug.moodleug.utils.Constants;
import retrofit2.Call;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.Button;
import android.content.Intent;
import ec.edu.ug.moodleug.ui.login.LoginActivity;

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
    private String userToken;

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
            userToken = getIntent().getStringExtra("USER_TOKEN");
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

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            // 1. Cerramos sesión en Firebase (vital si el usuario entró con Google)
            FirebaseAuth.getInstance().signOut();

            // 2. Cerrar sesión en Google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                    .requestEmail()
                    .build();
            GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);
            googleClient.signOut();

            // 3. (Opcional pero recomendado) Limpiar datos guardados en SharedPreferences si los tienes
            // SharedPreferences.Editor editor = getSharedPreferences("MiAppPrefs", MODE_PRIVATE).edit();
            // editor.clear().apply();

            // 4. Volver a la pantalla de Login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia el historial de pantallas
            startActivity(intent);
            finish();
        });
    }

    private void loadMoodleCourses(int userId) {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);
        String userToken = getIntent().getStringExtra("USER_TOKEN");

        // Llamamos a la función con el ID que recibimos del Login
        Call<List<Course>> call = api.getUserCourses(
                userToken,
                "core_enrol_get_users_courses",
                "json",
                userId
        );

        call.enqueue(new retrofit2.Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, retrofit2.Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();

                    // Asignamos los datos visuales al RecyclerView
                    CourseAdapter adapter = new CourseAdapter(courses, new CourseAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Course course) {
                            // Al tocar la tarjeta, preparamos el viaje a la pantalla de detalles
                            android.content.Intent intent = new android.content.Intent(CoursesActivity.this, CourseDetailActivity.class);

                            // Enviamos el ID del curso, su nombre, y no olvidemos el Token del usuario
                            intent.putExtra("COURSE_ID", course.getId());
                            intent.putExtra("COURSE_NAME", course.getFullname());
                            intent.putExtra("USER_TOKEN", userToken);
                            intent.putExtra("USER_ID", userId);

                            startActivity(intent);
                        }
                    });
                    recyclerCourses.setAdapter(adapter);

                } else {
                    Toast.makeText(CoursesActivity.this, getString(R.string.error_fetch_courses), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(CoursesActivity.this, getString(R.string.error_network_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }
}