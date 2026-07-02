package com.petshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.dto.WxLoginDTO;
import com.petshop.entity.User;
import com.petshop.vo.LoginVO;

public interface UserService extends IService<User> {

    LoginVO wxLogin(WxLoginDTO dto);
}
