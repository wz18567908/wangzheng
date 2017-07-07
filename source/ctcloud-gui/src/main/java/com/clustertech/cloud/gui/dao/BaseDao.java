package com.clustertech.cloud.gui.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.utils.CloudCondition;
import com.clustertech.cloud.gui.utils.CloudConstants.OrderByEnum;
import com.clustertech.cloud.gui.utils.CloudOrderBy;
import com.clustertech.cloud.gui.utils.CommandUtil;
import com.clustertech.cloud.gui.utils.Page;

@SuppressWarnings("unchecked")
public class BaseDao<T extends Serializable> {
    private Class<T> entityClass;
    @Autowired
    private SessionFactory sessionFactory;

    @SuppressWarnings("rawtypes")
    public BaseDao() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        entityClass = (Class) params[0];
    }

    /*
     * HQL function part
     */
    public Query getQuery(String hql) {
        return getSession().createQuery(hql);
    }
    
    public Query getQueryTest(String sql) {
        return getSession().createSQLQuery(sql);
    }

    public List<String> getDistinctProperty(String entityName, String propertyName) {
        String hql = "select distinct " + propertyName + " from " + entityName;
        return getQuery(hql).list();
    }

    public List<String> getDistinctPropertyByInClause(String entityName, String propertyName,
            String filterPorperty, String filterValue) {
        String hql = "select distinct " + propertyName + " from " + entityName
                + " where " + filterPorperty + " in (" + filterValue + ")";
        return getQuery(hql).list();
    }

    public void delete(String entityName, String propertyName, String propertyValue) {
        String hql = "delete from " + entityName + " where " + propertyName + "='" + propertyValue + "'";
        getQuery(hql).executeUpdate();
    }

    /*
     * Criteria function part
     */
    public void duplicateSave(T entity) {
        getSession().save(entity);
    }

    public void update(T entity) {
        getSession().update(entity);
    }

    public List<T> getEntityList() {
        return createCriteria().list();
    }

    public boolean exists(Serializable id) {
        return getEntityById(id) != null;
    }

    public T getEntityById(Serializable id) {
        return (T) getSession().get(entityClass, id);
    }

    public void distinctSave(T entity, Map<String, Object> primaryKeyValueMap) throws CTCloudException {
        T result = getEntityByPrimaryKeys(primaryKeyValueMap);
        if (result != null) {
            throw new CTCloudException(
                    String.format("Faild to save the object due to %s exist.", primaryKeyValueMap.toString()));
        }
        duplicateSave(entity);
    }

    public T getEntityByPrimaryKeys(Map<String, Object> propertyValueMap) {
        int index = 0;
        Criterion[] criterionArray = new Criterion[propertyValueMap.size()];
        for (String propertyName : propertyValueMap.keySet()) {
            Object value = propertyValueMap.get(propertyName);
            criterionArray[index] = Restrictions.eq(propertyName, value);
            index++;
        }
        return (T) createCriteria(criterionArray).uniqueResult();
    }

    public Criteria getEntityListByFilterData(Map<String, Object> propertyValueMap) {
        CommandUtil commandutil = new CommandUtil();
        Criteria criteria = createCriteria();
        for (String propertyName : propertyValueMap.keySet()) {
            Object value = propertyValueMap.get(propertyName);
            if (value instanceof String && !value.equals("")) {
                criteria.add(Restrictions.in(propertyName, (Object[]) value.toString().split(",")));
            } else if (value instanceof Integer && Integer.parseInt(value.toString()) != 0) {
                criteria.add(Restrictions.ge(propertyName, Timestamp.valueOf(commandutil.getPastTime(value))));
            }
        }
        return criteria;
    }

    public List<T> getEntityList(String propertyName, Object value, String orderByPorperty, OrderByEnum type) {
        Criteria criteria = createCriteria();
        criteria.add(Restrictions.eq(propertyName, value));
        if (OrderByEnum.ASC == type) {
            criteria.addOrder(Order.asc(orderByPorperty));
        } else {
            criteria.addOrder(Order.desc(orderByPorperty));
        }
        return criteria.list();
    }

    public List<T> getEntityList(String propertyName, Object value) {
        Criterion criterion = Restrictions.eq(propertyName, value);
        return getEntityList(criterion);
    }

    public List<T> getEntityList(Criterion criterion) {
        Criteria criteria = createCriteria();
        criteria.add(criterion);
        return criteria.list();
    }

    public Criteria createCriteria() {
        return getSession().createCriteria(entityClass);
    }

    public Criteria createCriteria(Criterion... criterions) {
        Criteria criteria = createCriteria();
        for (Criterion c : criterions) {
            criteria.add(c);
        }
        return criteria;
    }

    public int countAllEntity(Criteria criteria) {
        return Integer.valueOf(criteria.setProjection(Projections.rowCount()).uniqueResult().toString());
    }

    public List<T> getEntityList(Criteria criteria) {
        return criteria.list();
    }

    public List<T> getEntityListByPage(Criteria criteria, int pageNo, int pageSize) {
        criteria.setFirstResult((pageNo - 1) * pageSize);
        criteria.setMaxResults(pageSize);
        return getEntityList(criteria);
    }

    public Page<T> getPage(CloudCondition cloudCondition, CloudOrderBy cloudOrderBy, int pageNo, int pageSize,
            String currentUserName, String orderBy, String searchValue, String filter, Map<String, Object> filterData) {
        List<T> entityList = new ArrayList<T>();
        Criteria criteria = createCriteria();
        criteria.add(Restrictions.eq("userName", currentUserName));
        if (filter.equals("on")) {
            criteria = getEntityListByFilterData(filterData);
        }
        if (!searchValue.equals("")) {
            criteria.add(Restrictions.sqlRestriction(" JOB_ID like " + "'" + "%" + searchValue + "%" + "'"));
        }
        criteria.addOrder(Order.desc(orderBy));
        if (cloudCondition != null) {
            cloudCondition.build(criteria);
        }
        if (cloudOrderBy != null) {
            cloudOrderBy.build(criteria);
        }
        long totalCount = countAllEntity(criteria);
        // remove the count option.
        criteria.setProjection(null);

        if (totalCount < 1) {
            return new Page<T>();
        }

        int startIndex = Page.getStartOfPage(pageNo, pageSize);
        entityList = getEntityListByPage(criteria, pageNo, pageSize);
        return new Page<T>(startIndex, totalCount, pageSize, entityList);
    }

    public Page<T> getPage(CloudOrderBy cloudOrderBy, int pageNo, int pageSize, String currentUserName, String orderBy,
            String searchValue, String filter, Map<String, Object> filterData) {
        return getPage(null, cloudOrderBy, pageNo, pageSize, currentUserName, orderBy, searchValue, filter, filterData);
    }

    public Page<T> getPage(int pageNo, int pageSize, String currentUserName, String orderBy, String searchValue,
            String filter, Map<String, Object> filterData) {
        return getPage(null, null, pageNo, pageSize, currentUserName, orderBy, searchValue, filter, filterData);
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
