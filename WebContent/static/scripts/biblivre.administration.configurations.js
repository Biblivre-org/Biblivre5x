/**
 *  Este arquivo é parte do Biblivre5.
 *  
 *  Biblivre5 é um software livre; você pode redistribuí-lo e/ou 
 *  modificá-lo dentro dos termos da Licença Pública Geral GNU como 
 *  publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 *  Licença, ou (caso queira) qualquer versão posterior.
 *  
 *  Este programa é distribuído na esperança de que possa ser  útil, 
 *  mas SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 *  MERCANTIBILIDADE OU ADEQUAÇÃO PARA UM FIM PARTICULAR. Veja a
 *  Licença Pública Geral GNU para maiores detalhes.
 *  
 *  Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 *  com este programa, Se não, veja em <http://www.gnu.org/licenses/>.
 * 
 *  @author Alberto Wagner <alberto@biblivre.org.br>
 *  @author Danniel Willian <danniel@biblivre.org.br>
 * 
 */
var Configurations = Configurations || {};
Configurations.googleDrive = Configurations.googleDrive || {};
Configurations.dropbox = Configurations.dropbox || {};

$(document).ready(function() {
	var businessDays = $('#business_days');
	var businessSelected = BusinessValues.split(',');
	var businessText = [];

	for (var i = 0; i < 7; i++) {
		var checked = false;
		for (var j = 0; j < businessSelected.length; j++) {
			if (businessSelected[j] == i + 1) {
				checked = true;
				break;
			}
		}
		
		var text = Globalize.culture().calendars.standard.days.names[i];

		var input = $('<input type="checkbox" name="' + Configurations.businessDays + '" class="finput" value="' + (i + 1) + '" id="bd_' + (i + 1) + '" >');
		if (checked) {
			input.attr('checked', 'checked');
			businessText.push(text);
		}
		input.appendTo(businessDays);
		$('<label for="bd_' + (i + 1) + '"></label>').text(' ' + text).appendTo(businessDays);
		$('<br>').appendTo(businessDays);
	}
	
	$('#business_days_current').text(businessText.join(', '));

	var selector = $('#cloud_backup_service_selector');
	var sections = $('.cloud_service_section');
	if (selector.size()) {
		sections.hide();
		selector.change(function() {
			var value = $(this).val();
			sections.hide();
			if (value) {
				sections.filter('[data-service="' + value + '"]').show();
			}
		});
		if (!selector.val()) {
			selector.val('google_drive');
		}
		selector.trigger('change');
	}
});

Configurations.googleDriveConnect = function(button) {
	var clientIdInput = $('input[name="administration.backup.google_drive.client_id"]');
	var clientSecretInput = $('input[name="administration.backup.google_drive.client_secret"]');
	var refreshTokenInput = $('input[name="administration.backup.google_drive.refresh_token"]');
	var accountEmailInput = $('input[name="administration.backup.google_drive.account_email"]');
	var enabledInput = $('input[name="administration.backup.google_drive.enabled"]');
	var status = $('#google_drive_connect_status');
	var clientId = $.trim(clientIdInput.val());
	var clientSecret = $.trim(clientSecretInput.val());

	status.text(_('administration.configuration.google_drive.oauth.status.opening'));

	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.configurations',
			action: 'google_drive_auth_url',
			client_id: clientId,
			client_secret: clientSecret
		},
		loadingButton: button
	}).done(function(response) {
		if (!response.success || !response.authorization_url) {
			Core.msg(response);
			status.text(_('administration.configuration.google_drive.oauth.status.error'));
			return;
		}

		var popup = window.open(response.authorization_url, 'biblivre_google_drive_oauth', 'width=700,height=760');
		if (!popup) {
			status.text(_('administration.configuration.google_drive.oauth.error.popup_blocked'));
			return;
		}

		Configurations.googleDrive.popup = popup;
		Configurations.googleDrive.statusLabel = status;
		Configurations.googleDrive.clientId = response.client_id || clientId;
		Configurations.googleDrive.clientSecret = clientSecret;
		Configurations.googleDrive.refreshTokenInput = refreshTokenInput;
		Configurations.googleDrive.accountEmailInput = accountEmailInput;
		Configurations.googleDrive.enabledInput = enabledInput;
		Configurations.googleDrive.state = response.state || '';

		status.text(_('administration.configuration.google_drive.oauth.status.waiting'));
		Configurations.googleDriveStartPolling();
	}).fail(function() {
		status.text(_('administration.configuration.google_drive.oauth.status.error'));
	});
};

Configurations.googleDriveStartPolling = function() {
	if (Configurations.googleDrive.pollTimer) {
		clearInterval(Configurations.googleDrive.pollTimer);
	}

	Configurations.googleDrive.pollTimer = setInterval(function() {
		var popup = Configurations.googleDrive.popup;
		var status = Configurations.googleDrive.statusLabel;
		if (!popup || popup.closed) {
			clearInterval(Configurations.googleDrive.pollTimer);
			Configurations.googleDrive.pollTimer = null;
			if (status && $.trim(status.text()) === _('administration.configuration.google_drive.oauth.status.waiting')) {
				status.text(_('administration.configuration.google_drive.oauth.status.cancelled'));
			}
			return;
		}

		var query = '';
		try {
			query = popup.location.search || '';
		} catch (e) {
			return;
		}

		if (!query) {
			return;
		}

		var params = Configurations.googleDriveParseQuery(query);
		var code = params.code || '';
		var state = params.state || '';
		var error = params.error || '';

		if (!code && !error) {
			return;
		}

		clearInterval(Configurations.googleDrive.pollTimer);
		Configurations.googleDrive.pollTimer = null;
		popup.close();

		if (error) {
			status.text(_('administration.configuration.google_drive.oauth.error.user_denied'));
			return;
		}

		Configurations.googleDriveExchangeCode(code, state);
	}, 500);
};

Configurations.googleDriveExchangeCode = function(code, state) {
	var status = Configurations.googleDrive.statusLabel;
	status.text(_('administration.configuration.google_drive.oauth.status.exchanging'));

	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.configurations',
			action: 'google_drive_exchange_code',
			code: code,
			state: state,
			client_id: Configurations.googleDrive.clientId,
			client_secret: Configurations.googleDrive.clientSecret
		}
	}).done(function(response) {
		if (!response.success) {
			Core.msg(response);
			status.text(_('administration.configuration.google_drive.oauth.status.error'));
			return;
		}

		if (Configurations.googleDrive.refreshTokenInput) {
			Configurations.googleDrive.refreshTokenInput.val(response.refresh_token || '');
		}
		if (Configurations.googleDrive.accountEmailInput) {
			Configurations.googleDrive.accountEmailInput.val(response.account_email || '');
		}
		if (Configurations.googleDrive.enabledInput && Configurations.googleDrive.enabledInput.size()) {
			Configurations.googleDrive.enabledInput.prop('checked', true);
		}

		status.text(_('administration.configuration.google_drive.oauth.status.connected'));
	}).fail(function() {
		status.text(_('administration.configuration.google_drive.oauth.status.error'));
	});
};

Configurations.googleDriveParseQuery = function(query) {
	var result = {};
	var q = query || '';
	if (q.indexOf('?') === 0) {
		q = q.substring(1);
	}
	var chunks = q.split('&');
	for (var i = 0; i < chunks.length; i++) {
		var pair = chunks[i].split('=');
		if (!pair.length || !pair[0]) {
			continue;
		}
		var key = decodeURIComponent(pair[0]);
		var value = pair.length > 1 ? decodeURIComponent((pair[1] || '').replace(/\+/g, ' ')) : '';
		result[key] = value;
	}
	return result;
};

Configurations.buildOAuthRedirectUri = function() {
	var origin = window.location.protocol + '//' + window.location.host;
	var path = window.location.pathname || '/';

	if (path.length > 1 && path.charAt(path.length - 1) === '/') {
		path = path.substring(0, path.length - 1);
	}

	return origin + path;
};

Configurations.dropboxConnect = function(button) {
	var appKeyInput = $('input[name="administration.backup.dropbox.app_key"]');
	var appSecretInput = $('input[name="administration.backup.dropbox.app_secret"]');
	var accessTokenInput = $('input[name="administration.backup.dropbox.access_token"]');
	var refreshTokenInput = $('input[name="administration.backup.dropbox.refresh_token"]');
	var expiresAtInput = $('input[name="administration.backup.dropbox.access_token_expires_at"]');
	var accountEmailInput = $('input[name="administration.backup.dropbox.account_email"]');
	var enabledInput = $('input[name="administration.backup.dropbox.enabled"]');
	var status = $('#dropbox_connect_status');
	var appKey = $.trim(appKeyInput.val());
	var appSecret = $.trim(appSecretInput.val());

	status.text(_('administration.configuration.dropbox.oauth.status.opening'));

	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.configurations',
			action: 'dropbox_auth_url',
			app_key: appKey,
			app_secret: appSecret,
			redirect_uri: Configurations.buildOAuthRedirectUri()
		},
		loadingButton: button
	}).done(function(response) {
		if (!response.success || !response.authorization_url) {
			Core.msg(response);
			status.text(_('administration.configuration.dropbox.oauth.status.error'));
			return;
		}

		var popup = window.open(response.authorization_url, 'biblivre_dropbox_oauth', 'width=700,height=760');
		if (!popup) {
			status.text(_('administration.configuration.dropbox.oauth.error.popup_blocked'));
			return;
		}

		Configurations.dropbox.popup = popup;
		Configurations.dropbox.statusLabel = status;
		Configurations.dropbox.appKey = response.app_key || appKey;
		Configurations.dropbox.appSecret = appSecret;
		Configurations.dropbox.accessTokenInput = accessTokenInput;
		Configurations.dropbox.refreshTokenInput = refreshTokenInput;
		Configurations.dropbox.expiresAtInput = expiresAtInput;
		Configurations.dropbox.accountEmailInput = accountEmailInput;
		Configurations.dropbox.enabledInput = enabledInput;

		status.text(_('administration.configuration.dropbox.oauth.status.waiting'));
		Configurations.dropboxStartPolling();
	}).fail(function() {
		status.text(_('administration.configuration.dropbox.oauth.status.error'));
	});
};

Configurations.dropboxStartPolling = function() {
	if (Configurations.dropbox.pollTimer) {
		clearInterval(Configurations.dropbox.pollTimer);
	}

	Configurations.dropbox.pollTimer = setInterval(function() {
		var popup = Configurations.dropbox.popup;
		var status = Configurations.dropbox.statusLabel;
		if (!popup || popup.closed) {
			clearInterval(Configurations.dropbox.pollTimer);
			Configurations.dropbox.pollTimer = null;
			if (status && $.trim(status.text()) === _('administration.configuration.dropbox.oauth.status.waiting')) {
				status.text(_('administration.configuration.dropbox.oauth.status.cancelled'));
			}
			return;
		}

		var query = '';
		try {
			query = popup.location.search || '';
		} catch (e) {
			return;
		}

		if (!query) {
			return;
		}

		var params = Configurations.googleDriveParseQuery(query);
		var code = params.code || '';
		var state = params.state || '';
		var error = params.error || '';

		if (!code && !error) {
			return;
		}

		clearInterval(Configurations.dropbox.pollTimer);
		Configurations.dropbox.pollTimer = null;
		popup.close();

		if (error) {
			status.text(_('administration.configuration.dropbox.oauth.error.user_denied'));
			return;
		}

		Configurations.dropboxExchangeCode(code, state);
	}, 500);
};

Configurations.dropboxExchangeCode = function(code, state) {
	var status = Configurations.dropbox.statusLabel;
	status.text(_('administration.configuration.dropbox.oauth.status.exchanging'));

	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.configurations',
			action: 'dropbox_exchange_code',
			code: code,
			state: state,
			app_key: Configurations.dropbox.appKey,
			app_secret: Configurations.dropbox.appSecret
		}
	}).done(function(response) {
		if (!response.success) {
			Core.msg(response);
			status.text(_('administration.configuration.dropbox.oauth.status.error'));
			return;
		}

		if (Configurations.dropbox.accessTokenInput) {
			Configurations.dropbox.accessTokenInput.val(response.access_token || '');
		}
		if (Configurations.dropbox.refreshTokenInput) {
			Configurations.dropbox.refreshTokenInput.val(response.refresh_token || '');
		}
		if (Configurations.dropbox.expiresAtInput) {
			Configurations.dropbox.expiresAtInput.val(response.access_token_expires_at || '');
		}
		if (Configurations.dropbox.accountEmailInput) {
			Configurations.dropbox.accountEmailInput.val(response.account_email || '');
		}
		if (Configurations.dropbox.enabledInput && Configurations.dropbox.enabledInput.size()) {
			Configurations.dropbox.enabledInput.prop('checked', true);
		}

		status.text(_('administration.configuration.dropbox.oauth.status.connected'));
	}).fail(function() {
		status.text(_('administration.configuration.dropbox.oauth.status.error'));
	});
};

Configurations.save = function(button) {
	var result = {};
	
	$('.biblivre_form :input:not(:checkbox)').each(function() {
		var el = $(this);
		result[el.attr('name')] = el.val();
	});

	$('.biblivre_form #business_days :checkbox:checked').each(function() {
		var el = $(this);
		var name = el.attr('name');
		var val = el.val();

		if (result[name]) {
			result[name] += ',' + val;
		} else {
			result[name] = val;
		}
	});

	var z3950Checkbox = $('#z3950_server_active');
	if (z3950Checkbox.size()) {
		result[z3950Checkbox.attr('name')] = z3950Checkbox.is(':checked');
	}
	
	// Tratamento para os radio buttons de formato de documento (PDF/WORD)
	var docFormatRadios = $('input[id="docTrue"], input[id="docFalse"]');
	if (docFormatRadios.size()) {
		var docName = docFormatRadios.first().attr('name');
		var selectedValue = $('input[id="docTrue"]:checked, input[id="docFalse"]:checked').val();
		if (selectedValue) {
			result[docName] = selectedValue;
		}
	}

	var multiSchema = $('#multi_schema_active');
	var multiSchemaChecked = false;

	if (multiSchema.size()) {
		multiSchemaChecked = multiSchema.is(':checked');
		result[multiSchema.attr('name')] = multiSchemaChecked;
	}

	$('.biblivre_form .cloud_backup_checkbox').each(function() {
		var el = $(this);
		result[el.attr('name')] = el.is(':checked');
	});
	
	Core.clearFormErrors();
	
	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.configurations',
			action: 'save',
			configurations: JSON.stringify(result)
		},
		loadingButton: button,
		loadingTimedOverlay: true
	}).done($.proxy(function(response) {
		if (!response.success) {
			Core.msg(response);
			Core.formErrors(response.errors);
		} else if (response.reload) {
			window.location.href = window.location.pathname;
		} else {
			var obj = Core.qso();
			obj.msg = response.message;
			obj.level = response.message_level;

			 window.location.href = window.location.pathname + '?' + $.param(obj);
		}
	}, this));

};
