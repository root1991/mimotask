package com.example.root.mimotask.repo

import com.example.root.mimotask.LessonResult
import com.example.root.mimotask.entity.Result
import com.example.root.mimotask.entity.Result.AllLessonsComplete
import com.example.root.mimotask.entity.Result.ActiveLesson
import com.example.root.mimotask.network.LessonsRemoteDataSource
import com.example.root.mimotask.persistence.LessonsInMemoryDataSource
import com.squareup.sqldelight.runtime.rx.asObservable
import com.squareup.sqldelight.runtime.rx.mapToList
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import com.example.root.mimotask.LessonResultQueries as LessonResultLocalDataSource

class LessonsRepo(
    private val lessonsRemoteDataSource: LessonsRemoteDataSource,
    private val lessonsInMemoryDataSource: LessonsInMemoryDataSource,
    private val lessonResultLocalDataSource: LessonResultLocalDataSource
) {

    private fun loadLessons(): Observable<List<ActiveLesson>> =
        if (lessonsInMemoryDataSource.lessonsCache().isEmpty()) {
            lessonsRemoteDataSource.loadLessons()
                .subscribeOn(Schedulers.io())
                .map {
                    lessonsInMemoryDataSource.addLessons(it.lessons.map { networkLesson ->
                        ActiveLesson(
                            lessonId = networkLesson.id,
                            lessonUiElements = networkLesson.mapLessonContentToUIObject()
                        )
                    })
                }
        } else {
            Observable.just(lessonsInMemoryDataSource.lessonsCache())
        }

    fun insertLessonComplete(activeLesson: ActiveLesson) {
        lessonResultLocalDataSource.insertLessonCompleted(
            lessonCompleted = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString(),
            id = activeLesson.lessonId
        )
    }

    fun loadActiveLesson(): Observable<Result> = Observable.zip(
        loadLessons(), lessonResultLocalDataSource.selectAll().asObservable().mapToList().retry(),
        BiFunction<List<ActiveLesson>,
                List<LessonResult>, Result> { lessons: List<ActiveLesson>,
                                                                               results: List<LessonResult> ->
            lessons.firstOrNull { lesson ->
                !results.any { it.id == lesson.lessonId && it.lessonCompleted != null }
            } ?: AllLessonsComplete

        }).doOnNext {
        if (it is ActiveLesson) {
            lessonResultLocalDataSource.insertLessonResult(
                id = it.lessonId,
                lessonStarted = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
            )
        }
    }

    fun clearResults() {
        lessonResultLocalDataSource.deleteResults()
    }
}