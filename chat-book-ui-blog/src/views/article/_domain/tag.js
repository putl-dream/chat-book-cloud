import request from '@/utils/http.js'

export function searchAuthorTags(keyword, limit = 10) {
    return request.get('/author-tag/search', {
        params: { keyword, limit }
    })
}

export function getHotAuthorTags(limit = 12) {
    return request.get('/author-tag/hot', {
        params: { limit }
    })
}
