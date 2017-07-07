package com.clustertech.cloud.gui.service.report;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clustertech.cloud.gui.dao.report.ShareStorageUsageDao;
import com.clustertech.cloud.gui.utils.CloudConstants.ShareStorageEnum;

@Service
@Transactional
public class ShareStorageUsageService extends ReportBaseService {

    @Autowired
    private ShareStorageUsageDao shareStorageUsageDao;

    public List<Map<String, Object>> getShareStorageList(String storages) {
        List<Map<String, Object>> storageUsageObjList = shareStorageUsageDao.getShareStorageUsageList(getUnitTimeFormat(),
                getStartTime(), getEndTime());
        List<String> storageArrayList = getStorageArrayList(storages);
        return generateStorageUsageAllObj(storageArrayList, storageUsageObjList);
    }

    private List<String> getStorageArrayList(String storages) {
        List<String> result = new ArrayList<String>();
        if (storages != null && !storages.isEmpty()) {
            result.addAll(Arrays.asList(storages.split(",")));
        } else {
            result.addAll(shareStorageUsageDao.getShareStorageInfoList());
        }
        return result;
    }

    private List<String> getStorageList() {
        List<String> result = new ArrayList<String>();
        result.addAll(shareStorageUsageDao.getShareStorageInfoList());
        return result;
    }

    private List<Map<String, Object>> removeAllZeroObj(List<Map<String, Object>> originalList) {
        boolean nonZeroFlag = false;
        for (Map<String, Object> obj : originalList) {

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> objDataList = (List<Map<String, Object>>) obj.get("data");
            for (Map<String, Object> objData : objDataList) {
                @SuppressWarnings("unchecked")
                List<List<String>> dataList = (List<List<String>>) objData.get("data");
                for (List<String> data : dataList) {
                    if (data.size() != 2) {
                        // should never get here.
                        continue;
                    }
                    if (!DEFAULT_VALUE.equals(data.get(1))) {
                        nonZeroFlag = true;
                        break;
                    }
                    if (nonZeroFlag) {
                        break;
                    }
                }
                if (nonZeroFlag) {
                    break;
                }
            }
        }

        if (nonZeroFlag) {
            return originalList;
        }

        return new ArrayList<Map<String, Object>>();
    }

    private List<Map<String, Object>> generateStorageUsageAllObj(List<String> storageList,
            List<Map<String, Object>> storageUsageList) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        SimpleDateFormat sf = getDateFormat();
        Calendar startCalendar = getStartCalendar();
        Calendar endCalendar = getEndCalendar();

        String currentDateTime = null;
        Map<String, Object> elementBasedObj = new HashMap<String, Object>();
        elementBasedObj.put("element", "report.table.DATE");
        List<Map<String, Object>> elementData = new ArrayList<Map<String, Object>>();
        List<String> totalList = getStorageList();

        for (String storage : storageList) {
            Map<String, Object> utilObj = new HashMap<String, Object>();
            Map<String, Object> usedObj = new HashMap<String, Object>();
            Map<String, Object> totalObj = new HashMap<String, Object>();
            List<List<String>> utilDataList = new ArrayList<List<String>>();
            List<List<String>> usedDataList = new ArrayList<List<String>>();
            List<List<String>> totalDataList = new ArrayList<List<String>>();
            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.clear();
            tempCalendar.setTime(startCalendar.getTime());

            while (tempCalendar.before(endCalendar)) {
                currentDateTime = sf.format(tempCalendar.getTime());
                add1UnitToCalendar(tempCalendar);
                List<String> utilData = new ArrayList<String>();
                List<String> usedData = new ArrayList<String>();
                List<String> totalData = new ArrayList<String>();
                if (shareStorageEnum.get() == ShareStorageEnum.USED) {
                    usedData.add(currentDateTime);
                    totalData.add(currentDateTime);
                    usedObj.put("label", storage);
                    totalObj.put("label", storage + ":" + "Total.Storage");
                } else {
                    utilData.add(currentDateTime);
                    utilObj.put("label", "util");
                    utilObj.put("label", storage);
                }
                for (Map<String, Object> storageUsage : storageUsageList) {
                    BigDecimal usedStorage = new BigDecimal(storageUsage.get("used").toString());
                    BigDecimal totalStorage = new BigDecimal(storageUsage.get("total").toString());

                    if (storage.equals(storageUsage.get("storage")) && currentDateTime.equals(storageUsage.get("time"))
                            && usedStorage.add(totalStorage).compareTo(BigDecimal.ZERO) > 0) {
                        if (shareStorageEnum.get() == ShareStorageEnum.USED) {
                            usedData.add(storageUsage.get("used").toString());
                            totalData.add(storageUsage.get("total").toString());
                        } else {
                            utilData.add(storageUsage.get("util").toString());
                        }
                        break;
                    }
                }
                if (shareStorageEnum.get() == ShareStorageEnum.USAGE) {
                    if (utilData.size() == 1) {
                        utilData.add(DEFAULT_VALUE);
                    }
                } else {
                    if (totalData.size() == 1) {
                        totalData.add(DEFAULT_VALUE);
                    }
                    if (usedData.size() == 1) {
                        usedData.add(DEFAULT_VALUE);
                    }
                }
                if (shareStorageEnum.get() == ShareStorageEnum.USAGE) {
                    utilDataList.add(utilData);
                } else {
                    usedDataList.add(usedData);
                    totalDataList.add(totalData);
                }
            }
            if (shareStorageEnum.get() == ShareStorageEnum.USAGE) {
                utilObj.put("data", utilDataList);
                elementData.add(utilObj);
            } else {
                usedObj.put("data", usedDataList);
                totalObj.put("data", totalDataList);
                for (String totalDev : totalList) {
                    if (totalDev.equals(storage)) {
                        elementData.add(totalObj);
                    }
                }
                elementData.add(usedObj);
            }
        }
        elementBasedObj.put("data", elementData);
        result.add(elementBasedObj);
        return removeAllZeroObj(result);
    }

    // private void generateStorageUsageObj(String storage, List<Map<String,
    // Object>> storageUsageList,
    // List<Map<String, Object>> resultList) {
    // SimpleDateFormat sf = getDateFormat();
    // Calendar startCalendar = getStartCalendar();
    // Calendar endCalendar = getEndCalendar();
    //
    // String currentDateTime = null;
    // Map<String, Object> elementBasedObj = new HashMap<String, Object>();
    // elementBasedObj.put("element", storage);
    // List<Map<String, Object>> elementData = new ArrayList<Map<String,
    // Object>>();
    // Map<String, Object> utilObj = new HashMap<String, Object>();
    // Map<String, Object> usedObj = new HashMap<String, Object>();
    // Map<String, Object> totalObj = new HashMap<String, Object>();
    // List<List<String>> utilDataList = new ArrayList<List<String>>();
    // List<List<String>> usedDataList = new ArrayList<List<String>>();
    // List<List<String>> totalDataList = new ArrayList<List<String>>();
    // List<String> totalList = getStorageList();
    // Calendar tempCalendar = Calendar.getInstance();
    // tempCalendar.clear();
    // tempCalendar.setTime(startCalendar.getTime());
    //
    // while (tempCalendar.before(endCalendar)) {
    // currentDateTime = sf.format(tempCalendar.getTime());
    // add1UnitToCalendar(tempCalendar);
    // List<String> utilData = new ArrayList<String>();
    // List<String> usedData = new ArrayList<String>();
    // List<String> totalData = new ArrayList<String>();
    // if (shareStorageEnum.get() == ShareStorageEnum.USED) {
    // usedData.add(currentDateTime);
    // totalData.add(currentDateTime);
    // usedObj.put("label", storage);
    // totalObj.put("label", storage + ":" + "Total.Storage");
    // } else {
    // utilData.add(currentDateTime);
    // utilObj.put("label", "util");
    // utilObj.put("label", storage);
    // }
    // for (Map<String, Object> storageUsage : storageUsageList) {
    // BigDecimal usedStorage = (BigDecimal) storageUsage.get("used");
    // BigDecimal totalStorage = (BigDecimal) storageUsage.get("total");
    //
    // if (storage.equals(storageUsage.get("storage")) &&
    // currentDateTime.equals(storageUsage.get("time"))
    // && usedStorage.add(totalStorage).compareTo(BigDecimal.ZERO) > 0) {
    // if (shareStorageEnum.get() == ShareStorageEnum.USED) {
    // usedData.add(storageUsage.get("used").toString());
    // totalData.add(storageUsage.get("total").toString());
    // } else {
    // utilData.add(storageUsage.get("util").toString());
    // }
    // break;
    // }
    // }
    // if (shareStorageEnum.get() == ShareStorageEnum.USAGE) {
    // if (utilData.size() == 1) {
    // utilData.add(DEFAULT_VALUE);
    // }
    // } else {
    // if (totalData.size() == 1) {
    // totalData.add(DEFAULT_VALUE);
    // }
    // if (usedData.size() == 1) {
    // usedData.add(DEFAULT_VALUE);
    // }
    // }
    // if (shareStorageEnum.get() == ShareStorageEnum.USAGE) {
    // utilDataList.add(utilData);
    // } else {
    // usedDataList.add(usedData);
    // totalDataList.add(totalData);
    // }
    // }
    // if (shareStorageEnum.get() == ShareStorageEnum.USAGE) {
    // utilObj.put("data", utilDataList);
    // elementData.add(utilObj);
    // } else {
    // usedObj.put("data", usedDataList);
    // totalObj.put("data", totalDataList);
    // for (String totalDev : totalList) {
    // if (totalDev.equals(storage)) {
    // elementData.add(totalObj);
    // }
    // }
    // elementData.add(usedObj);
    // }
    // elementBasedObj.put("data", elementData);
    // resultList.add(elementBasedObj);
    // }

}
