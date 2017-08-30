package com.cpiz.android.playground.RetrofitTest;

import android.util.Log;

import com.cpiz.android.common.OkHttpHelper;
import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxServiceBuilder;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
        Observable<GitUser> obCache = Observable.create(e -> {
            if (mGitUser != null) {
                e.onNext(mGitUser);
            }
            e.onComplete();
        });

        Observable<GitUser> obRemote = new RxServiceBuilder()
                .client(OkHttpHelper.newClient())
                .create(IService.class)
                .getUser("cpiz")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(gitUser -> {
                    mGitUser = gitUser;
                    Log.i(TAG, "set data to cache");
                });

        Observable.concat(obCache, obRemote)
                .first(new GitUser())
                .timeout(5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(() -> Log.i(TAG, "doOnDispose"))
                .subscribe(gitUser -> {
                    Log.i(TAG, String.format("data = %s", gitUser));
                    appendLine("data: " + new Gson().toJson(gitUser));
                }, throwable -> {
                    Log.i(TAG, "Error occured", throwable);
                    appendLine(String.format("Get data failed, error=%s", throwable));
                });
    }

    static class GitUser {
        GitUser() {
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
