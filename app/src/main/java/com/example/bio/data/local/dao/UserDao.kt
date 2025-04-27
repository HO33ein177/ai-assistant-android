package com.example.bio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bio.data.local.entity.User

@Dao
interface UserDao{
    @Insert
    suspend fun insert(user : User): Long // return the id of the inserted user

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :userid")
    suspend fun getUserById(userid: Int): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?



}