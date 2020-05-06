package com.hdu.libnetwork;

//网络请求返回类型
public class ApiResponse<T> {

    public boolean success;
    public int status;
    public String message;
    public T  body;
}
