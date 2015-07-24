package syncthing.api;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import timber.log.Timber;

public class NullHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
