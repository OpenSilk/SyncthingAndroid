package syncthing.android.identicon;

import java.security.MessageDigest;

//https://github.com/davidhampgonsalves/Contact-Identicons
public class MessageDigestHashGenerator implements HashGeneratorInterface {
	MessageDigest messageDigest;

	public MessageDigestHashGenerator(String algorithim) {
		try {
			messageDigest = MessageDigest.getInstance(algorithim);
		}catch(Exception e) {
			System.err.println("Error setting algorithim: " + algorithim);
		}
	}

	public byte[] generate(String input) {
        synchronized (this) {
            return messageDigest.digest(input.getBytes());
        }
	}

}