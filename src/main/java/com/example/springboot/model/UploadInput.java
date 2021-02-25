package com.example.springboot.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UploadInput {
    private String accountName;
    private String accountKey;
    private String fileName;
    private String message;
}
