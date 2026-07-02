package com.petshop.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 管理端接口：必须是店主角色（role=2）
            SaRouter.match("/admin/**")
                    .check(r -> {
                        StpUtil.checkLogin();
                        // 检查是否是店主
                        Object role = StpUtil.getSession().get("role");
                        if (!"admin".equals(role)) {
                            throw new RuntimeException("无权限，仅店主可操作");
                        }
                    });

            // 用户端需要登录的接口
            SaRouter.match("/app/cart/**", "/app/order/**", "/app/address/**", "/app/distribution/**")
                    .check(r -> StpUtil.checkLogin());

            SaRouter.match("/app/product/**", "/app/banner/**", "/app/user/login", "/app/user/devLogin")
                    .stop();

        })).addPathPatterns("/**")
                .excludePathPatterns("/doc.html", "/webjars/**", "/v3/**", "/swagger-ui/**");
    }
}
