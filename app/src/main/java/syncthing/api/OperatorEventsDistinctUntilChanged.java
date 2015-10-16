/**
 * Copyright 2014 Netflix, Inc.
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package syncthing.api;

import rx.Observable.Operator;
import rx.Subscriber;
import syncthing.api.model.event.Event;
import syncthing.api.model.event.EventType;
import timber.log.Timber;

/**
 * Returns an Observable that emits all sequentially distinct items emitted by the source.
 * @param <T> the value type
 * @param <U> the key type
 */
public final class OperatorEventsDistinctUntilChanged implements Operator<Event, Event> {

    public static final OperatorEventsDistinctUntilChanged INSTANCE = new OperatorEventsDistinctUntilChanged();

    private OperatorEventsDistinctUntilChanged() {}

    @Override
    public Subscriber<? super Event> call(final Subscriber<? super Event> child) {
        return new Subscriber<Event>(child) {
            EventType previousType;
            boolean hasPrevious;
            @Override
            public void onNext(Event e) {
                EventType currentType = previousType;
                EventType type = e.type;
                previousType = type;

                if (hasPrevious) {
                    //We only want to filter certain events
                    boolean canDrop;
                    switch (currentType) {
                        case PING:
                            canDrop = true;
                            break;
                        default:
                            canDrop = false;
                            break;
                    }
                    if (canDrop && currentType == type) {
                        Timber.i("Dropping event %s", type);
                        request(1);
                    } else {
                        child.onNext(e);
                    }
                } else {
                    hasPrevious = true;
                    child.onNext(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

        };
    }

}
