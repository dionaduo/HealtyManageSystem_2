package cn.cszyedu.miku.yyh.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class VolunteerDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String name;
    private Integer sex;
    private String nation;
    private Integer age;
    private String idCard;
    private String mobile;
    private String address;
    private String serviceStartHour;
    private String serviceEndHour;
    private Integer certificateFlag;
    private Integer weekendsTakeOrders;
    private String idCardFrontImage;
    private String idCardBackImage;
    private String idCardHolderImage;
    private String certificateImage;
    private Integer auditStatus;
    private Integer source;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long serviceTypeId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long serviceContentId;

    private String serviceTypeName;
    private String serviceContentName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    private Date createTime;
    private Date updateTime;
}