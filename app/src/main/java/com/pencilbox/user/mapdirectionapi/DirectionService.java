package com.pencilbox.user.mapdirectionapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by User on 1/15/2018.
 */

public interface DirectionService {

    @GET
    Call<DirectionResponse>getDirectionResponse(@Url String urlString);
}
