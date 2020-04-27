package com.example.root.mimotask.lesson

import com.example.root.mimotask.Rx2SchedulersOverrideRule
import com.example.root.mimotask.entity.LessonUiElement
import com.example.root.mimotask.entity.Result
import com.example.root.mimotask.lesson.viewmodel.LessonViewModel
import com.example.root.mimotask.lesson.viewmodel.LessonViewModel.State
import com.example.root.mimotask.repo.LessonsRepo
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LessonViewModelTest {

    @get:Rule
    internal var rxSchedulersOverrideRule = Rx2SchedulersOverrideRule()

    private val mockedParsedUiData = listOf(
        LessonUiElement.TextElement("val", "#FF0000"),
        LessonUiElement.EditableTextElement(mutableListOf(), ": Int"),
        LessonUiElement.TextElement(" = 42", "#FFFFFF")
    )
    private val mockedActiveLesson = Result.ActiveLesson(1, mockedParsedUiData)
    private val repo: LessonsRepo = mock {
        on { loadActiveLesson() }.thenReturn(Observable.just(mockedActiveLesson))
    }
    private val testObserver = TestObserver<State>()
    private var viewModel = LessonViewModel(repo)

    @Test
    fun `lessons loaded successfully and LessonLoaded state produced`() {
        viewModel.loadActiveLesson()
        viewModel.observeStateChanges().subscribe(testObserver)

        assertTrue(testObserver.values().first() is State.LessonsLoaded)
    }

    @Test
    fun `lesson completed produces LessonLoaded state`() {
        viewModel.completeLesson(mockedActiveLesson)
        viewModel.observeStateChanges().subscribe(testObserver)

        assertTrue(testObserver.values().first() is State.LessonsLoaded)
    }

    @Test
    fun `correct input produces processing state with correct flag`() {
        val editableTextElement =
            mockedParsedUiData.component2() as LessonUiElement.EditableTextElement

        viewModel.observeTextChanges(mock(), ": Int", editableTextElement)
        viewModel.observeStateChanges().subscribe(testObserver)

        val state = testObserver.values().first()
        assertTrue(state is State.LessonProcessing)
        assertTrue((testObserver.values().first() as State.LessonProcessing).enableRunButton)
    }

    @Test
    fun `state is correct when loading lessons throw an Error`() {
        val publishSubject = PublishSubject.create<Result>()

        val repo: LessonsRepo = mock {
            on { loadActiveLesson() }.thenReturn(publishSubject)
        }
        publishSubject.onError(Exception("Test error"))

        val viewModel = LessonViewModel(repo)
        viewModel.loadActiveLesson()
        viewModel.observeStateChanges().subscribe(testObserver)

        val state = testObserver.values().first()
        assertTrue(state is State.Error)
        assertEquals((state as State.Error).message, "Test error")
    }
}