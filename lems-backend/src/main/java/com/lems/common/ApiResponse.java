
package com.lems.common;

public class ApiResponse<T> {

    private Integer code;

    private String message;

    private T data;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 操作成功
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    // 新增成功
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "新增成功", data);
    }

    // 操作失败
    public static <T> ApiResponse<T> failure(
            Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
