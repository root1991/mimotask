package com.example.root.mimotask.entity

sealed class Result {
    data class ActiveLesson(
        val lessonId: Long = -1,
        val lessonUiElements: List<LessonUiElement> = listOf()
    ) : Result()
    object AllLessonsComplete : Result()
}


sealed class LessonUiElement {
    data class TextElement(
        val text: String,
        val color: String
    ) : LessonUiElement()

    data class EditableTextElement(
        val textElements: MutableList<Editable>,
        var text: String = ""
    ) : LessonUiElement()

    data class Editable(
        val color: String,
        val startIndex: Int,
        val endIndex: Int
    )
}

