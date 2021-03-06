package org.springframework.cloud.openfeign.analysis.provider.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.openfeign.analysis.pojo.vo.Result;
import org.springframework.cloud.openfeign.analysis.pojo.vo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author: kongLiuYi
 * @Date: 2020/5/5 0005 19:15
 */
@RestController
@RequestMapping("/user/provider")
@Api("userProvider")
@Slf4j
public class UserProviderController {

	@ApiOperation(value = "获取用户", notes = "获取指定用户信息")
	@ApiImplicitParam(paramType = "path", name = "id", value = "用户ID", required = true, dataType = "string")
	@GetMapping(value = "/{id}")
	public Result get(@PathVariable String id) {
		log.debug("get with id:{}", id);
		return Result.success(new User().setUsername("kongLiuYi").setMobile("1397097....").setName("空留意"));
	}

}
