<%@ page session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:mondrianQuery id="query01" jdbcDriver="com.mysql.jdbc.Driver" jdbcUrl="jdbc:mysql://rdsjymmrjjymmrj.mysql.rds.aliyuncs.com/ofbizzuczugolap?user=starscream&password=starscream" catalogUri="/WEB-INF/queries/FoodMart.xml"
   jdbcUser="starscream" jdbcPassword="starscream" connectionPooling="false">
select
  {[Measures].[List Price], [Measures].[Quantity], [Measures].[Gross Amount]} on columns,
  {([Store].[All Store], [Product].[All Products])} ON rows
from SalesOrderItemBM
</jp:mondrianQuery>





<c:set var="title01" scope="session">订单分析</c:set>