package com.scenicgo.reserve.service;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.reserve.dto.CheckinFormDTO;
import com.scenicgo.reserve.dto.CheckinLogQuery;
import com.scenicgo.reserve.vo.CheckinLogVO;
import com.scenicgo.reserve.vo.CheckinVO;

public interface CheckinService {

    /** 核销：已预约的订单 → 已核销，写一条流水 */
    CheckinVO checkin(CheckinFormDTO form);

    /** 核销流水分页（按景点 + 核销日期范围） */
    PageResult<CheckinLogVO> listLogs(CheckinLogQuery query);
}
