package com.base.common.entity.base;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分页数据
 *
 * @author GISirFive
 */
@Data
public class PaginationEntity<T> implements Serializable {

    private static final long serialVersionUID = -8436377139071417565L;

    public PaginationEntity() {
    }

    /**
     * 分页对象
     *
     * @param rows  记录信息
     * @param total 总记录条数
     */
    public PaginationEntity(List<T> rows, int total) {
        this.rows = rows;
        this.total = total;
    }

    public PaginationEntity(List<T> rows, int total, int current, int pageSize) {
        this.rows = rows;
        this.total = total;
        this.current = current;
        this.pageSize = pageSize;
    }

    /**
     * 记录信息
     */
    private List<T> rows;
    /**
     * 总记录数
     */
    private int total;
    /**
     * 当前页
     */
    private int current;
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 其它信息
     */
    private Map<String, Object> extra;

}
