package com.scenicgo.reserve.service;

import com.scenicgo.reserve.dto.ReserveFormDTO;
import com.scenicgo.reserve.vo.ReserveCreateVO;

public interface ReserveService {

    /** 下单：Lua 原子预扣 Redis 库存 → 建单 → DB 记已售 */
    ReserveCreateVO create(ReserveFormDTO form);
}
