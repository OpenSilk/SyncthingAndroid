package syncthing.android.identicon;

//https://github.com/davidhampgonsalves/Contact-Identicons
public interface HashGeneratorInterface {
	byte[] generate(String userName);
}