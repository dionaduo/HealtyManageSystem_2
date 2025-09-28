package cn.cszyedu.miku.yyh.dto;

import lombok.Data;

@Data
public class VolunteerQueryDTO {
    private String name;
    private String mobile;
    private Integer auditStatus;
    private Long serviceTypeId;
    private Integer certificateFlag;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}