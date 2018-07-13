package com.yjh.project.commitprogress.presenter.ownerRepo

import com.omjoonkim.project.interviewtask.model.Person
import com.yjh.project.commitprogress.di.app.App
import com.yjh.project.commitprogress.domain.Repository.UserDataNetworkRepository
import javax.inject.Inject


class OwnerRepoPresenter(
        val view: OwnerRepoContract.View
) : OwnerRepoContract.UserActionsListener{

    @Inject lateinit var userDataNetworkRepository: UserDataNetworkRepository

    init { App.component.inject(this) }

    override fun loadRepositories(userName : String) {
        userDataNetworkRepository.getRepositories(userName)
                .subscribe {
                    repo -> view.showRepositories(repo.sortedByDescending { repo -> repo.first.stargazersCount })
                }
    }

    override fun openRepositoriesDetails(repoName: String) {
        view.moveRepositoryDetailUi(repoName)
    }

    override fun loadProfile(userName : String) {
        userDataNetworkRepository.getUserProfile(userName)
                .subscribe { response -> view.showProfile(response) }
    }

    override fun openStargazerProfile(person: Person) {
        view.showProfile(person)
    }
}