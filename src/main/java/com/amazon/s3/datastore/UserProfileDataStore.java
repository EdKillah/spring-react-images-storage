package com.amazon.s3.datastore;

import com.amazon.s3.profile.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileDataStore extends MongoRepository<UserProfile, UUID> {
}















