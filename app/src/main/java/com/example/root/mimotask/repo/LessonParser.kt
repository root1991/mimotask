package com.example.root.mimotask.repo

import com.example.root.mimotask.entity.Input
import com.example.root.mimotask.entity.Lesson
import com.example.root.mimotask.entity.LessonUiElement
import com.example.root.mimotask.entity.LessonUiElement.*

/**
 * Entry point to the parser. This function processes each activeLesson
 * and converts content data to more convenient data classes
 * @see LessonLocal has lessonId and List<LessonUiElement>
 * @see LessonUiElement is sealed class which is a parent for
 * @see TextElement class which includes string and color to be used
 * @see EditableTextElement class which has text and List<Editable>
 * @see Editable is a class which has color and start/end indexes to separate
 * different colors options
 */
fun Lesson.mapLessonContentToUIObject(): List<LessonUiElement> {
    val lessonItems = mutableListOf<LessonUiElement>()
    // when we don't have input object we are not parsing input at all
    if (input != null) {
        var statement = ""
        var inputParsed = false
        var contentItemStart = 1
        // As input could cover different content elements we should cache EditableTextElement
        var editableTextElement: EditableTextElement? = null

        content.forEach {
            statement += it.text
            if (statement.hasInput(input.startIndex) && !inputParsed) {
                //This happens when our content item contains input
                nonEditableOnStart(statement, input, it.color, contentItemStart)
                    .storeIfNotNull(lessonItems)

                val endIndex = if(statement.hasEndInput(input.endIndex)) input.endIndex else
                    statement.length
                // EndIndex could be different (if content item has last input index or not)
                editableTextElement = editableTextElement?.processEditable(
                    statement, contentItemStart,
                    endIndex, it.color
                ) ?: createEditable(statement, input, it.color, endIndex)
                // If EditableTextElement is already created we just need to calculate indexes
                // and add new item to List<Editable>

                if (statement.hasEndInput(input.endIndex)) {
                    //When content item has endIndex of input, we can finish parsing input
                    editableTextElement.storeIfNotNull(lessonItems)
                    nonEditableEnd(statement, input, it.color).storeIfNotNull(lessonItems)
                    //Here we set that input parsing is finished
                    inputParsed = true
                }
            } else {
                // This happens when content doesn't contain input
                // or input was already parsed
                lessonItems.add(TextElement(it.text, it.color))
            }
            contentItemStart += it.text.length
        }
    } else {
        //This happens for lessons without input field
        lessonItems.addAll(content.map {
            TextElement(it.text, it.color)
        })
    }
    return lessonItems
}

//This is used when there is non editable part in content
//before input
private fun nonEditableOnStart(
    text: String,
    input: Input,
    color: String,
    contentItemStart: Int
): LessonUiElement? {
    if (contentItemStart < input.startIndex) {
        val plainText = text.substring(contentItemStart - 1, input.startIndex - 1)
        return TextElement(plainText, color)
    }
    return null
}

private fun nonEditableEnd(text: String, input: Input, color: String): LessonUiElement? {
    if (input.endIndex != text.length) {
        val plaintText = text.substring(input.endIndex, text.length)
        return TextElement(plaintText, color)
    }
    return null
}

private fun EditableTextElement.processEditable(
    statement: String,
    contentItemStart: Int,
    endIndex: Int,
    color: String
): EditableTextElement {
    val inputText = text

    val inputChunk = statement.substring(
        contentItemStart - 1,
        endIndex
    )

    val editable = Editable(
        color,
        inputText.length,
        inputText.length + inputChunk.length
    )

    this.text = inputText + inputChunk
    textElements.add(editable)
    return this
}


private fun createEditable(
    text: String, input: Input,
    color: String, endIndex: Int
): EditableTextElement {
    val inputChunk = text.substring(
        input.startIndex - 1,
        endIndex
    )
    val textElement = Editable(
        color,
        startIndex = 0,
        endIndex = inputChunk.length
    )
    return EditableTextElement(
        mutableListOf(textElement),
        text = inputChunk
    )
}

private fun LessonUiElement?.storeIfNotNull(lessonItems: MutableList<LessonUiElement>) {
    if (this != null) lessonItems.add(this)
}

private fun String.hasInput(startIndex: Int) = length >= startIndex
private fun String.hasEndInput(endIndex: Int) = hasInput(endIndex)