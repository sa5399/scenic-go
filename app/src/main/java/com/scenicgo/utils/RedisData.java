package com.scenicgo.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 带逻辑过期时间的缓存包装。
 *
 * <p>key 不设物理 TTL，过期与否看 {@code expireTime}：逻辑过期后由后台线程重建，
 * 当前请求照常返回旧数据，所以高并发下不会有请求卡在重建上。
 *
 * @param <T> 缓存的数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisData<T> {

    private T data;

    private LocalDateTime expireTime;
}
