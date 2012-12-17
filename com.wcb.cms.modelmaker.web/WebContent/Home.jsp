<%@ page language="java" contentType="text/html; charset=ISO-8859-1"  
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
	"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title> Rose Sql Helper</title>
	</head>
	<body>
		<form action="ModellingResult" method="post" name="sqlform">
			<input type="submit" value="Build">
			<br>	
			Please paste a select SQL query <br>
			<textarea id="textAreaText" rows="80" cols="80" name="sql" ></textarea>
		</form>
	</body>
</html>