// VolunteerForm.java
package cn.cszyedu.miku.yyh.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class VolunteerDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.AUTO)
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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long serviceTypeId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long serviceContentId;

    private String serviceContentName; // 新增字段

    private Integer auditStatus;

    private List<ServiceContentInfo> serviceContents;

    @Data
    public static class ServiceContentInfo {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long serviceTypeId;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long serviceContentId;
    }
}

