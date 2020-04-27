package com.example.root.mimotask

import com.example.root.mimotask.network.LessonsRemoteDataSource
import com.example.root.mimotask.persistence.LessonsInMemoryDataSource
import com.example.root.mimotask.repo.LessonsRepo
import com.squareup.sqldelight.android.AndroidSqliteDriver
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import com.example.root.mimotask.LessonResultQueries as LessonResultLocalDataSource

object ServiceLocator {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://mimochallenge.azurewebsites.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    private val lessonsRemoteDataSource: LessonsRemoteDataSource
        get() = retrofit.create(LessonsRemoteDataSource::class.java)

    private val lessonsInMemoryDataSource: LessonsInMemoryDataSource = LessonsInMemoryDataSource()

    private val lessonResultLocalDataSource: LessonResultLocalDataSource
        get() = database.lessonResultQueries

    private val lessonsRepo: LessonsRepo
        get() = LessonsRepo(lessonsRemoteDataSource, lessonsInMemoryDataSource, lessonResultLocalDataSource)

    private val database: Database by lazy {
        Database(AndroidSqliteDriver(Database.Schema, MimoApplication.appContext, "contacts.db"))
    }

    val viewModelFactory by lazy {
        ViewModelFactory(lessonsRepo)
    }

}