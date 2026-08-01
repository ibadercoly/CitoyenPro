package com.ibader.citoyenpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ibader.citoyenpro.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE firebase_uid = :firebaseUid")
    suspend fun getByFirebaseUid(firebaseUid: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY nom")
    fun getAll(): Flow<List<UserEntity>>
}
