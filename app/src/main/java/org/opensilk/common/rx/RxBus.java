package org.opensilk.common.rx;

import org.opensilk.common.core.dagger2.ActivityScope;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

/**
 * Uber simple event bus. all access must be on main thread
 *
 * Created by drew on 3/12/15.
 */
@ActivityScope
public class RxBus {
    public static final String SERVICE_NAME = RxBus.class.getName();

    final BehaviorSubject<Object> monitorSubject = BehaviorSubject.create();

    @Inject
    public RxBus() {
    }

    public void post(Object event) {
        monitorSubject.onNext(event);
    }

    public <T> Subscription  subscribe(Action1<T> onNext, Class<T> type) {
        return monitorSubject.asObservable().ofType(type).subscribe(onNext);
    }

}
