package com.example.bio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "username") val name: String,
    @ColumnInfo(name = "email") val email: String,
    // plain text password without hash
    @ColumnInfo(name = "password") val password: String
)
