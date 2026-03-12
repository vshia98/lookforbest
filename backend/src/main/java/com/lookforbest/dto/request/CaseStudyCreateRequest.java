package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CaseStudyCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 300, message = "标题不能超过300字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private List<Long> robotIds;

    @Size(max = 100, message = "行业分类不能超过100字")
    private String industry;

    private List<String> images;
}
