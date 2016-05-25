package com.zuczug.analysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
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
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ProductStoreAnalysis {
    public static final String module = ProductStoreAnalysis.class.getName();
	private static Map<String, String> dimensionFeatureMap = FastMap.newInstance();

	private static String getPriceSegementId(Double price) {
		String priceSegmentId = null;
		if (price < 500) {
			priceSegmentId = "0-500";
		}
		if (price < 1000 && price >= 500) {
			priceSegmentId = "500-1000";
		}
		if (price < 2000 && price >= 1000) {
			priceSegmentId = "1000-2000";
		}
		if (price < 3000 && price >= 2000) {
			priceSegmentId = "2000-3000";
		}
		if (price < 4000 && price >= 3000) {
			priceSegmentId = "3000-4000";
		}
		if (price < 5000 && price >= 4000) {
			priceSegmentId = "4000-5000";
		}
		if (price >= 5000) {
			priceSegmentId = "5000-UP";
		}
		return priceSegmentId;
	}

	private static void addPriceScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		BigDecimal extGrossAmount = salesOrderItem.getBigDecimal("extGrossAmount");
		BigDecimal unitPrice = extGrossAmount.divide(score);
		String priceSegmentId = null;
		Double unitDoublePrice = unitPrice.doubleValue();
		priceSegmentId = getPriceSegementId(unitDoublePrice);
		// Debug.logInfo("------- " + salesOrderItem.getString("orderId") + " ---------- " + extGrossAmount + " -------------- " + unitPrice + " -------------- " + priceSegmentId, module);
		if (UtilValidate.isNotEmpty(unitPrice)) {
			addStoreProductFeatureScore(delegator, productStoreId, priceSegmentId, "PRICE", score);
		}
	}

	private static void addSubseriesScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		//String productId = salesOrderItem.getString("productProductId");
		String subseriesId = salesOrderItem.getString("productSubseriesId");
		if (UtilValidate.isNotEmpty(subseriesId)) {
			addStoreProductFeatureScore(delegator, productStoreId, subseriesId, "SUBSERIES", score);
		}
	}

	private static void addMainSupplierScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		//String productId = salesOrderItem.getString("productProductId");
		String mainSupplierPartyId = salesOrderItem.getString("productMainSupplierPartyId");
		if (UtilValidate.isNotEmpty(mainSupplierPartyId)) {
			addStoreProductFeatureScore(delegator, productStoreId, mainSupplierPartyId, "MAIN_SUPPLIER_PARTY", score);
		}
	}

	private static void addSeriesScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		//String productId = salesOrderItem.getString("productProductId");
		String seriesId = salesOrderItem.getString("productSeriesId");
		if (UtilValidate.isNotEmpty(seriesId)) {
			addStoreProductFeatureScore(delegator, productStoreId, seriesId, "SERIES", score);
		}
	}

	private static void addStyleScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		//String productId = salesOrderItem.getString("productProductId");
		String style = salesOrderItem.getString("productStyle");
		if (UtilValidate.isNotEmpty(style)) {
			addStoreProductFeatureScore(delegator, productStoreId, style, "STYLE", score);
		}
	}

	private static void addSizeScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		String sizeCode = salesOrderItem.getString("productSizeCode");
		String sizeId = dimensionFeatureMap.get(sizeCode);
		if (UtilValidate.isEmpty(sizeId)) {
			List<GenericValue> productFeatues = delegator.findByAnd("ProductFeature",
					UtilMisc.toMap("productFeatureTypeId", "SIZE", "idCode", sizeCode));
			if (UtilValidate.isEmpty(productFeatues)) {
				return;
			}
			sizeId = EntityUtil.getFirst(productFeatues).getString("productFeatureId");
			dimensionFeatureMap.put(sizeCode, sizeId);
		}
		addStoreProductFeatureScore(delegator, productStoreId, sizeId, "SIZE", score);
	}

	private static void addColorScore(Delegator delegator, GenericValue salesOrderItem) throws GenericEntityException {
		BigDecimal score = salesOrderItem.getBigDecimal("quantity");
		String productStoreId = salesOrderItem.getString("storeProductStoreId");
		String colorCode = salesOrderItem.getString("productColorCode");
		String colorId = dimensionFeatureMap.get(colorCode);
		if (UtilValidate.isEmpty(colorId)) {
			List<GenericValue> productFeatues = delegator.findByAnd("ProductFeatureAndGroupView",
					UtilMisc.toMap("productFeatureGroupId", "ZUCZUG_COLOR", "idCode", colorCode));
			if (UtilValidate.isEmpty(productFeatues)) {
				productFeatues = delegator.findByAnd("ProductFeature",
						UtilMisc.toMap("productFeatureTypeId", "COLOR", "idCode", colorCode));
				if (UtilValidate.isEmpty(productFeatues)) {
					Debug.logInfo("------------------- idCode " + colorCode + " has no featureId found!", module);
					return;
				}
				Debug.logInfo("------------------- idCode " + colorCode + " with productFeatureId " + EntityUtil.getFirst(productFeatues).getString("productFeatureId") + " has no featureGroupId found!", module);
			}
			colorId = EntityUtil.getFirst(productFeatues).getString("productFeatureId");
			dimensionFeatureMap.put(colorCode, colorId);
		}
		addStoreProductFeatureScore(delegator, productStoreId, colorId, "COLOR", score);
	}

	private static void addStoreProductFeatureScore(Delegator delegator, String productStoreId,
			String dimensionId, String dimensionTypeId, BigDecimal score) throws GenericEntityException {
		if (UtilValidate.isEmpty(dimensionId)) {
			return;
		}
		if (dimensionId.equalsIgnoreCase("_NA_") || dimensionId.equalsIgnoreCase("_NF_")) {
			return;
		}
		GenericValue dsAnalysisStoreFeatureScore = delegator.findByPrimaryKey("DsAnalysisStoreFeatureScore",
				UtilMisc.toMap("productStoreId", productStoreId, "analysisDimensionId", dimensionId));
		if (UtilValidate.isNotEmpty(dsAnalysisStoreFeatureScore)) {
			BigDecimal currentScore = dsAnalysisStoreFeatureScore.getBigDecimal("score");
			BigDecimal newScore = currentScore.add(score);
			dsAnalysisStoreFeatureScore.set("score", newScore);
			delegator.store(dsAnalysisStoreFeatureScore);
			return;
		}
		// if it's empty
		// check if DsAnalysisDimension has this dimension
		GenericValue dsAnalysisDimension = delegator.findByPrimaryKey("DsAnalysisDimension",
				UtilMisc.toMap("analysisDimensionId", dimensionId));
		if (UtilValidate.isEmpty(dsAnalysisDimension)) {
			dsAnalysisDimension = delegator.makeValue("DsAnalysisDimension", UtilMisc.toMap("analysisDimensionId", dimensionId, "analysisDimensionTypeId", dimensionTypeId, "description", dimensionId));
			delegator.create(dsAnalysisDimension);
		}
		dsAnalysisStoreFeatureScore = delegator.makeValue("DsAnalysisStoreFeatureScore",
				UtilMisc.toMap("productStoreId", productStoreId, "analysisDimensionId", dimensionId, "score", score));
		delegator.create(dsAnalysisStoreFeatureScore);
	}

  	// 为所有sku给所有店铺打分
	public static Map<String, Object> scoreAllProductsForAllStores(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Date fromDate = (Date) context.get("fromDate");
        Date thruDate = (Date) context.get("thruDate");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar thruCalendar = Calendar.getInstance();
        thruCalendar.setTime(thruDate);
        thruCalendar.set(Calendar.HOUR, 0);
        thruCalendar.set(Calendar.MINUTE, 0);
        thruCalendar.set(Calendar.SECOND, 0);
        thruCalendar.set(Calendar.MILLISECOND, 0);

        while (calendar.before(thruCalendar)) {
        	Timestamp theTimestamp = UtilDateTime.toTimestamp(calendar.getTime());
	        try {
	        	dispatcher.runSync("analysis.scoreProductStoreByDate", UtilMisc.toMap("theDate", theTimestamp, "userLogin", userLogin));
	        } catch (GenericServiceException e) {
				e.printStackTrace();
				return ServiceUtil.returnError(e.getMessage());
			}
	        calendar.add(Calendar.DATE, 1);
        }
  		return result;
  	}

  	// 为所有sku给所有店铺打分
	public static Map<String, Object> scoreProductStoreByDate(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Date theDate = (Date) context.get("theDate");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(theDate);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        java.sql.Date theSqlDate = new java.sql.Date(calendar.getTimeInMillis());
        Debug.logInfo("-------------------------- the date time " + theSqlDate, module);
        List<EntityExpr> exprs = FastList.newInstance();
        exprs.add(EntityCondition.makeCondition("dateDateValue", EntityOperator.EQUALS, theSqlDate));
        // exprs.add(EntityCondition.makeCondition("dateDateValue", EntityOperator.LESS_THAN_EQUAL_TO, thruSqlDate));

        try {
			List<GenericValue> salesOrderItems = delegator.findList("ZzSalesOrderItemFactStarSchema", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
			for (GenericValue salesOrderItem : salesOrderItems) {
				if (UtilValidate.isEmpty(salesOrderItem.getString("productProductId"))) {
					continue;
				}
				if (UtilValidate.isEmpty(salesOrderItem.getString("storeProductStoreId"))) {
					continue;
				}
				if (salesOrderItem.getString("storeProductStoreId").equalsIgnoreCase("EC00")
						|| salesOrderItem.getString("storeProductStoreId").equalsIgnoreCase("EC10")
						|| salesOrderItem.getString("storeProductStoreId").equalsIgnoreCase("S1")
						|| salesOrderItem.getString("storeProductStoreId").equalsIgnoreCase("S3")
						|| salesOrderItem.getString("storeProductStoreId").equalsIgnoreCase("T")) {
					continue;
				}
				// Debug.logInfo("OrderId = " + salesOrderItem.getString("orderId") + " , productId = " + salesOrderItem.getString("productProductId"), module);
				// Color
				addColorScore(delegator, salesOrderItem);
				// Size
				addSizeScore(delegator, salesOrderItem);
				// Style
				addStyleScore(delegator, salesOrderItem);
				// Series
				addSeriesScore(delegator, salesOrderItem);
				// Subseries
				addSubseriesScore(delegator, salesOrderItem);
				// Main Supplier
				addMainSupplierScore(delegator, salesOrderItem);
				// Price
				addPriceScore(delegator, salesOrderItem);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
  		return result;
  	}

	public static Map<String, Object> forecastAllStoresProduct(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
  		String productId=(String) context.get("productId");
  		// String productStoreId=(String) context.get("productStoreId");
  		BigDecimal totalScore = BigDecimal.ZERO;
		try {
			scoreEachProduct(delegator, productId);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}

	private static BigDecimal forecastPriceScore(Delegator delegator, String productId, String productStoreId, StringBuffer comment) throws GenericEntityException {
		List<GenericValue> productPrices = delegator.findByAnd("ProductPrice",
				UtilMisc.toMap("productId", productId, "productPriceTypeId", "LIST_PRICE"));
		productPrices = EntityUtil.filterByDate(productPrices);
		if (UtilValidate.isEmpty(productPrices)) {
			return BigDecimal.ZERO;
		}
		GenericValue productPrice = EntityUtil.getFirst(productPrices);
		Double listPrice = productPrice.getBigDecimal("price").doubleValue();
		String priceSegmentId = getPriceSegementId(listPrice);
		return getStoreProductFeatureScore(delegator, productStoreId, priceSegmentId, comment);
	}

	private static BigDecimal forecastSizeScore(Delegator delegator, String productId, String productStoreId, StringBuffer comment) throws GenericEntityException {
		List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl",
				UtilMisc.toMap("productId", productId, "productFeatureTypeId", "SIZE", "productFeatureApplTypeId", "STANDARD_FEATURE"));
		if (UtilValidate.isEmpty(productFeatureAndAppls)) {
			return BigDecimal.ZERO;
		}
		GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatureAndAppls);
		String productFeatureId = productFeatureAndAppl.getString("productFeatureId");
		return getStoreProductFeatureScore(delegator, productStoreId, productFeatureId, comment);
	}

	private static BigDecimal forecastColorScore(Delegator delegator, String productId, String productStoreId, StringBuffer comment) throws GenericEntityException {
		List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl",
				UtilMisc.toMap("productId", productId, "productFeatureTypeId", "COLOR", "productFeatureApplTypeId", "STANDARD_FEATURE"));
		if (UtilValidate.isEmpty(productFeatureAndAppls)) {
			return BigDecimal.ZERO;
		}
		GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatureAndAppls);
		String productFeatureId = productFeatureAndAppl.getString("productFeatureId");
		return getStoreProductFeatureScore(delegator, productStoreId, productFeatureId, comment);
	}

	private static BigDecimal getStoreProductFeatureScore(Delegator delegator, String productStoreId,
			String productFeatureId, StringBuffer commentBuffer) throws GenericEntityException {
		GenericValue dsAnalysisStoreFeatureScore = delegator.findByPrimaryKey("DsAnalysisStoreFeatureScore",
				UtilMisc.toMap("productStoreId", productStoreId, "analysisDimensionId", productFeatureId));
		if (UtilValidate.isEmpty(dsAnalysisStoreFeatureScore)) {
			return BigDecimal.ZERO;
		}
		// return dsAnalysisStoreFeatureScore.getBigDecimal("score");
		BigDecimal theScore = dsAnalysisStoreFeatureScore.getBigDecimal("score");
		commentBuffer.append(productFeatureId).append("=").append(theScore.intValue()).append(",");
		return theScore;
	}

	public static Map<String, Object> forecastStorePercent(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
  		GenericValue userLogin = (GenericValue) context.get("userLogin");
  		String productId=(String) context.get("productId");
  		List<Map<String, Object>> storePercentList = FastList.newInstance();
  		BigDecimal totalScore = BigDecimal.ZERO;
  		List<GenericValue> forecastScores = null;
  		try {
			forecastScores = delegator.findByAnd("DsForecastStoreProductScore", UtilMisc.toMap("productId", productId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
  		if (UtilValidate.isEmpty(forecastScores)) {
  			return result;
  		}
  		try {
			for (GenericValue forecastScore : forecastScores) {
				boolean flag = isStoreCatalogProduct(delegator,dctx.getDispatcher(),(String)forecastScore.get("productStoreId"),productId,userLogin);
				if(flag){
					totalScore = totalScore.add(forecastScore.getBigDecimal("score"));
				}
			}
  		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		if (totalScore.equals(BigDecimal.ZERO)) {
			return result;
		}
		
		Double totalDoubleScore = totalScore.doubleValue();
		try {
			for (GenericValue forecastScore : forecastScores) {
				Double doubleScore = forecastScore.getBigDecimal("score").doubleValue();
				Double doublePercent = doubleScore / totalDoubleScore;
		  		Map<String, Object> storePercentMap = FastMap.newInstance();
				// storePercentMap.put(forecastScore.getString("productStoreId"), doublePercent);
				storePercentMap.put("productStoreId", forecastScore.getString("productStoreId"));
				storePercentMap.put("score", doubleScore);
				boolean flag = isStoreCatalogProduct(delegator,dctx.getDispatcher(),(String)forecastScore.get("productStoreId"),productId,userLogin);
				if(flag){
					storePercentMap.put("percent", doublePercent);
				}else{
					storePercentMap.put("percent", new Double(0));
				}
				storePercentMap.put("comment", forecastScore.getString("comment"));
				storePercentList.add(storePercentMap);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		result.put("storePercentList", storePercentList);
  		
  		return result;
	}

	
	private static boolean isStoreCatalogProduct(Delegator delegator,LocalDispatcher dispatcher,String productStoreId,String productId,GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		boolean flag = false;
		GenericValue productStoreCatalogAllStyle = EntityUtil.getFirst(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId",productStoreId,"prodCatalogId","RETAIL-DEFAULT")));
		//如果是全款的店铺 直接return true;
		if(!productStoreCatalogAllStyle.isEmpty()){
			return true;
		}
		GenericValue productStoreCatalog = EntityUtil.getFirst(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId",productStoreId)));
		Map serviceResult = dispatcher.runSync("getProdCatalogCategoryProductIds", UtilMisc.toMap("prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT", "prodCatalogId", productStoreCatalog.get("prodCatalogId"), "userLogin", userLogin));
		List productIdList = (List) serviceResult.get("productIdList");
		String productVariantId = "";
		GenericValue productAssoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo",productId,"productAssocTypeId","PRODUCT_VARIANT")));
		if(!productAssoc.isEmpty()){
			productVariantId = productAssoc.getString("productId");
		}
		if(productIdList.contains(productVariantId)||productIdList.contains(productId)){
			flag=true;
		}
		return flag;
	}

	// TODO
	public static Map<String, Object> forecastStoreProductQuantityTest(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
  		String productId=(String) context.get("productId");
  		if (UtilValidate.isEmpty(productId)) {
  			return result;
  		}
  		
  		try {
			List<GenericValue> salesOrderItems = delegator.findByAnd("ZzSalesOrderItemFactStarSchema", UtilMisc.toMap("productProductId", productId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
  		return result;
	}

	public static Map<String, Object> forecastQuantityByInventory(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
  		String productId=(String) context.get("productId");
  		String isManufactCount = (String) context.get("isManufactCount");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
  		if (UtilValidate.isEmpty(productId)) {
  			return result;
  		}
  		List<GenericValue> inventoryItems = null;
  		try {
  			inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
  		BigDecimal totalQuantity = BigDecimal.ZERO;
  		for (GenericValue inventoryItem : inventoryItems) {
  			totalQuantity = totalQuantity.add(inventoryItem.getBigDecimal("availableToPromiseTotal"));
  		}
  		if (isManufactCount.equals("Y")) {
  			totalQuantity = totalQuantity.add(getManufactTotal(delegator, productId));
  		}
  		if (totalQuantity.equals(BigDecimal.ZERO)) {
  			return result;
  		}
  		try {
			Map serviceResult = dispatcher.runSync("analysis.forecastStoreProductQuantity", UtilMisc.toMap("productId", productId, "quantity", totalQuantity, "userLogin", userLogin));
			List storeProductQuantityList = (List) serviceResult.get("storeProductQuantityList");
			result.put("storeProductQuantityList", storeProductQuantityList);
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  		return result;
	}

	private static BigDecimal getManufactTotal(Delegator delegator, String productId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 按分类中的商品预测所有店铺的得分
	 * by liujia
	 */
	public static Map<String, Object> forecastAllStoresProductByCat(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
  		String productCategoryId=(String) context.get("productCategoryId");
		try {
			//获取分类下所有sku
			List<String> productSkuList = getSkuByCatId(delegator, productCategoryId);
			for(String productSku : productSkuList){
				//为某个商品进行打分
				scoreEachProduct(delegator, productSku);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}

	private static void scoreEachProduct(Delegator delegator, String productId) throws GenericEntityException {
		BigDecimal totalScore = BigDecimal.ZERO;
		List<GenericValue> productDimensions = delegator.findByAnd("ZzProductDimension",
				UtilMisc.toMap("productId", productId));
		if (UtilValidate.isEmpty(productDimensions)) {
			return;
		}
		GenericValue productDimension = EntityUtil.getFirst(productDimensions);
        List<EntityExpr> exprs = FastList.newInstance();
        exprs.add(EntityCondition.makeCondition("primaryStoreGroupId", EntityOperator.NOT_EQUAL, "_NA_"));
		List<GenericValue> productStores = delegator.findList("ProductStore", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
		for (GenericValue productStore : productStores) {
			StringBuffer comment = new StringBuffer();
	  		totalScore = BigDecimal.ZERO;
			String productStoreId = productStore.getString("productStoreId");
			totalScore = totalScore.add(forecastColorScore(delegator, productId, productStoreId, comment));
			totalScore = totalScore.add(forecastSizeScore(delegator, productId, productStoreId, comment));
			totalScore = totalScore.add(forecastPriceScore(delegator, productId, productStoreId, comment));
			totalScore = totalScore.add(getStoreProductFeatureScore(delegator, productStoreId, productDimension.getString("style"), comment));
			totalScore = totalScore.add(getStoreProductFeatureScore(delegator, productStoreId, productDimension.getString("seriesId"), comment));
			totalScore = totalScore.add(getStoreProductFeatureScore(delegator, productStoreId, productDimension.getString("subseriesId"), comment));
			totalScore = totalScore.add(getStoreProductFeatureScore(delegator, productStoreId, productDimension.getString("mainSupplierPartyId"), comment));
			// result.put("score", totalScore);
			if (totalScore.equals(BigDecimal.ZERO)) {
				continue;
			}
			GenericValue dsForecastStoreProductScore = delegator.findByPrimaryKey("DsForecastStoreProductScore",
					UtilMisc.toMap("productStoreId", productStoreId, "productId", productId));
			if (UtilValidate.isEmpty(dsForecastStoreProductScore)) {
				dsForecastStoreProductScore = delegator.makeValue("DsForecastStoreProductScore",
						UtilMisc.toMap("productStoreId", productStoreId, "productId", productId, "score", totalScore, "comment", comment.toString()));
				delegator.create(dsForecastStoreProductScore);
			} else {
				dsForecastStoreProductScore.set("score", totalScore);
				dsForecastStoreProductScore.set("comment", comment.toString());
				dsForecastStoreProductScore.store();
			}
		}
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
	
	
}
