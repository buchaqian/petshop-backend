package com.petshop.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.common.Result;
import com.petshop.dto.WxLoginDTO;
import com.petshop.entity.User;
import com.petshop.mapper.UserMapper;
import com.petshop.service.UserService;
import com.petshop.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-用户接口")
@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
public class AppUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "微信小程序登录")
    @PostMapping("/login")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return Result.success(userService.wxLogin(dto));
    }

    @Operation(summary = "开发环境模拟登录（上线前删除）")
    @GetMapping("/devLogin")
    public Result<LoginVO> devLogin(@RequestParam(defaultValue = "admin") String role) {
        User user;
        if ("admin".equals(role)) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getRole, 2).last("LIMIT 1"));
        } else {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getRole, 0).last("LIMIT 1"));
        }
        if (user == null) {
            user = new User();
            user.setOpenid("dev_" + System.currentTimeMillis());
            user.setNickname("admin".equals(role) ? "店主" : "测试用户");
            user.setRole("admin".equals(role) ? 2 : 0);
            userMapper.insert(user);
        }

        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", user.getRole() == 2 ? "admin" : "user");

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return Result.success(vo);
    }
}
