package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryReplyRequest {

    @NotBlank
    @Size(min = 5, max = 2000)
    private String replyContent;
}
