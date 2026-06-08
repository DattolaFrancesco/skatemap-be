package fra.skatemap.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@Service
public class StorageService {
    private final S3Client s3Client;

    @Value("${cloudflare.bucket-raw}")
    private String bucketRaw;

    @Value("${cloudflare.bucket-processed}")
    private String bucketProcessed;

    @Value("${cloudflare.endpoint}")
    private String endpoint;

    @Value("${cloudflare.public.url}")
    private String publicUrl;
    @Value("${cloudflare.raw.url}")
    private String rawUrl;

    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(File file, String fileName){
        String key = "images/" + UUID.randomUUID() +"/" +fileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketProcessed)
                .key(key)
                .contentType("image/jpeg")
                .build();
        s3Client.putObject(request, RequestBody.fromFile(file));
        return key;
    }
    public String uploadRawVideo(File file, String fileName){
        String key = "videos/raw/" + UUID.randomUUID() +"/" +fileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketRaw)
                .key(key)
                .contentType("video/mp4")
                .build();
        s3Client.putObject(request, RequestBody.fromFile(file));
        return key;
    }
    public void delete(String bucket, String key) {
        s3Client.deleteObject(r -> r
                .bucket(bucket)
                .key(key)
        );
    }
    public String getPublicUrl(String key) {
        return publicUrl + "/" + key;
    }
    public String getRawUrl(String key) {
        return rawUrl + "/" + key;
    }

}
