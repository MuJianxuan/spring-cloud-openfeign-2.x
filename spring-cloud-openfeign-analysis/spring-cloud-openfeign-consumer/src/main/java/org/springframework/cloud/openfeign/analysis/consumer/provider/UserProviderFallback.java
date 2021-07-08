package org.springframework.cloud.openfeign.analysis.consumer.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.analysis.pojo.vo.Result;
import org.springframework.cloud.openfeign.analysis.pojo.vo.User;
import org.springframework.stereotype.Component;


/**
 * @Description
 * @Author: kongLiuYi
 * @Date: 2020/5/5 0005 19:50
 */
@Slf4j
@Component
public class UserProviderFallback implements  UserProvider {
	@Override
	public Result<User> get(String id) {
		log.error("获取用户信息异常！未加载到用户信息");
		return Result.fail();
	}
}
