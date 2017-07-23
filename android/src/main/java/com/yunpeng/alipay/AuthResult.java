package com.yunpeng.alipay;
import java.util.Map;
import android.text.TextUtils;
public class AuthResult {

    private String resultStatus;
    private String result;
    private String memo;
    private String resultCode;
    private String authCode;
    private String alipayOpenId;

    public AuthResult( String rawResult, boolean removeBrackets) {
        if (rawResult == null) {
            return;
        }
        String[] resultParams = rawResult.split(";");
        for (String resultParam : resultParams) {
            if (resultParam.startsWith("resultStatus")) {
                resultStatus = gatValue(resultParam, "resultStatus");
            }
            if (resultParam.startsWith("result")) {
                result = gatValue(resultParam, "result");
            }
            if (resultParam.startsWith("memo")) {
                memo = gatValue(resultParam, "memo");
            }
        }

        String[] resultValue = result.split("&");
        for (String value : resultValue) {
            if (value.startsWith("alipay_open_id")) {
                alipayOpenId = removeBrackets(getValue("alipay_open_id=", value), removeBrackets);
                continue;
            }
            if (value.startsWith("auth_code")) {
                authCode = removeBrackets(getValue("auth_code=", value), removeBrackets);
                continue;
            }
            if (value.startsWith("result_code")) {
                resultCode = removeBrackets(getValue("result_code=", value), removeBrackets);
                continue;
            }
        }

    }
    private String gatValue(String content, String key) {
        String prefix = key + "={";
        return content.substring(content.indexOf(prefix) + prefix.length(),
                content.lastIndexOf("}"));
    }
    private String removeBrackets(String str, boolean remove) {
        if (remove) {
            if (!TextUtils.isEmpty(str)) {
                if (str.startsWith("\"")) {
                    str = str.replaceFirst("\"", "");
                }
                if (str.endsWith("\"")) {
                    str = str.substring(0, str.length() - 1);
                }
            }
        }
        return str;
    }

    @Override
    public String toString() {
        return "resultStatus={" + resultStatus + "};memo={" + memo + "};result={" + result + "}";
    }

    private String getValue(String header, String data) {
        return data.substring(header.length(), data.length());
    }

    /**
     * @return the resultStatus
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @return the resultCode
     */
    public String getResultCode() {
        return resultCode;
    }

    /**
     * @return the authCode
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * @return the alipayOpenId
     */
    public String getAlipayOpenId() {
        return alipayOpenId;
    }
}