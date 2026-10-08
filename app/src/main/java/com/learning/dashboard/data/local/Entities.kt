package com.learning.dashboard.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
)

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("courseId")],
)
data class LessonEntity(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val position: Int,
    val title: String,
    val completed: Boolean,
    /** True when completed locally but not yet acknowledged by the server. */
    val pendingSync: Boolean = false,
)

data class CourseWithLessons(
    @Embedded val course: CourseEntity,
    @Relation(parentColumn = "id", entityColumn = "courseId")
    val lessons: List<LessonEntity>,
)
