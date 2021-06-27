package com.amazon.s3.profile;

import com.amazon.s3.bucket.BucketName;
import com.amazon.s3.filestore.FileStore;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;


@Service
public class UserProfileService {

    private final UserProfileDataAccessService userProfileDataAccessService;

    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessService userProfileDataAccessService, FileStore fileStore){
        this.userProfileDataAccessService = userProfileDataAccessService;
        this.fileStore = fileStore;
    }

    List<UserProfile> getUserProfiles(){
        return userProfileDataAccessService.getUserProfiles();
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file){

        //Validating file isn't empty
        isEmptyFile(file);

        //validating image format
        isImageFile(file);

        //validating userProfileId exists in db
        UserProfile user = getUserOrElseThrow(userProfileId);

        //metadata
        Map<String, String> metadata = getMetadata(file);

        //Storing in s3 bucket
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path, filename, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }


    public byte[] downloadUserProfileImage(UUID userProfileId){
        UserProfile user = getUserOrElseThrow(userProfileId);
        //String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUsername());
        String imageKey = String.format("%s",user.getUsername());
        return user.getUserProfileImageLink()
                .map(path -> {
                    System.out.println("\nEsta es el path: "+path+" imageKey: "+imageKey+"\n");
                    return fileStore.download(path, imageKey);
                })
                .orElse(new byte[0]);
    }

    private Map<String, String> getMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private UserProfile getUserOrElseThrow(UUID userProfileId) {
        return userProfileDataAccessService
                .getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("userProfileId %s not found", userProfileId)));
    }

    private void isImageFile(MultipartFile file) {
        if(!Arrays.asList(ContentType.IMAGE_PNG.getMimeType(),
                ContentType.IMAGE_GIF.getMimeType(),
                ContentType.IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("Image format not valid!");
        }
    }

    private void isEmptyFile(MultipartFile file) {
        if(file.isEmpty()) {
            throw new IllegalStateException("Can't upload empty file [" + file.getSize() + "]");
        }
    }
}







