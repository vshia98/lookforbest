package com.lookforbest.dto.response;

import com.lookforbest.entity.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryDTO {

    private Long id;
    private Long robotId;
    private String robotName;
    private String robotSlug;
    private Long manufacturerId;
    private String manufacturerName;
    private String message;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String contactCompany;
    private String status;
    private String replyContent;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;

    public static InquiryDTO from(Inquiry inquiry) {
        return InquiryDTO.builder()
                .id(inquiry.getId())
                .robotId(inquiry.getRobot().getId())
                .robotName(inquiry.getRobot().getName())
                .robotSlug(inquiry.getRobot().getSlug())
                .manufacturerId(inquiry.getManufacturer().getId())
                .manufacturerName(inquiry.getManufacturer().getName())
                .message(inquiry.getMessage())
                .contactName(inquiry.getContactName())
                .contactEmail(inquiry.getContactEmail())
                .contactPhone(inquiry.getContactPhone())
                .contactCompany(inquiry.getContactCompany())
                .status(inquiry.getStatus().name())
                .replyContent(inquiry.getReplyContent())
                .repliedAt(inquiry.getRepliedAt())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
