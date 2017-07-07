package com.clustertech.cloud.gui.utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;

public class CloudCondition {

    private List<Criterion> criterionList = new ArrayList<Criterion>();

    public void add(Criterion criterion) {
        criterionList.add(criterion);
    }

    public void build(Criteria criteria) {
        for (Criterion criterion : criterionList) {
            criteria.add(criterion);
        }
    }
}
