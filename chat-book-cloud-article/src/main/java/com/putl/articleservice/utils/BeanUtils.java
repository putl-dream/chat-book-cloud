package com.putl.articleservice.utils;

import cn.hutool.core.bean.BeanUtil;

import java.util.List;

/**
 * Bean 工具类
 *
 * 1. 默认使用 {@link BeanUtil} 作为实现类，虽然不同 bean 工具的性能有差别，但是对绝大多数同学的项目，不用在意这点性能
 * 2. 针对复杂的对象转换，可以搜参考 AuthConvert 实现，通过 mapstruct + default 配合实现
 *
 * @author huangsz
 */
public class BeanUtils {
    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        List<T> list = toBean(source.getList(), targetType);
        return new PageResult<>(list, source.getTotal());
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        return source.stream().map(item -> BeanUtil.toBean(item, targetType)).toList();
    }

}
