package com.example.demo;

import org.springframework.stereotype.Service;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

@Service
public class UserService {

    private final Datastore datastore;
    private final KeyFactory userKeyFactory;

    public UserService() {
        // Uses the project’s default credentials and project ID
        this.datastore = DatastoreOptions.getDefaultInstance().getService();
        this.userKeyFactory = datastore.newKeyFactory().setKind("User");
    }

    // Check if a user already exists in Datastore
    public boolean userExists(String username) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);
        return entity != null;
    }

    // Register a new user in Datastore
    public void registerUser(String username, String password) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = Entity.newBuilder(key)
                .set("username", username)
                .set("password", password)   // plain text is OK for class project
                .build();
        datastore.put(entity);
    }

    // Validate login by reading from Datastore
    public boolean validateUser(String username, String password) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);

        if (entity == null) {
            return false;
        }

        String storedPassword = entity.getString("password");
        return storedPassword.equals(password);
    }
}
