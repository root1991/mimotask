package com.example.root.mimotask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.root.mimotask.completion.viewmodel.LessonsCompletionViewModel
import com.example.root.mimotask.lesson.viewmodel.LessonViewModel
import com.example.root.mimotask.repo.LessonsRepo

class ViewModelFactory(
    private val lessonsRepo: LessonsRepo
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when (modelClass) {
        LessonViewModel::class.java -> LessonViewModel(
            lessonsRepo
        )
        LessonsCompletionViewModel::class.java -> LessonsCompletionViewModel(
            lessonsRepo
        )
        else -> error("Unknown view model class $modelClass")
    } as T
}