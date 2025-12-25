package com.animal.avatar.charactor.maker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.animal.avatar.charactor.maker.data.local.dao.UserDao
import com.animal.avatar.charactor.maker.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}