package top.weixiansen574.bilibiliArchive.bean.response;

public enum ApiProxyRCode implements CodeProvider{
    USER_NOT_FOUND(10404);

    private final int code;

    ApiProxyRCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
