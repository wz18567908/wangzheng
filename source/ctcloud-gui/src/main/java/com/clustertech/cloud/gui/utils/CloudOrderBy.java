package com.clustertech.cloud.gui.utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

public class CloudOrderBy {
    private List<Order> orderList = new ArrayList<Order>();

    public void add(Order order){
        orderList.add(order);
    }

    public void build(Criteria criteria){
        for(Order order : orderList){
            criteria.addOrder(order);
        }
    }
}
