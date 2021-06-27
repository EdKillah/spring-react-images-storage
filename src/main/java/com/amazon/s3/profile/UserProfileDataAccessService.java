package com.amazon.s3.profile;

import com.amazon.s3.datastore.UserProfileDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserProfileDataAccessService {

    @Autowired
    private UserProfileDataStore userProfileDataStore;


    List<UserProfile> getUserProfiles(){
        return userProfileDataStore.findAll();
    }

}
