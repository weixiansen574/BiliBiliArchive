package top.weixiansen574.bilibiliArchive.bean.response;

public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;

    // 构造方法
    public BaseResponse(CodeProvider responseCode, String message, T data) {
        this.code = responseCode.getCode();
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(CodeProvider responseCode, String message) {
        this(responseCode, message, null);
    }

    // 静态工厂方法
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(ResponseCode.SUCCESS, "success", data);
    }

    public static <T> BaseResponse<T> ok(T data,String message) {
        return new BaseResponse<>(ResponseCode.SUCCESS, message, data);
    }

  /*  public static <T> BaseResponse<T> ok(String message) {
        return new BaseResponse<>(ResponseCode.SUCCESS, message, null);
    }*/

    public static <T> BaseResponse<T> error(CodeProvider responseCode, String message) {
        return new BaseResponse<>(responseCode, message);
    }

    public static <T> BaseResponse<T> badRequest(String message){
        return error(ResponseCode.ILLEGAL_REQUEST,message);
    }

    public static <T> BaseResponse<T> notfound(String message){
        return error(ResponseCode.NOT_FOUND,message);
    }

    public static <T> BaseResponse<T> illegalRequest(String message){
        return new BaseResponse<>(ResponseCode.ILLEGAL_REQUEST,message,null);
    }

    // Getter 和 Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}



