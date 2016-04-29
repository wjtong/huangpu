<%@ page session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:mondrianQuery id="query01" jdbcDriver="com.mysql.jdbc.Driver" jdbcUrl="jdbc:mysql://localhost/bluemountainolap?user=root&password=root" catalogUri="/WEB-INF/queries/FoodMart.xml"
   jdbcUser="root" jdbcPassword="root" connectionPooling="false">
select
  {[Quantity]
  } on columns,
  {(Agent,VirtualProductId,ColorCode,SizeCode)} ON rows
from QuoteDataAnalysis
</jp:mondrianQuery>





<c:set var="title01" scope="session">订货会数据分析</c:set>