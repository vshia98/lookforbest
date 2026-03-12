package com.lookforbest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManufacturerApplicationRequest {

    @NotBlank(message = "公司名称不能为空")
    @Size(max = 300, message = "公司名称不能超过300字")
    private String companyName;

    @Size(max = 300, message = "英文公司名称不能超过300字")
    private String companyNameEn;

    @NotBlank(message = "国家不能为空")
    @Size(max = 100, message = "国家不能超过100字")
    private String country;

    @Size(max = 500, message = "官网地址不能超过500字")
    private String websiteUrl;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 100, message = "联系人不能超过100字")
    private String contactPerson;

    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 200, message = "联系邮箱不能超过200字")
    private String contactEmail;

    @Size(max = 50, message = "联系电话不能超过50字")
    private String contactPhone;

    @Size(max = 500, message = "营业执照URL不能超过500字")
    private String businessLicense;

    private String description;

    /** 认领已有厂商时填写 */
    private Long manufacturerId;
}
