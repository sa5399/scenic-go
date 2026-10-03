package com.scenicgo.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果：不把 MyBatis-Plus 的 IPage 暴露给前端。
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }
}
