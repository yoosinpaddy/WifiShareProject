package com.icelabs.codesnippets.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.icelabs.codesnippets.room.dao.UserDao;
import com.icelabs.codesnippets.room.model.User;

/*Add all entities here*/
@Database(entities = User.class, exportSchema = false, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;
    private static final String DB_NAME = "app_database";

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return INSTANCE;
    }

    /*Define all dao classes here*/
    public abstract UserDao userDao();

}
