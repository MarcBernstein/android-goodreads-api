package com.github.marcbernstein.grapi.auth;

import com.github.marcbernstein.grapi.GoodreadsAPI;

import java.io.IOException;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;

public class SigningClient implements Client {
    private final Client mWrappedClient;
    private final GoodreadsAPI mGoodreadsAPI;

    public SigningClient(Client client, GoodreadsAPI goodreadsAPI) {
        mWrappedClient = client;
        mGoodreadsAPI = goodreadsAPI;
    }

    @Override
    public Response execute(Request request) throws IOException {
        try {
            mGoodreadsAPI.getConsumer().sign(request);
        } catch (Exception e) {
            // FIXME
        }
        return mWrappedClient.execute(request);
    }
}