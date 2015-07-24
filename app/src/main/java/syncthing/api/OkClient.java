package syncthing.api;

import com.squareup.okhttp.OkHttpClient;

import javax.net.ssl.SSLSocketFactory;

public class OkClient extends retrofit.client.OkClient {
    OkHttpClient client;

    public OkClient(OkHttpClient client) {
        super(client);
        this.client = client;
        this.client.setHostnameVerifier(new NullHostNameVerifier());
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        client.setSslSocketFactory(sslSocketFactory);
    }

}
