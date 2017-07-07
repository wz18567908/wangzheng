package com.clustertech.cloud.gui.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private long start;
    private long totalPageNo;
    private List<T> dataList;

    public Page() {
        this(0, 0, DEFAULT_PAGE_SIZE, new ArrayList<T>());
    }

    public Page(long start, long totalPageNo, int pageSize, List<T> dataList) {
        this.pageSize = pageSize;
        this.start = start;
        this.totalPageNo = totalPageNo;
        this.dataList = dataList;
    }

    public long getTotalPageNo() {
        return totalPageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public long getCurrentPageNo() {
        return start / pageSize + 1;
    }

    public boolean hasNextPage() {
        return getCurrentPageNo() < getTotalPageNo();
    }

    public boolean hasPreviousPage() {
        return this.getCurrentPageNo() > 1;
    }

    /**
     * Get the first data index in any page by default page size.
     *
     * @param pageNo:
     *            page number
     * @return the data index
     */
    public static int getStartOfPage(int pageNo) {
        return getStartOfPage(pageNo, DEFAULT_PAGE_SIZE);
    }

    /**
     * Get the first data index in any page.
     *
     * @param pageNo:
     *            page number
     * @param pageSize:
     *            page size
     * @return the data index
     */
    public static int getStartOfPage(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}