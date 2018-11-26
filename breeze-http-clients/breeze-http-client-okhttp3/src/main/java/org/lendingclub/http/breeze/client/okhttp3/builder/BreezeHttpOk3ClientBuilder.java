package org.lendingclub.http.breeze.client.okhttp3.builder;

import okhttp3.OkHttpClient;
import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.builder.AbstractBreezeHttpClientBuilder;
import org.lendingclub.http.breeze.client.json.BreezeHttpJsonMapper;
import org.lendingclub.http.breeze.client.okhttp3.BreezeHttpOk3Client;

public class BreezeHttpOk3ClientBuilder extends AbstractBreezeHttpClientBuilder<BreezeHttpOk3ClientBuilder> {
    protected OkHttpClient okClient;
    protected BreezeHttpJsonMapper jsonMapper;

    public BreezeHttpOk3ClientBuilder() {
        this.me = this;
    }

    public BreezeHttpOk3ClientBuilder okClient(OkHttpClient okClient) {
        this.okClient = okClient.newBuilder().build();
        return this;
    }

    public BreezeHttpOk3ClientBuilder jsonMapper(BreezeHttpJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    @Override
    public BreezeHttp build() {
        return decorate(new BreezeHttpOk3Client(
                okClient,
                requestLogger,
                converters,
                filters,
                errorHandler,
                jsonMapper
        ));
    }
}
