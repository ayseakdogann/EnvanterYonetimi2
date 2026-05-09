package com.koleksiyon.envanter.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data
public class UserDTO {
    private String bio;
    private String city;
    private LocalDate birthDate;
    private MultipartFile profileImageFile;
}