
import { request } from '@/utils/utils'

const baseUrl = '/security';
export function login(params) {
    return request({
        url: `${baseUrl}/login`,
        method: 'post',
        data: params
    })
}

export function logout() {
    return request({
        url: `${baseUrl}/logout`,
        method: 'post',
        noAutoLogin: true, //no auto login
        noPopup: true, // no global error popup
    })
}

export function getInfo() {
    return request({
        url: `${baseUrl}/info`,
        method: 'get',
    });
}

const lang = navigator.language || navigator.userLanguage;
export function getAllMetadata() {
    return request({
        url: `/public/metadata`,
        method: 'get',
        params: { lang }
    });
}

export function getVersion() {
    return request({
        url: `/misc/version`,
        method: 'get',
    })
}
