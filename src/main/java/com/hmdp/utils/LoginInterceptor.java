package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
     //1.判斷是否需要拦截(登录信息保存到Threadlocal中，保证线程安全。从Threadlocal中读取是否用户)
        //获取session
        //HttpSession session = request.getSession();
        //获取session中的用户
        //Object user = session.getAttribute("user");
        //判断用户是否存在
        /*if (user == null) {
            不存在，拦截
            response.setStatus(401);
            return false;
        }*/
        //UserHolder.saveUser((User) user);
        if(UserHolder.getUser()==null){
            //没有，需要拦截，设置状态码
            response.setStatus(401);
            return false;
        }
        //放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
