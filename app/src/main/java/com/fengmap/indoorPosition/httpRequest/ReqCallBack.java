package com.fengmap.indoorPosition.httpRequest;

/**
 * Created by ACER on 2017/12/22.
 */

public interface ReqCallBack<T> {
    /**
     * 响应成功
     */
    void onReqSuccess(T result);

    /**
     * 响应失败
     */
    void onReqFailed(String errorMsg);
}
