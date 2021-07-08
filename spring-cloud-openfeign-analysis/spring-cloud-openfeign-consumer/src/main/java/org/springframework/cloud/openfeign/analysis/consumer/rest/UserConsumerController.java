package org.springframework.cloud.openfeign.analysis.consumer.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.analysis.consumer.provider.UserProvider;
import org.springframework.cloud.openfeign.analysis.pojo.vo.Result;
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
@RequestMapping("/user/consumer")
@Api("userConsumer")
@Slf4j
public class UserConsumerController {

	@Autowired
	private UserProvider userProvider;


	@ApiOperation(value = "获取用户", notes = "获取指定用户信息")
	@ApiImplicitParam(paramType = "path", name = "id", value = "用户ID", required = true, dataType = "string")
	@GetMapping(value = "/{id}")
	public Result get(@PathVariable String id) {
		log.debug("get with id:{}", id);
		return userProvider.get(id);
	}

}
