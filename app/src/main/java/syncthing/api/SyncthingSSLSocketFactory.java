package syncthing.api;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import timber.log.Timber;

public class SyncthingSSLSocketFactory {

    public static SSLSocketFactory createSyncthingSSLSocketFactory(String cert) {
        InputStream inStream = null;
        X509Certificate tmpCa = null;
        if (cert != null) {
            try {
                inStream = new ByteArrayInputStream(cert.getBytes("UTF-8"));
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                tmpCa = (X509Certificate) cf.generateCertificate(inStream);
            } catch (CertificateException | UnsupportedEncodingException e) {
                Timber.e("Failed to parse certificate", e);
            } finally {
                try {
                    if (inStream != null)
                        inStream.close();
                } catch (IOException e) {
                    Timber.e("Certificate error", e);
                }
            }
        }
        final X509Certificate ca = tmpCa;
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        if (ca == null) {
                            // Accept credentials without a cert
                            Timber.d("Accepting any certificate");
                            return;
                        }
                        Timber.d("Verifying Syncthing certificate");
                        try {
                            for (X509Certificate cert : chain) {
                                cert.verify(ca.getPublicKey());
                            }
                        } catch (NoSuchAlgorithmException | InvalidKeyException |
                                NoSuchProviderException | SignatureException e) {
                            throw new CertificateException("Untrusted Certificate!", e);
                        }
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public static SSLSocketFactory createSyncthingSSLSocketFactory(String certPath) {
//        final TrustManager[] trustAllCerts = new TrustManager[]{
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                        InputStream inStream = null;
//                        try {
//                            inStream = new FileInputStream(certPath);
//                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                            X509Certificate ca = (X509Certificate)
//                                    cf.generateCertificate(inStream);
//                            for (X509Certificate cert : chain) {
//                                cert.verify(ca.getPublicKey());
//                            }
//                        } catch (FileNotFoundException | NoSuchAlgorithmException | InvalidKeyException |
//                                NoSuchProviderException | SignatureException e) {
//                            throw new CertificateException("Untrusted Certificate!", e);
//                        } finally {
//                            try {
//                                if (inStream != null)
//                                    inStream.close();
//                            } catch (IOException e) {
//                                Timber.e("SSL verification error", e);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                }
//        };
//        try {
//            final SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustAllCerts, null);
//            return sslContext.getSocketFactory();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
