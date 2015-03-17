package syncthing.android.identicon;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by drew on 3/11/15.
 */
@Module
public class IdenticonModule {
    @Provides @Singleton
    public HashGeneratorInterface provideHashGenerator() {
        return new MessageDigestHashGenerator("MD5");
    }
}
