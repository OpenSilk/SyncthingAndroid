package syncthing.android.identicon;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//https://github.com/davidhampgonsalves/Contact-Identicons
@Singleton
public class IdenticonGenerator {
	public static int height = 5;
	public static int width = 5;

    final HashGeneratorInterface hashGenerator;

    @Inject
    public IdenticonGenerator(HashGeneratorInterface hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    public Observable<Bitmap> generateAsync(String userName) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap;
                try {
                    bitmap = generate(userName);
                    subscriber.onNext(bitmap);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    public Bitmap generate(String userName) {
		
		byte[] hash = hashGenerator.generate(userName);

		Bitmap identicon = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		// get byte values as unsigned ints
		int r = hash[0] & 255;
		int g = hash[1] & 255;
		int b = hash[2] & 255;

		int background = Color.TRANSPARENT;
		int foreground = Color.argb(255, r, g, b);

		for (int x = 0; x < width; x++) {
			
			//make identicon horizontally symmetrical 
			int i = x < 3 ? x : 4 - x;
			int pixelColor;
			for (int y = 0; y < height; y++) {
				
				if ((hash[i] >> y & 1) == 1)
					pixelColor = foreground;
				else
					pixelColor = background;

				identicon.setPixel(x, y, pixelColor);
			}
		}
		
		//scale image by 2 to add border
		Bitmap bmpWithBorder = Bitmap.createBitmap(12, 12, identicon.getConfig());
	    Canvas canvas = new Canvas(bmpWithBorder);
	    canvas.drawColor(background);
	    identicon = Bitmap.createScaledBitmap(identicon, 10, 10, false);
	    canvas.drawBitmap(identicon, 1, 1, null);
	    
		return bmpWithBorder;
	}
}