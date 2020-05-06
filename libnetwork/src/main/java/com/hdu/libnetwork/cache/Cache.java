package com.hdu.libnetwork.cache;

import java.io.Serializable;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "cache")
public class Cache implements Serializable {
    @PrimaryKey
    @NonNull
    public String key;

    //@ColumnInfo(name = "_data") 重命名数据库表中的列名
    public byte[] data;

 //   @TypeConverters(value = {DataConverter.class}) //转换数据类型 存入数据库
   //  public Date mDate;


}
