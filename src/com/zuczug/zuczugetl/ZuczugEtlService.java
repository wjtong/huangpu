package com.zuczug.zuczugetl;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import com.ibm.icu.util.Calendar;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by zuczug on 14-11-24.
 */
public class ZuczugEtlService {

    public static Map<String, Object> dataFromSwToOlap(DispatchContext ctx, Map<String, ? extends Object> context) {

        String entityName = (String) context.get("entityName");

        if(UtilValidate.isNotEmpty(entityName)){

            if("DmProdSalesDimension".equals(entityName)){
                ZuczugEtlUtil.importDmProdSalesDimension(ctx,context);
            }else if("DmTempagentStoreDimension".equals(entityName)){
                ZuczugEtlUtil.importDmTempagentStoreDimension(ctx,context);
            }else if("DmFinanceSalesDeptProdDimension".equals(entityName)){
                ZuczugEtlUtil.importDmFinanceSalesDeptProdDimension(ctx,context);
            }else if("DmFinanceOrderFact".equals(entityName)){
                ZuczugEtlUtil.importDmFinanceOrderFact(ctx,context);
            }else if("DmSalesDeptOrderFact".equals(entityName)){
                ZuczugEtlUtil.importDmSalesDeptOrderFact(ctx,context);
            }

        } else {

            ZuczugEtlUtil.importDmProdSalesDimension(ctx,context);

            ZuczugEtlUtil.importDmTempagentStoreDimension(ctx,context);

            ZuczugEtlUtil.importDmFinanceSalesDeptProdDimension(ctx,context);

            ZuczugEtlUtil.importDmSalesDeptOrderFact(ctx,context);

            ZuczugEtlUtil.importDmFinanceOrderFact(ctx,context);

        }

        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> findReleaseDate(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Timestamp  releaseDate =  (Timestamp) context.get("releaseDate");
    	//get(Calendar.MONTH) + 1;
		Calendar cale=Calendar.getInstance();
		cale.setTime(releaseDate);
    	int Month=cale.get(Calendar.MONTH)+1;
    	String releaseMonth=Integer.toString(Month)+"月";
    	result.put("releaseMonth", releaseMonth);
		return result;
    	
    	
    }
    public static Map<String, Object> judgeDataNumber(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		String productId=(String) context.get("productId");
		List<GenericValue> productAssocsList=null;
		
		try {
			productAssocsList=delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",productId,"productAssocTypeId","PRODUCT_VARIANT"));
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String number;
		if (productAssocsList.size()==1) {
			number="1";
		}else {
			number="2";
		}
		result.put("number", number);
    	return result;
    }
    
  //获取订单项最小的orderItemSeqId zhoulei
  	public static Map<String, Object> findMinimumOrderItemSeq(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Map<String, Object> result = ServiceUtil.returnSuccess();
  		Delegator delegator = dctx.getDelegator();
  		String orderId=(String) context.get("orderId");
  		List<GenericValue> orderItemSeqList=null;
  		
  		try {
  			orderItemSeqList=delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId",orderId));
  		} catch (GenericEntityException e) {
  			e.printStackTrace();
  		}
  		 String minimumOrderItemSeq="";
  		if (orderItemSeqList != null) {
  			//转换为int取最小值
  			int minimum=100;
  	        for (int i = 0; i <= orderItemSeqList.size()-1; i++) {
  				int orderItemSeqId=Integer.parseInt((String) orderItemSeqList.get(i).get("orderItemSeqId"));
  	        	if (orderItemSeqId<minimum) {
  	        		minimum=orderItemSeqId;
  	        	}
  			}
  	        //把最小值转回字符串
  	       
  	        if (minimum > 9) {
  	            minimumOrderItemSeq = "000" + String.valueOf(minimum);
  	    	}else {
  	    		minimumOrderItemSeq = "0000" + String.valueOf(minimum);
  	    	}
  		}
  		
  		result.put("minimumOrderItemSeq", minimumOrderItemSeq);
  		return result;
  	}
  	/**
  	 * 获取商品的成份列表，洗涤方式
  	 * zhoulei
  	 * @param dctx
  	 * @param context
  	 * @return
  	 */
  	public static Map<String, Object> getIngredientsListWashingWay(DispatchContext dctx,
  			Map<String, ? extends Object> context) {
  		Delegator delegator = dctx.getDelegator();
  		String productId=(String) context.get("productId");
		Map<String, Object> result = ServiceUtil.returnSuccess();

  		List<GenericValue> IngredientsList;
  		GenericValue SpecialInstructionsList;
  		String ingredientsType = "";
  		String IngredientsText = "";
  		String SpecialInstructions="";
  		
  		try {
  			//成份列表
			IngredientsList=delegator.findByAnd("ProductIngredientGroupAndAppl", UtilMisc.toMap("productId",productId,"ingredientTypeId","PRODUCT_INGREDIENT"));
			if (UtilValidate.isNotEmpty(IngredientsList)) {
				for(int i = 0; i < IngredientsList.size(); i++){
					if (ingredientsType.equals(IngredientsList.get(i).get("ProductIngredientGroupDescription"))) {
						IngredientsText=IngredientsText+"//"+IngredientsList.get(i).get("percent")+
								"%"+IngredientsList.get(i).get("description");
					}else {
						ingredientsType=(String) IngredientsList.get(i).get("ProductIngredientGroupDescription");
						IngredientsText=IngredientsText+(String) IngredientsList.get(i).get("ProductIngredientGroupDescription")
								+IngredientsList.get(i).get("percent")+"%"+IngredientsList.get(i).get("description");
					}
				}
			}
			//特殊说明
			SpecialInstructionsList=(GenericValue) delegator.findOne("ProductAttribute", UtilMisc.toMap("productId",productId,"attrName","SPECIAL_INSTRUCTIONS"),true);
			if (UtilValidate.isNotEmpty(SpecialInstructionsList)) {
				SpecialInstructions=(String) SpecialInstructionsList.get("attrValue");
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
  		result.put("ingredientsTextBm", IngredientsText);
  		result.put("specialInstructionsBm", SpecialInstructions);
  		
  		return result;
  	}
  	
  	public static Map<String, Object> shareEquallySalesOrderItemFact(
			DispatchContext dctx, Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();
		String orderId = (String) context.get("orderId");
		GenericHelperInfo helperInfo = delegator
				.getGroupHelperInfo("org.ofbiz.olap");
		SQLProcessor sqlproc = new SQLProcessor(helperInfo);
		updateOrderItemFact(sqlproc, orderId, "ZUCZUG_SALES_ORDER_ITEM_FACT");
		updateOrderItemFact(sqlproc, orderId, "ZUC_ORDER_ITEM_PAYMENT_FACT");
		return ServiceUtil
				.returnSuccess("Generate snapshot data successfully!");
	}
  	
  	public static void updateOrderItemFact(SQLProcessor sqlproc,
			String orderId, String tableName) {
		ResultSet rs = null;
		BigDecimal totalGrossAmt = BigDecimal.ZERO;
		BigDecimal totalIntegral = BigDecimal.ZERO;
		BigDecimal totalCoupon = BigDecimal.ZERO;
		BigDecimal totalIntegralGrossAmt = BigDecimal.ZERO;
		try {

			sqlproc.prepareStatement(queryTotalAmoutFormOrderItemFact(tableName));
			sqlproc.setValue(orderId);
			rs = sqlproc.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					if (rs.getBigDecimal("totalGrossAmt") != null)
						totalGrossAmt = rs.getBigDecimal("totalGrossAmt");
					if (rs.getBigDecimal("totalIntegral") != null)
						totalIntegral = rs.getBigDecimal("totalIntegral");
					if (rs.getBigDecimal("totalCoupon") != null)
						totalCoupon = rs.getBigDecimal("totalCoupon");
					if (rs.getBigDecimal("totalIntegralGrossAmt") != null)
						totalIntegralGrossAmt = rs
								.getBigDecimal("totalIntegralGrossAmt");

					break;
				}
			}
			sqlproc.prepareStatement(shareEquallyValue(tableName,
					totalGrossAmt, totalIntegralGrossAmt));
			int i = totalGrossAmt.compareTo(BigDecimal.ZERO);
			int j = totalIntegralGrossAmt.compareTo(BigDecimal.ZERO);
			if (j != 0) {
				sqlproc.setValue(totalIntegral);
				sqlproc.setValue(totalIntegralGrossAmt);
			}
			if (i != 0) {
				sqlproc.setValue(totalCoupon);
				sqlproc.setValue(totalGrossAmt);
			}

			sqlproc.setValue(orderId);
			sqlproc.executeUpdate();

			sqlproc.prepareStatement(shareEquallyValue2(tableName));
			sqlproc.setValue(orderId);
			sqlproc.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				sqlproc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
  	
  	private static String queryTotalAmoutFormOrderItemFact(String tableName) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SUM(INTEGRAL_AMOUNT_DISCOUNT) totalIntegral,SUM(COUPON_AMOUNT) totalCoupon,SUM(EXT_GROSS_AMOUNT)");
		sql.append(" totalGrossAmt,SUM(CASE WHEN INTEGRAL_PROD = 'Y' THEN EXT_GROSS_AMOUNT ELSE 0 END) totalIntegralGrossAmt FROM ");
		sql.append(tableName);
		sql.append(" WHERE ORDER_ID = ?");
		return sql.toString();
	}
  	
  	private static String shareEquallyValue(String tableName,
			BigDecimal totalGrossAmt, BigDecimal totalIntegralGrossAmt) {

		StringBuffer sql = new StringBuffer();

		sql.append("UPDATE ");
		sql.append(tableName);
		sql.append(" SET INTEGRAL_AMOUNT_DISCOUNT = ");
		int i = totalGrossAmt.compareTo(BigDecimal.ZERO);
		int j = totalIntegralGrossAmt.compareTo(BigDecimal.ZERO);
		if (j != 0)
			sql.append("CASE WHEN INTEGRAL_PROD = 'Y' THEN EXT_GROSS_AMOUNT  * ? / ? ELSE 0 END,");
		else
			sql.append(" INTEGRAL_AMOUNT_DISCOUNT,");

		if (i != 0)
			sql.append(" COUPON_AMOUNT = EXT_GROSS_AMOUNT * ? / ?");
		else
			sql.append("COUPON_AMOUNT = 0");

		sql.append(" WHERE ORDER_ID = ?");
		return sql.toString();
	}
  	
  	private static String shareEquallyValue2(String tableName) {
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE ");
		sql.append(tableName);
		sql.append(" SET EXT_DISCOUNT_AMOUNT = INTEGRAL_AMOUNT_DISCOUNT + COUPON_AMOUNT,");
		sql.append(" INTEGRAL_AMOUNT = INTEGRAL_AMOUNT_DISCOUNT * 15,");
		sql.append(" EXT_NET_AMOUNT = EXT_GROSS_AMOUNT + INTEGRAL_AMOUNT_DISCOUNT + COUPON_AMOUNT + EXT_SHIPPING_AMOUNT");
		sql.append(" WHERE ORDER_ID = ?");
		return sql.toString();
	}


}
