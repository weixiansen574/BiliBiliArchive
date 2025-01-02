package top.weixiansen574.bilibiliArchive.bean.response;

public enum UserRCode implements CodeProvider{
    ILLEGAL_COOKIE(1001),
    USER_EXISTS(1002),
    UID_INCONSISTENCY(1003),
    USER_IS_REFERENCED(1004);


    private final int code;

    UserRCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
