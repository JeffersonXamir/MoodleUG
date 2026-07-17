package ec.edu.ug.moodleug.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import ec.edu.ug.moodleug.R;
import ec.edu.ug.moodleug.api.ApiClient;
import ec.edu.ug.moodleug.api.MoodleApi;
import ec.edu.ug.moodleug.models.UserProfile;
import ec.edu.ug.moodleug.ui.courses.CoursesActivity;
import ec.edu.ug.moodleug.utils.Constants;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ec.edu.ug.moodleug.R;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private TextView textStatus;

    // Registramos el launcher para recibir el resultado de la ventana de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // El usuario seleccionó su cuenta correctamente
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(TAG, "Fallo el inicio de sesión con Google", e);
                        textStatus.setText("Error en la autenticación.");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textStatus = findViewById(R.id.textStatus);
        Button buttonOAuth = findViewById(R.id.buttonOAuth);

        // 1. Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Configurar las opciones de inicio de sesión de Google (usando tu Constants.java)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 3. Evento del botón de Google
        buttonOAuth.setOnClickListener(v -> {
            textStatus.setText("Abriendo cuentas de Google...");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // 4. Autenticar el token en Firebase
    private void firebaseAuthWithGoogle(String idToken) {
        textStatus.setText("Autenticando en Firebase...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String email = mAuth.getCurrentUser().getEmail();
                        textStatus.setText("Validando " + email + " en Moodle...");
                        validarCorreoEnMoodle(email);
                    } else {
                        textStatus.setText("Fallo la autenticación en Firebase.");
                    }
                });
    }

    private void validarCorreoEnMoodle(String email) {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        Call<List<UserProfile>> call = api.getUserByField(
                Constants.MOODLE_TEST_TOKEN,
                "core_user_get_users_by_field",
                "json",
                "email",
                email
        );

        call.enqueue(new retrofit2.Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, retrofit2.Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UserProfile user = response.body().get(0);
                    textStatus.setText("Estudiante validado: " + user.getFullname());
                    Toast.makeText(LoginActivity.this, "¡Acceso concedido!", Toast.LENGTH_SHORT).show();

                    // Siguiente paso: Navegar a la pantalla de lista de cursos pasando el user.getId()
                    // Siguiente paso: Navegar a la pantalla de lista de cursos pasando el user.getId()
                    Intent intent = new Intent(LoginActivity.this, CoursesActivity.class);
                    intent.putExtra("USER_ID", user.getId()); // Generalmente es un int o long
                    intent.putExtra("USER_NAME", user.getFullname());
                    startActivity(intent);

                    // Finalizamos LoginActivity para que el usuario no pueda regresar con el botón "Atrás"
                    finish();

                } else {
                    textStatus.setText("Error: El correo no pertenece a Moodle.");
                    // Opcional: Cerrar sesión en Firebase si no existe en Moodle
                    mAuth.signOut();
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                textStatus.setText("Error de conexión con el servidor LEMP.");
                Log.e(TAG, "Retrofit Error: ", t);
            }
        });
    }
}