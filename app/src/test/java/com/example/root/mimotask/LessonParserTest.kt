package com.example.root.mimotask

import com.example.root.mimotask.entity.Content
import com.example.root.mimotask.entity.Input
import com.example.root.mimotask.entity.Lesson
import com.example.root.mimotask.entity.LessonUiElement
import com.example.root.mimotask.repo.mapLessonContentToUIObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LessonParserTest {

    private val mockedLessons = listOf(
        Lesson(
            id = 1, content = listOf(
                Content("#FF0000", "class "),
                Content("#FFFFFF", "SimpleClass "),
                Content("#FF0000", "extends"),
                Content("#FFFFFF", " SimpleBaseClass"),
                Content("#FF0000", " implements "),
                Content("#FFFFFF", "SimpleInterface"),
                Content("#FFFFFF", " {} ")
            ), input = Input(1, 1)
        ), Lesson(
            id = 2, content = listOf(
                Content("#FF0000", "setContentView("),
                Content("#FF0000", text = "R.layout.activity_main"),
                Content("#FF0000", text = ")")
            )
        )
    )

    @Test
    fun `input starts and ends in the same content item`() {
        mockedLessons.first().input?.startIndex = 7
        mockedLessons.first().input?.endIndex = 15

        val parsedData = mockedLessons.first().mapLessonContentToUIObject()
        val input = parsedData.find { it is LessonUiElement.EditableTextElement }

        assertEquals("SimpleCla", (input as LessonUiElement.EditableTextElement).text)
    }

    @Test
    fun `input starts in the middle and ends in the same content item`() {
        mockedLessons.first().input?.startIndex = 10
        mockedLessons.first().input?.endIndex = 15

        val parsedData = mockedLessons.first().mapLessonContentToUIObject()
        val input = parsedData.find { it is LessonUiElement.EditableTextElement }

        assertEquals("pleCla", (input as LessonUiElement.EditableTextElement).text)
    }

    @Test
    fun `input starts in the end and ends in the next content item`() {
        mockedLessons.first().input?.startIndex = 6
        mockedLessons.first().input?.endIndex = 15

        val parsedData = mockedLessons.first().mapLessonContentToUIObject()
        val input = parsedData.find { it is LessonUiElement.EditableTextElement }

        assertEquals(" SimpleCla", (input as LessonUiElement.EditableTextElement).text)
        assertEquals(2, input.textElements.size)
    }

    @Test
    fun `input starts in the middle and ends in the "n" content item`() {
        mockedLessons.first().input?.startIndex = 4
        mockedLessons.first().input?.endIndex = 30

        val parsedData = mockedLessons.first().mapLessonContentToUIObject()
        val input = parsedData.find { it is LessonUiElement.EditableTextElement }

        assertEquals(
            "ss SimpleClass extends Simp",
            (input as LessonUiElement.EditableTextElement).text
        )
        assertEquals(4, input.textElements.size)
    }

    @Test
    fun `input has one char length`() {
        val parsedData = mockedLessons.first().mapLessonContentToUIObject()
        val input = parsedData.find { it is LessonUiElement.EditableTextElement }

        assertEquals(
            "c",
            (input as LessonUiElement.EditableTextElement).text
        )
    }

    @Test
    fun `no EditableTextElement if input is null`() {
        val parsedData = mockedLessons.component2().mapLessonContentToUIObject()
        val input = parsedData.find { it is LessonUiElement.EditableTextElement }

        assertNull(input)
    }

    @Test
    fun `parsed data has correct length without input`() {
        val parsedData = mockedLessons.component2().mapLessonContentToUIObject()
        assertEquals(3, parsedData.size)
    }
}
