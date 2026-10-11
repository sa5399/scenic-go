package com.scenicgo.reserve.dto;

import lombok.Data;

/**
 * 核销入参。
 */
@Data
public class CheckinFormDTO {

    private String checkinCode;

    /** 见 {@link com.scenicgo.reserve.constant.CheckinChannelEnum}，不传按人工算 */
    private int channel = 2;
}
