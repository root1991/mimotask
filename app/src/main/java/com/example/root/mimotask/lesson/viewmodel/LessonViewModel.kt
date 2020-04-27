package com.example.root.mimotask.lesson.viewmodel

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.ViewModel
import com.example.root.mimotask.entity.LessonUiElement.EditableTextElement
import com.example.root.mimotask.lesson.viewmodel.LessonViewModel.State.*
import com.example.root.mimotask.entity.Result.ActiveLesson
import com.example.root.mimotask.entity.Result.AllLessonsComplete
import com.example.root.mimotask.repo.LessonsRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject

class LessonViewModel(private val lessonsRepo: LessonsRepo) : ViewModel() {

    private val state = BehaviorSubject.create<State>()
    internal fun observeStateChanges() = state.hide()

    private val disposables = CompositeDisposable()

    fun loadActiveLesson() {
        state.onNext(Loading)
        disposables += lessonsRepo.loadActiveLesson()
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                when (it) {
                    is ActiveLesson -> LessonsLoaded(it)
                    is AllLessonsComplete -> State.AllLessonsComplete
                }
            }.subscribeBy(
                onNext = { state.onNext(it) },
                onError = {
                    state.onNext(Error(it.message ?: "Something wend wrong"))
                }
            )
    }

    fun completeLesson(activeLesson: ActiveLesson) {
        lessonsRepo.insertLessonComplete(activeLesson)
        loadActiveLesson()
    }

    fun dispose() {
        disposables.clear()
    }

    fun observeTextChanges(
        spannable: Spannable,
        charSequence: CharSequence,
        editableTextElement: EditableTextElement
    ) {
        if (charSequence.isNotEmpty()) {
            editableTextElement.textElements.forEach {
                if (charSequence.length in it.startIndex..it.endIndex) {
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(it.color)),
                        it.startIndex,
                        charSequence.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        state.onNext(LessonProcessing(editableTextElement.text == charSequence.toString()))
    }

    sealed class State {
        object Loading : State()
        data class LessonsLoaded(val activeLesson: ActiveLesson) : State()
        data class LessonProcessing(val enableRunButton: Boolean = false) : State()
        data class Error(val message: String) : State()
        object AllLessonsComplete : State()
    }
}