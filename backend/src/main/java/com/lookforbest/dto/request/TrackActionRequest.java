package com.lookforbest.dto.request;

import com.lookforbest.entity.UserActionLog.ActionType;
import com.lookforbest.entity.UserActionLog.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TrackActionRequest {

    @NotBlank(message = "session_id不能为空")
    private String sessionId;

    @NotNull(message = "action_type不能为空")
    private ActionType actionType;

    @NotNull(message = "target_type不能为空")
    private TargetType targetType;

    private Long targetId;
    private String targetSlug;
    private Map<String, Object> extraData;
}
