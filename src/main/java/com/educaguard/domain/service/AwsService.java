package com.educaguard.domain.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

@Service
public class AwsService {

    private final String BUCKET_PATH = "recfacial/";
    private final float SIMILARITY_THRESHOLD = 75f;

    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;

    @Autowired
    private UserService userService;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public AwsService(
            @Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretAccessKey}") String secretAccessKey,
            @Value("${aws.region}") String region) {

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.valueOf(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard()
                .withRegion(Regions.valueOf(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }


    public String uploadFile(MultipartFile file, String fileName) {
        try {
            s3Client.putObject(bucketName, BUCKET_PATH + fileName, file.getInputStream(), null);
            return String.format("https://%s.s3.amazonaws.com/%s%s", bucketName, BUCKET_PATH, fileName);
        } catch (AmazonServiceException | IOException e) {
            throw new RuntimeException("Erro ao fazer upload para o S3: " + e.getMessage());
        }
    }

    public boolean faceMatch(String username, MultipartFile file) {
        String fileName = BUCKET_PATH + username;

        try {
            Image sourceImage = new Image().withS3Object(new S3Object().withBucket(bucketName).withName(fileName));
            ByteBuffer targetImageBytes = ByteBuffer.wrap(file.getBytes());
            Image targetImage = new Image().withBytes(targetImageBytes);

            // Create request
            CompareFacesRequest request = new CompareFacesRequest()
                    .withSourceImage(sourceImage)
                    .withTargetImage(targetImage)
                    .withSimilarityThreshold(SIMILARITY_THRESHOLD);

            // Compare Faces
            CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);
            List<CompareFacesMatch> faceMatches = compareFacesResult.getFaceMatches();

            // If there is a result, one or more faces match
            return !faceMatches.isEmpty();
        } catch (IOException e) {
            return false;
        }

    }
}
