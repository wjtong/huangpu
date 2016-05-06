package com.zuczug.analysis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import javolution.util.FastList;
import javolution.util.FastMap;

public class AnalysisEvent {

    public static final String module = AnalysisEvent.class.getName();

    public static String forecastVsRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        Locale locale = UtilHttp.getLocale(request);
        
        String productId = (String) request.getParameter("productId");
        List<Map<String, Object>> resultList = FastList.newInstance();
        
        Map serviceResult;
		try {
			serviceResult = dispatcher.runSync("analysis.forecastQuantityByInventory", UtilMisc.toMap("productId", productId, "isManufactCount", "F", "userLogin", userLogin));
	        List<Map<String, Object>> storeProductQuantityList = (List<Map<String, Object>>) serviceResult.get("storeProductQuantityList");
	        if (UtilValidate.isEmpty(storeProductQuantityList)) {
	        	Debug.logInfo("====================== storeProductQuantityList is empty", module);
	        	return "success";
	        }
	        
	        for (Map<String, Object> storeProductQuantity : storeProductQuantityList) {
	        	Map<String, Object> resultItem = FastMap.newInstance();
	        	String productStoreId = (String) storeProductQuantity.get("productStoreId");
	        	resultItem.put("productStoreId", productStoreId);
	        	resultItem.put("quantity", storeProductQuantity.get("quantity"));
	        	resultItem.put("percent", storeProductQuantity.get("percent"));
	        	resultItem.put("score", storeProductQuantity.get("score"));
	        	resultItem.put("comment", storeProductQuantity.get("comment"));
	        	BigDecimal storeQuantity = getStoreRequestQuantity(delegator, productId, productStoreId);
	        	resultItem.put("requestQuantity", storeQuantity);
	        	resultList.add(resultItem);
	        }
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        
        request.setAttribute("resultList", resultList);
        return "success";
    }

	private static BigDecimal getStoreRequestQuantity(Delegator delegator, String productId, String productStoreId) throws GenericEntityException {
    	String storePartyId = getStorePartyId(productStoreId);
    	List<GenericValue> custRequests = delegator.findByAnd("CustRequest", UtilMisc.toMap("fromPartyId", storePartyId));
		BigDecimal storeQuantity = BigDecimal.ZERO;
    	for (GenericValue custRequest : custRequests) {
    		String custRequestId = custRequest.getString("custRequestId");
    		// TODO: 查找所有该店铺所有的带有这个productId的custRequest，问题是statusId应该是哪些状态？
    		List<GenericValue> custRequestItems = delegator.findByAnd("CustRequestItem", UtilMisc.toMap("productId", productId, "custRequestId", custRequestId));
    		for (GenericValue custRequestItem : custRequestItems) {
    			storeQuantity = storeQuantity.add(custRequestItem.getBigDecimal("quantity"));
    		}
    	}
    	return storeQuantity;
	}

	private static String getStorePartyId(String productStoreId) {
		// 目前为止，所有的店铺的partyId的字符串值就等于productStoreId，但是逻辑上是可能不对的，应该从数据库表结构关系查找。
		return productStoreId;
	}
}
