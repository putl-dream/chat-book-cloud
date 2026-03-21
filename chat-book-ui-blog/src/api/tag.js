import request from '@/utils/http.js'

/**
 * 分页查询标签
 */
export function getTagPage(params) {
    return request.post('/tag/page', params)
}

/**
 * 获取所有标签
 */
export function getAllTags() {
    return request.get('/tag/list')
}

/**
 * 根据类型获取标签
 */
export function getTagsByType(type) {
    return request.get('/tag/listByType', { params: { type } })
}

/**
 * 创建标签
 */
export function createTag(data) {
    return request.post('/tag/create', data)
}

/**
 * 更新标签
 */
export function updateTag(data) {
    return request.post('/tag/update', data)
}

/**
 * 删除标签
 */
export function deleteTag(id) {
    return request.delete('/tag/delete', { params: { id } })
}
