package com.example.root.mimotask.lesson

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.root.mimotask.R
import com.example.root.mimotask.ServiceLocator
import com.example.root.mimotask.completion.LessonsCompletionActivity
import com.example.root.mimotask.entity.LessonUiElement.EditableTextElement
import com.example.root.mimotask.entity.LessonUiElement.TextElement
import com.example.root.mimotask.lesson.viewmodel.LessonViewModel
import com.example.root.mimotask.lesson.viewmodel.LessonViewModel.State.*
import com.example.root.mimotask.entity.Result.ActiveLesson
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*

const val COMPLETION_REQUEST_CODE = 49033

class LessonActivity : AppCompatActivity() {

    private val viewModel: LessonViewModel by viewModels {
        ServiceLocator.viewModelFactory
    }

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.loadActiveLesson()

        disposable += viewModel.observeStateChanges().subscribe {
            when (it) {
                is LessonsLoaded -> processLesson(it.activeLesson)
                is Loading -> handleLoadingState(isLoading = true)
                is LessonProcessing -> buttonComplete.isEnabled = it.enableRunButton
                is Error -> handleErrorState(it.message)
                is AllLessonsComplete -> showCompletionScreen()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == COMPLETION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.loadActiveLesson()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.dispose()
    }

    private fun editTextActions(editText: EditText, element: EditableTextElement) {
        RxTextView.textChanges(editText).subscribe {
            val spannable = editText.text
            viewModel.observeTextChanges(spannable, it, element)
        }
    }

    private fun processLesson(activeLesson: ActiveLesson) {
        handleLoadingState(isLoading = false)
        activeLesson.lessonUiElements.map {
            when (it) {
                is TextElement -> TextView(this).apply {
                    text = it.text
                    setTextColor(Color.parseColor(it.color))
                }
                is EditableTextElement -> EditText(this).apply {
                    bindLesson(it)
                }
            }
        }.forEach {
            lessonContainer.addView(it)
        }
        RxView.clicks(buttonComplete).subscribe {
            viewModel.completeLesson(activeLesson)
            lessonContainer.removeAllViews()
        }
    }

    private fun EditText.bindLesson(element: EditableTextElement) {
        setBackgroundResource(android.R.color.transparent)
        width = paint.measureText(element.text).toInt()
        maxLines = 1
        filters = arrayOf(InputFilter.LengthFilter(element.text.length))

        requestFocus()
        editTextActions(this, element)
    }

    private fun showCompletionScreen() {
        startActivityForResult(
            Intent(this, LessonsCompletionActivity::class.java),
            COMPLETION_REQUEST_CODE
        )
    }

    private fun handleLoadingState(isLoading: Boolean) {
        val contentVisibility = if (isLoading) GONE else VISIBLE
        spaceContent.visibility = contentVisibility
        lessonContainer.visibility = contentVisibility
        buttonComplete.visibility = contentVisibility
        progressBarLoading.visibility = if (!isLoading) GONE else VISIBLE
    }

    private fun handleErrorState(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_dialog_title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->
                viewModel.loadActiveLesson()
            }.show()
    }
}
