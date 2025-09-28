package cn.cszyedu.miku.yyh.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.List;

@Data
public class VolunteerAuditForm {
    @JsonSerialize(using = ToStringSerializer.class)
    private List<Long> ids;
    private Integer auditStatus;
}