package com.hdu.libnetwork.cache;

import java.util.Date;

import androidx.room.TypeConverter;

public class DataConverter {

    @TypeConverter
    public static Long data2Long(Date date){
        return date.getTime();
    }

    @TypeConverter
    public static Date long2Date(Long date){
        return new Date(date);
    }
}
