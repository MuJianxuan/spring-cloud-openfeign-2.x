package org.springframework.cloud.openfeign.analysis.consumer.remote;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Rao
 * @Date 2021/7/14
 **/
public interface UserRpcRemote<T> {

	/**
	 * 获取用户ID
	 * @param id
	 */
	T getById(String id);

	/**
	 * 集合查询
	 * @param ids
	 * @return
	 */
	List<T> listByIds(Collection<Serializable> ids);

}
