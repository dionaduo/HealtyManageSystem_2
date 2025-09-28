package cn.cszyedu.miku.yyh.service;

import cn.cszyedu.miku.yyh.domain.Volunteer;
import cn.cszyedu.miku.yyh.dto.VolunteerAuditForm;
import cn.cszyedu.miku.yyh.dto.VolunteerDTO;
import cn.cszyedu.miku.yyh.dto.VolunteerQueryDTO;
import cn.cszyedu.miku.yyh.vo.VolunteerDetailVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VolunteerService extends IService<Volunteer> {
    Volunteer addVolunteer(VolunteerDTO form, Long currentUserId);
    Volunteer updateVolunteer(VolunteerDTO form);
    void deleteVolunteer(Long id);
    VolunteerDetailVO getVolunteerDetail(Long id);
    Page<VolunteerDetailVO> getVolunteerPage(VolunteerQueryDTO queryDTO);
    void auditVolunteer(VolunteerAuditForm form);
    void batchAuditVolunteer(VolunteerAuditForm form);
    // 新增导出方法
    List<VolunteerDetailVO> getVolunteersForExport(List<Long> ids);
    List<VolunteerDetailVO> getAllVolunteersForExport(VolunteerQueryDTO queryDTO);
}