<%@ page session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:mondrianQuery id="query01" jdbcDriver="com.mysql.jdbc.Driver" jdbcUrl="jdbc:mysql://localhost/bluemountainolap?user=root&password=root" catalogUri="/WEB-INF/queries/FoodMart.xml"
   jdbcUser="root" jdbcPassword="root" connectionPooling="false">
select
  {[Measures].[Request List Price], [Measures].[Request Quantity],
  [Measures].[Dinghuo Size 0], [Measures].[Dinghuo Size 2], [Measures].[Dinghuo Size 4], [Measures].[Dinghuo Size 6], [Measures].[Dinghuo Size 8],
  [Measures].[Requirement List Price], [Measures].[Requirement Quantity],
  [Measures].[Requirement Size 0], [Measures].[Requirement Size 2], [Measures].[Requirement Size 4], [Measures].[Requirement Size 6], [Measures].[Requirement Size 8],
  [Measures].[List Price Amount],[Measures].[Quote Price Amount],[Measures].[Quantity],
  [Measures].[QuoteItem Size 0], [Measures].[QuoteItem Size 2], [Measures].[QuoteItem Size 4], [Measures].[QuoteItem Size 6], [Measures].[QuoteItem Size 8]
  } on columns,
  {([Customers].[All Customers], [Product].[All Products])} ON rows
from RequirementsAndSales
</jp:mondrianQuery>





<c:set var="title01" scope="session">订货会数据分析</c:set>
