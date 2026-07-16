package ec.edu.ug.moodleug.api;

import java.util.List;

import ec.edu.ug.moodleug.models.Course;
import ec.edu.ug.moodleug.models.UserProfile;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MoodleApi {

    // Obtener cursos
    @GET("webservice/rest/server.php")
    Call<List<Course>> getCourses(
            @Query("wstoken") String token,
            @Query("wsfunction") String function,
            @Query("moodlewsrestformat") String format,
            @Query("userid") int userId
    );

    // Buscar usuario por campo (correo)
    @GET("webservice/rest/server.php")
    Call<List<UserProfile>> getUserByField(
            @Query("wstoken") String token,
            @Query("wsfunction") String function,
            @Query("moodlewsrestformat") String format,
            @Query("field") String field,
            @Query("values[0]") String value
    );
}