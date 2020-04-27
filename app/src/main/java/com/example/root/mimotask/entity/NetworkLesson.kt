package com.example.root.mimotask.entity

data class ResponseStructure(val lessons: List<Lesson>)
data class Lesson(val id: Long, val content: List<Content>, val input: Input? = null)
data class Content(val color: String, val text: String)
data class Input(var startIndex: Int, var endIndex: Int)