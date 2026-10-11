package com.scenicgo.reserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.reserve.constant.ReserveStatusEnum;
import com.scenicgo.reserve.dto.CheckinFormDTO;
import com.scenicgo.reserve.dto.CheckinLogQuery;
import com.scenicgo.reserve.dto.CheckinTarget;
import com.scenicgo.reserve.entity.CheckinLog;
import com.scenicgo.reserve.entity.Reserve;
import com.scenicgo.reserve.mapper.CheckinLogMapper;
import com.scenicgo.reserve.mapper.ReserveMapper;
import com.scenicgo.reserve.service.CheckinService;
import com.scenicgo.reserve.vo.CheckinLogVO;
import com.scenicgo.reserve.vo.CheckinVO;
import com.scenicgo.user.constant.UserRoleEnum;
import com.scenicgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 核销实现。
 *
 * <p>防重两道闸：`reserve` 的状态用带原状态条件的 UPDATE 改（并发时只有一个能成功），
 * `checkin_log.uk_reserve` 唯一键再兜一道。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    /** 核销：核销员与管理员都能干 */
    private static final List<Integer> CHECKIN_ROLES =
            List.of(UserRoleEnum.CHECKIN_STAFF.getCode(), UserRoleEnum.ADMIN.getCode());

    /** 流水查询：只给管理员 */
    private static final List<Integer> LOG_ROLES = List.of(UserRoleEnum.ADMIN.getCode());

    private final ReserveMapper reserveMapper;

    private final CheckinLogMapper checkinLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinVO checkin(CheckinFormDTO form) {
        requireRole(CHECKIN_ROLES);

        CheckinTarget target = reserveMapper.selectCheckinTarget(form.getCheckinCode());
        if (target == null) {
            throw new BizException(ResultCode.NOT_FOUND, "核销码不存在");
        }
        if (!ReserveStatusEnum.BOOKED.getCode().equals(target.getStatus())) {
            throw new BizException(ReserveStatusEnum.CHECKED_IN.getCode().equals(target.getStatus())
                    ? "该票已核销" : "订单状态不可核销");
        }

        LocalDateTime checkinTime = LocalDateTime.now().withNano(0);
        int rows = reserveMapper.update(null, new LambdaUpdateWrapper<Reserve>()
                .eq(Reserve::getId, target.getId())
                .eq(Reserve::getStatus, ReserveStatusEnum.BOOKED.getCode())
                .set(Reserve::getStatus, ReserveStatusEnum.CHECKED_IN.getCode())
                .set(Reserve::getCheckinTime, checkinTime));
        if (rows == 0) {
            // 闸机与人工同时刷同一张票：上面那条 UPDATE 只有一个能成功
            throw new BizException("该票已核销");
        }

        CheckinLog checkinLog = new CheckinLog();
        checkinLog.setReserveId(target.getId());
        checkinLog.setOrderNo(target.getOrderNo());
        checkinLog.setUserId(target.getUserId());
        checkinLog.setSpotId(target.getSpotId());
        checkinLog.setOperatorId(UserHolder.get().getId());
        checkinLog.setChannel(form.getChannel());
        checkinLog.setCheckinTime(checkinTime);
        try {
            checkinLogMapper.insert(checkinLog);
        } catch (DuplicateKeyException e) {
            // uk_reserve 兜底：同一张票只允许一条流水
            throw new BizException("该票已核销");
        }

        log.info("核销成功 orderNo={} spotId={} quantity={} channel={} operatorId={}",
                target.getOrderNo(), target.getSpotId(), target.getQuantity(),
                form.getChannel(), checkinLog.getOperatorId());
        return new CheckinVO(target.getOrderNo(), target.getSpotName(),
                target.getQuantity(), checkinTime);
    }

    @Override
    public PageResult<CheckinLogVO> listLogs(CheckinLogQuery query) {
        requireRole(LOG_ROLES);
        IPage<CheckinLogVO> page = checkinLogMapper.selectLogPage(
                Page.of(query.getCurrent(), query.getSize()),
                query.getSpotId(),
                query.getStartDate(),
                // 结束日期按"含当天"处理：拿次日零点做 < 比较
                query.getEndDate() == null ? null : query.getEndDate().plusDays(1));
        return PageResult.of(page);
    }

    /**
     * 目前只有核销与流水两个接口要判角色，直接写在 service 里。
     * 等出现第三个角色场景再抽成注解 + 拦截器，现在上那套属于给两个人配门禁系统。
     */
    private void requireRole(List<Integer> allowedRoles) {
        if (!allowedRoles.contains(UserHolder.get().getRole())) {
            throw new BizException(ResultCode.FORBIDDEN, "没有操作权限");
        }
    }
}
