package petarkitanovic.androidkurs.pogledanifilmovi.net;


import java.util.Map;

import petarkitanovic.androidkurs.pogledanifilmovi.net.model.Detalji;
import petarkitanovic.androidkurs.pogledanifilmovi.net.model2.Example;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface MyApiEndpointInterface {

    @GET("/")
    Call<Example> getMovieByName(@QueryMap Map<String, String> options);

    @GET("/")
    Call<Detalji> getMovieData(@QueryMap Map<String, String> options);

}
