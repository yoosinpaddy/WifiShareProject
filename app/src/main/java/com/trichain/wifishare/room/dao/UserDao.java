package com.trichain.wifishare.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.trichain.wifishare.room.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void addUser(User user);

    //Get all users
    @Query("SELECT * FROM users ")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users WHERE id=:user_id")
    LiveData<User> getUserById(int user_id);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    /*Custom update query*/
    @Query("UPDATE users SET username=:user_name WHERE id=:user_id")
    void updateUsername(String user_name, int user_id);

}
