package top.weixiansen574.bilibiliArchive.bean.response;

public enum ResponseCode implements CodeProvider{
    SUCCESS(0),
    ILLEGAL_REQUEST(400),
    NOT_FOUND(404),

    IO_EXCEPTION(600),
    MODIFY_CONFIG_DURING_OPERATION(700);



    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
