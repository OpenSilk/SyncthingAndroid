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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import syncthing.api.model.Event;
import timber.log.Timber;

/**
 * Created by drew on 3/2/15.
 */
public class EventMonitor {

    public interface EventListener {
        void handleEvent(Event e);
        void onError(Error e);
    }

    public static enum Error {
        UNAUTHORIZED,
        STOPPING,
        DISCONNECTED,
        UNKNOWN,
    }

    final SyncthingApi restApi;
    final EventListener listener;

    volatile long lastEvent = 0;
    int unhandledErrorCount = 0;
    int connectExceptionCount = 0;
    Subscription eventSubscription;

    public EventMonitor(SyncthingApi restApi, EventListener listener) {
        this.restApi = restApi;
        this.listener = listener;
    }

    public void start() {
        start(500);
    }

    public void start(long delay) {
        //TODO check connectivity and fail fast
        eventSubscription = Observable.timer(delay, TimeUnit.MILLISECONDS)
                .flatMap(ii -> restApi.events(lastEvent))
                .flatMap(events -> {
                    if (lastEvent == 0) {
                        // if we are just starting
                        // we eat all the events to
                        // avoid flooding the clients
                        List<Event> topass = new ArrayList<>();
                        int dispached = 0;
                        for (Event e : events) {
                            switch (e.type) {
                                case STARTUP_COMPLETE:
                                case DEVICE_REJECTED:
                                case FOLDER_REJECTED:
                                    dispached++;
                                    topass.add(e);
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (dispached == 0) {
                            topass.add(events[events.length - 1]);
                        }
                        return Observable.from(topass);
                    } else {
                        return Observable.from(events);
                    }
                })
                //drop duplicate events some events like INDEX_UPDATED or STATE_CHANGED will flood
                .distinctUntilChanged(event -> event.type)
                //.debounce(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        event -> {
                            listener.handleEvent(event);
                            lastEvent = event.id;
                            unhandledErrorCount = 0;
                            connectExceptionCount = 0;
                        },
                        t -> {
                            if (t instanceof RetrofitError) {
                                RetrofitError e = (RetrofitError) t;
                                switch (e.getKind()) {
                                    case NETWORK: {
                                        Throwable cause = t.getCause();
                                        unhandledErrorCount--;//network errors handle themselves
                                        if (cause instanceof SocketTimeoutException) {
                                            //just means no events for long time, can safely ignore
                                            Timber.w("SocketTimeout: %s", cause.getMessage());
                                        } else if (cause instanceof ConnectException) {
                                            //We could either be offline or the server could be
                                            //offline, or the server could still be booting
                                            //or other stuff idk, so we retry for a while
                                            //before giving up.
                                            Timber.w("ConnectException: %s", cause.getMessage());
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
                                            Timber.e(cause, "Unhandled network error");
                                            listener.onError(Error.DISCONNECTED);
                                            unhandledErrorCount++;//undo decrement above
                                        }
                                        break;
                                    }
                                    case CONVERSION: {
                                        //Shouldnt happen so dont ignore
                                        throw new RuntimeException(e.getCause());
                                    }
                                    case HTTP: {
                                        //NOTE cause is null here
                                        Response r = e.getResponse();
                                        Timber.w("HTTP Error: code=%d, reason=%s", r.getStatus(), r.getReason());
                                        if (r.getStatus() == 401) {
                                            listener.onError(Error.UNAUTHORIZED);
                                        } else {
                                            //TODO maybe less generic
                                            listener.onError(Error.STOPPING);
                                        }
                                        return;
                                    }
                                    default: {
                                        Timber.e(e.getCause(), "Unknown RetrofitError:");
                                        unhandledErrorCount++;//undo decrement above
                                        break;
                                    }
                                }
                            } else if (t instanceof RejectedExecutionException) {
                                //Ideally this wont happen, but were running on a bounded pool
                                unhandledErrorCount--;//ignore this error
                                Timber.e("RejectedExecutionException: %s", t.getMessage());
                            } else {
                                Timber.e(t, "Unforeseen Exception: %s %s",t.getClass().getSimpleName(), t.getMessage());
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
                        },
                        this::start
                );
    }

    public void stop() {
        if (eventSubscription != null) {
            eventSubscription.unsubscribe();
        }
    }

    public boolean isRunning() {
        return eventSubscription != null && !eventSubscription.isUnsubscribed();
    }

    public void resetCounter() {
        lastEvent = 0;
    }

}
