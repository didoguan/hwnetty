package com.deepspc.hwnetty.core.enums;

public enum BizEnum {

    SUCCESS("200", "操作成功"),
	FORMAT_ERR("201", "格式错误")
    ;

    String code;

    String message;

    BizEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String getMessage(String code) {
        if (code == null) {
            return null;
        } else {
            for (BizEnum bizEnum : BizEnum.values()) {
                if (bizEnum.getCode().equals(code)) {
                    return bizEnum.getMessage();
                }
            }
            return null;
        }
    }
}
