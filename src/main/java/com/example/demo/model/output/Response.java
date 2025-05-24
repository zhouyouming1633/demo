package com.example.demo.model.output;

import com.example.demo.emums.CodeMsg;
import lombok.Data;

/***
 * @description 通用返回对象
 * @author ZhouYouMing
 * @date 2025/5/24 18:42
 */
@Data
public class Response<T> {

    //返回状态码
    private String code;

    //状态码对应的描述
    private String message;

    //返回对象
    private T data;

    private Response(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private Response(CodeMsg codeMsg) {
        this.code = codeMsg.getCode();
        this.message = codeMsg.getMsg();
    }

    private Response(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success() {
        return new Response<>(CodeMsg.SUCCESS);
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(CodeMsg.SUCCESS.getCode(),CodeMsg.SUCCESS.getMsg(),data);
    }

    public static <T> Response<T> error(String code, String message) {
        return new Response<>(code,message);
    }

    public static <T> Response<T> error(CodeMsg codeMsg) {
        return new Response<>(codeMsg.getCode(),codeMsg.getMsg());
    }

    public boolean isSuccess() {
        return CodeMsg.SUCCESS.getCode().equals(getCode());
    }
}
