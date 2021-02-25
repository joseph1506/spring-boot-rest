package com.example.springboot;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.example.springboot.model.ReadInput;
import com.example.springboot.model.UploadInput;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
public class BlobController {


	public static final String ENDPOINT_FORMAT = "https://%s.blob.core.windows.net";

	@PostMapping(path="/read",consumes = "application/json", produces = "application/json")
	public ResponseEntity<StreamingResponseBody> readFromBlob(@RequestBody ReadInput input) throws Exception {

		try{
			BlobContainerClient containerClient = getBlobContainerClient(input.getAccountName(),
					input.getAccountKey(),input.getContainerName());
			BlockBlobClient blobClient = containerClient.getBlobClient(input.getFileName()).getBlockBlobClient();
			int dataSize = (int) blobClient.getProperties().getBlobSize();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
			blobClient.download(outputStream);
			byte[] dataBytes = outputStream.toByteArray();
			outputStream.close();
			StreamingResponseBody responseBody = new StreamingResponseBody() {
				@Override
				public void writeTo(OutputStream outputStream) throws IOException {
					outputStream.write(dataBytes);
				}
			};
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+input.getFileName())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(responseBody);
		}catch (Exception e){
			e.printStackTrace();
			throw new Exception("Error while downloading--->"+input.getFileName());
		}
	}

	@PostMapping(path = "/write", consumes = "application/json", produces = "application/json")
	public String writeToBlob(@RequestBody UploadInput input) {
		try {
			BlobContainerClient containerClient = getBlobContainerClient(input.getAccountName(),
					input.getAccountKey(),input.getContainerName());
			containerClient.create();
			BlockBlobClient blobClient = containerClient.getBlobClient(input.getFileName()).getBlockBlobClient();
			InputStream dataStream = new ByteArrayInputStream(input.getMessage().getBytes(StandardCharsets.UTF_8));
			blobClient.upload(dataStream, input.getMessage().length());
			dataStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "FALSE";
		}
		return "SUCCESS";
	}

	@RequestMapping("/")
	public String serviceCheck() {
		return "Services up";
	}

	private BlobContainerClient getBlobContainerClient(String accountName, String accountKey,String containerName) {
		StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
		String endpoint = String.format(Locale.ROOT, ENDPOINT_FORMAT, accountName);
		BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
		BlobContainerClient containerClient = storageClient.getBlobContainerClient(containerName);
		return containerClient;
	}

}
