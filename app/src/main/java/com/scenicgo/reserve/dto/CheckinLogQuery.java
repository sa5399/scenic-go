package com.scenicgo.reserve.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 核销流水查询条件。
 */
@Data
public class CheckinLogQuery {

    /** 景点 id，不传表示不限 */
    private Long spotId;

    /** 开始日期（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 结束日期（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 页码，从 1 开始 */
    private int current = 1;

    /** 每页条数，上限由分页插件兜 */
    private int size = 10;
}
