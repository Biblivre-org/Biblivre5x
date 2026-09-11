<%@page import="biblivre.core.schemas.Schemas"%>
<%@page import="biblivre.core.enums.PrinterType"%>
<%@page import="biblivre.core.utils.FileIOUtils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="biblivre.core.translations.LanguageDTO"%>
<%@page import="java.io.File"%>
<%@page import="biblivre.core.utils.DatabaseUtils"%>
<%@page import="biblivre.administration.backup.BackupBO"%>
<%@page import="biblivre.core.translations.Languages"%>
<%@page import="biblivre.core.utils.Constants"%>
<%@page import="biblivre.core.configurations.Configurations"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" uri="/WEB-INF/tlds/layout.tld" %>
<%@ taglib prefix="i18n" uri="/WEB-INF/tlds/translations.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<layout:head>
	<script type="text/javascript" src="static/scripts/biblivre.administration.configurations.js"></script>

	<script>
		Configurations.businessDays = '<%= Constants.CONFIG_BUSINESS_DAYS %>';
	</script>
</layout:head>

<layout:body>

	<div class="page_help"><i18n:text key="administration.configurations.page_help" /></div>
	
	<% String value; %>
	<% String key; %>
	<% boolean active; %>
	<% String schema = (String) request.getAttribute("schema"); %>
	<div class="biblivre_form">
		<fieldset>
			<% 
				key = Constants.CONFIG_TITLE;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">Biblivre IV</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>	

		<fieldset>
			<% 
				key = Constants.CONFIG_SUBTITLE;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">Software Livre para Gestão de Bibliotecas</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>	

		<fieldset>
			<legend><i18n:text key="administration.configuration.title.logged_in_text" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.logged_in_text" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<textarea name="text.main.logged_in"><i18n:text key="text.main.logged_in" escapeHTML="true" /></textarea>
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>	

		<fieldset>
			<legend><i18n:text key="administration.configuration.title.logged_out_text" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.logged_out_text" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<textarea name="text.main.logged_out"><i18n:text key="text.main.logged_out" escapeHTML="true" /></textarea>
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>	

		<fieldset>
			<% 
				key = Constants.CONFIG_ACCESSION_NUMBER_PREFIX;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">Bib</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_BUSINESS_DAYS;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value" id="business_days_current"><script>var BusinessValues = '<c:out value="${value}"/>';</script></div>
					<div class="clear"></div>
					
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="fleft" id="business_days"></div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_DEFAULT_LANGUAGE;
				value = Configurations.getString(schema, key);

				LanguageDTO ldto = Languages.getLanguage(schema, value);
				if (ldto != null) {
					request.setAttribute("default_language", ldto.getName());
				}
				
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">Português (Brasil)</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${default_language}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<select name="${key}">
							<c:forEach var="language" items="<%= Languages.getLanguages(schema) %>">
								<c:choose>
									<c:when test="${language.language == value}">
										<option value="${language.language}" selected="selected">${language.name}</option>
									</c:when>
									<c:otherwise>
										<option value="${language.language}">${language.name}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>						
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_CURRENCY;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">R$</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>	

		<fieldset>
			<% 
				key = Constants.CONFIG_SEARCH_RESULTS_PER_PAGE;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">25</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_SEARCH_RESULT_LIMIT;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">6000</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_Z3950_RESULT_LIMIT;
				value = Configurations.getString(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value">100</div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><c:out value="${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>
		
		<fieldset>
			<% 
				key = Constants.CONFIG_Z3950_SERVER_ACTIVE;
				active = Configurations.getBoolean(schema, key);
				request.setAttribute("key", key);
				request.setAttribute("active", active);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" param1="${schema}"/></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><input type="checkbox" id="z3950_server_active" name="${key}" class="finput" <c:if test="${active}">checked="checked"</c:if> style="width: auto;"/></div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>
		
		<fieldset>
			<% 
				key = Constants.CONFIG_LENDING_PRINTER_TYPE;
				value = Configurations.getString(schema, key);				
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.original_value" /></div>
					<div class="value"><i18n:text key="administration.configuration.printer_type.printer_common" /></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><i18n:text key="administration.configuration.printer_type.${value}"/></div>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<select name="${key}">
							<c:forEach var="printerType" items="<%= PrinterType.values() %>">
								<c:choose>
									<c:when test="${value eq printerType.string}">
										<option value="${printerType.string}" selected="selected"><i18n:text key="administration.configuration.printer_type.${printerType.string}"/></option>
									</c:when>
									<c:otherwise>
										<option value="${printerType.string}"><i18n:text key="administration.configuration.printer_type.${printerType.string}"/></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>						
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<% if (!Schemas.isMultipleSchemasEnabled()) { %>
		<fieldset>
			<% 
				key = Constants.CONFIG_MULTI_SCHEMA;
				active = Schemas.isMultipleSchemasEnabled();
				request.setAttribute("key", key);
				request.setAttribute("active", active);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" param1="${schema}"/></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<div class="value"><input type="checkbox" id="multi_schema_active" name="${key}" class="finput" <c:if test="${active}">checked="checked"</c:if> style="width: auto;"/></div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>
		<% } else { %>
		<fieldset>
			<% 
				key = Constants.CONFIG_MULTI_SCHEMA;
				active = Schemas.isMultipleSchemasEnabled();
				request.setAttribute("key", key);
				request.setAttribute("active", active);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<p class="description" style="margin: 10px;"><i18n:text key="administration.configuration.description.multi_schema.enabled" /></p>
		</fieldset>
		<% } %>
		
		<fieldset>
			<% 
				key = Constants.CONFIG_BACKUP_PATH;
				value = BackupBO.getInstance(schema).getBackupPath();
				request.setAttribute("key", key);
				request.setAttribute("value", value);
				
				boolean writeable = FileIOUtils.isWritablePath(value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<% if (writeable) {%>
						<div class="value"><c:out value="${value}"/></div>
					<% } else {%>
						<div class="value value_error"><c:out value="${value}"/><br><i18n:text key="administration.configuration.invalid_backup_path" /></div>
					<% }%>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_PGDUMP_PATH;
				File pgDump = DatabaseUtils.getPgDump(schema);
				value = (pgDump == null) ? null : pgDump.getAbsolutePath();
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<% if (value != null) { %>
						<div class="value"><c:out value="${value}"/></div>
					<% } else {%>
						<div class="value value_error"><i18n:text key="administration.configuration.invalid_pg_dump_path" /></div>
					<% }%>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>

		<fieldset>
			<% 
				key = Constants.CONFIG_PSQL_PATH;
				File psql = DatabaseUtils.getPsql(schema);
				value = (psql == null) ? null : psql.getAbsolutePath();
				request.setAttribute("key", key);
				request.setAttribute("value", value);
			%>
			<legend><i18n:text key="administration.configuration.title.${key}" /></legend>
			<div class="description"><i18n:text key="administration.configuration.description.${key}" /></div>
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.current_value" /></div>
					<% if (value != null) {%>
						<div class="value"><c:out value="${value}"/></div>
					<% } else {%>
						<div class="value value_error"><i18n:text key="administration.configuration.invalid_psql_path" /></div>
					<% }%>
					<div class="clear"></div>
				</div>
				<div>
					<div class="label"><i18n:text key="administration.configuration.new_value" /></div>
					<div class="value">
						<input type="text" name="${key}" class="finput" value="<c:out value="${value}"/>">
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</fieldset>
		
		
	
	
<fieldset>
    <%
    key = Constants.CONFIG_DOCUMENT_FORMAT_PDF;
    active = Configurations.getBoolean(schema, key);
    request.setAttribute("key", key);
    request.setAttribute("active", active);
    %>
    <legend><i18n:text key="administration.configuration.title.${key}" /></legend><%-- administration.configuration.title.general.document.format.pdf --%>
    <div class="description">
        <i18n:text key="administration.configuration.description.${key}" param1="${schema}"/>
    </div>
    <div class="fields">
        <div>
            <div class="label"><i18n:text key="administration.configuration.current_value" /></div>
            <div class="value">
                <c:choose>
                    <c:when test="${active}">PDF</c:when>
                    <c:otherwise>WORD</c:otherwise>
                </c:choose>
            </div>
            <div class="clear"></div>
        </div>
        <div>
            <div class="label"><i18n:text key="administration.configuration.new_value" /></div>
            <div class="value">
                <input type="radio" id="docTrue" name="${key}" value="true"
                    <c:if test="${active == true}">checked="checked"</c:if> style="width: auto;" />
                <label for="docTrue">PDF</label>

                <input type="radio" id="docFalse" name="${key}" value="false"
                    <c:if test="${active == false}">checked="checked"</c:if> style="width: auto;" />
                <label for="docFalse">WORD</label>
            </div>
            <div class="clear"></div>
        </div>
    </div>
</fieldset>

		<fieldset>
			<legend>Backup em Nuvem e E-mail</legend>
			<div class="description">Configurações para envio automático de backup para serviços de nuvem e e-mail.</div>
			
			<div class="fields">
				<div>
					<div class="label"><i18n:text key="administration.configuration.cloud.selector.label" /></div>
					<div class="value">
						<select id="cloud_backup_service_selector" class="finput">
							<option value=""><i18n:text key="administration.configuration.cloud.selector.placeholder" /></option>
							<option value="email">E-mail</option>
							<option value="box">Box</option>
							<option value="dropbox">Dropbox</option>
							<option value="google_drive">Google Drive</option>
							<option value="mega">MEGA</option>
							<option value="onedrive">OneDrive</option>
							<option value="pcloud">pCloud</option>
							<option value="proton">Proton Drive</option>
						</select>
					</div>
					<div class="clear"></div>
				</div>

				<div class="cloud_service_section" data-service="email">
				<h3 style="margin-top: 15px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">E-mail</h3>
				<% 
					String[] emailKeys = {
						Constants.CONFIG_BACKUP_EMAIL_ENABLED,
						Constants.CONFIG_BACKUP_EMAIL_HOST,
						Constants.CONFIG_BACKUP_EMAIL_PORT,
						Constants.CONFIG_BACKUP_EMAIL_USER,
						Constants.CONFIG_BACKUP_EMAIL_PASSWORD,
						Constants.CONFIG_BACKUP_EMAIL_FROM,
						Constants.CONFIG_BACKUP_EMAIL_TO,
						Constants.CONFIG_BACKUP_EMAIL_SSL
					};
					for (String k : emailKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled") || k.endsWith(".ssl")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled") || k.endsWith(".ssl")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".password")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
				</div>

				<div class="cloud_service_section" data-service="google_drive">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">Google Drive</h3>
				<% 
					String[] gdKeys = {
						Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED,
						Constants.CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_ID,
						Constants.CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_SECRET,
						Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN,
						Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ACCOUNT_EMAIL
					};
					for (String k : gdKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".client_secret") || k.endsWith(".refresh_token")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
					<div>
						<div class="label">Autenticacao Google Drive</div>
						<div class="value">
							<a class="button" id="google_drive_connect_button" onclick="Configurations.googleDriveConnect(this);">Conectar com Google</a>
							<div id="google_drive_connect_status" style="margin-top: 8px;">Clique em "Conectar com Google" para gerar o Refresh Token automaticamente.</div>
						</div>
						<div class="clear"></div>
					</div>
				</div>

				<div class="cloud_service_section" data-service="onedrive">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">OneDrive</h3>
				<% 
					String[] odKeys = {
						Constants.CONFIG_BACKUP_ONEDRIVE_ENABLED,
						Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_ID,
						Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_SECRET,
						Constants.CONFIG_BACKUP_ONEDRIVE_REFRESH_TOKEN
					};
					for (String k : odKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".client_secret") || k.endsWith(".refresh_token")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
				</div>

				<div class="cloud_service_section" data-service="dropbox">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">Dropbox</h3>
				<% 
					String[] dbKeys = {
						Constants.CONFIG_BACKUP_DROPBOX_ENABLED,
						Constants.CONFIG_BACKUP_DROPBOX_APP_KEY,
						Constants.CONFIG_BACKUP_DROPBOX_APP_SECRET,
						Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN,
						Constants.CONFIG_BACKUP_DROPBOX_REFRESH_TOKEN,
						Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN_EXPIRES_AT,
						Constants.CONFIG_BACKUP_DROPBOX_ACCOUNT_EMAIL
					};
					for (String k : dbKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".app_secret") || k.endsWith(".access_token") || k.endsWith(".refresh_token")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
					<div>
						<div class="label">Autenticacao Dropbox</div>
						<div class="value">
							<a class="button" id="dropbox_connect_button" onclick="Configurations.dropboxConnect(this);">Conectar com Dropbox</a>
							<div id="dropbox_connect_status" style="margin-top: 8px;">Clique em "Conectar com Dropbox" para gerar o Refresh Token automaticamente.</div>
						</div>
						<div class="clear"></div>
					</div>
				</div>
				<div class="cloud_service_section" data-service="box">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">Box</h3>
				<% 
					String[] boxKeys = {
						Constants.CONFIG_BACKUP_BOX_ENABLED,
						Constants.CONFIG_BACKUP_BOX_CLIENT_ID,
						Constants.CONFIG_BACKUP_BOX_CLIENT_SECRET,
						Constants.CONFIG_BACKUP_BOX_ACCESS_TOKEN,
						Constants.CONFIG_BACKUP_BOX_REFRESH_TOKEN,
						Constants.CONFIG_BACKUP_BOX_FOLDER_ID
					};
					for (String k : boxKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".client_secret") || k.endsWith(".access_token") || k.endsWith(".refresh_token")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
				</div>

				<div class="cloud_service_section" data-service="pcloud">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">pCloud</h3>
				<% 
					String[] pcKeys = {
						Constants.CONFIG_BACKUP_PCLOUD_ENABLED,
						Constants.CONFIG_BACKUP_PCLOUD_AUTH_TOKEN,
						Constants.CONFIG_BACKUP_PCLOUD_PATH
					};
					for (String k : pcKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".auth_token")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
				</div>

				<div class="cloud_service_section" data-service="mega">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">MEGA</h3>
				<% 
					String[] megaKeys = {
						Constants.CONFIG_BACKUP_MEGA_ENABLED,
						Constants.CONFIG_BACKUP_MEGA_CMD_PATH,
						Constants.CONFIG_BACKUP_MEGA_EMAIL,
						Constants.CONFIG_BACKUP_MEGA_PASSWORD,
						Constants.CONFIG_BACKUP_MEGA_REMOTE_PATH
					};
					for (String k : megaKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else if (k.endsWith(".password")) { %>
								<input type="password" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
				</div>

				<div class="cloud_service_section" data-service="proton">
				<h3 style="margin-top: 25px; border-bottom: 1px solid #ccc; padding-bottom: 5px;">Proton Drive</h3>
				<% 
					String[] protonKeys = {
						Constants.CONFIG_BACKUP_PROTON_ENABLED,
						Constants.CONFIG_BACKUP_PROTON_RCLONE_PATH,
						Constants.CONFIG_BACKUP_PROTON_RCLONE_REMOTE
					};
					for (String k : protonKeys) {
						request.setAttribute("k", k);
						if (k.endsWith(".enabled")) {
							request.setAttribute("v", Configurations.getBoolean(schema, k));
						} else {
							request.setAttribute("v", Configurations.getString(schema, k));
						}
				%>
					<div>
						<div class="label"><i18n:text key="administration.configuration.title.${k}" /></div>
						<div class="value">
							<% if (k.endsWith(".enabled")) { %>
								<input type="checkbox" name="${k}" class="finput cloud_backup_checkbox" style="width: auto;" <c:if test="${v}">checked="checked"</c:if>>
							<% } else { %>
								<input type="text" name="${k}" class="finput" value="<c:out value="${v}"/>">
							<% } %>
						</div>
						<div class="clear"></div>
					</div>
				<% } %>
				</div>

			</div>
		</fieldset>


		<div class="footer_buttons">
			<a class="button center main_button" onclick="Configurations.save(this);"><i18n:text key="common.save" /></a>
		</div>		
	</div>
</layout:body>
