package com.hrm.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.hrm.common.Constants;
import com.hrm.utils.FileUtil;

@Service
public class AwsS3Service {
	private final AmazonS3 amazonS3;
	
	@Autowired 
	public AwsS3Service(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}
	
	@Value("${aws.s3.bucket_image}")
	private String bucketImage;
	@Value("${aws.s3.endpointUrl}")
    private String endpointUrl;
	
	private String evidenceImage = "its-hrm-images/evidences";
	
	public String uploadImage(final MultipartFile mpfile, String fileName, Integer imageType) throws Exception {
		if(mpfile.isEmpty()) {
			throw new IllegalStateException("Empty File");
		}
		if(!Constants.EXTENSION_ACCEPT_IMAGE.contains(mpfile.getContentType())) {
			throw new IllegalStateException("Upload file is not an image type");
		}
		String bucketDirectory = imageType==Constants.AVATAR?bucketImage:imageType==Constants.EVIDENCE?evidenceImage:"";
		uploadFileToS3(bucketDirectory, mpfile, fileName);
		
		return endpointUrl.concat("/").concat(bucketDirectory).concat("/").concat(fileName);
	}
	
	private void uploadFileToS3(final String bucket, final MultipartFile mpfile, final String fileName) throws Exception {
		try {
			final File file = FileUtil.convertMPFileToFile(mpfile);
			if(file.exists()) {
				final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead);
				amazonS3.putObject(putObjectRequest);
				file.delete();
			}
		} catch (Exception awsErr) {
			throw new Exception(awsErr.getMessage());
		}
	}
}
