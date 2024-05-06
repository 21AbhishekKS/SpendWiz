package com.abhi.expencetracker.Database.money

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "money")
data class Money(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var amount: String,
    var discription: String,
    var type: String,
    var date: String

)
