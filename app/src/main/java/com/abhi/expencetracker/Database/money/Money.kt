package com.abhi.expencetracker.Database.money

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.Date

@Entity(tableName = "money")
data class Money(

    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var amount: String,
    var discription: String,
    var type: String,
    var date: Date

)
