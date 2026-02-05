package com.base.common.entity.base;

import com.base.common.constant.DelFlagConst;
import com.base.common.exception.MyException;

import java.io.Serializable;

import lombok.Data;

/**
 * 查询基类
 *
 * @author GISirFive
 * @date Create on 2017/11/24 23:53
 */
@Data
public class BaseRequest implements Serializable {

    /**
     * 第几页
     */
    private Integer page;
//    /**
//     * 【兼容前端框架逻辑】第几页
//     */
//    private Integer pageNo;
//    /**
//     * 每页多少行
//     */
//    private Integer rows;
    /**
     * 【兼容前端框架逻辑】每页多少行
     */
    private Integer pageSize;
    /**
     * 关键字
     */
    private String keywords;
    /**
     * 删除标志
     */
    private String deleteFlag;

    public String getDeleteFlag() {
        return deleteFlag == null ? DelFlagConst.NORMAL : deleteFlag;
    }


//    public Integer getRows() {
//        return rows == null ? getPageSize() : rows;
//    }
//
//    public Integer getPageNo() {
//        return pageNo == null ? 1 : pageNo;
//    }

    public Integer getPageSize() {
        if (pageSize == null) {
            pageSize = 10;
        }
        if (pageSize > 2000) {
            //每页记录数量不能为空，且不能大于2000
            throw new MyException("每页记录总数超过最大阈值");
        }
        return pageSize;
    }
}
