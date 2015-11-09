package syncthing.android.identicon;

import java.security.MessageDigest;

import timber.log.Timber;

//https://github.com/davidhampgonsalves/Contact-Identicons
public class MessageDigestHashGenerator implements HashGeneratorInterface {
    MessageDigest messageDigest;

    public MessageDigestHashGenerator(String algorithim) {
        try {
            messageDigest = MessageDigest.getInstance(algorithim);
        }catch(Exception e) {
            Timber.e(e, "Unable to obtain digest %s", algorithim);
        }
    }

    public byte[] generate(String input) {
        synchronized (this) {
            if (messageDigest != null) {
                messageDigest.reset();
                return messageDigest.digest(input.getBytes());
            } else {
                return input.getBytes();
            }
        }
    }
}