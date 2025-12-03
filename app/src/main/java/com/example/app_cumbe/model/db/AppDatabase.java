package com.example.app_cumbe.model.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NotificacionEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract NotificacionDao notificacionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "cumbe_database")
                            .allowMainThreadQueries() // Simplificación para empezar (luego usaremos hilos)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}