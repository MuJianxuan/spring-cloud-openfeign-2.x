package org.springframework.cloud.openfeign.analysis.pojo.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description
 * @Author: kongLiuYi
 * @Date: 2020/5/5 0005 19:50
 */
@ApiModel
@Data
@Accessors(chain = true)
public class User implements Serializable {
	private static final long serialVersionUID = 662285482455000678L;

	public User() {
	}

	@ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @ApiModelProperty(value = "查询开始时间")
    private Date createdTimeStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @ApiModelProperty(value = "查询结束时间")
    private Date createdTimeEnd;
}
