// VolunteerDetailDTO.java
package cn.cszyedu.miku.yyh.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class VolunteerDetailDTO {
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
    
    // 新增字段用于存储所有图片
    private List<String> certificateImages;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long serviceTypeId;
    private String serviceTypeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long serviceContentId;
    private String serviceContentName;
    
    // 新增字段用于存储所有服务内容
    private List<ServiceContentInfo> serviceContents;

    private Integer auditStatus;
    private Date createTime;
    
    @Data
    public static class ServiceContentInfo {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long serviceTypeId;
        private String serviceTypeName;
        
        @JsonSerialize(using = ToStringSerializer.class)
        private Long serviceContentId;
        private String serviceContentName;
    }
}