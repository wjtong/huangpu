<%@ page session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:mondrianQuery id="query01" jdbcDriver="com.mysql.jdbc.Driver" jdbcUrl="jdbc:mysql://localhost/bluemountainolap?user=root&password=root" catalogUri="/WEB-INF/queries/FoodMart.xml"
   jdbcUser="root" jdbcPassword="root" connectionPooling="false">
select
  {[Measures].[List Price], [Measures].[Quantity], [Measures].[Gross Amount]} on columns,
  {([Customers].[All Customers], [Product].[All Products])} ON rows
from SalesOrderItem
</jp:mondrianQuery>





<c:set var="title01" scope="session">订单分析</c:set>
