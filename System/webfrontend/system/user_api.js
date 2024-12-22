import CrudApi from '@/utils/api_base';
import { request } from '@/utils/utils'

export default class UserApi extends CrudApi {
    constructor() {
        super('/users');
    }

    async assignRoles(userId, orgId, roles) {
        return await request({
            url: `${this.baseUrl}/${userId}/assign`,
            data: roles,
            method: 'post',
            params: { orgId }
        });
    }

    async removeFromOrg(userId, orgId) {
        return await request({
            url: `${this.baseUrl}/${userId}/remove/org/${orgId}`,
            method: 'delete',
        });
    }

    async resetPassword(userId) {
        return await request({
            url: `${this.baseUrl}/${userId}/password-reset`,
            method: 'put',
        });
    }

    async getMyProfile() {
        return await request({
            url: `${this.baseUrl}/my`,
            method: 'get',
        });
    }

    async updateMyProfile(user) {
        return await request({
            url: `${this.baseUrl}/my`,
            method: 'put',
            data: user,
        });
    }

    async updateMyPwd(oldPass, newPass) {
        return await request({
            url: `${this.baseUrl}/my/password`,
            method: 'put',
            data: { oldPass, newPass },
        });
    }

}
export const userApi = new UserApi();