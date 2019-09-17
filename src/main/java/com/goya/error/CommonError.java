package com.goya.error;

import com.goya.response.CommonReturnType;

/**
 * @author cj
 * @date 2019-09-16 - 23:11
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();

    public CommonError setErrMsg(String errMsg);
}
