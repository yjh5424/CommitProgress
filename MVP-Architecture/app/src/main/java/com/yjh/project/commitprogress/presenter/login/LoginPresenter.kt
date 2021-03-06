package com.yjh.project.commitprogress.presenter.login

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.yjh.project.commitprogress.di.app.App
import com.google.firebase.auth.GithubAuthProvider
import com.yjh.project.commitprogress.di.module.AppModule
import com.yjh.project.commitprogress.domain.Repository.UserTokenNetworkRepository
import com.yjh.project.commitprogress.domain.model.AccessToken
import com.yjh.project.commitprogress.presenter.base.BasePresenter
import io.reactivex.disposables.CompositeDisposable
import okhttp3.HttpUrl
import java.math.BigInteger
import java.util.*
import javax.inject.Inject


class LoginPresenter @Inject constructor(
        var userTokenNetworkRepository: UserTokenNetworkRepository,
        var sharedPreferences: SharedPreferences,
        disposable: CompositeDisposable
) : LoginContract.UserActionListener, BasePresenter<LoginContract.View>(disposable) {

    private fun saveToken(uId : String){
        sharedPreferences.edit().putString(AppModule.USER_ID_KEY,uId).commit()
    }

    override fun confirmSession() {
        var userName =sharedPreferences.getString(AppModule.USER_ID_KEY,"null")

        if( userName != "null") {
            view?.moveMainActivity(userName);
        }else{
            view?.onError()
        }
    }

    override fun loadGithubToken(mAuth: FirebaseAuth,code : String, state : String) {
        userTokenNetworkRepository.getAccessToken(App.CLIENT_ID,App.CLIENT_SECRET,code,App.redirect_uri,state)
                .subscribe { response-> loginWithGithub(mAuth,response) }
    }

    override fun loginWithGithub(mAuth: FirebaseAuth, accessToken: AccessToken) {

        val credential = GithubAuthProvider.getCredential(accessToken.accessToken)

        mAuth.signInWithCredential(credential).addOnCompleteListener { it ->
            if (it.isSuccessful) {
                view?.moveMainActivity(it.result.additionalUserInfo.username)
                saveToken(it.result.additionalUserInfo.username)
            }
        }
    }

    override fun loadGithubWebView() {
        val httpUrl = HttpUrl.Builder()
                .scheme("http")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id",App.CLIENT_ID)
                .addQueryParameter("redirect_uri", App.redirect_uri)
                .addQueryParameter("state", getRandomString())
                .addQueryParameter("scope", "user")
                .addQueryParameter("allow_signup","false")
                .build()

        view?.moveGithubWebView(httpUrl)
    }

    private fun getRandomString(): String = BigInteger(130, Random()).toString(32)

}