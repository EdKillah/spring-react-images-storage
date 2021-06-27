package com.amazon.s3.filestore;

import com.amazon.s3.datastore.UserProfileDataStore;
import com.amazon.s3.profile.UserProfile;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStore {

    private final AmazonS3 s3;

    @Autowired
    private UserProfileDataStore userProfileDataStore;

    @Autowired
    public FileStore(AmazonS3 s3){
        this.s3 = s3;
    }

    public void save(String path,
                     String fileName,
                     Optional<Map<String, String>> optionalMetadata,
                     InputStream inputStream){
        ObjectMetadata metadata = new ObjectMetadata();
        optionalMetadata.ifPresent(map -> {
            if(!map.isEmpty()){
                map.forEach(metadata::addUserMetadata);
            }
        });


        UserProfile user = new UserProfile(UUID.randomUUID(), fileName, path);
        userProfileDataStore.save(user);
        try{
            s3.putObject(path, fileName, inputStream, metadata);
        }catch(AmazonServiceException e){
            throw new IllegalStateException("Failed to store file to S3 ",e);
        }

    }

    public byte[] download(String path, String key){
        try{
            S3Object object = s3.getObject(path,key);
            return IOUtils.toByteArray(object.getObjectContent());
        }catch(AmazonServiceException | IOException e){
            throw new IllegalStateException("Failed to download file from s3", e);

        }
    }
}













