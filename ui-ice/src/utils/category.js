import { CATEGORY_NAMES } from '@/constants';

/**
 * 根据分类ID获取分类名称
 * @param {number} category - 分类ID
 * @returns {string} 分类名称
 */
export function getCategoryName(category) {
    return CATEGORY_NAMES[category] || CATEGORY_NAMES[4];
}
