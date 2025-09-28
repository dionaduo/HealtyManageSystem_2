package cn.cszyedu.miku.yyh.controller;

import cn.cszyedu.miku.yyh.common.BusinessException;
import cn.cszyedu.miku.yyh.common.Result;
import cn.cszyedu.miku.yyh.domain.Volunteer;
import cn.cszyedu.miku.yyh.dto.VolunteerAuditForm;
import cn.cszyedu.miku.yyh.dto.VolunteerDTO;
import cn.cszyedu.miku.yyh.dto.VolunteerExportRequest;
import cn.cszyedu.miku.yyh.dto.VolunteerQueryDTO;
import cn.cszyedu.miku.yyh.service.VolunteerService;
import cn.cszyedu.miku.yyh.service.impl.VolunteerServiceImpl;
import cn.cszyedu.miku.yyh.vo.VolunteerDetailVO;
import cn.cszyedu.miku.yyh.vo.VolunteerExportVO;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static cn.cszyedu.miku.yyh.util.GetUserById.getCurrentUserId;

@RestController
@RequestMapping("/volunteer")
@Tag(name = "志愿者管理", description = "志愿者增删改查接口")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final VolunteerServiceImpl volunteerServiceImpl;

    @PostMapping("/add")
    @Operation(summary = "新增志愿者")
    public Result<Volunteer> addVolunteer(@RequestBody VolunteerDTO form, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            Volunteer volunteer = volunteerService.addVolunteer(form, currentUserId);
            return Result.ok(volunteer);
        } catch (Exception e) {
            return Result.error("新增志愿者失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新志愿者信息")
    public Result<Volunteer> updateVolunteer(@RequestBody VolunteerDTO form) {
        try {
            Volunteer volunteer = volunteerService.updateVolunteer(form);
            return Result.ok(volunteer);
        } catch (Exception e) {
            return Result.error("更新志愿者失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除志愿者")
    public Result<String> deleteVolunteer(@PathVariable Long id) {
        try {
            volunteerService.deleteVolunteer(id);
            return Result.ok("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取志愿者详情")
    public Result<VolunteerDetailVO> getVolunteerDetail(@PathVariable Long id) {
        try {
            VolunteerDetailVO detail = volunteerService.getVolunteerDetail(id);
            return Result.ok(detail);
        } catch (Exception e) {
            return Result.error("获取志愿者详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询志愿者列表")
    public Result<Page<VolunteerDetailVO>> getVolunteerPage(VolunteerQueryDTO queryDTO) {
        try {
            Page<VolunteerDetailVO> page = volunteerService.getVolunteerPage(queryDTO);
            return Result.ok(page);
        } catch (Exception e) {
            return Result.error("查询志愿者列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/audit")
    @Operation(summary = "审核志愿者")
    public Result<String> auditVolunteer(@RequestBody VolunteerAuditForm form) {
        try {
            volunteerService.auditVolunteer(form);
            return Result.ok("审核成功");
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    @PostMapping("/batchAudit")
    @Operation(summary = "批量审核志愿者")
    public Result<String> batchAuditVolunteer(@RequestBody VolunteerAuditForm form) {
        try {
            volunteerService.batchAuditVolunteer(form);
            return Result.ok("批量审核成功");
        } catch (Exception e) {
            return Result.error("批量审核失败: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    @Operation(summary = "导出志愿者数据")
    public void exportVolunteers(@RequestBody VolunteerExportRequest exportRequest,
                                 HttpServletResponse response) {
        try {
            List<VolunteerDetailVO> volunteers;

            if (exportRequest.getIds() != null && !exportRequest.getIds().isEmpty()) {
                // 导出选中的志愿者
                volunteers = volunteerService.getVolunteersForExport(exportRequest.getIds());
            } else {
                // 导出所有或根据条件导出
                volunteers = volunteerService.getAllVolunteersForExport(exportRequest.getQueryDTO());
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("志愿者数据_" + System.currentTimeMillis(), StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 生成Excel
            EasyExcel.write(response.getOutputStream(), VolunteerExportVO.class)
                    .sheet("志愿者数据")
                    .doWrite(volunteerServiceImpl.convertToExportVO(volunteers));

        } catch (Exception e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }
}