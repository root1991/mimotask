package com.example.root.mimotask.network

import com.example.root.mimotask.entity.ResponseStructure
import io.reactivex.Observable
import retrofit2.http.GET

interface LessonsRemoteDataSource {

    @GET("/api/lessons")
    fun loadLessons(): Observable<ResponseStructure>
}