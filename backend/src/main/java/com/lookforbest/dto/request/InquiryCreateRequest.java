package com.lookforbest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryCreateRequest {

    @NotNull
    private Long robotId;

    @NotBlank
    @Size(min = 10, max = 2000, message = "询价内容长度须在 10~2000 字之间")
    private String message;

    @Size(max = 100)
    private String contactName;

    @NotBlank
    @Email
    @Size(max = 200)
    private String contactEmail;

    @Size(max = 50)
    private String contactPhone;

    @Size(max = 200)
    private String contactCompany;
}
