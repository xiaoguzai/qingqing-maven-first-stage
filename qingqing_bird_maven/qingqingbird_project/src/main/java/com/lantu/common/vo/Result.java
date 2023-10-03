package com.lantu.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    //这里Result<T>定义范型为T，同时定义类型的时候
    //也需要把T具体的类放入进去
    //public Result<List<User>>
    private Integer code;
    private String message;
    private T data;
    //这里使用Object也可以，但是使用范型更好

    public static<T>  Result<T> success(){
        //这里static<T>定义了T为范型
        return new Result<>(20000,"success",null);
    }

    public static<T>  Result<T> success(T data){
        return new Result<>(20000,"success",data);
    }

    public static<T>  Result<T> success(T data, String message){
        return new Result<>(20000,message,data);
    }

    public static<T>  Result<T> success(String message){
        return new Result<>(20000,message,null);
    }

    public static<T>  Result<T> fail(){
        return new Result<>(20001,"fail",null);
    }

    public static<T>  Result<T> fail(Integer code){
        return new Result<>(code,"fail",null);
    }

    public static<T>  Result<T> fail(Integer code, String message){
        return new Result<>(code,message,null);
    }

    public static<T>  Result<T> fail( String message){
        return new Result<>(20001,message,null);
    }

}