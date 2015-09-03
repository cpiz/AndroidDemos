package com.cpiz.android.playground.RetrofitTest;

import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxServicesFactory;
import com.google.gson.Gson;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Retrofit使用
 *
 * Created by caijw on 2015/9/3.
 */
public class RetrofitTestActivity extends BaseTestActivity {
    private static final String TAG = "RetrofitTestActivity";

    @Override
    public void onLeftClick() {
        RxServicesFactory.getService(IService.class).getUser("cpiz")
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        appendLine("doOnSubscribe");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        appendLine("doOnUnsubscribe");
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        appendLine("doOnTerminate");
                    }
                })
                .subscribe(new Action1<GitUser>() {
                    @Override
                    public void call(GitUser gitUser) {
                        appendLine("GitUsf: " + new Gson().toJson(gitUser));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        appendLine(String.format("Get git user info failed, error=%s", throwable));
                    }
                });
    }

    static class GitUser {
        String login;
        int id;
        String avatar_url;
        String url;
    }

    private interface IService {
        @GET("/users/{username}")
        Observable<GitUser> getUser(
                @Path("username") String username
        );
    }
}
