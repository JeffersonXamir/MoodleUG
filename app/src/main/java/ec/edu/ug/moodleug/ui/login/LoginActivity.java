package ec.edu.ug.moodleug.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import ec.edu.ug.moodleug.models.SiteInfoResponse;
import ec.edu.ug.moodleug.models.TokenResponse;
import ec.edu.ug.moodleug.models.UserProfile;
import ec.edu.ug.moodleug.ui.courses.CoursesActivity;
import ec.edu.ug.moodleug.utils.Constants;

import androidx.activity.EdgeToEdge;

import java.util.List;

import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private TextView textStatus;
    private EditText editUsername;
    private EditText editPassword;

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
                        // FIX: Cambiamos textStatus por Toast
                        Toast.makeText(this, "Error en la autenticación de Google.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        //textStatus = findViewById(R.id.textStatus);
        Button buttonOAuth = findViewById(R.id.btnGoogleLogin);
        Button btnLogin = findViewById(R.id.btnLogin);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        // 1. Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Configurar las opciones de inicio de sesión de Google (usando tu Constants.java)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();

        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                loginManual(username, password);
            } else {
                Toast.makeText(LoginActivity.this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 3. Evento del botón de Google
        // 3. Evento del botón de Google
        buttonOAuth.setOnClickListener(v -> {
            // FIX: Cambiamos textStatus por Toast
            Toast.makeText(this, "Abriendo cuentas de Google...", Toast.LENGTH_SHORT).show();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // 4. Autenticar el token en Firebase
    private void firebaseAuthWithGoogle(String idToken) {
        Toast.makeText(this, "Autenticando con Google...", Toast.LENGTH_SHORT).show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String email = mAuth.getCurrentUser().getEmail();
                        Toast.makeText(this, "Validando usuario en Moodle...", Toast.LENGTH_SHORT).show();
                        validarCorreoEnMoodle(email);
                    } else {
                        Toast.makeText(this, "Fallo la autenticación en Firebase.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginActivity.this, "¡Acceso concedido por Google!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, CoursesActivity.class);
                    intent.putExtra("USER_ID", user.getId());
                    intent.putExtra("USER_NAME", user.getFullname());

                    // ¡EL FIX CLAVE! Enviamos el token maestro para que CoursesActivity pueda trabajar
                    intent.putExtra("USER_TOKEN", Constants.MOODLE_TEST_TOKEN);

                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Error: El correo no pertenece a Moodle.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red con el servidor.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Retrofit Error: ", t);
            }
        });
    }

    // 2. Añade este método en la clase LoginActivity:
    private void loginManual(String username, String password) {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        // IMPORTANTE: El tercer parámetro es el "Nombre corto" de tu servicio en Moodle.
        // "moodle_mobile_app" es el servicio por defecto. Si configuraste uno propio (como api_rest_app_movil), pon el "shortname" exacto aquí.
        Call<TokenResponse> call = api.getMoodleToken(username, password, "moodle_mobile_app");

        call.enqueue(new retrofit2.Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getToken() != null) {
                        String userToken = response.body().getToken();
                        Toast.makeText(LoginActivity.this, "¡Token obtenido!", Toast.LENGTH_SHORT).show();

                        // 1. ¡Token obtenido! Ahora encadenamos la segunda petición:
                        fetchUserDetailsAndNavigate(userToken);
                    } else {
                        Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // NUEVO MÉTODO: Pide los datos del usuario usando el nuevo token
    private void fetchUserDetailsAndNavigate(String token) {
        MoodleApi api = ApiClient.getClient().create(MoodleApi.class);

        // Usamos la función maestra de Moodle para obtener la info del sitio/usuario
        Call<SiteInfoResponse> call = api.getSiteInfo(token, "core_webservice_get_site_info", "json");

        call.enqueue(new retrofit2.Callback<SiteInfoResponse>() {
            @Override
            public void onResponse(Call<SiteInfoResponse> call, retrofit2.Response<SiteInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SiteInfoResponse siteInfo = response.body();

                    // 2. Armamos las maletas (Intent) con los datos y el token
                    Intent intent = new Intent(LoginActivity.this, CoursesActivity.class);
                    intent.putExtra("USER_ID", siteInfo.getUserid());
                    intent.putExtra("USER_NAME", siteInfo.getFullname());
                    intent.putExtra("USER_TOKEN", token); // Pasamos el token dinámico

                    Toast.makeText(LoginActivity.this, "¡Bienvenido " + siteInfo.getFullname() + "!", Toast.LENGTH_SHORT).show();

                    // 3. Saltamos a la pantalla de cursos
                    startActivity(intent);
                    finish(); // Destruimos el Login para no poder volver atrás
                }
            }

            @Override
            public void onFailure(Call<SiteInfoResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error al obtener perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }
}