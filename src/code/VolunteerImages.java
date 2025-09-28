package cn.cszyedu.miku.yyh.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@TableName("volunteer_images")
public class VolunteerImages {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long volunteerId;

    private String certificateImage;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}