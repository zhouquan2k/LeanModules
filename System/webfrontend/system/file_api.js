
import { request } from '@/utils/utils'

export default class FileApi {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async delete(id) {
        return await request({
            url: `${this.baseUrl}/${id}`,
            method: 'delete',
        });
    }
}
export const fileApi = new FileApi('/api/files');