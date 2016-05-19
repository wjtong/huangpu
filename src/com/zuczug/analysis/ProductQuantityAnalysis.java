package com.zuczug.analysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class ProductQuantityAnalysis {

	/**
	 * 预测一个新商品可能的销售数量
	 * by liujia
	 */
	public static Map<String, Object> forecastProductQuantity(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
  		String productId=(String) context.get("productId");
  		BigDecimal quantity = BigDecimal.ZERO;
  		String method = "";
		try {
			//根据sku获取商品信息
			Map<String,String> productInfo = getProductInfo(delegator, productId);
			//获取销售数量
			quantity = getForecastProductSaleQuantity(delegator, productInfo,"max");
			method="getForecastProductSaleQuantity";
			if(quantity.compareTo(BigDecimal.ZERO)==0){
				//从大色商品中取平均销量
				quantity = getLargeColorAvgQuantity(delegator,productInfo);
				method="getLargeColorAvgQuantity";
			}
			if(quantity.compareTo(BigDecimal.ZERO)==0){
				//对应尺码列表计算销量0 2 4 6 8--25 26 27 28 29
				quantity = getCorrespondSizeQuantity(delegator,productInfo);
				method="getCorrespondSizeQuantity";
			}
			if(quantity.compareTo(BigDecimal.ZERO)==0){
				//取款和色平均销量
				quantity = getStyleAndColorAvgQuantity(delegator,productInfo);
				method="getStyleAndColorAvgQuantity";
			}
			if(quantity.compareTo(BigDecimal.ZERO)==0){
				//取款和价格带的平均销量
				quantity = getStyleAndPriceAvgQuantity(delegator,productInfo);
				method="getStyleAndPriceAvgQuantity";
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		result.put("prudouctForecastSaleQuantity", quantity);
		result.put("method", method);
		return result;
	}
	
	private static BigDecimal getProductForecastQuantityCollection(Delegator delegator, Map<String, String> productInfo) throws GenericEntityException {
		BigDecimal quantity = BigDecimal.ZERO;
		quantity = getForecastProductSaleQuantity(delegator, productInfo,"max");
		if(quantity.compareTo(BigDecimal.ZERO)==0){
			//从大色商品中取平均销量
			quantity = getLargeColorAvgQuantity(delegator,productInfo);
		}
		if(quantity.compareTo(BigDecimal.ZERO)==0){
			//对应尺码列表计算销量0 2 4 6 8--25 26 27 28 29
			quantity = getCorrespondSizeQuantity(delegator,productInfo);
		}
		if(quantity.compareTo(BigDecimal.ZERO)==0){
			//取款和色平均销量
			quantity = getStyleAndColorAvgQuantity(delegator,productInfo);
		}
		if(quantity.compareTo(BigDecimal.ZERO)==0){
			//取款和价格带的平均销量
			quantity = getStyleAndPriceAvgQuantity(delegator,productInfo);
		}
		return quantity;
	}

	//获取商品信息
	private static Map<String,String> getProductInfo(Delegator delegator,String productId) throws GenericEntityException {
		Map<String,String> productInfoMap= new HashMap();
		String productColorId = "";
		String productSizeId = "";
		String productStyleName = "";
		String productSeriesId = "";
		String productSeasonId = "";
		BigDecimal productListPrice = BigDecimal.ZERO;
		GenericValue productColor = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId",productId,"productFeatureTypeId","COLOR")));
		if(productColor!=null&&!productColor.isEmpty()){
			productColorId = (String)productColor.get("idCode");
		}else{
			productColorId="_NA_";
		}
		GenericValue productSize = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId",productId,"productFeatureTypeId","SIZE")));
		if(productSize!=null&&!productSize.isEmpty()){
			productSizeId = (String)productSize.get("idCode");
		}else{
			productSizeId="_NA_";
		}
		GenericValue productStyle = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId",productId,"productFeatureTypeId","STYLE")));
		if(productStyle!=null&&!productStyle.isEmpty()){
			productStyleName = (String)productStyle.get("description");
		}
		else{
			productStyleName="_NA_";
		}
		String assocProductId = "";
		GenericValue productAssoc = EntityUtil.getFirst(delegator.findByAnd("productCategoryMemberAssoc", UtilMisc.toMap("productIdTo",productId)));
		if(productAssoc.get("isVirtual").equals("Y")){
			assocProductId = (String)productAssoc.get("productId");
		}else{
			assocProductId = (String)productAssoc.get("productIdTo");
		}
		GenericValue group = EntityUtil.getFirst( delegator.findByAnd("ProductCategoryMemberView", UtilMisc.toMap("productId",assocProductId,"productCategoryTypeId","GROUPNAME")));
		if(UtilValidate.isNotEmpty(group)){
			GenericValue pgv = EntityUtil.getFirst(delegator.findByAndCache("ProductCategoryGroupView", UtilMisc.toMap("groupId",group.getString("productCategoryId"))));
			if(UtilValidate.isNotEmpty(pgv)){
				productSeriesId = pgv.getString("seriesId");
				productSeasonId = pgv.getString("seasonId");
			}
		}
		
		GenericValue productPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId",productId)));
		if(productPrice!=null&&!productPrice.isEmpty()){
			productListPrice = (BigDecimal) productPrice.get("price");
		}
		productInfoMap.put("productColorId",productColorId);
		productInfoMap.put("productSizeId",productSizeId);
		productInfoMap.put("productStyleName",productStyleName);
		productInfoMap.put("productSeriesId",productSeriesId);
		productInfoMap.put("productListPrice",String.valueOf(productListPrice));
		productInfoMap.put("productSeasonId",productSeasonId);
		return productInfoMap;
	}

	private static BigDecimal getForecastProductSaleQuantity(Delegator delegator,Map<String, String> productTempInfo,String mode) throws GenericEntityException {
		List<EntityExpr> exprs = FastList.newInstance();
		if(!productTempInfo.get("productSeriesId").equals("")){
			exprs.add(EntityCondition.makeCondition("seriesId", EntityOperator.EQUALS, productTempInfo.get("productSeriesId")));
		}
		if(!productTempInfo.get("productColorId").equals("")){
			exprs.add(EntityCondition.makeCondition("colorCode", EntityOperator.EQUALS, productTempInfo.get("productColorId")));
		}
		if(!productTempInfo.get("productSizeId").equals("")){
			exprs.add(EntityCondition.makeCondition("sizeCode", EntityOperator.EQUALS, productTempInfo.get("productSizeId")));
		}
		if(!productTempInfo.get("productStyleName").equals("")){
			exprs.add(EntityCondition.makeCondition("style", EntityOperator.EQUALS, productTempInfo.get("productStyleName")));
		}
		if(!productTempInfo.get("productListPrice").equals("")){
	        if(Double.parseDouble(productTempInfo.get("productListPrice"))>=5000){
	        	exprs.add(EntityCondition.makeCondition("listPrice", EntityOperator.GREATER_THAN, "5000"));
	        }else{
	        	Map<String,String> priceSegement = getPriceSegement(Double.parseDouble(productTempInfo.get("productListPrice")));
	        	exprs.add(EntityCondition.makeCondition("listPrice", EntityOperator.GREATER_THAN, new BigDecimal(priceSegement.get("priceFrom"))));
	        	exprs.add(EntityCondition.makeCondition("listPrice", EntityOperator.LESS_THAN, new BigDecimal(priceSegement.get("priceEnd"))));
	        }
		}
        exprs.add(EntityCondition.makeCondition("zuczugSeasonId", EntityOperator.NOT_EQUAL, productTempInfo.get("productSeasonId")));
        
        BigDecimal productQuantity = BigDecimal.ZERO;
        BigDecimal sumQuantity = BigDecimal.ZERO;
        BigDecimal returnQuantity = BigDecimal.ZERO;
		List<GenericValue> productList = delegator.findList("ZzProductDimension", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
		if(productList!=null&&!productList.isEmpty()){
			if(mode.equals("max")){
				for(GenericValue product : productList){
					List<GenericValue> salesOrderItems = delegator.findByAnd("ZzSalesOrderItemFact", UtilMisc.toMap("productDimId", product.get("dimensionId")));
					if(salesOrderItems!=null&&!salesOrderItems.isEmpty()){
						for(GenericValue salesOrderItem : salesOrderItems){
							productQuantity = productQuantity.add((BigDecimal)salesOrderItem.get("quantity"));
						}
					}
					if(returnQuantity.compareTo(productQuantity)==-1){
						returnQuantity = productQuantity;
					}
					productQuantity = BigDecimal.ZERO;
				}
			}else if(mode.equals("avg")){
				int count=0;
				for(GenericValue product : productList){
					List<GenericValue> salesOrderItems = delegator.findByAnd("ZzSalesOrderItemFact", UtilMisc.toMap("productDimId", product.get("dimensionId")));
					if(salesOrderItems!=null&&!salesOrderItems.isEmpty()){
						for(GenericValue salesOrderItem : salesOrderItems){
							productQuantity = productQuantity.add((BigDecimal)salesOrderItem.get("quantity"));
						}
					}
					if(productQuantity.compareTo(BigDecimal.ZERO)==1){
						sumQuantity = sumQuantity.add(productQuantity);
						count++;
					}
				}
				if(count!=0){
					returnQuantity = sumQuantity.divide(new BigDecimal(count), 0, RoundingMode.CEILING);
				}
			}
		}else{
			return returnQuantity;
		}
		return returnQuantity;
	}
	
	private static BigDecimal getStyleAndColorAvgQuantity(Delegator delegator,Map<String, String> productInfo) throws GenericEntityException {
		Map<String,String> productInfoTemp = new HashMap();
		for(Iterator it = productInfo.keySet().iterator() ; it.hasNext();){
			String key = it.next().toString(); 
			productInfoTemp.put(key, productInfo.get(key)); 
		}
		int count=0;
		productInfoTemp.put("productSizeId", "");
		productInfoTemp.put("productSeriesId", "");
		productInfoTemp.put("productListPrice", "");
		BigDecimal quantity = getForecastProductSaleQuantity(delegator, productInfoTemp,"avg");

		return quantity;
	}
	
	private static BigDecimal getStyleAndPriceAvgQuantity(Delegator delegator,Map<String, String> productInfo) throws GenericEntityException {
		Map<String,String> productInfoTemp = new HashMap();
		for(Iterator it = productInfo.keySet().iterator() ; it.hasNext();){
			String key = it.next().toString(); 
			productInfoTemp.put(key, productInfo.get(key)); 
		}
		int count=0;
		productInfoTemp.put("productSizeId", "");
		productInfoTemp.put("productSeriesId", "");
		productInfoTemp.put("productColorId", "");
		BigDecimal quantity = getForecastProductSaleQuantity(delegator, productInfoTemp,"avg");

		return quantity;
	}
	
	private static BigDecimal getLargeColorAvgQuantity(Delegator delegator,Map<String, String> productInfo) throws GenericEntityException {
		BigDecimal sumQuantity = BigDecimal.ZERO;
		Map<String,String> productInfoTemp = new HashMap();
		for(Iterator it = productInfo.keySet().iterator() ; it.hasNext();){
			String key = it.next().toString(); 
			productInfoTemp.put(key, productInfo.get(key)); 
		}
		
		int count=0;
		GenericValue productFetureColor = EntityUtil.getFirst(delegator.findByAnd("ProductFeature", UtilMisc.toMap("idCode",(String)productInfoTemp.get("productColorId"))));
		GenericValue productColorIactin = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureIactn", UtilMisc.toMap("productFeatureIdTo",(String)productFetureColor.get("productFeatureId"),"productFeatureIactnTypeId","FEATURE_IACTN_DEPEND")));
		if(productColorIactin!=null&&!productColorIactin.isEmpty()){
			List<GenericValue> productColorList = delegator.findByAnd("ProductFeatureIactn",UtilMisc.toMap("productFeatureId", productColorIactin.get("productFeatureId")));
			for(GenericValue productColor:productColorList){
				GenericValue productFeture = EntityUtil.getFirst(delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureId",(String)productColor.get("productFeatureIdTo"))));
				productInfoTemp.put("productColorId", (String)productFeture.get("idCode"));
				BigDecimal quantity = getForecastProductSaleQuantity(delegator, productInfoTemp,"max");
				if(quantity.compareTo(BigDecimal.ZERO)==1){
					sumQuantity = sumQuantity.add(quantity);
					count++;
				}
			}
		}
		
		if(count!=0){
			sumQuantity = sumQuantity.divide(new BigDecimal(count), 0, RoundingMode.CEILING);
		}
		return sumQuantity;
	}

	private static Map<String,String> getPriceSegement(Double price) {
		Map<String,String> priceSegement = new HashMap();
		String priceFrom = "";
		String priceEnd = "";
		if (price < 500) {
			priceFrom = "0";
			priceEnd = "500";
		}
		if (price < 1000 && price >= 500) {
			priceFrom = "500";
			priceEnd = "1000";
		}
		if (price < 2000 && price >= 1000) {
			priceFrom = "1000";
			priceEnd = "2000";
		}
		if (price < 3000 && price >= 2000) {
			priceFrom = "2000";
			priceEnd = "3000";
		}
		if (price < 4000 && price >= 3000) {
			priceFrom = "3000";
			priceEnd = "4000";
		}
		if (price < 5000 && price >= 4000) {
			priceFrom = "4000";
			priceEnd = "5000";
		}
		if (price >= 5000) {
			priceFrom = "5000";
			priceEnd = "";
		}
		priceSegement.put("priceFrom", priceFrom);
		priceSegement.put("priceEnd", priceEnd);
		return priceSegement;
	}
	
	/**
	 * 预测系列中商品销售数量
	 * by liujia
	 */
	public static Map<String, Object> forecastProductQuantityByCat(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
  		String productCategoryId=(String) context.get("productCategoryId");
  		if(productCategoryId==null||productCategoryId.equals("")){
  			return ServiceUtil.returnError("please choose Category.");
  		}
  		BigDecimal quantity = BigDecimal.ZERO;
  		List<Map <String,Object>> prudouctForecastSaleQuantityList = FastList.newInstance();
		try {
			//获取分类下所有sku
			List<String> productSkuList = getSkuByCatId(delegator, productCategoryId);
			for(String productSku : productSkuList){
				Map<String, Object> productQuantityMap = FastMap.newInstance();
				Map<String,String> productInfo = getProductInfo(delegator, productSku);
				//获取预测销售数量
				quantity = getProductForecastQuantityCollection(delegator,productInfo);
				productQuantityMap.put("productId", productSku);
				productQuantityMap.put("quantity", quantity);
				prudouctForecastSaleQuantityList.add(productQuantityMap);
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		result.put("prudouctForecastSaleQuantityList", prudouctForecastSaleQuantityList);
		return result;
	}
	
	/**
	 * 根据商品预测销售量生成Requirement
	 * by liujia
	 */
	public static Map<String, Object> createRequirementByForecast(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
  		LocalDispatcher dispatcher = dctx.getDispatcher();
  		GenericValue userLogin = (GenericValue) context.get("userLogin");
  		String productId=(String) context.get("productId");
  		String isCancel=(String) context.get("isCancel");
  		BigDecimal quantity = BigDecimal.ZERO;
		try {
			//根据sku获取商品信息
			Map<String,String> productInfo = getProductInfo(delegator, productId);
			//获取预测销售数量
			quantity = getProductForecastQuantityCollection(delegator,productInfo);
			//生成Requirement
			createRequirement(dispatcher,productId,quantity,isCancel,userLogin);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 根据系列中商品销售量生成Requirement
	 * by liujia
	 */
	public static Map<String, Object> createRequirementByForecastCat(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
  		LocalDispatcher dispatcher = dctx.getDispatcher();
  		GenericValue userLogin = (GenericValue) context.get("userLogin");
  		String productCategoryId=(String) context.get("productCategoryId");
  		String isCancel=(String) context.get("isCancel");
  		BigDecimal quantity = BigDecimal.ZERO;
		try {
			//获取分类下所有sku
			List<String> productSkuList = getSkuByCatId(delegator, productCategoryId);
			for(String productSku : productSkuList){
				//根据sku获取商品信息
				Map<String,String> productInfo = getProductInfo(delegator, productSku);
				//获取预测销售数量
				quantity = getProductForecastQuantityCollection(delegator,productInfo);
				//生成Requirement
				createRequirement(dispatcher,productSku,quantity,isCancel,userLogin);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private static List<String> getSkuByCatId(Delegator delegator,
			String productCategoryId) throws GenericEntityException {
		List productSkuList = new ArrayList();
		List<GenericValue> productList = delegator.findByAnd("productCategoryMemberAssoc",UtilMisc.toMap("productCategoryId", productCategoryId));
		for(GenericValue productSku:productList){
			if(productSku.get("isVirtual").equals("Y")){
				productSkuList.add((String)productSku.get("productIdTo"));
			}else{
				productSkuList.add((String)productSku.get("productId"));
			}
		}
		return productSkuList;
	}
	
	private static String createRequirement(LocalDispatcher dispatcher,String productId,BigDecimal quantity,String isCancel,GenericValue userLogin) throws GenericServiceException {
		String error="";
        Map<String, Object> resultInfo = dispatcher.runSync("createRequirement", UtilMisc.toMap("requirementTypeId", "INTERNAL_REQUIREMENT","facilityId","ZUCZUG_CLOTHESFACILITY",
				"statusId","REQ_CREATED","productId",productId,"quantity",quantity,"reason","预测","userLogin", userLogin));
        if (resultInfo.containsKey("errorMessage")){
        	error=(String) resultInfo.get("errorMessage");
        	return error;
        }
        if(isCancel!=null&&isCancel.equals("Y")){
        	dispatcher.runSync("updateRequirement", UtilMisc.toMap("requirementId", (String)resultInfo.get("requirementId"),"statusId","REQ_REJECTED"
    				,"userLogin", userLogin));
        }
		return error;
	}
	
	private static BigDecimal getCorrespondSizeQuantity(Delegator delegator,Map<String, String> productInfo) throws GenericEntityException {
		BigDecimal sumQuantity = BigDecimal.ZERO;
		if(productInfo.get("productSizeId").equals("F")){
			return BigDecimal.ZERO;
		}
		Map<String,String> productInfoTemp = new HashMap();
		for(Iterator it = productInfo.keySet().iterator() ; it.hasNext();){
			String key = it.next().toString(); 
			productInfoTemp.put(key, productInfo.get(key)); 
		}
		String productSizeCode = getCorrespondSize(productInfo.get("productSizeId"));
		productInfoTemp.put("productColorId", productSizeCode);
		sumQuantity = getForecastProductSaleQuantity(delegator, productInfoTemp,"max");
		return sumQuantity;
	}
	
	private static String getCorrespondSize(String sizeCode) {
		String productSizeCode = "";
		if(sizeCode.equals("0")){
			productSizeCode = "25";
		}else if(sizeCode.equals("25")){
			productSizeCode = "0";
		}
		if(sizeCode.equals("2")){
			productSizeCode = "26";
		}else if(sizeCode.equals("26")){
			productSizeCode = "2";
		}
		if(sizeCode.equals("4")){
			productSizeCode = "27";
		}else if(sizeCode.equals("27")){
			productSizeCode = "4";
		}
		if(sizeCode.equals("6")){
			productSizeCode = "28";
		}else if(sizeCode.equals("28")){
			productSizeCode = "6";
		}
		if(sizeCode.equals("8")){
			productSizeCode = "29";
		}else if(sizeCode.equals("29")){
			productSizeCode = "6";
		}
		return productSizeCode;
	}

	
}