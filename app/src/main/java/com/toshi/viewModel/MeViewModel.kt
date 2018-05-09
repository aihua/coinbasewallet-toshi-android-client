/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.toshi.model.local.User
import com.toshi.model.local.network.Network
import com.toshi.model.local.network.Networks
import com.toshi.model.network.Balance
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class MeViewModel(
        private val subsribeScheduler: Scheduler = Schedulers.io(),
        private val observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    private val userManager by lazy { BaseApplication.get().userManager }
    private val balanceManager by lazy { BaseApplication.get().balanceManager }

    val user by lazy { MutableLiveData<User>() }
    val singelBalance by lazy { SingleLiveEvent<Balance>() }
    val currentNetwork by lazy { MutableLiveData<Network>() }

    init {
        fetchUser()
        getCurrentNetwork()
    }

    private fun fetchUser() {
        val sub = userManager
                .getCurrentUserObservable()
                .subscribeOn(subsribeScheduler)
                .observeOn(observeScheduler)
                .subscribe(
                        { user.value = it },
                        { LogUtil.exception("Error during fetching user $it") }
                )

        subscriptions.add(sub)
    }

    fun getBalance() {
        val sub = balanceManager
                .balanceObservable
                .first()
                .toSingle()
                .subscribeOn(subsribeScheduler)
                .observeOn(observeScheduler)
                .subscribe(
                        { singelBalance.value = it },
                        { LogUtil.exception("Error showing dialog $it") }
                )

        subscriptions.add(sub)
    }

    private fun getCurrentNetwork() {
        val sub = Networks.getInstance()
                .networkObservable
                .subscribeOn(subsribeScheduler)
                .observeOn(observeScheduler)
                .subscribe(
                        { currentNetwork.value = it },
                        { LogUtil.exception("Error getting current network $it") }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}