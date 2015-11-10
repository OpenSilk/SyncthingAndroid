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

package syncthing.android.identicon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import org.opensilk.common.core.dagger2.ForApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import syncthing.android.R;
import timber.log.Timber;

@Singleton
public class IdenticonGenerator {

    final int size;
    final int scaledSize;
    final int color;

    @Inject
    public IdenticonGenerator(
            @ForApplication Context appContext
    ) {
        size = 5;
        scaledSize = appContext.getResources().getDimensionPixelSize(R.dimen.identicon_size);
        color = ContextCompat.getColor(appContext, R.color.grey_600);
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

    public Bitmap generate(String deviceId) {
        Identicon identicon = new Identicon(deviceId, size, scaledSize);
        return identicon.createBitmap(color);
    }

    /**
     * creates an identicon (almost) equivalent to the one created by the identiconDirective in syncthing.core
     */
    private static class Identicon {
        final String value;
        final int size;
        final int scaledSize;
        final int middleCol;

        public Identicon(String value, int size, int scaledSize) {
            this.value = value.toUpperCase().replaceAll("[\\W_]", "");
            this.size = (size % 2) == 1 ? size : size + 1; //must be odd;
            this.scaledSize = scaledSize;
            this.middleCol = (this.size - 1) / 2; //0 based idx
        }

        private boolean shouldFillRectAt(int row, int col) {
            int idx = row + col * size;
            while (idx >= value.length()) {
                //wrap around
                idx -= value.length();
            }
            return (Character.codePointAt(value, idx) % 2) == 0;
        }

        private boolean shouldMirrorRectAt(int row, int col) {
            return col != middleCol;
        }

        private int mirrorColFor(int col) {
            return size - col - 1;
        }

        public Bitmap createBitmap(int foreground) {
            int background = Color.TRANSPARENT;
            Bitmap identicon = Bitmap.createBitmap(size, size, Config.ARGB_8888);
            for (int row = 0; row < size; row++) {
                for (int col = 0; col <= middleCol; col++) {
                    int pixelColor = shouldFillRectAt(row, col) ? foreground : background;
                    identicon.setPixel(col, row, pixelColor);
                    if (shouldMirrorRectAt(row, col)) {
                        identicon.setPixel(mirrorColFor(col), row, pixelColor);
                    }
                }
            }
            Bitmap scaledIdenticon = Bitmap.createScaledBitmap(identicon, scaledSize, scaledSize, false);
            if (scaledIdenticon != identicon) {
                identicon.recycle();
            }
            return scaledIdenticon;
        }
    }


}