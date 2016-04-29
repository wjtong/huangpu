package com.zuczug.zuczugetl;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.widget.menu.ModelMenuCondition.IfEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import jxl.*;
import jxl.read.biff.BiffException;

/**
 * Created by zuczug on 14-11-25.
 */
public class ZuczugEtlUtil {

    /**
     * 导入DmTempagentStoreDimension数据
     * @param ctx
     * @param context
     */
    public static void importDmTempagentStoreDimension(DispatchContext ctx, Map<String, ? extends Object> context){

        Delegator delegator = ctx.getDelegator();

        try {
            List<GenericValue> tempagentStoreDimensionList = delegator.findList("TempagentStoreDimension",null,null,null,null,true);
            for(GenericValue tempagentStoreDimensionMap:tempagentStoreDimensionList){

                GenericValue dmTempagentStoreDimension = delegator.findOne("DmTempagentStoreDimension", UtilMisc.toMap("dimensionId",tempagentStoreDimensionMap.getString("dimensionId")),true);

                //不重复插入数据
                if(UtilValidate.isNotEmpty(dmTempagentStoreDimension)){
                    continue;
                }

                GenericValue dmTempagentStoreDimensionMap = delegator.makeValue("DmTempagentStoreDimension");

                Set<String> tempagentStoreDimensionSet = tempagentStoreDimensionMap.keySet();

                for(String tempagentStoreDimensionKey:tempagentStoreDimensionSet){

                    if("storeMethod".equals(tempagentStoreDimensionKey)){
                        continue;
                    }

                    dmTempagentStoreDimensionMap.put(tempagentStoreDimensionKey,tempagentStoreDimensionMap.get(tempagentStoreDimensionKey));
                }

                dmTempagentStoreDimensionMap.create();
            }

        }catch (GenericEntityException e){
            e.printStackTrace();
            Debug.logError(e.getMessage(),"ZuczugEtlUtil");
            return;
        }finally {
            delegator.clearAllCaches();
        }

    }

    /**
     * 导入DmProdSalesDimension数据
     * @param ctx
     * @param context
     */
    public static void importDmProdSalesDimension(DispatchContext ctx, Map<String, ? extends Object> context){

        Delegator delegator = ctx.getDelegator();

        try {
            List<GenericValue> prodSalesDimensionList = delegator.findList("ProdSalesDimension", null, null, null, null, true);
            for(GenericValue prodSalesDimensionMap:prodSalesDimensionList){

                GenericValue dmProdSalesDimension = delegator.findOne("DmProdSalesDimension", UtilMisc.toMap("dimensionId",prodSalesDimensionMap.getString("dimensionId")),true);

                //避免重复插入数据
                if(UtilValidate.isNotEmpty(dmProdSalesDimension)){
                    continue;
                }

                GenericValue dmProdSalesDimensionMap = delegator.makeValue("DmProdSalesDimension");

                Set<String> prodSalesDimensionSet = prodSalesDimensionMap.keySet();

                for(String prodSalesDimensionKey:prodSalesDimensionSet){
                    dmProdSalesDimensionMap.put(prodSalesDimensionKey,prodSalesDimensionMap.get(prodSalesDimensionKey));
                }

                dmProdSalesDimensionMap.create();
            }
        }catch (GenericEntityException e){
            e.printStackTrace();
            Debug.logError(e.getMessage(),"ZuczugEtlUtil");
            return;
        }finally {
            delegator.clearAllCaches();
        }

    }

    /**
     * 导入DmFinanceSalesDeptProdDimension数据
     * @param ctx
     * @param context
     */
    public static void importDmFinanceSalesDeptProdDimension(DispatchContext ctx, Map<String, ? extends Object> context){

        Delegator delegator = ctx.getDelegator();

        try{

            List<GenericValue> prodSalesDimensionList = delegator.findList("ProdSalesDimension",null,null,null,null,true);

            //取得当前所有的虚拟商品
            List<GenericValue> allVariantProdSalesDimensionList = EntityUtil.filterByAnd(prodSalesDimensionList,UtilMisc.toMap("productType","变形"));
            List<String> virtualProductIds = EntityUtil.getFieldListFromEntityList(allVariantProdSalesDimensionList,"virtualProductId",true);

            for(String virtualProductId:virtualProductIds){

                //取得当前虚拟商品下的所有颜色
                List<GenericValue> variantColorProdSalesDimensionList = EntityUtil.filterByAnd(allVariantProdSalesDimensionList,UtilMisc.toMap("virtualProductId",virtualProductId));
                List<String> colorCodes = EntityUtil.getFieldListFromEntityList(variantColorProdSalesDimensionList,"colorCode",true);

                for(String colorCode:colorCodes){

                    //通过颜色取得当前产品的所有变形商品，取得数据
                    GenericValue variantSizeProdSalesDimensionMap = EntityUtil.getFirst(
                            EntityUtil.filterByAnd(variantColorProdSalesDimensionList,UtilMisc.toMap("colorCode",colorCode)));

                    GenericValue dmFinanceSalesDeptProdDimension = delegator.findOne("DmFinanceSalesDeptProdDimension", UtilMisc.toMap("dimensionId",variantSizeProdSalesDimensionMap.getString("dimensionId")),true);

                    //避免重复插入数据
                    if(UtilValidate.isNotEmpty(dmFinanceSalesDeptProdDimension)){
                        continue;
                    }

                    GenericValue dmFinanceSalesDeptProdDimensionMap = delegator.makeValue("DmFinanceSalesDeptProdDimension");

                    dmFinanceSalesDeptProdDimensionMap.put("dimensionId",variantSizeProdSalesDimensionMap.getString("dimensionId"));
                    dmFinanceSalesDeptProdDimensionMap.put("prodGroup",variantSizeProdSalesDimensionMap.getString("prodGroup"));
                    dmFinanceSalesDeptProdDimensionMap.put("series",variantSizeProdSalesDimensionMap.getString("series"));
                    dmFinanceSalesDeptProdDimensionMap.put("virtualProductId",variantSizeProdSalesDimensionMap.getString("virtualProductId"));
                    dmFinanceSalesDeptProdDimensionMap.put("internalName",variantSizeProdSalesDimensionMap.getString("internalName"));
                    dmFinanceSalesDeptProdDimensionMap.put("colorCode",variantSizeProdSalesDimensionMap.getString("colorCode"));
                    dmFinanceSalesDeptProdDimensionMap.put("color",variantSizeProdSalesDimensionMap.getString("color"));
                    dmFinanceSalesDeptProdDimensionMap.put("styleNoAndColor",variantSizeProdSalesDimensionMap.getString("virtualProductId") + variantSizeProdSalesDimensionMap.getString("colorCode"));
                    dmFinanceSalesDeptProdDimensionMap.put("wave",variantSizeProdSalesDimensionMap.getString("wave"));
                    dmFinanceSalesDeptProdDimensionMap.put("listPrice",variantSizeProdSalesDimensionMap.getBigDecimal("listPrice"));

                    dmFinanceSalesDeptProdDimensionMap.create();

                }

            }

        }catch (GenericEntityException e){
            e.printStackTrace();
            return;
        }finally {
            delegator.clearAllCaches();
        }

    }

    /**
     * 导入DmSalesDeptOrderFact数据
     * @param ctx
     * @param context
     */
    public static void importDmSalesDeptOrderFact(DispatchContext ctx, Map<String, ? extends Object> context){

        Delegator delegator = ctx.getDelegator();

        try {

            //取得需要遍历数据放入内存
            List<GenericValue> tempQuoteItemFactList = delegator.findList("TempQuoteItemFact",null,null,null,null,true);
            List<GenericValue> tempRequestItemFactList = delegator.findList("TempRequestItemFact",null,null,null,null,true);
            List<GenericValue> dmFinanceSalesDeptProdDimensionList = delegator.findList("DmFinanceSalesDeptProdDimension",null,null,null,null,true);
            List<GenericValue> dmTempagentStoreDimensionList = delegator.findList("DmTempagentStoreDimension",null,null,null,null,true);

            int total = dmFinanceSalesDeptProdDimensionList.size()*dmTempagentStoreDimensionList.size();
            int curr = 0;

            for(GenericValue dmFinanceSalesDeptProdDimensionMap:dmFinanceSalesDeptProdDimensionList){

                //取得当前颜色下的所有商品,所有尺码
                List<GenericValue> dmProdSalesDimensionList = delegator.findByAndCache("DmProdSalesDimension",
                        UtilMisc.toMap("virtualProductId",dmFinanceSalesDeptProdDimensionMap.getString("virtualProductId"),
                                "colorCode",dmFinanceSalesDeptProdDimensionMap.getString("colorCode")));

                for(GenericValue dmTempagentStoreDimensionMap:dmTempagentStoreDimensionList){

                    //通过逻辑主键过滤重复数据
                    GenericValue dmSalesDeptOrderFact = EntityUtil.getFirst(delegator.findByAndCache("DmSalesDeptOrderFact",
                            UtilMisc.toMap("deptProdDimId", dmFinanceSalesDeptProdDimensionMap.getString("dimensionId"),
                                    "agentStoreDimId", dmTempagentStoreDimensionMap.getString("dimensionId"))));
                    if(UtilValidate.isNotEmpty(dmSalesDeptOrderFact)){
                        continue;
                    }

                    Debug.logInfo("begin%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + dmFinanceSalesDeptProdDimensionMap.getString("dimensionId") + "******************************************","");

                    GenericValue dmSalesDeptOrderFactMap = delegator.makeValue("DmSalesDeptOrderFact");

                    dmSalesDeptOrderFactMap.put("factId",delegator.getNextSeqId("DmSalesDeptOrderFact"));

                    dmSalesDeptOrderFactMap.put("deptProdDimId",dmFinanceSalesDeptProdDimensionMap.getString("dimensionId"));
                    dmSalesDeptOrderFactMap.put("agentStoreDimId",dmTempagentStoreDimensionMap.getString("dimensionId"));

                    for(GenericValue dmProdSalesDimensionMap:dmProdSalesDimensionList){

                        GenericValue tempQuoteItemFactMap = EntityUtil.getFirst(EntityUtil.filterByAnd(
                                tempQuoteItemFactList,UtilMisc.toMap("productDimId",dmProdSalesDimensionMap.getString("dimensionId"),
                                        "agentStoreDimId",dmTempagentStoreDimensionMap.getString("agentStoreId"))));

                        BigDecimal reqQuantityTotal = BigDecimal.ZERO;
                        BigDecimal reqListAmount = BigDecimal.ZERO;
                        if(UtilValidate.isNotEmpty(tempQuoteItemFactMap)){
                            dmSalesDeptOrderFactMap.put("reqQuantitySize" + ZuczugEtlStaticField.sizeField.get(dmProdSalesDimensionMap.getString("size")),
                                    tempQuoteItemFactMap.getBigDecimal("quantity"));

                            reqQuantityTotal = reqQuantityTotal.add(tempQuoteItemFactMap.getBigDecimal("quantity"));
                            reqListAmount = reqListAmount.add(tempQuoteItemFactMap.getBigDecimal("listAmount"));

                        }

                        dmSalesDeptOrderFactMap.put("reqQuantityTotal",reqQuantityTotal);
                        dmSalesDeptOrderFactMap.put("reqListAmount",reqListAmount);

                        GenericValue tempRequestItemFactMap = EntityUtil.getFirst(EntityUtil.filterByAnd(
                                tempRequestItemFactList,UtilMisc.toMap("productDimId",dmProdSalesDimensionMap.getString("dimensionId"),
                                        "agentStoreDimId",dmTempagentStoreDimensionMap.getString("agentStoreId"))));

                        BigDecimal adjustListAmount = BigDecimal.ZERO;
                        if(UtilValidate.isNotEmpty(tempRequestItemFactMap)){
                            dmSalesDeptOrderFactMap.put("adjustQuantitySize" + ZuczugEtlStaticField.sizeField.get(dmProdSalesDimensionMap.getString("size")),
                                    tempRequestItemFactMap.getBigDecimal("quantity"));
                        }

                        dmSalesDeptOrderFactMap.put("adjustListAmount",adjustListAmount);

                        dmSalesDeptOrderFactMap.put("diffQuantity",BigDecimal.ZERO);
                        dmSalesDeptOrderFactMap.put("diffListAmount",adjustListAmount.min(reqListAmount));

                    }

                    dmSalesDeptOrderFactMap.create();

                    curr ++;

                    if(curr == 1000){
                        return;
                    }

                    Debug.logInfo("end%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + dmFinanceSalesDeptProdDimensionMap.getString("dimensionId") + "******************************************curr:" + curr + "total:"+total,"");

                }

            }

        }catch (GenericEntityException e){
            e.printStackTrace();
            Debug.logError(e.getMessage(),"ZuczugEtlUtil");
            return;
        }finally {
            delegator.clearAllCaches();
        }

    }

    /**
     * 导入DmFinanceOrderFact数据
     * @param ctx
     * @param context
     */
    public static void importDmFinanceOrderFact(DispatchContext ctx, Map<String, ? extends Object> context){

        Delegator delegator = ctx.getDelegator();

        try {

            //读取数据进入内存
            List<GenericValue> tempRequirementFactList = delegator.findList("TempRequirementFact",null,null,null,null,true);
            List<GenericValue> tempQuoteItemFactList = delegator.findList("TempQuoteItemFact",null,null,null,null,true);
            List<GenericValue> tempRequestItemFactList = delegator.findList("TempRequestItemFact",null,null,null,null,true);
            List<GenericValue> financeDeptOrdersList = delegator.findList("FinanceDeptOrders",null,null,null,null,true);

            //取出产品信息数据,到颜色
            List<GenericValue> dmFinanceSalesDeptProdDimensionList = delegator.findList("DmFinanceSalesDeptProdDimension",null,null,null,null,true);

            for(GenericValue dmFinanceSalesDeptProdDimensionMap:dmFinanceSalesDeptProdDimensionList){

                //过滤重复数据,通过逻辑主键
                GenericValue dmFinanceOrderFact = EntityUtil.getFirst(
                        delegator.findByAndCache("DmFinanceOrderFact",
                                UtilMisc.toMap("deptProdDimId",dmFinanceSalesDeptProdDimensionMap.getString("dimensionId"))));

                if(UtilValidate.isNotEmpty(dmFinanceOrderFact)){
                    continue;
                }

                Debug.logInfo("**************************%%%%%%%%%%%%%%%start%%%%%%%%%%%%%","");

                GenericValue dmFinanceOrderFactMap = delegator.makeValue("DmFinanceOrderFact");

                dmFinanceOrderFactMap.put("factId",delegator.getNextSeqId("DmFinanceOrderFact"));
                dmFinanceOrderFactMap.put("estimatedOrderQuantity",BigDecimal.ZERO);
                dmFinanceOrderFactMap.put("deptProdDimId",dmFinanceSalesDeptProdDimensionMap.getString("dimensionId"));

                GenericValue financeDeptOrdersMap = EntityUtil.getFirst(
                        EntityUtil.filterByAnd(financeDeptOrdersList,
                                UtilMisc.toMap("styleNo",dmFinanceSalesDeptProdDimensionMap.getString("virtualProductId"),
                                        "colorCode",dmFinanceSalesDeptProdDimensionMap.getString("colorCode"))));

                if(UtilValidate.isNotEmpty(financeDeptOrdersMap)){

                    //预估价格
                    dmFinanceOrderFactMap.put("estimatedPrice",financeDeptOrdersMap.getBigDecimal("estimatedPrice"));

                    // 商品预估下单
                    dmFinanceOrderFactMap.put("estimatedOrderQuantity",financeDeptOrdersMap.getBigDecimal("estimatedOrderQuantity"));
                    dmFinanceOrderFactMap.put("estimatedOrderAmount",financeDeptOrdersMap.getBigDecimal("estimatedOrderAmount"));

                    // 商品调整下单
                    dmFinanceOrderFactMap.put("adjustmentOrderQuantity",financeDeptOrdersMap.getBigDecimal("adjustmentOrderQuantity"));
                    dmFinanceOrderFactMap.put("adjustmentOrderAmount",financeDeptOrdersMap.getBigDecimal("adjustmentOrderAmount"));
                }

                //客户订单对这个商品这个颜色下的订单量
                List<GenericValue> prodSalesDimensionList = delegator.findByAndCache("ProdSalesDimension",
                        UtilMisc.toMap("virtualProductId",dmFinanceSalesDeptProdDimensionMap.getString("virtualProductId"),
                                "colorCode",dmFinanceSalesDeptProdDimensionMap.getString("colorCode")));

                BigDecimal reqTotalQuantity = BigDecimal.ZERO;
                BigDecimal reqTotalAmt = BigDecimal.ZERO;

                BigDecimal custAdjTotalQuantity = BigDecimal.ZERO;
                BigDecimal custAdjTotalAmt = BigDecimal.ZERO;

                BigDecimal prodOrdTotalQuantity = BigDecimal.ZERO;
                BigDecimal prodOrdTotalAmt = BigDecimal.ZERO;

                for(GenericValue prodSalesDimensionMap:prodSalesDimensionList){

                    //客户订单
                    GenericValue tempRequirementFactMap = EntityUtil.getFirst(
                            EntityUtil.filterByAnd(tempRequirementFactList,
                                    UtilMisc.toMap("productDimId",prodSalesDimensionMap.getString("dimensionId"))));

                    if(UtilValidate.isNotEmpty(tempRequirementFactMap)){
                        dmFinanceOrderFactMap.put("reqQuantitySize" + ZuczugEtlStaticField.sizeField.get(prodSalesDimensionMap.getString("size")),tempRequirementFactMap.getBigDecimal("quantity"));

                        if(UtilValidate.isNotEmpty(tempRequirementFactMap.getBigDecimal("quantity"))){
                            reqTotalQuantity = reqTotalQuantity.add(tempRequirementFactMap.getBigDecimal("quantity"));
                        }

                        if(UtilValidate.isNotEmpty(tempRequirementFactMap.getBigDecimal("listAmount"))){
                            reqTotalAmt = reqTotalAmt.add(tempRequirementFactMap.getBigDecimal("listAmount"));
                        }

                    }

                    //客户调整订货量
                    GenericValue tempQuoteItemFactMap = EntityUtil.getFirst(
                            EntityUtil.filterByAnd(tempQuoteItemFactList,
                                    UtilMisc.toMap("productDimId",prodSalesDimensionMap.getString("dimensionId"))));

                    if(UtilValidate.isNotEmpty(tempQuoteItemFactMap)){
                        dmFinanceOrderFactMap.put("cstAdjQuantitySize" + ZuczugEtlStaticField.sizeField.get(prodSalesDimensionMap.getString("size")),tempQuoteItemFactMap.getBigDecimal("quantity"));

                        if(UtilValidate.isNotEmpty(tempQuoteItemFactMap.getBigDecimal("quantity"))){
                            custAdjTotalQuantity = custAdjTotalQuantity.add(tempQuoteItemFactMap.getBigDecimal("quantity"));
                        }

                        if(UtilValidate.isNotEmpty(tempQuoteItemFactMap.getBigDecimal("listAmount"))){
                            custAdjTotalAmt = custAdjTotalAmt.add(tempQuoteItemFactMap.getBigDecimal("listAmount"));
                        }

                    }

                    //商品正式下单量
                    GenericValue tempRequestItemFactMap = EntityUtil.getFirst(
                            EntityUtil.filterByAnd(tempRequestItemFactList,
                                    UtilMisc.toMap("productDimId",prodSalesDimensionMap.getString("dimensionId"))));

                    if(UtilValidate.isNotEmpty(tempRequestItemFactMap)){
                        dmFinanceOrderFactMap.put("prodOrdQuantitySize" + ZuczugEtlStaticField.sizeField.get(prodSalesDimensionMap.getString("size")),tempRequestItemFactMap.getBigDecimal("quantity"));

                        if(UtilValidate.isNotEmpty(tempRequestItemFactMap.getBigDecimal("quantity"))){
                            prodOrdTotalQuantity = prodOrdTotalQuantity.add(tempRequestItemFactMap.getBigDecimal("quantity"));
                        }

                        if(UtilValidate.isNotEmpty(tempRequestItemFactMap.getBigDecimal("listAmount"))){
                            prodOrdTotalAmt = prodOrdTotalAmt.add(tempRequestItemFactMap.getBigDecimal("listAmount"));
                        }

                    }

                }

                //总和
                dmFinanceOrderFactMap.put("reqTotalQuantity",reqTotalQuantity);
                dmFinanceOrderFactMap.put("reqTotalAmt",reqTotalAmt);

                dmFinanceOrderFactMap.put("custAdjTotalQuantity",custAdjTotalQuantity);
                dmFinanceOrderFactMap.put("custAdjTotalAmt",custAdjTotalAmt);

                dmFinanceOrderFactMap.put("prodOrdTotalQuantity",prodOrdTotalQuantity);
                dmFinanceOrderFactMap.put("prodOrdTotalAmt",prodOrdTotalAmt);

                dmFinanceOrderFactMap.put("estimatedDiffQuantity",
                        dmFinanceOrderFactMap.getBigDecimal("estimatedOrderQuantity").min(dmFinanceOrderFactMap.getBigDecimal("reqTotalQuantity")));

                //TODO 素然备货
                dmFinanceOrderFactMap.put("bkTotalQuantity",BigDecimal.ZERO);

                if(dmFinanceOrderFactMap.getString("deptProdDimId").equals("M151PA01-69-2-20141126111754")){
                    System.out.print("");
                }

                Debug.logInfo("**************************"+dmFinanceOrderFactMap+"%%%%%%%%%%%%%%%end%%%%%%%%%%%%%","");

                dmFinanceOrderFactMap.create();

            }

        }catch (GenericEntityException e){
            e.printStackTrace();
            Debug.logError(e.getMessage(),"ZuczugEtlUtil");
            return;
        }finally {
            delegator.clearAllCaches();
        }

    }
    
    public static Map<String, Object> ImportInventoryData(DispatchContext dctx,Map<String, ? extends Object> context) {
    	LocalDispatcher dispatcher = dctx.getDispatcher();
    	Delegator delegator = dctx.getDelegator();
    	Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
    	Timestamp fromDate = UtilDateTime.nowTimestamp();//时间
    	File outFilePath=new File("hot-deploy/zuczugetl/data/ImportInventoryData.xls");
    	if (outFilePath.exists()) {
    		Debug.log("++++++++++++存在");
		}
			try {
				Workbook book = Workbook.getWorkbook(new File("hot-deploy/zuczugetl/data/ImportInventoryData.xls"));
				Sheet sheet = (Sheet) book.getSheet(0);
				for(int i=0;i<sheet.getRows()-1;i++){
					Cell cell1 = sheet.getCell( 0 , i );//前一个参数代表列，后一个参数代表行
					String virtualProduct = cell1.getContents();//商品Id

					Cell cell2 = sheet.getCell( 1 , i );
					String yanse = cell2.getContents();//颜色
					
					Cell cell3 = sheet.getCell( 2 , i );
					String chima = cell3.getContents();//尺码
					
					Cell cell4 = sheet.getCell( 3 , i );
					String shuliang = cell4.getContents();//数量
					
					String productId=virtualProduct+"-"+yanse+"-"+chima;
					
					Map<String,Object> inventoryProduct=FastMap.newInstance();
					Debug.log("++++++++++++1"+productId);
					inventoryProduct.put("productId", productId);
					inventoryProduct.put("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
					inventoryProduct.put("datetimeReceived", fromDate);
					inventoryProduct.put("quantityAccepted", shuliang);
					inventoryProduct.put("quantityRejected", "0");
					inventoryProduct.put("facilityId", "ZUCZUG_CLOTHESFACILITY");
					inventoryProduct.put("userLogin", userLogin);
	                //判断仓库里面是否有这条库存明细
//	                List<GenericValue> inventory = delegator.findByAnd("InventoryItem", 
//	                		UtilMisc.toMap("productId",productId,"facilityId","ZUCZUG_CLOTHESFACILITY"));
	                
//	                if (UtilValidate.isEmpty(inventory)) {
	                dispatcher.runSync("receiveInventoryProduct", inventoryProduct);
//	                	Debug.log("++++++++++++2"+productId);
//					}
				}
			} catch (IOException e) {
				Debug.log("++++++++++++11:"+e.getMessage());
				e.printStackTrace();
			} catch (BiffException e) {
				Debug.log("++++++++++++12:"+e.getMessage());
				e.printStackTrace();
			} catch (GenericServiceException e) {
				e.printStackTrace();
			} 
			
			
			

    	return result;
    	
    }

}
