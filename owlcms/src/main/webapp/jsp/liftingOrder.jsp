<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false" import="org.concordiainternational.competition.ui.generators.*,org.concordiainternational.competition.ui.*,org.concordiainternational.competition.data.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html><!-- 
/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */ 
 --><head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
	String platform = request.getParameter("platformName");
	if (platform == null) {
		out.println("Platform parameter expected. URL must include ?platformName=X");
		return;
	}
	pageContext.setAttribute("platform", platform);
	
	ServletContext sCtx = this.getServletContext();
	SessionData groupData = (SessionData)sCtx.getAttribute(SessionData.MASTER_KEY+platform);
	if (groupData == null) return;

	java.util.List<Lifter> lifters = groupData.getCurrentLiftingOrder();
	if (lifters == null || lifters.size() == 0) {
		out.println("</head><body></body></html>");
		out.flush();
		return;
	}
	pageContext.setAttribute("lifters", lifters);
	pageContext.setAttribute("isMasters", Competition.isMasters());
	
	CompetitionSession group = groupData.getCurrentSession();
	if (group == null) {
		pageContext.removeAttribute("groupName");
		pageContext.setAttribute("useGroupName", false);
	} else {
		String groupName = group.getName();
		pageContext.setAttribute("groupName", groupName);
		pageContext.setAttribute("useGroupName", true);
		pageContext.setAttribute("liftsDone", 
				TryFormatter.htmlFormatLiftsDone(groupData.getLiftsDone(),java.util.Locale.CANADA_FRENCH) 
				+ " &ndash; "
				+ TryFormatter.htmlFormatLiftsDone(groupData.getLiftsDone(),java.util.Locale.ENGLISH)
				);
	}
%>
<title>Ordre de passage - Lifting Order</title>
<link rel="stylesheet" type="text/css" href="result.css" />
<style type="text/css">
.requestedWeight {
	color: navy;
	font-size: medium;
	font-style: italic;
	font-weight: 100;
	text-align: center;
	width: 7%;
}
</style>
</head>
<body>
<div class="title">
<c:choose>
	<c:when test='${useGroupName}'>
		<span class="title">Tableau des essais, groupe ${groupName} &ndash; Group ${groupName} Attempt Board</span>
		<span class="liftsDone">${liftsDone}</span>
	</c:when>
	<c:otherwise>
		<span class="title">Tableau des essais &ndash; Attempt Board</span>
		<span class="liftsDone">${liftsDone}</span>
	</c:otherwise>
</c:choose>
</div>

<table>
	<thead>
		<tr>
			<th>Nom<div class='english'>Surname</div></th>
			<th>Prénom<div class='english'>Given Name</div></th>
			<th class='weight'>Poids demandé<div class='english'>Requested Weight</div></th>
			<th>Essai<div class='english'>Try</div></th>
			<th class='narrow'>Club<div class='english'>Team</div></th>
			<th class="narrow" style='text-align: center'>TAS<div class='english'>Lot</div></th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="lifter" items="${lifters}">
			<jsp:useBean id="lifter" type="org.concordiainternational.competition.data.Lifter" />
			<tr>
				<c:choose>
					<c:when test="${lifter.currentLifter}">
						<td class='name current'>${lifter.lastName}</td>
						<td class='name current'>${lifter.firstName}</td>
					</c:when>
					<c:otherwise>
						<td class='name'>${lifter.lastName}</td>
						<td class='name'>${lifter.firstName}</td>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${lifter.currentLifter}">
						<td class='current weight'>${lifter.nextAttemptRequestedWeight}</td>
					</c:when>
					<c:otherwise>
						<td class='weight'>${lifter.nextAttemptRequestedWeight}</td>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${lifter.currentLifter}">
						<td class='weight current'><%= TryFormatter.htmlFormatTry(lifters,lifter) %></td>
					</c:when>
					<c:otherwise>
						<td class='weight'><%= TryFormatter.htmlFormatTry(lifters,lifter) %></td>
					</c:otherwise>
				</c:choose>
				<td class="weight"><nobr>${lifter.club}</nobr></td>
				<td class='narrow' style='text-align: right'>${lifter.lotNumber}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
</body>
</html>