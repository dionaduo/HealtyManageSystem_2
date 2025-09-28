package cn.cszyedu.miku.yyh.service.impl;

import cn.cszyedu.miku.yyh.common.BusinessException;
import cn.cszyedu.miku.yyh.domain.*;
import cn.cszyedu.miku.yyh.dto.VolunteerAuditForm;
import cn.cszyedu.miku.yyh.dto.VolunteerDTO;
import cn.cszyedu.miku.yyh.dto.VolunteerQueryDTO;
import cn.cszyedu.miku.yyh.mapstruct.VolunteerMapStruct;
import cn.cszyedu.miku.yyh.mapper.VolunteerMapper;
import cn.cszyedu.miku.yyh.service.*;
import cn.cszyedu.miku.yyh.vo.VolunteerDetailVO;
import cn.cszyedu.miku.yyh.vo.VolunteerExportVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl extends ServiceImpl<VolunteerMapper, Volunteer> implements VolunteerService {

    private final VolunteerImagesService volunteerImagesService;
    private final VolunteerServiceContentService volunteerServiceContentService;
    private final ServiceTypeService serviceTypeService;
    private final ServiceContentService serviceContentService;
    private final UserService userService;
    private final VolunteerMapStruct volunteerMapStruct;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Volunteer addVolunteer(VolunteerDTO form, Long currentUserId) {
        // 1. 校验身份证号是否已存在
        checkIdCardExists(form.getIdCard());
        // 2. 处理服务内容
        handleServiceContent(form);
        // 3. 创建volunteer
        Volunteer volunteer = createVolunteer(form, currentUserId);
        boolean saved = this.save(volunteer);
        if (!saved) {
            throw new BusinessException("新增志愿者失败");
        }

        // 4. 处理关联数据
        handleVolunteerImages(form, volunteer.getId());
        handleVolunteerServiceContent(form, volunteer.getUserId());

        return volunteer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Volunteer updateVolunteer(VolunteerDTO form) {
        // 1. 校验志愿者是否存在
        Volunteer oldVolunteer = this.getById(form.getId());
        if (oldVolunteer == null) {
            throw new BusinessException("志愿者不存在");
        }

        // 2. 校验身份证号是否已存在（排除自己）
        checkIdCardExists(form.getIdCard(), form.getId());

        // 3. 处理服务内容
        handleServiceContent(form);

        // 4. 更新volunteer
        Volunteer volunteer = volunteerMapStruct.formToEntity(form);
        volunteer.setUpdateTime(new Date());
        boolean updated = this.updateById(volunteer);
        if (!updated) {
            throw new BusinessException("更新志愿者失败");
        }

        // 5. 更新关联数据
        updateVolunteerImages(form, volunteer.getId());
        updateVolunteerServiceContent(form, volunteer.getUserId());

        return volunteer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVolunteer(Long id) {
        Volunteer volunteer = this.getById(id);
        if (volunteer == null) {
            throw new BusinessException("志愿者不存在");
        }

        // 删除志愿者
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException("删除志愿者失败");
        }

        // 删除关联数据
        deleteVolunteerImages(id);
        deleteVolunteerServiceContent(volunteer.getUserId());
    }

    @Override
    public VolunteerDetailVO getVolunteerDetail(Long id) {
        Volunteer volunteer = this.getById(id);
        if (volunteer == null) {
            throw new BusinessException("志愿者不存在");
        }
        VolunteerDetailVO detailVO = volunteerMapStruct.entityToDetailVO(volunteer);
        // 获取图片信息
        LambdaQueryWrapper<VolunteerImages> imagesQuery = new LambdaQueryWrapper<>();
        imagesQuery.eq(VolunteerImages::getVolunteerId, id);
        List<VolunteerImages> volunteerImagesList = volunteerImagesService.list(imagesQuery);
        if (!volunteerImagesList.isEmpty()) {
            detailVO.setCertificateImage(volunteerImagesList.get(0).getCertificateImage());
        }

        // 获取服务内容信息
        LambdaQueryWrapper<VolunteerServiceContent> serviceContentQuery = new LambdaQueryWrapper<>();
        serviceContentQuery.eq(VolunteerServiceContent::getVolunteerUserId, volunteer.getUserId());
        List<VolunteerServiceContent> volunteerServiceContentList = volunteerServiceContentService.list(serviceContentQuery);
        if (!volunteerServiceContentList.isEmpty()) {
            VolunteerServiceContent volunteerServiceContent = volunteerServiceContentList.get(0);
            detailVO.setServiceTypeId(volunteerServiceContent.getServiceTypeId());
            detailVO.setServiceContentId(volunteerServiceContent.getServiceContentId());

            ServiceType serviceType = serviceTypeService.getById(volunteerServiceContent.getServiceTypeId());
            if (serviceType != null) {
                detailVO.setServiceTypeName(serviceType.getName());
            }

            ServiceContent serviceContent = serviceContentService.getById(volunteerServiceContent.getServiceContentId());
            if (serviceContent != null) {
                detailVO.setServiceContentName(serviceContent.getServiceContentName());
            }
        }

        // 获取创建者信息
        User createUser = userService.getById(volunteer.getCreateUserId());
        if (createUser != null) {
            detailVO.setCreateUserId(createUser.getId());
        }

        return detailVO;
    }

    // VolunteerServiceImpl.java - 修复分页查询方法
    @Override
    public Page<VolunteerDetailVO> getVolunteerPage(VolunteerQueryDTO queryDTO) {
        Page<Volunteer> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());

        LambdaQueryWrapper<Volunteer> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(Volunteer::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getMobile())) {
            queryWrapper.eq(Volunteer::getMobile, queryDTO.getMobile());
        }
        if (queryDTO.getAuditStatus() != null) {
            queryWrapper.eq(Volunteer::getAuditStatus, queryDTO.getAuditStatus());
        }
        if (queryDTO.getCertificateFlag() != null) {
            queryWrapper.eq(Volunteer::getCertificateFlag, queryDTO.getCertificateFlag());
        }

        // 修复：添加服务类型ID的查询条件
        if (queryDTO.getServiceTypeId() != null) {
            // 需要通过关联表查询
            LambdaQueryWrapper<VolunteerServiceContent> serviceContentWrapper = new LambdaQueryWrapper<>();
            serviceContentWrapper.eq(VolunteerServiceContent::getServiceTypeId, queryDTO.getServiceTypeId());
            List<VolunteerServiceContent> serviceContents = volunteerServiceContentService.list(serviceContentWrapper);

            if (!serviceContents.isEmpty()) {
                List<Long> userIds = serviceContents.stream()
                        .map(VolunteerServiceContent::getVolunteerUserId)
                        .collect(Collectors.toList());
                queryWrapper.in(Volunteer::getUserId, userIds);
            } else {
                // 如果没有匹配的服务内容，返回空结果
                queryWrapper.eq(Volunteer::getId, -1);
            }
        }

        queryWrapper.orderByDesc(Volunteer::getCreateTime);

        this.page(page, queryWrapper);

        // 转换为VO
        Page<VolunteerDetailVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        voPage.setPages(page.getPages());

        List<VolunteerDetailVO> voList = page.getRecords().stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditVolunteer(VolunteerAuditForm form) {
        if (form.getIds() == null || form.getIds().isEmpty()) {
            throw new BusinessException("请选择要审核的志愿者");
        }

        LambdaUpdateWrapper<Volunteer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Volunteer::getId, form.getIds())
                .set(Volunteer::getAuditStatus, form.getAuditStatus())
                .set(Volunteer::getUpdateTime, new Date());

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new BusinessException("审核失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditVolunteer(VolunteerAuditForm form) {
        auditVolunteer(form);
    }

    private void checkIdCardExists(String idCard) {
        checkIdCardExists(idCard, null);
    }

    private void checkIdCardExists(String idCard, Long excludeId) {
        if (StringUtils.hasText(idCard)) {
            LambdaQueryWrapper<Volunteer> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Volunteer::getIdCard, idCard);
            if (excludeId != null) {
                queryWrapper.ne(Volunteer::getId, excludeId);
            }
            List<Volunteer> existingVolunteers = this.list(queryWrapper);
            if (!existingVolunteers.isEmpty()) {
                throw new BusinessException("身份证号已存在");
            }
        }
    }

    private void handleServiceContent(VolunteerDTO form) {
        if (form.getServiceTypeId() != null && StringUtils.hasText(form.getServiceContentName())) {
            // 先尝试根据名称和服务类型ID查找
            LambdaQueryWrapper<ServiceContent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ServiceContent::getServiceContentName, form.getServiceContentName())
                    .eq(ServiceContent::getServiceTypeId, form.getServiceTypeId());

            ServiceContent existingContent = serviceContentService.getOne(queryWrapper);

            if (existingContent != null) {
                // 如果存在相同名称和服务类型的服务内容，使用现有的
                form.setServiceContentId(existingContent.getServiceContentId());
            } else {
                // 创建新的服务内容
                ServiceContent newContent = new ServiceContent();
                newContent.setServiceContentName(form.getServiceContentName());
                newContent.setServiceTypeId(form.getServiceTypeId());
                newContent.setSequence(0);
                serviceContentService.save(newContent);
                form.setServiceContentId(newContent.getServiceContentId());
            }
        }
    }

    private Volunteer createVolunteer(VolunteerDTO form, Long currentUserId) {
        Volunteer volunteer = volunteerMapStruct.formToEntity(form);
        // 检查currentUserId是否为null
        if (currentUserId == null) {
            throw new BusinessException("当前用户ID不能为空");
        }
        // 手动设置ID（如果数据库不支持自增）
        if (volunteer.getId() == null) {
            // 使用时间戳作为ID，确保唯一性
            volunteer.setId(System.currentTimeMillis());
        }
        volunteer.setUserId(currentUserId);
        volunteer.setCreateTime(new Date());
        volunteer.setUpdateTime(new Date());
        volunteer.setAuditStatus(0); // 默认未审核
        volunteer.setSource(1); // 默认来源
        volunteer.setCreateUserId(currentUserId);
        return volunteer;
    }

    private void handleVolunteerImages(VolunteerDTO form, Long volunteerId) {
        if (StringUtils.hasText(form.getCertificateImage())) {
            VolunteerImages volunteerImages = new VolunteerImages();
            volunteerImages.setVolunteerId(volunteerId);
            volunteerImages.setCertificateImage(form.getCertificateImage());
            volunteerImages.setCreateTime(new Date());
            volunteerImages.setUpdateTime(new Date());
            volunteerImagesService.save(volunteerImages);
        }
    }

    // 修改handleVolunteerServiceContent方法
    private void handleVolunteerServiceContent(VolunteerDTO form, Long userId) {
        if (form.getServiceContents() != null && !form.getServiceContents().isEmpty()) {
            // 先删除原有的服务内容关联
            deleteVolunteerServiceContent(userId);

            // 添加新的服务内容关联
            for (VolunteerDTO.ServiceContentInfo serviceContent : form.getServiceContents()) {
                if (serviceContent.getServiceTypeId() != null && serviceContent.getServiceContentId() != null) {
                    VolunteerServiceContent volunteerServiceContent = new VolunteerServiceContent();
                    volunteerServiceContent.setId(System.currentTimeMillis());
                    volunteerServiceContent.setVolunteerUserId(userId);
                    volunteerServiceContent.setServiceTypeId(serviceContent.getServiceTypeId());
                    volunteerServiceContent.setServiceContentId(serviceContent.getServiceContentId());
                    volunteerServiceContent.setCreateTime(new Date());
                    volunteerServiceContent.setUpdateTime(new Date());
                    volunteerServiceContentService.save(volunteerServiceContent);
                }
            }
        }
    }

    private void updateVolunteerImages(VolunteerDTO form, Long volunteerId) {
        LambdaQueryWrapper<VolunteerImages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VolunteerImages::getVolunteerId, volunteerId);
        List<VolunteerImages> volunteerImagesList = volunteerImagesService.list(queryWrapper);
        VolunteerImages volunteerImages = volunteerImagesList.isEmpty() ? null : volunteerImagesList.get(0);

        if (volunteerImages != null) {
            if (StringUtils.hasText(form.getCertificateImage())) {
                volunteerImages.setCertificateImage(form.getCertificateImage());
                volunteerImages.setUpdateTime(new Date());
                volunteerImagesService.updateById(volunteerImages);
            } else {
                volunteerImagesService.remove(queryWrapper);
            }
        } else if (StringUtils.hasText(form.getCertificateImage())) {
            handleVolunteerImages(form, volunteerId);
        }
    }

    private void updateVolunteerServiceContent(VolunteerDTO form, Long userId) {
        LambdaQueryWrapper<VolunteerServiceContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VolunteerServiceContent::getVolunteerUserId, userId);
        List<VolunteerServiceContent> volunteerServiceContentList = volunteerServiceContentService.list(queryWrapper);
        VolunteerServiceContent volunteerServiceContent = volunteerServiceContentList.isEmpty() ? null : volunteerServiceContentList.get(0);

        if (volunteerServiceContent != null) {
            if (form.getServiceTypeId() != null && form.getServiceContentId() != null) {
                volunteerServiceContent.setServiceTypeId(form.getServiceTypeId());
                volunteerServiceContent.setServiceContentId(form.getServiceContentId());
                volunteerServiceContent.setUpdateTime(new Date());
                volunteerServiceContentService.updateById(volunteerServiceContent);
            } else {
                volunteerServiceContentService.remove(queryWrapper);
            }
        } else if (form.getServiceTypeId() != null && form.getServiceContentId() != null) {
            handleVolunteerServiceContent(form, userId);
        }
    }

    private void deleteVolunteerImages(Long volunteerId) {
        LambdaQueryWrapper<VolunteerImages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VolunteerImages::getVolunteerId, volunteerId);
        volunteerImagesService.remove(queryWrapper);
    }

    private void deleteVolunteerServiceContent(Long userId) {
        LambdaQueryWrapper<VolunteerServiceContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VolunteerServiceContent::getVolunteerUserId, userId);
        volunteerServiceContentService.remove(queryWrapper);
    }

    private VolunteerDetailVO convertToDetailVO(Volunteer volunteer) {
        VolunteerDetailVO detailVO = volunteerMapStruct.entityToDetailVO(volunteer);

        // 获取服务内容信息
        LambdaQueryWrapper<VolunteerServiceContent> serviceContentQuery = new LambdaQueryWrapper<>();
        serviceContentQuery.eq(VolunteerServiceContent::getVolunteerUserId, volunteer.getUserId());
        List<VolunteerServiceContent> volunteerServiceContentList = volunteerServiceContentService.list(serviceContentQuery);
        
        if (!volunteerServiceContentList.isEmpty()) {
            VolunteerServiceContent volunteerServiceContent = volunteerServiceContentList.get(0);
            ServiceType serviceType = serviceTypeService.getById(volunteerServiceContent.getServiceTypeId());
            if (serviceType != null) {
                detailVO.setServiceTypeName(serviceType.getName());
            }

            ServiceContent serviceContent = serviceContentService.getById(volunteerServiceContent.getServiceContentId());
            if (serviceContent != null) {
                detailVO.setServiceContentName(serviceContent.getServiceContentName());
            }
        }

        return detailVO;
    }

    // VolunteerServiceImpl.java
    @Override
    public List<VolunteerDetailVO> getVolunteersForExport(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Volunteer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Volunteer::getId, ids);
        List<Volunteer> volunteers = this.list(queryWrapper);

        return volunteers.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VolunteerDetailVO> getAllVolunteersForExport(VolunteerQueryDTO queryDTO) {
        LambdaQueryWrapper<Volunteer> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(Volunteer::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getMobile())) {
            queryWrapper.eq(Volunteer::getMobile, queryDTO.getMobile());
        }
        if (queryDTO.getAuditStatus() != null) {
            queryWrapper.eq(Volunteer::getAuditStatus, queryDTO.getAuditStatus());
        }
        if (queryDTO.getCertificateFlag() != null) {
            queryWrapper.eq(Volunteer::getCertificateFlag, queryDTO.getCertificateFlag());
        }

        queryWrapper.orderByDesc(Volunteer::getCreateTime);

        List<Volunteer> volunteers = this.list(queryWrapper);

        return volunteers.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    public List<VolunteerExportVO> convertToExportVO(List<VolunteerDetailVO> volunteers) {
        return volunteers.stream().map(vo -> {
            VolunteerExportVO exportVO = new VolunteerExportVO();
            exportVO.setName(vo.getName());
            exportVO.setSex(vo.getSex() == 1 ? "男" : "女");
            exportVO.setAge(vo.getAge());
            exportVO.setMobile(vo.getMobile());
            exportVO.setIdCard(vo.getIdCard());
            exportVO.setServiceTypeName(vo.getServiceTypeName());
            exportVO.setServiceContentName(vo.getServiceContentName());
            exportVO.setCertificateFlag(vo.getCertificateFlag() == 1 ? "是" : "否");
            exportVO.setWeekendsTakeOrders(vo.getWeekendsTakeOrders() == 1 ? "是" : "否");
            exportVO.setAuditStatus(getAuditStatusText(vo.getAuditStatus()));
            exportVO.setCreateTime(vo.getCreateTime() != null ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vo.getCreateTime()) : "");
            return exportVO;
        }).collect(Collectors.toList());
    }

    private String getAuditStatusText(Integer status) {
        switch (status) {
            case 0: return "未审核";
            case 1: return "已通过";
            case 2: return "未通过";
            default: return "未知";
        }
    }
}