package cn.cszyedu.miku.yyh.mapstruct;

import cn.cszyedu.miku.yyh.domain.Volunteer;
import cn.cszyedu.miku.yyh.dto.VolunteerDTO;
import cn.cszyedu.miku.yyh.vo.VolunteerDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VolunteerMapStruct {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "auditStatus", ignore = true)
    Volunteer formToEntity(VolunteerDTO form);

    @Mapping(target = "serviceTypeName", ignore = true)
    @Mapping(target = "serviceContentName", ignore = true)
    @Mapping(target = "certificateImage", ignore = true)
    @Mapping(target = "serviceTypeId", ignore = true)
    @Mapping(target = "serviceContentId", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    VolunteerDetailVO entityToDetailVO(Volunteer volunteer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "source", ignore = true)
    void updateEntityFromForm(VolunteerDTO form, @MappingTarget Volunteer volunteer);
}