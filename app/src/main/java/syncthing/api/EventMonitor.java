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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import retrofit.HttpException;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.HandlerScheduler;
import rx.observers.Subscribers;
import syncthing.api.model.event.Event;
import syncthing.api.model.event.EventType;
import timber.log.Timber;

/**
 * Created by drew on 3/2/15.
 */
public class EventMonitor {

    public interface EventListener {
        void handleEvent(Event e);
        void onError(Error e);
    }

    public enum Error {
        UNAUTHORIZED,
        STOPPING,
        DISCONNECTED,
        UNKNOWN,
    }

    final SyncthingApi restApi;
    final EventListener listener;
    final Observable<Event> eventsObservable;
    final Observer<Event> eventsObserver;

    final AtomicLong lastEvent = new AtomicLong(0);

    int unhandledErrorCount = 0;
    int connectExceptionCount = 0;
    Subscription eventSubscription;

    boolean running;
    HandlerThread handlerThread;
    Scheduler scheduler;

    public EventMonitor(SyncthingApi restApi, EventListener listener) {
        this.restApi = restApi;
        this.listener = listener;
        this.eventsObservable = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .flatMap(ii -> getRestCall())
                .flatMap(events -> {
                    if (events == null || events.length == 0) {
                        lastEvent.set(0);
                        return Observable.empty();
                    } else {
                        lastEvent.set(events[events.length - 1].id);
                        return Observable.from(events);
                    }
                }).filter(event -> {
                    // drop unknown events
                    if (event.type == EventType.UNKNOWN) {
                        Timber.w("Dropping unknown event %s", event.data);
                    }
                    return (event.type != null && event.type != EventType.UNKNOWN);
                    //drop selected duplicate events like PING
                }).lift(OperatorEventsDistinctUntilChanged.INSTANCE);
        this.eventsObserver = new Observer<Event>() {
            @Override
            public void onCompleted() {
                start();
            }

            @Override
            public void onError(Throwable t) {
                unhandledErrorCount--;//network errors handle themselves
                if (t instanceof HttpException) {
                    HttpException e = (HttpException) t;
                    Timber.w("HttpException code=%d msg=%s", e.code(), e.message());
                    if (e.code() == 401) {
                        listener.onError(Error.UNAUTHORIZED);
                    } else {
                        listener.onError(Error.STOPPING);
                    }
                    return;
                } else if (t instanceof SocketTimeoutException) {
                    //just means no events for long time, can safely ignore
                    Timber.w("SocketTimeout: %s", t.getMessage());
                } else if (t instanceof java.io.InterruptedIOException) {
                    //just means socket timeout / Syncthing startup stuttering
                    Timber.w("InterruptedIOException: %s", t.getMessage());
                } else if (t instanceof ConnectException) {
                    //We could either be offline or the server could be
                    //offline, or the server could still be booting
                    //or other stuff idk, so we retry for a while
                    //before giving up.
                    Timber.w("ConnectException: %s", t.getMessage());
                    if (++connectExceptionCount > 50) {
                        connectExceptionCount = 0;
                        Timber.w("Too many ConnectExceptions... server likely offline");
                        listener.onError(Error.STOPPING);
                    } else {
                        listener.onError(Error.DISCONNECTED);
                        resetCounter();//someone else could have restarted it
                        start(1200);
                    }
                    return;
                } else {
                    Timber.e(t, "Unforeseen Exception: %s %s", t.getClass().getSimpleName(), t.getMessage());
                    unhandledErrorCount++;//undo decrement above
                }
                connectExceptionCount = 0;//Incase we just came out of a connecting loop.
                if (++unhandledErrorCount < 20) {
                    start(1200);
                } else {
                    //At this point we have no fucking clue what is going on
                    Timber.w("Too many errors suspending longpoll");
                    unhandledErrorCount = 0;
                    listener.onError(Error.STOPPING);
                }
            }

            @Override
            public void onNext(Event event) {
                unhandledErrorCount = 0;
                connectExceptionCount = 0;
                listener.handleEvent(event);
            }
        };
    }

    public void start() {
        start(500);
    }

    public synchronized void start(long delay) {
        running = true;
        Timber.d("start(%d) lastEvent=%d", delay, lastEvent.get());
        if (handlerThread == null) {
            handlerThread = new HandlerThread("EventMonitor");
            handlerThread.start();
            scheduler = HandlerScheduler.from(new Handler(handlerThread.getLooper()));
        }
        //TODO check connectivity and fail fast
        eventSubscription = eventsObservable
                .subscribeOn(scheduler)
                .subscribe(Subscribers.from(eventsObserver));
    }

    private Observable<Event[]> getRestCall() {
        if (lastEvent.get() == 0) {
            //only pull the last event
            return restApi.events(0, 1);
        } else {
            return restApi.events(lastEvent.get());
        }
    }

    public synchronized void stop() {
        running = false;
        if (eventSubscription != null) {
            eventSubscription.unsubscribe();
            eventSubscription = null;
        }
        if (handlerThread != null) {
            Looper l = handlerThread.getLooper();
            if (l != null) l.quit();
            handlerThread = null;
            scheduler = null;
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public void resetCounter() {
        lastEvent.set(0);
    }

}
