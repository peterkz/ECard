package com.wetoop.ecard.event;

import cn.edots.nest.event.MessageEvent;

/**
 * @author Parck.
 * @date 2017/10/16.
 * @desc
 */

public class FinishMessageEvent extends MessageEvent {

    public FinishMessageEvent(Class<?> clazz) {
        super(CMD_FINISH_ACTIVITY, clazz);
    }
}
