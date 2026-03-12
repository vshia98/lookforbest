package com.lookforbest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReviewCreateRequest {

    @NotNull(message = "机器人ID不能为空")
    private Long robotId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 300, message = "标题不能超过300字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String pros;

    private String cons;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;

    private List<String> images;
}
