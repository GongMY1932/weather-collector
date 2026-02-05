package com.base.common.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 功能描述：分页参数类
 *
 * @Author: shigf
 * @Date: 2020/7/29 9:56
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PageEntity {
    //要查询的页数
    private int pageNo = 1;
    //每页显示多少条记录
    private int pageSize = 10;
}
