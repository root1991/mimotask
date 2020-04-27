package com.example.root.mimotask.persistence

import com.example.root.mimotask.entity.Result.ActiveLesson

class LessonsInMemoryDataSource {
    private val lessons = mutableListOf<ActiveLesson>()

    fun addLessons(activeLessons: List<ActiveLesson>): List<ActiveLesson> {
        this.lessons.clear()
        this.lessons.addAll(activeLessons)
        return this.lessons
    }

    fun lessonsCache() = lessons

}