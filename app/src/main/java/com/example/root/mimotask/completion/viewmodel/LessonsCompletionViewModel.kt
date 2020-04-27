package com.example.root.mimotask.completion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.root.mimotask.repo.LessonsRepo

class LessonsCompletionViewModel(private val repo: LessonsRepo) : ViewModel() {

    fun doRestart() {
        repo.clearResults()
    }
}