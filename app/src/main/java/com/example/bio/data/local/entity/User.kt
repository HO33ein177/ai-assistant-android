package com.example.bio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index // Import Index


@Entity(
    tableName = "users",
    // Add indices for columns you query often
    indices = [Index(value = ["email"], unique = true), Index(value = ["firebaseUid"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Keep auto-generated primary key for local relations
    val email: String,
    val password: String, // Keep this field, but store "" or null after signup
    val name: String? = null, // Make name nullable if it's optional
    val firebaseUid: String? = null // <<< ADD THIS FIELD (make nullable initially)
)