package ec.edu.ug.moodleug.api;

import ec.edu.ug.moodleug.utils.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // IMPORTANTE: El emulador de Android usa 10.0.2.2 para referirse al localhost (127.0.0.1) de tu computadora física.
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.MOODLE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}