package husaynhakeem.io.androidroom_crud.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import husaynhakeem.io.androidroom_crud.dao.AddressDao;
import husaynhakeem.io.androidroom_crud.dao.CatDao;
import husaynhakeem.io.androidroom_crud.dao.PersonDao;
import husaynhakeem.io.androidroom_crud.entity.Address;
import husaynhakeem.io.androidroom_crud.entity.Cat;
import husaynhakeem.io.androidroom_crud.entity.Person;

/**
 * Created by husaynhakeem on 6/12/17.
 */

@Database(entities = {Person.class, Address.class, Cat.class}, version = 1, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    private static AppDataBase instance;


    public abstract CatDao catDao();
    public abstract PersonDao personDao();
    public abstract AddressDao addressDao();


    public static AppDataBase getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDataBase.class,
                    "cat-owners-db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
