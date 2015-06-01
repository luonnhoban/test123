/**
 *     Copyright (C) 2009-2014  Jack A. Rider All rights reserved.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 */
 
 

package vn.hss.etrans.yeucauvantai;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletURL;
import javax.portlet.ProcessAction;
import javax.portlet.ProcessEvent;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletConfig;
import javax.xml.namespace.QName;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.poi.ss.usermodel.Workbook;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.dao.search.SearchContainer;

import vn.hss.etrans.yeucauvantai.model.QuanHuyen;
import vn.hss.etrans.yeucauvantai.model.YeuCauChiTiet;
import vn.hss.etrans.yeucauvantai.service.QuanHuyenLocalServiceUtil;
import vn.hss.etrans.yeucauvantai.service.YeuCauChiTietLocalServiceUtil;
import vn.hss.etrans.yeucauvantai.model.YeuCauVanTai;
import vn.hss.etrans.yeucauvantai.model.impl.YeuCauChiTietImpl;
import vn.hss.etrans.yeucauvantai.model.impl.YeuCauVanTaiImpl;
import vn.hss.etrans.yeucauvantai.service.YeuCauVanTaiLocalServiceUtil;
import vn.hss.etrans.yeucauvantai.service.permission.YeuCauVanTaiPermission;
import vn.hss.etrans.yeucauvantai.service.permission.YeuCauVanTaiEntryPermission;


import vn.hss.etrans.yeucauvantai.util.YeuCauVanTaiUtil;

import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Region;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.RegionServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortalPreferences;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;


/**
 * Portlet implementation class YeuCauVanTai
 */
public class YeuCauVanTaiPortlet extends MVCPortlet {


	private YeuCauVanTaiUpload uploadManager;

	public void init() throws PortletException {

		// Edit Mode Pages
		editJSP = getInitParameter("edit-jsp");

		// Help Mode Pages
		helpJSP = getInitParameter("help-jsp");

		// View Mode Pages
		viewJSP = getInitParameter("view-jsp");

		// View Mode Edit YeuCauVanTai
		editYeuCauVanTaiJSP = getInitParameter("edit-YeuCauVanTai-jsp");

		// View Mode Entry YeuCauVanTai
		viewYeuCauVanTaiJSP = getInitParameter("view-YeuCauVanTai-jsp");
	}

	protected void include(String path, RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		PortletRequestDispatcher portletRequestDispatcher = getPortletContext()
				.getRequestDispatcher(path);

		if (portletRequestDispatcher == null) {
			// do nothing
			// _log.error(path + " is not a valid include");
		} else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}

	public void doView(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		String jsp = (String) renderRequest.getParameter("view");
		if (jsp == null || jsp.equals("")) {
			showViewDefault(renderRequest, renderResponse);
		} else if (jsp.equalsIgnoreCase("editYeuCauVanTai")) {
			try {
				showViewEditYeuCauVanTai(renderRequest, renderResponse);
			} catch (Exception ex) {
				_log.debug(ex);
				try {
					showViewDefault(renderRequest, renderResponse);
				} catch (Exception ex1) {
					_log.debug(ex1);
				}
			}
		}
	}

	public void doEdit(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		showEditDefault(renderRequest, renderResponse);
	}

	public void doHelp(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		include(helpJSP, renderRequest, renderResponse);
	}

	@SuppressWarnings("unchecked")
	public void showViewDefault(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getScopeGroupId();

		PermissionChecker permissionChecker = themeDisplay
				.getPermissionChecker();

		boolean hasAddPermission = YeuCauVanTaiPermission.contains(permissionChecker, groupId, "ADD_YEUCAUVANTAI");

		boolean hasModelPermissions = YeuCauVanTaiPermission.contains(permissionChecker, groupId, ActionKeys.PERMISSIONS);

		List<YeuCauVanTai> tempResults = Collections.EMPTY_LIST;

		PortletPreferences prefs = renderRequest.getPreferences();

		String YeuCauVanTaiFilter = ParamUtil.getString(renderRequest, "YeuCauVanTaiFilter");

		String rowsPerPage = prefs.getValue("rows-per-page","5");
		String viewType = prefs.getValue("view-type", "0");

		Integer cur = 1;
		int containerStart = 0;
		int containerEnd = 0;
		String orderByType = renderRequest.getParameter("orderByType");
		String orderByCol = renderRequest.getParameter("orderByCol");
		try{
			cur = ParamUtil.getInteger(renderRequest, "cur");

		}catch (Exception e){
			cur = 1;
		}

		if (cur < 1){
			cur = 1;
		}

		if (Validator.isNotNull(YeuCauVanTaiFilter) || !YeuCauVanTaiFilter.equalsIgnoreCase("")) {
			rowsPerPage = "100";
			cur = 1;
		}

		containerStart = (cur - 1) * Integer.parseInt(rowsPerPage);
		containerEnd = containerStart + Integer.parseInt(rowsPerPage);

		int total = 0;
		try {
			PortalPreferences portalPrefs = PortletPreferencesFactoryUtil.getPortalPreferences(renderRequest);

			if (Validator.isNull(orderByCol) && Validator.isNull(orderByType)) {
				orderByCol = portalPrefs.getValue("YeuCauVanTai_order", "YeuCauVanTai-order-by-col", "idYeuCau");
				orderByType = portalPrefs.getValue("YeuCauVanTai_order", "YeuCauVanTai-order-by-type", "asc");
			}
			OrderByComparator comparator = YeuCauVanTaiComparator.getYeuCauVanTaiOrderByComparator(orderByCol,orderByType);


				if (viewType.equals("0")){
					tempResults = YeuCauVanTaiLocalServiceUtil.findAllInGroup(groupId, containerStart, containerEnd, comparator);
					total = YeuCauVanTaiLocalServiceUtil.countAllInGroup(groupId);								
				}else if (viewType.equals("1")){
					tempResults = YeuCauVanTaiLocalServiceUtil.findAllInUser(themeDisplay.getUserId(), containerStart, containerEnd, comparator);
					total = YeuCauVanTaiLocalServiceUtil.countAllInUser(themeDisplay.getUserId());
				}else{
					tempResults = YeuCauVanTaiLocalServiceUtil.findAllInUserAndGroup(themeDisplay.getUserId(), groupId, containerStart, containerEnd, comparator);
					total = YeuCauVanTaiLocalServiceUtil.countAllInUserAndGroup(themeDisplay.getUserId(), groupId);
				}

				if (orderByCol.equalsIgnoreCase("noiNhanQH")) {
					BeanComparator noiNhanQH_bc = new BeanComparator("noiNhanQH", YeuCauVanTaiComparator.getYeuCauVanTaiOrderByComparator(orderByCol,orderByType));
					List newList = new ArrayList(tempResults);
					Collections.sort(newList, noiNhanQH_bc);
					if (!orderByType.trim().equalsIgnoreCase("asc")) {
						Collections.reverse(newList);
					}
					tempResults = Collections.unmodifiableList(newList);
				}
				if (orderByCol.equalsIgnoreCase("noiGiaoQH")) {
					BeanComparator noiGiaoQH_bc = new BeanComparator("noiGiaoQH", YeuCauVanTaiComparator.getYeuCauVanTaiOrderByComparator(orderByCol,orderByType));
					List newList = new ArrayList(tempResults);
					Collections.sort(newList, noiGiaoQH_bc);
					if (!orderByType.trim().equalsIgnoreCase("asc")) {
						Collections.reverse(newList);
					}
					tempResults = Collections.unmodifiableList(newList);
				}

		} catch (Exception e) {
			_log.debug(e);
		}
		renderRequest.setAttribute("highlightRowWithKey", renderRequest.getParameter("highlightRowWithKey"));
		renderRequest.setAttribute("containerStart", containerStart);
		renderRequest.setAttribute("containerEnd", containerEnd);
		renderRequest.setAttribute("cur", cur);
		renderRequest.setAttribute("tempResults", tempResults);
		renderRequest.setAttribute("totalCount", total);
		renderRequest.setAttribute("rowsPerPage", rowsPerPage);
		renderRequest.setAttribute("hasAddPermission", hasAddPermission);
		renderRequest.setAttribute("hasModelPermissions", hasModelPermissions);
		renderRequest.setAttribute("orderByType", orderByType);
		renderRequest.setAttribute("orderByCol", orderByCol);		
		renderRequest.setAttribute("YeuCauVanTaiFilter", YeuCauVanTaiFilter);

		PortletURL addYeuCauVanTaiURL = renderResponse.createActionURL();
		addYeuCauVanTaiURL.setParameter("javax.portlet.action", "newYeuCauVanTai");
		renderRequest.setAttribute("addYeuCauVanTaiURL", addYeuCauVanTaiURL.toString());

		PortletURL YeuCauVanTaiFilterURL = renderResponse.createRenderURL();
		YeuCauVanTaiFilterURL.setParameter("javax.portlet.action", "doView");
		renderRequest.setAttribute("YeuCauVanTaiFilterURL", YeuCauVanTaiFilterURL.toString());

		include(viewJSP, renderRequest, renderResponse);
	}

	public void showViewEditYeuCauVanTai(RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {
		YeuCauChiTietPortlet yeucauchitietportlet = new YeuCauChiTietPortlet();
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

		PortletURL editYeuCauVanTaiURL = renderResponse.createActionURL();
		String editType = (String) renderRequest.getParameter("editType");

		YeuCauVanTai yeuCauVanTai = null;
		YeuCauChiTiet yeuCauChiTiet = null;
		boolean viewEntryMode=false;
		if (editType.equalsIgnoreCase("edit")) {
			
			editYeuCauVanTaiURL.setParameter("javax.portlet.action", "updateYeuCauVanTai");

			long $fname = Long.parseLong(renderRequest.getParameter("idYeuCau"));
			yeuCauVanTai = YeuCauVanTaiLocalServiceUtil.getYeuCauVanTai($fname);

			if (!YeuCauVanTaiEntryPermission.contains(
				permissionChecker, yeuCauVanTai, ActionKeys.UPDATE)){							
				SessionErrors.add(renderRequest, "permission-error");
				return;
	     		}

		    	String folderDLId = yeuCauVanTai.getFolderDLId()+"";
			renderRequest.setAttribute("folderDLId", folderDLId);
            	     	renderRequest.setAttribute("yeuCauVanTai", yeuCauVanTai);
        long $fnameChiTiet = Long.parseLong(renderRequest.getAttribute("idChiTiet").toString());  
        yeuCauChiTiet = YeuCauChiTietLocalServiceUtil.getYeuCauChiTiet($fnameChiTiet);
        renderRequest.setAttribute("yeuCauChiTiet", yeuCauChiTiet);   	     	
		} else if (editType.equalsIgnoreCase("view")) {

			viewEntryMode= true;

			long $fname = Long.parseLong(renderRequest.getParameter("idYeuCau"));
			yeuCauVanTai = YeuCauVanTaiLocalServiceUtil.getYeuCauVanTai($fname);


			if (!YeuCauVanTaiEntryPermission.contains(
				permissionChecker, yeuCauVanTai, ActionKeys.VIEW)){							
				SessionErrors.add(renderRequest, "permission-error");
				return;
	     		}
			
			renderRequest.setAttribute("yeuCauVanTai", yeuCauVanTai);
			
			
			 long $fnameChiTiet = Long.parseLong(renderRequest.getAttribute("idChiTiet").toString());  
		        yeuCauChiTiet = YeuCauChiTietLocalServiceUtil.getYeuCauChiTiet($fnameChiTiet);
		        renderRequest.setAttribute("yeuCauChiTiet", yeuCauChiTiet);   	     	
			
		} else {

			if (!YeuCauVanTaiPermission.contains(
					permissionChecker, themeDisplay.getScopeGroupId(), "ADD_YEUCAUVANTAI")){	
						SessionErrors.add(renderRequest, "permission-error");
						return;
	    		}

			editYeuCauVanTaiURL.setParameter("javax.portlet.action", "addYeuCauVanTai");
			YeuCauVanTai errorYeuCauVanTai = (YeuCauVanTai) renderRequest.getAttribute("errorYeuCauVanTai");
			if (errorYeuCauVanTai != null) {
				if (editType.equalsIgnoreCase("update")) {
					editYeuCauVanTaiURL.setParameter("javax.portlet.action", "updateYeuCauVanTai");
                		}
				renderRequest.setAttribute("yeuCauVanTai", errorYeuCauVanTai);

		        	String folderDLId = errorYeuCauVanTai.getFolderDLId()+"";
				renderRequest.setAttribute("folderDLId",folderDLId);
			} else {				
				
				YeuCauVanTai addYeuCauVanTai = null;
				if (Validator.isNull(renderRequest.getParameter("addErrors"))){
				
					addYeuCauVanTai = new YeuCauVanTaiImpl();
					addYeuCauVanTai.setIdYeuCau(0);
					addYeuCauVanTai.setMaYeuCau("");
					addYeuCauVanTai.setTen("");
					addYeuCauVanTai.setNgayNhanHang(new Date());				
					addYeuCauVanTai.setNgayGiaoHang(new Date());				
					addYeuCauVanTai.setNoiNhan(0);
					addYeuCauVanTai.setNoiGiao(0);
					addYeuCauVanTai.setNoiNhanQH(0);
					addYeuCauVanTai.setNoiGiaoQH(0);
					addYeuCauVanTai.setTuGia(0);
					addYeuCauVanTai.setDenGia(0);
					addYeuCauVanTai.setHanCuoiDauGia(new Date());				
					addYeuCauVanTai.setIdDauGia(0);
					addYeuCauVanTai.setMoTa("");
					addYeuCauVanTai.setIsDeleted(true);
					
				}else{
					addYeuCauVanTai = YeuCauVanTaiFromRequest(renderRequest);
				}
				renderRequest.setAttribute("yeuCauVanTai", addYeuCauVanTai);
				
				YeuCauChiTiet addYeuCauChiTiet = null;


				if (Validator.isNull(renderRequest.getParameter("addErrors"))){
				
					addYeuCauChiTiet = new YeuCauChiTietImpl();
					addYeuCauChiTiet.setIdYeuCauChiTiet(0);
					addYeuCauChiTiet.setIdYeuCau(0);
					addYeuCauChiTiet.setIdBacHang(0);
					addYeuCauChiTiet.setKhoiLuong(0);
					addYeuCauChiTiet.setDonViKhoiLuong(0);
					addYeuCauChiTiet.setSoLuong(0);
					addYeuCauChiTiet.setDonViSoLuong(0);
					addYeuCauChiTiet.setKichThuocD(0);
					addYeuCauChiTiet.setKichThuocR(0);
					addYeuCauChiTiet.setKichThuocC(0);
					addYeuCauChiTiet.setMoTa("");
					addYeuCauChiTiet.setIsDeleted(true);
				}else{
					addYeuCauChiTiet = yeucauchitietportlet.YeuCauChiTietFromRequest(renderRequest);
				}
				renderRequest.setAttribute("yeuCauChiTiet", addYeuCauChiTiet);
				
				
			}

		}		

        renderRequest.setAttribute("editType", editType);

		if (!viewEntryMode){
			renderRequest.setAttribute("editYeuCauVanTaiURL", editYeuCauVanTaiURL.toString());		
			include(editYeuCauVanTaiJSP, renderRequest, renderResponse);
		}else{
			include(viewYeuCauVanTaiJSP, renderRequest, renderResponse);
		}
	}

	private String dateToJsp(ActionRequest request, Date date) {
		PortletPreferences prefs = request.getPreferences();
		return dateToJsp(prefs, date);
	}
	private String dateToJsp(RenderRequest request, Date date) {
		PortletPreferences prefs = request.getPreferences();
		return dateToJsp(prefs, date);
	}
	private String dateToJsp(PortletPreferences prefs, Date date) {
		SimpleDateFormat format = new SimpleDateFormat(prefs.getValue("date-format", "yyyy/MM/dd"));
		String stringDate = format.format(date);
		return stringDate;
	}
	private String dateTimeToJsp(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String stringDate = format.format(date);
		return stringDate;
	}

	public void showEditDefault(RenderRequest renderRequest,
			RenderResponse renderResponse) throws PortletException, IOException {

		include(editJSP, renderRequest, renderResponse);
	}

	/* Portlet Actions */

	@ProcessAction(name = "newYeuCauVanTai")
	public void newYeuCauVanTai(ActionRequest request, ActionResponse response) throws Exception{
		YeuCauVanTaiUtil.addParametersForAdd(response);
	}

	@ProcessAction(name = "addYeuCauVanTai")
	public void addYeuCauVanTai(ActionRequest request, ActionResponse response) throws Exception {
		YeuCauChiTietPortlet yeuCauChiTietPorlet = new YeuCauChiTietPortlet();
            boolean isMultipart = PortletFileUpload.isMultipartContent(request);
            if (isMultipart) {
            	uploadManager = new YeuCauVanTaiUpload();
				request = extractFields(request,false);
            }
            YeuCauVanTai yeuCauVanTai = YeuCauVanTaiFromRequest(request);
            YeuCauChiTiet yeuCauChiTiet = yeuCauChiTietPorlet.YeuCauChiTietFromRequest(request);
	    ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
	    PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

	    if (!YeuCauVanTaiPermission.contains(
					permissionChecker, themeDisplay.getScopeGroupId(), "ADD_YEUCAUVANTAI")){	
				YeuCauVanTaiUtil.addParametersForDefaultView(response);						
				SessionErrors.add(request, "permission-error");
				return;
	    }
            ArrayList<String> errors = YeuCauVanTaiValidator.validateYeuCauVanTai(yeuCauVanTai, request);

            if (errors.isEmpty()) {
	            yeuCauVanTai = uploadManager.uploadFiles(request,yeuCauVanTai);
		try {
			ServiceContext serviceContext = ServiceContextFactory.getInstance(YeuCauVanTai.class.getName(), request);
			YeuCauVanTaiLocalServiceUtil.addYeuCauVanTai(yeuCauVanTai, serviceContext);
			ServiceContext serviceContextChiTiet = ServiceContextFactory.getInstance(YeuCauChiTiet.class.getName(), request);
			YeuCauChiTietLocalServiceUtil.addYeuCauChiTiet(yeuCauChiTiet, serviceContextChiTiet);
			YeuCauVanTaiUtil.addParametersForDefaultView(response);
                	SessionMessages.add(request, "yeucauvantai-added-successfully");

            	} catch (Exception cvex) {
            		SessionErrors.add(request, "please-enter-a-unique-code");
			PortalUtil.copyRequestParameters(request, response);			
			YeuCauVanTaiUtil.addParametersForAddWithErrors(response);		
            	}
            } else {
                for (String error : errors) {
                        SessionErrors.add(request, error);
                }
		PortalUtil.copyRequestParameters(request, response);			
		YeuCauVanTaiUtil.addParametersForAddWithErrors(response);	
            }
	}

	@ProcessAction(name = "eventYeuCauVanTai")
	public void eventYeuCauVanTai(ActionRequest request, ActionResponse response)
			throws Exception {
		long key = ParamUtil.getLong(request, "resourcePrimKey");
		int containerStart = ParamUtil.getInteger(request, "containerStart");
		int containerEnd = ParamUtil.getInteger(request, "containerEnd");
		int cur = ParamUtil.getInteger(request, "cur");
		String orderByType = ParamUtil.getString(request, "orderByType");
		String orderByCol = ParamUtil.getString(request, "orderByCol");
		String yeucauvantaiFilter = ParamUtil.getString(request, "YeuCauVanTaiFilter");
		if (Validator.isNotNull(key)) {
            		response.setRenderParameter("highlightRowWithKey", Long.toString(key));
            		response.setRenderParameter("containerStart", Integer.toString(containerStart));
            		response.setRenderParameter("containerEnd", Integer.toString(containerEnd));
			response.setRenderParameter("cur", Integer.toString(cur));
			response.setRenderParameter("orderByType", orderByType);
			response.setRenderParameter("orderByCol", orderByCol);
			response.setRenderParameter("YeuCauVanTaiFilter", yeucauvantaiFilter);
		}
	}

	@ProcessAction(name = "editYeuCauVanTai")

	public void editYeuCauVanTai(ActionRequest request, ActionResponse response)
			throws Exception {
		long key = ParamUtil.getLong(request, "resourcePrimKey");
		if (Validator.isNotNull(key)) {
			YeuCauVanTaiUtil.addParametersForEdit(response, key);
			
		}
	}

	@ProcessAction(name = "deleteYeuCauVanTai")
	public void deleteYeuCauVanTai(ActionRequest request, ActionResponse response)throws Exception {		

		long id = ParamUtil.getLong(request, "resourcePrimKey");

		if (Validator.isNotNull(id)) {
			ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

			if (!YeuCauVanTaiEntryPermission.contains(
					permissionChecker, id, ActionKeys.DELETE)){	
				YeuCauVanTaiUtil.addParametersForDefaultView(response);						
				SessionErrors.add(request, "permission-error");
				return;
			}

			YeuCauVanTai yeuCauVanTai = YeuCauVanTaiLocalServiceUtil.getYeuCauVanTai(id);
			YeuCauVanTaiLocalServiceUtil.deleteYeuCauVanTaiEntry(yeuCauVanTai);
			SessionMessages.add(request, "yeucauvantai-deleted-successfully");
            		response.setRenderParameter("idYeuCau", "0");
		} else {
			SessionErrors.add(request, "yeucauvantai-error-deleting");
		}
	}


	@ProcessAction(name = "updateYeuCauVanTai")
	public void updateYeuCauVanTai(ActionRequest request, ActionResponse response) throws Exception {
            boolean isMultipart = PortletFileUpload.isMultipartContent(request);
            if (isMultipart) {
				uploadManager = new YeuCauVanTaiUpload();
				request = extractFields(request,true);
            }
            YeuCauVanTai yeuCauVanTai = YeuCauVanTaiFromRequest(request);
	    ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
	    PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

	    if (!YeuCauVanTaiEntryPermission.contains(
				permissionChecker, yeuCauVanTai, ActionKeys.UPDATE)){	
				YeuCauVanTaiUtil.addParametersForDefaultView(response);						
				SessionErrors.add(request, "permission-error");
				return;
	     }

            ArrayList<String> errors = YeuCauVanTaiValidator.validateYeuCauVanTai(yeuCauVanTai, request);

		    yeuCauVanTai = uploadManager.uploadFiles(request, yeuCauVanTai);
            if (errors.isEmpty()) {
            	try {
			ServiceContext serviceContext = ServiceContextFactory.getInstance(YeuCauVanTai.class.getName(), request);
                	YeuCauVanTaiLocalServiceUtil.updateYeuCauVanTai(yeuCauVanTai, serviceContext);

			YeuCauVanTaiUtil.addParametersForDefaultView(response);
                	SessionMessages.add(request, "yeucauvantai-updated-successfully");

            	} catch (Exception cvex) {
            	    SessionErrors.add(request, "please-enter-a-unique-code");
		    		YeuCauVanTaiUtil.addParametersForEdit(response, null);
					request.setAttribute("yeuCauVanTai",yeuCauVanTai);
            	}
            } else {
                for (String error : errors) {
                        SessionErrors.add(request, error);
                }
				YeuCauVanTaiUtil.addParametersForEdit(response, Long.toString(yeuCauVanTai.getPrimaryKey()));
				request.setAttribute("yeuCauVanTai",yeuCauVanTai);
            }
        }

	@ProcessAction(name = "setYeuCauVanTaiPref")
	public void setYeuCauVanTaiPref(ActionRequest request, ActionResponse response) throws Exception {

		String rowsPerPage = ParamUtil.getString(request, "rows-per-page");
		String viewType = ParamUtil.getString(request, "view-type");
		String dateFormat = ParamUtil.getString(request, "date-format");
		String datetimeFormat = ParamUtil.getString(request, "datetime-format");

		ArrayList<String> errors = new ArrayList();
		if (YeuCauVanTaiValidator.validateEditYeuCauVanTai(rowsPerPage, dateFormat, datetimeFormat, errors)) {
			response.setRenderParameter("rows-per-page", "");
			response.setRenderParameter("date-format", "");
			response.setRenderParameter("datetime-format", "");
			response.setRenderParameter("view-type", "");

			PortletPreferences prefs = request.getPreferences();

			prefs.setValue("rows-per-page", rowsPerPage);
			prefs.setValue("view-type", viewType);
			prefs.setValue("date-format", dateFormat);
			prefs.setValue("datetime-format", datetimeFormat);
			prefs.store();

			SessionMessages.add(request, "prefs-success");
		}
	}

	private YeuCauVanTai YeuCauVanTaiFromRequest(PortletRequest request) {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		YeuCauVanTaiImpl yeuCauVanTai = new YeuCauVanTaiImpl();
        	try {
		    	yeuCauVanTai.setIdYeuCau(Long.valueOf(request.getAttribute("idYeuCau").toString()));
        	} catch (Exception nfe) {
		    //Controled en Validator
        	}
		yeuCauVanTai.setMaYeuCau(request.getAttribute("maYeuCau").toString());
		yeuCauVanTai.setTen(request.getAttribute("ten").toString());
        	PortletPreferences prefs = request.getPreferences();
        	SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        	int ngayNhanHangAno    = Integer.valueOf((String) request.getAttribute("ngayNhanHangYear"));
        	int ngayNhanHangMes    = Integer.valueOf((String) request.getAttribute("ngayNhanHangMonth"))+1;
        	int ngayNhanHangDia    = Integer.valueOf((String) request.getAttribute("ngayNhanHangDay"));
        	int ngayNhanHangHora   = Integer.valueOf((String) request.getAttribute("ngayNhanHangHour"));
        	int ngayNhanHangMinuto = Integer.valueOf((String) request.getAttribute("ngayNhanHangMinute"));
		int ngayNhanHangAmPm   = Integer.valueOf((String) request.getAttribute("ngayNhanHangAmPm"));
		
		if (ngayNhanHangAmPm == Calendar.PM) {
			ngayNhanHangHora += 12;
		}

        	try {
			SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            		yeuCauVanTai.setNgayNhanHang(formatterDateTime.parse(ngayNhanHangAno + "/" + ngayNhanHangMes + "/" + ngayNhanHangDia + " " + ngayNhanHangHora + ":" + ngayNhanHangMinuto));
        	} catch (ParseException e) {
			yeuCauVanTai.setNgayNhanHang(new Date());
        	}
        	int ngayGiaoHangAno    = Integer.valueOf((String) request.getAttribute("ngayGiaoHangYear"));
        	int ngayGiaoHangMes    = Integer.valueOf((String) request.getAttribute("ngayGiaoHangMonth"))+1;
        	int ngayGiaoHangDia    = Integer.valueOf((String) request.getAttribute("ngayGiaoHangDay"));
        	int ngayGiaoHangHora   = Integer.valueOf((String) request.getAttribute("ngayGiaoHangHour"));
        	int ngayGiaoHangMinuto = Integer.valueOf((String) request.getAttribute("ngayGiaoHangMinute"));
		int ngayGiaoHangAmPm   = Integer.valueOf((String) request.getAttribute("ngayGiaoHangAmPm"));
		
		if (ngayGiaoHangAmPm == Calendar.PM) {
			ngayGiaoHangHora += 12;
		}

        	try {
			SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            		yeuCauVanTai.setNgayGiaoHang(formatterDateTime.parse(ngayGiaoHangAno + "/" + ngayGiaoHangMes + "/" + ngayGiaoHangDia + " " + ngayGiaoHangHora + ":" + ngayGiaoHangMinuto));
        	} catch (ParseException e) {
			yeuCauVanTai.setNgayGiaoHang(new Date());
        	}
        	try {
		    	yeuCauVanTai.setNoiNhan(Long.valueOf(request.getAttribute("noiNhan").toString()));
        	} catch (Exception nfe) {
		    //Controled en Validator
        	}
        	try {
		    	yeuCauVanTai.setNoiGiao(Long.valueOf(request.getAttribute("noiGiao").toString()));
        	} catch (Exception nfe) {
		    //Controled en Validator
        	}
        	try {
		    	yeuCauVanTai.setNoiNhanQH(Long.valueOf(request.getAttribute("noiNhanQH").toString()));
        	} catch (Exception nfe) {
		    //Controled en Validator
        	}
        	try {
		    	yeuCauVanTai.setNoiGiaoQH(Long.valueOf(request.getAttribute("noiGiaoQH").toString()));
        	} catch (Exception nfe) {
		    //Controled en Validator
        	}
        	try {
		 	yeuCauVanTai.setTuGia(Integer.valueOf(request.getAttribute("tuGia").toString()));
        	} catch (Exception nfe) {
			//Controled en Validator
            		yeuCauVanTai.setTuGia(0);
        	}
        	try {
		 	yeuCauVanTai.setDenGia(Integer.valueOf(request.getAttribute("denGia").toString()));
        	} catch (Exception nfe) {
			//Controled en Validator
            		yeuCauVanTai.setDenGia(0);
        	}
        	int hanCuoiDauGiaAno    = Integer.valueOf((String) request.getAttribute("hanCuoiDauGiaYear"));
        	int hanCuoiDauGiaMes    = Integer.valueOf((String) request.getAttribute("hanCuoiDauGiaMonth"))+1;
        	int hanCuoiDauGiaDia    = Integer.valueOf((String) request.getAttribute("hanCuoiDauGiaDay"));
        	int hanCuoiDauGiaHora   = Integer.valueOf((String) request.getAttribute("hanCuoiDauGiaHour"));
        	int hanCuoiDauGiaMinuto = Integer.valueOf((String) request.getAttribute("hanCuoiDauGiaMinute"));
		int hanCuoiDauGiaAmPm   = Integer.valueOf((String) request.getAttribute("hanCuoiDauGiaAmPm"));
		
		if (hanCuoiDauGiaAmPm == Calendar.PM) {
			hanCuoiDauGiaHora += 12;
		}

        	try {
			SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            		yeuCauVanTai.setHanCuoiDauGia(formatterDateTime.parse(hanCuoiDauGiaAno + "/" + hanCuoiDauGiaMes + "/" + hanCuoiDauGiaDia + " " + hanCuoiDauGiaHora + ":" + hanCuoiDauGiaMinuto));
        	} catch (ParseException e) {
			yeuCauVanTai.setHanCuoiDauGia(new Date());
        	}
        	try {
		 	yeuCauVanTai.setIdDauGia(Integer.valueOf(request.getAttribute("idDauGia").toString()));
        	} catch (Exception nfe) {
			//Controled en Validator
            		yeuCauVanTai.setIdDauGia(0);
        	}
		yeuCauVanTai.setMoTa(request.getAttribute("moTa").toString());
        	if (request.getAttribute("isDeleted").toString().equalsIgnoreCase("false")) {
        		yeuCauVanTai.setIsDeleted(false);
        	} else  {
        		yeuCauVanTai.setIsDeleted(true);
        	}
		try {
			yeuCauVanTai.setPrimaryKey(Long.valueOf(request.getAttribute("resourcePrimKey").toString()));
		} catch (NumberFormatException nfe) {
			//Controled en Validator
        	}

		yeuCauVanTai.setCompanyId(themeDisplay.getCompanyId());
		yeuCauVanTai.setGroupId(themeDisplay.getScopeGroupId());
		yeuCauVanTai.setUserId(themeDisplay.getUserId());
		return yeuCauVanTai;
	}

	private ActionRequest extractFields(ActionRequest request,boolean edit) throws FileUploadException{

		FileItemFactory factory = new DiskFileItemFactory();
        	PortletFileUpload uploadItems = new PortletFileUpload(factory);
        	List <FileItem>allItems = uploadItems.parseRequest(request);
         	//Separate formFields <-> fileItems
         	for(FileItem item : allItems){
         		String formField = item.getFieldName();
         		if (item.isFormField()) {
         			//Non-file items
         			//Push all to request object
					String portletName = "_"+request.getAttribute(WebKeys.PORTLET_ID)+"_";
					if(formField.startsWith(YeuCauVanTaiUpload.HIDDEN)) {
						uploadManager.addHidden(formField,Long.parseLong(item.getString()));
					} else if (formField.endsWith(YeuCauVanTaiUpload.DOCUMENT_DELETE)) {
						int pos = formField.indexOf(YeuCauVanTaiUpload.DOCUMENT_DELETE);
						formField = formField.substring(0,pos-1);
						formField = formField.replaceAll(portletName, "");
						if(item.getString().equals("true")) uploadManager.addDeleted(formField);
					} else {
						formField=formField.replaceAll(portletName, "");
						try {
					    	request.setAttribute(formField,item.getString("UTF-8").trim());
						} catch (Exception e) {
						}
					}
         		} else {

         			uploadManager.add(item);
         		}
         	}
		return request;
	}


	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
String regionId = ParamUtil.getString(resourceRequest, "noiNhan");
		
		try {
			Region tinhThanhPho = RegionServiceUtil.getRegion(Long.valueOf(regionId));
			if(tinhThanhPho!=null)
			{
				List<QuanHuyen> listQuanHuyen = QuanHuyenLocalServiceUtil.getAllByIdTinhThanhPho(tinhThanhPho.getRegionCode());
				
				// build the JsonArray to be sent back
				JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
				for (QuanHuyen quanHuyen : listQuanHuyen) {
					String itemQuanHuyen = quanHuyen.getIdquanhuyen()+"$"+quanHuyen.getTen();
					jsonArray.put(itemQuanHuyen);
				}
				
				// set the content Type
				resourceResponse.setContentType("text/javascript");
	
				// using printWrite to write to the response
				PrintWriter writer = resourceResponse.getWriter();
	
				writer.write(jsonArray.toString());	
			}
		} catch (SystemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new PortletException("Get IdTinhThanhPho Error", e1);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		resourceResponse.setContentType("text/javascript");
		String resourceId = resourceRequest.getResourceID();
		
		if (Validator.isNotNull(resourceId) && resourceId.length() != 0) {

			if(resourceId.equalsIgnoreCase("exportFullYeuCauVanTaiResourceURL")) {

		    		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		    		Locale locale = themeDisplay.getLocale();
		    		PortletConfig portletConfig = (PortletConfig)resourceRequest.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);

		    		resourceResponse.setContentType("application/vnd.ms-excel");
		    		resourceResponse.setProperty("expires","-1d");
		    		resourceResponse.setProperty("Pragma","no-cache");
		    		resourceResponse.setProperty("Cache-control","no-cache");
				resourceResponse.addProperty(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\" YeuCauVanTai\"");

				try {
					Workbook book = YeuCauVanTaiExporter.generateFullExcel(themeDisplay.getScopeGroupId(), portletConfig, locale);
					OutputStream out = resourceResponse.getPortletOutputStream();
					book.write(out);
					out.flush();
					out.close();
				} catch (SystemException e) {
					e.printStackTrace();
					throw new PortletException("Export Excel Error",e);
				}
			}

		}
	}

	protected String editYeuCauVanTaiJSP;
	protected String editJSP;
	protected String helpJSP;
	protected String viewJSP;
	protected String viewYeuCauVanTaiJSP;

	private static Log _log = LogFactoryUtil.getLog(YeuCauVanTaiPortlet.class);

}
