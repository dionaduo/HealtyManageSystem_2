package cn.cszyedu.miku.yyh.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public  class VolunteerExportVO {
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String sex;

    @ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("服务类型")
    private String serviceTypeName;

    @ExcelProperty("服务内容")
    private String serviceContentName;

    @ExcelProperty("持证上岗")
    private String certificateFlag;

    @ExcelProperty("周末接单")
    private String weekendsTakeOrders;

    @ExcelProperty("审核状态")
    private String auditStatus;

    @ExcelProperty("创建时间")
    private String createTime;
}
