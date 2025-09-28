package cn.cszyedu.miku.yyh.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@TableName("volunteer")
public class Volunteer {
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
    private Integer auditStatus;
    private Integer source;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}