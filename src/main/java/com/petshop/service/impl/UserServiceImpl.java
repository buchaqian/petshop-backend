package com.petshop.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.dto.WxLoginDTO;
import com.petshop.entity.User;
import com.petshop.mapper.UserMapper;
import com.petshop.service.UserService;
import com.petshop.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        // 1. 用code换取openid
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + appid
                + "&secret=" + secret
                + "&js_code=" + dto.getCode()
                + "&grant_type=authorization_code";

        String response = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(response);

        String openid = json.getStr("openid");
        if (openid == null) {
            log.error("微信登录失败：{}", response);
            throw new RuntimeException("微信登录失败，请重试");
        }

        // 2. 查询或创建用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "宠物爱好者");
            user.setAvatar(dto.getAvatar());
            user.setRole(0);
            save(user);
        } else {
            // 更新昵称和头像
            if (dto.getNickname() != null) {
                user.setNickname(dto.getNickname());
                user.setAvatar(dto.getAvatar());
                updateById(user);
            }
        }

        // 3. Sa-Token 登录，生成token，写入角色
        StpUtil.login(user.getId());
        if (user.getRole() == 2) {
            StpUtil.getSession().set("role", "admin");
        } else {
            StpUtil.getSession().set("role", "user");
        }
        String token = StpUtil.getTokenValue();

        // 4. 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
