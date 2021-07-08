package org.springframework.cloud.openfeign.analysis.consumer.provider;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.analysis.pojo.vo.Result;
import org.springframework.cloud.openfeign.analysis.pojo.vo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



/**
 * @Description
 * @Author: kongLiuYi
 * @Date: 2020/5/5 0005 19:33
 */
@FeignClient(name = "openfeign-provider", fallback = UserProviderFallback.class)
public interface UserProvider {

	@GetMapping(value = "/user/provider/{id}")
	Result<User> get(@PathVariable String id);


}
