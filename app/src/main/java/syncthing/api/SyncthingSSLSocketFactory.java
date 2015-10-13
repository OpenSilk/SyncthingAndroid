/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package syncthing.api;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

    public static X509Certificate makeCert(String cert) {
        X509Certificate tmpCa = null;
        if (cert != null) {
            InputStream inStream = null;
            try {
                inStream = new ByteArrayInputStream(cert.getBytes("UTF-8"));
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                tmpCa = (X509Certificate) cf.generateCertificate(inStream);
            } catch (CertificateException | UnsupportedEncodingException e) {
                Timber.e("Failed to parse certificate", e);
            } finally {
                IOUtils.closeQuietly(inStream);
            }
        }
        return tmpCa;
    }

    public static SSLSocketFactory create(X509Certificate cert) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new SyncthingTrustManager(cert)}, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class SyncthingTrustManager implements X509TrustManager {
        final X509Certificate ca;

        public SyncthingTrustManager(X509Certificate ca) {
            this.ca = ca;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Timber.w("All clients trusted");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (ca == null) {
                // Accept credentials without a cert
                Timber.w("All servers trusted");
                return;
            }
            Timber.i("Verifying Syncthing certificate");
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

}
