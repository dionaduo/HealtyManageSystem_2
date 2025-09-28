package cn.cszyedu.miku.yyh.dto;

import lombok.Data;

import java.util.List;

@Data
public class VolunteerExportRequest {
    private List<Long> ids;
    private VolunteerQueryDTO queryDTO;
}