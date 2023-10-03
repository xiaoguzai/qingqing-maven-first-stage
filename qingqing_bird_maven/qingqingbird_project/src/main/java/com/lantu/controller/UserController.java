package com.lantu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lantu.common.vo.Result;
import com.lantu.entity.User;
import com.lantu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author laocai
 * @since 2023-09-25
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/all")
    public Result<List<User>> getAllUser(){
        List<User> list = userService.list();
        return Result.success(list);
    }

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@RequestBody User user){
        //@RequestBody将传入的json对象转为user类型
        System.out.println("user !!! login !!!");
        System.out.println("user = "+user.toString());
        Map<String,Object> data = userService.login(user);
        System.out.println("data = "+data.toString());
        if(data != null)
        //登录成功
        {
            return Result.success(data);
        }
        return Result.fail(20002,"用户名或密码错误");
        //这里在正式的项目中需要自己定义
    }

    @GetMapping("/info")
    public Result<Map<String,Object>> getUserInfo(@RequestParam("token") String token){
        //根据token获取用户信息，redis
        Map<String,Object> data = userService.getUserInfo(token);
        System.out.println("data = "+data.toString());
        if(data != null){
            return Result.success(data);
        }
        return Result.fail(20003,"登录信息无效，请重新登录");
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("X-Token") String token){
        System.out.println("use logout");
        userService.logout(token);
        return Result.success("注销成功");
    }

    @GetMapping("/list")
    public Result<Map<String,Object>> getUserList(@RequestParam(value = "username",required = false) String username,
                                              @RequestParam(value = "phone",required = false) String phone,
                                              @RequestParam(value = "pageNo",required = false) Long pageNo,
                                              @RequestParam(value = "pageSize",required = false)Long pageSize){
        //除此之外还需要pageNo和pageSize
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //这里lambdaQueryWrapper进行了一次重栽，Children eq(boolean condition, R column, Object val);
        //满足condition条件的才进行，可参照Children函数
        // default Children eq(R column, Object val) {
        //    return this.eq(true, column, val);
        // }
        System.out.println("username = "+username);
        System.out.println("phone = "+phone);
        System.out.println("pageNo = "+pageNo);
        System.out.println("pageSize = "+pageSize);
        lambdaQueryWrapper.eq(StringUtils.hasLength(username),User::getUsername,username);
        //hasLength相当于!=null&&!=""
        lambdaQueryWrapper.eq(StringUtils.hasLength(phone),User::getPhone,phone);
        lambdaQueryWrapper.orderByDesc(User::getId);

        Page<User> page = new Page<>(pageNo,pageSize);
        //注意这个Page是baomidou中的Page
        /***
         * 这里可以点进去看Page的构造函数
         * public Page(long current,long size) { this(current, size, 0L);}
         */
        System.out.println("page = "+page.toString());
        userService.page(page,lambdaQueryWrapper);
        //把page和查询的lambdaQueryWrapper放入进去
        System.out.println("finish1");

        //将前端需要传入的数据放进去，这里前后端的数据必须保持统一
        Map<String,Object> data = new HashMap<>();
        data.put("total",page.getTotal());
        //这里的total必须单独做一个count查询，必须做一个分页配置
        //可以去mybatisplus官网去查看
        //配置完成之后可以看到控制台第一次使用了count(*)查询，
        //第二次把每一个属性全部查询出来

        System.out.println("getRecords = "+page.getRecords());
        //在MpConfig中写一个拦截器
        data.put("rows",page.getRecords());
        return Result.success(data);
    }

    @PostMapping
    public Result<?> addUser(@RequestBody User user){
        //使用@RequestBody的原因是前端传过来的是一个json数据
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return Result.success("新增用户成功");
    }

    @PutMapping
    public Result<?> updateUser(@RequestBody User user){
        user.setPassword(null);
        //修改的时候不展示密码
        userService.updateById(user);
        return Result.success("修改用户成功");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable("id") Integer id){
        User user = userService.getById(id);
        return Result.success(user);
    }
    //同时每一个属性旁边还有一个按钮：修改、删除，点开之后要能显示信息，
    //因此这里再写一个接口接收返回的信息

    @DeleteMapping("/{id}")
    public Result<User> deleteUserById(@PathVariable("id") Integer id){
        userService.removeById(id);
        return Result.success("删除用户成功");
    }
}
