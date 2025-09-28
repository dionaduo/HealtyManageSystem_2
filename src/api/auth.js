// package/api/auth.js
import api from './axios'
import { getToken } from '@/utils/auth'

// 手动构造请求时使用
export async function manualRequest(url, options = {}) {
    const token = getToken()
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers
    }

    const response = await fetch(url, { ...options, headers })
    return response.json()
}

const instance = api.create({
    baseURL: '/api',
});

instance.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 登录
export const login = (credentials) => {
    return api.post('/user/login', credentials, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
};
// 注册
export const register = (credentials) => {
    return api.post('/user/register', credentials, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
};
// 服务类型相关API
export const typeService = () => {
    return api({
        url: '/service/serviceType',
        method: 'get'
    });
};
export const typeContent =(params={})=> {
    return api({
        url: '/service/serviceContent',
        method: 'get',
        params:params
    });
}
export const getServiceContent = (id) => {
    return api({
        url: `/service/serviceContent`,
        method: 'get'
    });
}
export const serviceContentById = (id) => {
    return api({
        url: `/service/serviceContentById/${id}`,
        method: 'get'
    });
};
export const serviceTypeById = () => {
    return api({
        url: '/service/serviceTypeById',
        method: 'get'
    });
};
// 新增服务
export const addService = (data) => {
    return api({
        url: '/service/addService',
        method: 'post',
        data: data
    });
};
// 更新服务
export const editService = (data) => {
    return api({
        url: '/service/editService',
        method: 'put',
        data: data
    });
};

// 服务列表
export const listService = (params) => {
    return api({
        url: '/service/GetServiceInfoList',
        method: 'get',
        params: params
    });
};

// 删除服务
export const delService = (id) => {
    return api({
        url: `/service/serviceDelete/${id}`,
        method: 'delete'
    });
};

export const listing = (data) => {
    return api({
        url: '/service/listing',
        method: 'post',
        data: data
    });
};

export const delist = (data) => {
    return api({
        url: '/service/delist',
        method: 'post',
        data: data
    });
};

export const listUser = (params) => {
    return api({
        url: '/user/page',
        method: 'get',
        params: params
    });
};

export const deleteUser = (id) => {
    return api({
        url: `/user/${id}`,
        method: 'delete'
    });
};

export const batchDeleteUser = (data) => {
    return api({
        url: '/user/batch',
        method: 'delete',
        data: data
    });
};

export const updateUser = (data) => {
    return api({
        url: '/user/edit',
        method: 'put',
        data: data
    });
};

export const listNeighborhood = (params) => {
    return api({
        url: '/neighborhood/list',
        method: 'get',
        params: params
    });
};

export const addNeighborhood = (data) => {
    return api({
        url: '/neighborhood/add',
        method: 'post',
        data: data
    });
};

export const editNeighborhood = (data) => {
    return api({
        url: '/neighborhood/edit',
        method: 'put',
        data: data
    });
};

export const deleteNeighborhood = (id) => {
    return api({
        url: `/neighborhood/delete/${id}`,
        method: 'delete'
    });
};

export const batchDeleteNeighborhood = (data) => {
    return api({
        url: '/neighborhood/batchDelete',
        method: 'delete',
        data: data
    });
};

export const batchAddNeighborhood = (formData) => {
    return api({
        url: '/neighborhood/batchAdd',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};

/**
 * 获取社区列表
 * @param {Object} params 查询参数
 * @returns {Promise} 社区列表数据
 */
export const listCommunityInfo = (params) => {
    return api({
        url: '/communityInfo/list',
        method: 'get',
        params
    });
};

/**
 * 新增社区信息
 * @param {Object} data 社区数据
 * @returns {Promise} 操作结果
 */
export const addCommunityInfo = (data) => {
    return api({
        url: '/communityInfo/add',
        method: 'post',
        data
    });
};

/**
 * 编辑社区信息
 * @param {Object} data 社区数据
 * @returns {Promise} 操作结果
 */
export const editCommunityInfo = (data) => {
    return api({
        url: '/communityInfo/edit',
        method: 'put',
        data
    });
};
/**
 * 删除社区
 * @param {Number|String} id 社区ID
 * @returns {Promise} 操作结果
 */
export const deleteCommunityInfo = (id) => {
    return api({
        url: `/communityInfo/delete/${id}`,
        method: 'delete'
    });
};
/**
 * 批量删除社区
 * @param {Array} ids 社区ID数组
 * @returns {Promise} 操作结果
 */
export const batchDeleteCommunityInfo = (ids) => {
    return api({
        url: '/communityInfo/batchDelete',
        method: 'delete',
        data: { ids }
    });
};


// 志愿者管理API
export const getVolunteerDetail = (id) => {
    return api({
        url: `/volunteer/detail/${id}`,
        method: 'get'
    });
};

export const editVolunteer = (data) => {
    return api({
        url: '/volunteer/update',
        method: 'put',
        data
    });
};

export const deleteVolunteer = (id) => {
    return api({
        url: `/volunteer/delete/${id}`,
        method: 'delete'
    });
};

export const auditVolunteer = (data) => {
    return api({
        url: '/volunteer/audit',
        method: 'post',
        data
    });
};
export const listVolunteer = (params) => {
    return api({
        url: '/volunteer/list',
        method: 'get',
        params
    });
};

export const addVolunteer = (data) => {
    return api({
        url: '/volunteer/add',
        method: 'post',
        data: {
            ...data,
            // 转换服务内容格式
            serviceContents: data.serviceContents || []
        }
    });
};
export const batchAuditVolunteer = (data) => {
    return api({
        url: '/volunteer/batchAudit',
        method: 'post',
        data
    });
};
export const batchDeleteVolunteer = (data) => {
    return api({
        url: '/volunteer/batchDelete',
        method: 'delete',
        data
    });
};
export const exportVolunteers = (data) => {
    return api({
        url: '/volunteer/export',
        method: 'post',
        data,
        responseType: 'blob' // 重要：指定响应类型为blob
    });
}

// 订单管理API
export const listOrder = (params) => {
    return api({
        url: '/order/list',
        method: 'get',
        params
    });
};

// 订单详情
export const getOrderDetail = (id) => {
    return api({
        url: `/order/detail/${id}`,
        method: 'get'
    });
};

// 回访订单
export const revisitOrder = (id, revisitRecord) => {
    return api({
        url: `/order/revisit/${id}`,
        method: 'put',
        data: revisitRecord,
        headers: {
            'Content-Type': 'application/json'
        }
    });
};

// 删除订单
export const deleteOrder = (id) => {
    return api({
        url: `/order/delete/${id}`,
        method: 'delete'
    });
};

// 批量删除订单
export const batchDeleteOrder =(ids)=>{
    return api({
        url: '/order/batchDelete',
        method: 'delete',
        data: { ids }
    })
}
export const exportOrders = (data) => {
    return api({
        url: '/order/export',
        method: 'post',
        data,
        responseType: 'blob' // 重要：指定响应类型为blob
    });
};

// 获取档案分页列表
export function getArchivePage(params) {
    return api({
        url: '/archive/page',
        method: 'get',
        params
    })
}

// 获取档案详情
export function getArchiveById(id) {
    return api({
        url: `/archive/${id}`,
        method: 'get'
    })
}

// 添加档案
export function addArchive(data) {
    return api({
        url: '/archive/add',
        method: 'post',
        data
    })
}

// 更新档案
export function updateArchive(data) {
    return api({
        url: '/archive/update/{id}',
        method: 'put',
        data
    })
}

// 删除档案
export function deleteArchive(id) {
    return api({
        url: `/archive/delete/${id}`,
        method: 'delete'
    })
}

// 导入档案
export function importArchive(data) {
    return api({
        url: '/archive/import',
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 导出档案
export function exportArchive(params) {
    return api({
        url: '/archive/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}

// 下载模板
export function downloadTemplate() {
    return api({
        url: '/archive/template',
        method: 'get',
        responseType: 'blob'
    })
}

export default {
    login,
    register
};
