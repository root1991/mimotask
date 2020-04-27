package com.example.root.mimotask.completion

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.root.mimotask.R
import com.example.root.mimotask.ServiceLocator
import com.example.root.mimotask.completion.viewmodel.LessonsCompletionViewModel
import kotlinx.android.synthetic.main.activity_lesson_completed.*

class LessonsCompletionActivity : AppCompatActivity() {

    private val viewModel: LessonsCompletionViewModel by viewModels {
        ServiceLocator.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_completed)
        buttonRestart.setOnClickListener {
            viewModel.doRestart()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}