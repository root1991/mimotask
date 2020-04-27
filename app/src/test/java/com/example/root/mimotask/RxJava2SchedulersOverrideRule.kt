package com.example.root.mimotask

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class Rx2SchedulersOverrideRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                Rx2SchedulersOverride.overrideSchedulers(Schedulers.trampoline())
                base.evaluate()
                Rx2SchedulersOverride.resetSchedulers()
            }
        }
    }
}

internal object Rx2SchedulersOverride {

    fun overrideSchedulers(scheduler: Scheduler) {
        RxAndroidPlugins.reset()
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler }

        RxJavaPlugins.reset()
        RxJavaPlugins.setIoSchedulerHandler { scheduler }
        RxJavaPlugins.setComputationSchedulerHandler { scheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { scheduler }
    }

    fun resetSchedulers() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }
}