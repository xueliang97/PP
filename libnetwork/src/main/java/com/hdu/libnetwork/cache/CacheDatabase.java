package com.hdu.libnetwork.cache;

import com.hdu.libcommon.global.AppGlobals;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//创建Room数据库 注解      entities 表映射成javaBean    导出关于数据库的json文件
@Database(entities = {Cache.class},version = 1,exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {
    private static final CacheDatabase database;

    static{
        //创建一个内存数据库，只能存在于内存中，进程被杀数据丢失
        //Room.inMemoryDatabaseBuilder()
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "ppjoke_cache")
                //是否允许在主线程查询
                .allowMainThreadQueries()
                //数据库创建和打开后的回调
                //.addCallback()
                //设置查询时的线程池
                //.setQueryExecutor()
                //room日志模式
                //.setJournalMode()
                //数据库升级异常的回滚
                //.fallbackToDestructiveMigration()
                //数据库升级异常后根据指定版本回滚
                //.fallbackToDestructiveMigrationFrom()
                //    .addMigrations(CacheDatabase.sMigration )升级数据库
                .build();
    }

    public static CacheDatabase get(){
        return database;
    }

    public abstract CacheDao getCache();

//    static Migration sMigration = new Migration(1,3) {//从1升级到3
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table teacher rename to student");
//
//        }
//    };
}
