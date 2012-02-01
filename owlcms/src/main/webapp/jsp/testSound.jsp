<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false" import="java.util.List,org.concordiainternational.competition.decision.*,javax.sound.sampled.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
List<Mixer> mixers = Speakers.getOutputs();
for (Mixer mixer: mixers) {
	System.out.println(mixer.getMixerInfo().getName());
	new Speakers().testSound(mixer);
}
%>
<title>Sound test.</title>
<link rel="stylesheet" type="text/css" href="result.css" />
</head>
<body>
Sound test. This page emits the initial warning sound, in sequence, to all available sound outputs.
</body>
</html>