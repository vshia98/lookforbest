package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompareRequest {

    @NotEmpty
    @Size(min = 2, max = 4, message = "对比机器人数量需在2-4个之间")
    private List<Long> robotIds;
}
