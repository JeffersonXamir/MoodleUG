package ec.edu.ug.moodleug.api;

import java.util.List;

import ec.edu.ug.moodleug.models.Course;
import ec.edu.ug.moodleug.models.CourseSection;
import ec.edu.ug.moodleug.models.ForumDiscussionsResponse;
import ec.edu.ug.moodleug.models.GradeReportResponse;
import ec.edu.ug.moodleug.models.SiteInfoResponse;
import ec.edu.ug.moodleug.models.TokenResponse;
import ec.edu.ug.moodleug.models.UserProfile;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MoodleApi {

    // Endpoint para obtener los cursos de un estudiante
    @GET("webservice/rest/server.php")
    Call<List<Course>> getUserCourses(
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

    @GET("login/token.php")
    Call<TokenResponse> getMoodleToken(
            @Query("username") String username,
            @Query("password") String password,
            @Query("service") String serviceName
    );

    @GET("webservice/rest/server.php")
    Call<SiteInfoResponse> getSiteInfo(
            @Query("wstoken") String token,
            @Query("wsfunction") String function,
            @Query("moodlewsrestformat") String format
    );

    // Endpoint para obtener el contenido (tareas, foros) de un curso específico
    @GET("webservice/rest/server.php")
    Call<List<CourseSection>> getCourseContents(
            @Query("wstoken") String token,
            @Query("wsfunction") String function,
            @Query("moodlewsrestformat") String format,
            @Query("courseid") int courseId
    );

    // Endpoint para enviar una tarea de texto en línea
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    Call<ResponseBody> saveAssignmentSubmission(
            @Field("wstoken") String token,
            @Field("wsfunction") String function,
            @Field("moodlewsrestformat") String format,
            @Field("assignmentid") int assignmentId,
            @Field("plugindata[onlinetext_editor][text]") String text,
            @Field("plugindata[onlinetext_editor][format]") int textFormat,
            @Field("plugindata[onlinetext_editor][itemid]") int itemId
    );

    // Endpoint para obtener las discusiones de un foro
    @GET("webservice/rest/server.php")
    Call<ForumDiscussionsResponse> getForumDiscussions(
            @Query("wstoken") String token,
            @Query("wsfunction") String function,
            @Query("moodlewsrestformat") String format,
            @Query("forumid") int forumId
    );

    // Endpoint para responder a un debate en el foro
    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    Call<ResponseBody> addForumPost(
            @Field("wstoken") String token,
            @Field("wsfunction") String function,
            @Field("moodlewsrestformat") String format,
            @Field("postid") int postId, // El ID del mensaje al que respondemos
            @Field("subject") String subject,
            @Field("message") String message
    );

    // Endpoint para obtener las calificaciones de un curso
    @GET("webservice/rest/server.php")
    Call<GradeReportResponse> getCourseGrades(
            @Query("wstoken") String token,
            @Query("wsfunction") String function,
            @Query("moodlewsrestformat") String format,
            @Query("courseid") int courseId,
            @Query("userid") int userId
    );
}