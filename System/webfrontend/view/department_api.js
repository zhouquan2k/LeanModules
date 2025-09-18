import CrudApi from '@/utils/api_base';
import { request } from '@/utils/utils'

export default class DepartmentApi extends CrudApi {
    constructor() {
        super('/departments', 'Department');
    }

    // 如果需要添加特殊的department相关接口，可以在这里添加
    // 例如：获取部门树形结构、批量操作等

    async getDepartmentTree() {
        return await request({
            url: `${this.baseUrl}/tree`,
            method: 'get'
        });
    }

    async moveDepartment(departmentId, targetDepartmentId) {
        return await request({
            url: `${this.baseUrl}/${departmentId}/move`,
            method: 'put',
            data: { targetDepartmentId }
        });
    }
}

export const departmentApi = new DepartmentApi();