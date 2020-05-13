package io.jenkins.plugins.worktile.client;

import java.io.IOException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.model.WTRestException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiConnection {
    private final String accessToken;

    private OkHttpClient httpClient;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public ApiConnection(String accessToken, OkHttpClient client) {
        this.accessToken = accessToken;
        this.httpClient = client;
    }

    public ApiConnection(OkHttpClient client) {
        this(null, client);
    }

    public ApiConnection() {
        this(null, new OkHttpClient());
    }

    private <TResponse> TResponse execute(Builder requestBuilder) throws IOException, WTRestException {
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }
        try (Response response = this.httpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                WTErrorEntity error = gson.fromJson(response.body().string(), WTErrorEntity.class);
                throw new WTRestException(error.getCode(), error.getMessage());
            }
            return gson.fromJson(response.body().string(), new TypeToken<TResponse>() {
            }.getType());
        }
    }

    public <TResponse> TResponse executeGet(String url) throws IOException, WTRestException {
        Builder requestBuilder = new Request.Builder().url(url).get();
        return execute(requestBuilder);
    }

    public <TResponse> TResponse executePost(String url, Object body) throws IOException, WTRestException {
        MediaType JSONMedia = MediaType.get("application/json; charset=utf-8");
        String json = gson.toJson(body);
        RequestBody reqBody = RequestBody.create(json, JSONMedia);
        Builder requestBuilder = new Request.Builder().url(url).post(reqBody);
        return execute(requestBuilder);
    }

    public <TResponse, TBody> TResponse executeDelete(String url, Class<TBody> body)
            throws IOException, WTRestException {
        MediaType JSONMedia = MediaType.get("application/json; charset=utf-8");
        String json = gson.toJson(body);
        RequestBody reqBody = RequestBody.create(json, JSONMedia);
        Builder requestBuilder = new Request.Builder().url(url).delete(reqBody);
        return execute(requestBuilder);
    }

    public <TResponse> TResponse executeDelete(String url) throws IOException, WTRestException {
        Builder requestBuilder = new Request.Builder().url(url).delete();
        return execute(requestBuilder);
    }
}
