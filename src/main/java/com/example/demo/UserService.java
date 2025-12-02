package com.example.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Datastore datastore;
    private final KeyFactory userKeyFactory;

    // Fallback store if Datastore is unavailable (for local / Cloud Shell)
    private final Map<String, String> inMemoryUsers = new ConcurrentHashMap<>();

    public UserService() {
        Datastore tmpDs = null;
        KeyFactory tmpKf = null;
        try {
            tmpDs = DatastoreOptions.getDefaultInstance().getService();
            tmpKf = tmpDs.newKeyFactory().setKind("User");
        } catch (Exception e) {
            // If this fails, we will just use inMemoryUsers
            e.printStackTrace();
        }
        this.datastore = tmpDs;
        this.userKeyFactory = tmpKf;
    }

    public boolean userExists(String username) {
        // Fallback if Datastore is not configured
        if (datastore == null || userKeyFactory == null) {
            return inMemoryUsers.containsKey(username);
        }

        try {
            Key key = userKeyFactory.newKey(username);
            Entity entity = datastore.get(key);
            return entity != null;
        } catch (DatastoreException e) {
            e.printStackTrace();
            // On error, fall back to in-memory
            return inMemoryUsers.containsKey(username);
        }
    }

    public void registerUser(String username, String password) {
        // Try Datastore first
        if (datastore != null && userKeyFactory != null) {
            try {
                Key key = userKeyFactory.newKey(username);
                Entity entity = Entity.newBuilder(key)
                        .set("username", username)
                        .set("password", password) // plain text for class project
                        .build();
                datastore.put(entity);
            } catch (DatastoreException e) {
                e.printStackTrace();
            }
        }

        // Always also save in memory so login works even if Datastore fails
        inMemoryUsers.put(username, password);
    }

    public boolean validateUser(String username, String password) {
        // First check in-memory fallback
        if (inMemoryUsers.containsKey(username)) {
            return inMemoryUsers.get(username).equals(password);
        }

        // Then try Datastore
        if (datastore != null && userKeyFactory != null) {
            try {
                Key key = userKeyFactory.newKey(username);
                Entity entity = datastore.get(key);
                if (entity == null) {
                    return false;
                }
                String stored = entity.getString("password");
                return stored.equals(password);
            } catch (DatastoreException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
