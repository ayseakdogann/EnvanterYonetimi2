package com.koleksiyon.envanter.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemDTO {
    private String name;
    private String type;
    private String description;
    private Double startingPrice;
    private MultipartFile imageFile;
}