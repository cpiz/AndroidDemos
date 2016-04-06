package com.cpiz.android.playground.RetrofitTest;

import android.util.Log;

import com.cpiz.android.common.OkHttpHelper;
import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxServiceBuilder;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Retrofit使用
 * <p/>
 * Created by caijw on 2015/9/3.
 */
public class RetrofitTestActivity extends BaseTestActivity {
    private static final String TAG = "RetrofitTestActivity";
    private GitUser mGitUser = null;

    @Override
    public void onLeftClick() {
        Observable<GitUser> obCache = Observable.create(new Observable.OnSubscribe<GitUser>() {
            @Override
            public void call(Subscriber<? super GitUser> subscriber) {
                subscriber.onNext(mGitUser);
                subscriber.onCompleted();
            }
        });

        Observable<GitUser> obRemote = new RxServiceBuilder()
                .client(OkHttpHelper.newClient())
                .create(IService.class)
                .getUser("cpiz")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Action1<GitUser>() {
                    @Override
                    public void call(GitUser gitUser) {
                        mGitUser = gitUser;
                        Log.i(TAG, "set data to cache");
                    }
                });

        Observable.concat(obCache, obRemote)
                .first(new Func1<GitUser, Boolean>() {
                    @Override
                    public Boolean call(GitUser gitUser) {
                        return gitUser != null;
                    }
                })
                .timeout(5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "doOnUnsubscribe");
                    }
                })
                .subscribe(new Action1<GitUser>() {
                    @Override
                    public void call(GitUser gitUser) {
                        Log.i(TAG, String.format("data = %s", gitUser));
                        appendLine("data: " + new Gson().toJson(gitUser));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.i(TAG, "Error occured", throwable);
                        appendLine(String.format("Get data failed, error=%s", throwable));
                    }
                });
    }

    static class GitUser {
        public GitUser() {
            Log.i(TAG, "GitUser constructed");
        }

        String login;
        int id;
        String avatar_url;
        String url;

        @Override
        public String toString() {
            return "GitUser{" +
                    "login='" + login + '\'' +
                    ", id=" + id +
                    ", avatar_url='" + avatar_url + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    private interface IService {
        @GET("https://api.github.com/users/{username}")
        Observable<GitUser> getUser(
                @Path("username") String username
        );
    }
}
