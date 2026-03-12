package com.lookforbest.dto.response;

import com.lookforbest.entity.UserMembership;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class UserMembershipDTO {

    private Long id;
    private Long userId;
    private MembershipPlanDTO plan;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Long daysRemaining;

    public static UserMembershipDTO from(UserMembership membership) {
        UserMembershipDTO dto = new UserMembershipDTO();
        dto.setId(membership.getId());
        dto.setUserId(membership.getUserId());
        dto.setPlan(MembershipPlanDTO.from(membership.getPlan()));
        dto.setStartDate(membership.getStartDate());
        dto.setEndDate(membership.getEndDate());
        dto.setStatus(membership.getStatus().name());

        if (membership.getEndDate() == null) {
            dto.setDaysRemaining(-1L); // -1 表示永久
        } else {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), membership.getEndDate());
            dto.setDaysRemaining(Math.max(0, days));
        }
        return dto;
    }
}
