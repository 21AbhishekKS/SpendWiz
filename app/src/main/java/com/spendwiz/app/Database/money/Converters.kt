package com.spendwiz.app.Database.money

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
     fun fromDate(date : Date) : Long{
         return date.time
     }

    @TypeConverter
    fun toDate(time : Long) : Date{
        return Date(time)
    }



    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
            return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
            return TransactionType.valueOf(value)
    }


}