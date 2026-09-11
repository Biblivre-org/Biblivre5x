/*******************************************************************************
 * Este arquivo é parte do Biblivre5.
 * 
 * Biblivre5 é um software livre; você pode redistribuí-lo e/ou 
 * modificá-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (caso queira) qualquer versão posterior.
 * 
 * Este programa é distribuído na esperança de que possa ser  útil, 
 * mas SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * MERCANTIBILIDADE OU ADEQUAÇÃO PARA UM FIM PARTICULAR. Veja a
 * Licença Pública Geral GNU para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 * com este programa, Se não, veja em <http://www.gnu.org/licenses/>.
 * 
 * @author Alberto Wagner <alberto@biblivre.org.br>
 * @author Danniel Willian <danniel@biblivre.org.br>
 ******************************************************************************/
package biblivre.administration.configurations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxSessionStore;

import biblivre.administration.backup.services.DropboxService;
import biblivre.administration.backup.services.GoogleDriveService;
import biblivre.administration.backup.services.GoogleDriveService.OAuthClientCredentials;
import biblivre.core.AbstractHandler;
import biblivre.core.ExtendedRequest;
import biblivre.core.ExtendedResponse;
import biblivre.core.configurations.Configurations;
import biblivre.core.configurations.ConfigurationsDTO;
import biblivre.core.enums.ActionResult;
import biblivre.core.exceptions.ValidationException;
import biblivre.core.schemas.Schemas;
import biblivre.core.utils.Constants;
import biblivre.core.translations.Translations;

public class Handler extends AbstractHandler {
	private static final Logger LOGGER = Logger.getLogger(Handler.class);
	private static final String GOOGLE_DRIVE_OAUTH_STATE = "google_drive_oauth_state";
	private static final String GOOGLE_DRIVE_OAUTH_REDIRECT_URI = "google_drive_oauth_redirect_uri";
	private static final String GOOGLE_DRIVE_OAUTH_CLIENT_ID = "google_drive_oauth_client_id";
	private static final String GOOGLE_DRIVE_OAUTH_CLIENT_SECRET = "google_drive_oauth_client_secret";
	private static final String DROPBOX_OAUTH_STATE = "dropbox_oauth_state";
	private static final String DROPBOX_OAUTH_REDIRECT_URI = "dropbox_oauth_redirect_uri";
	private static final String DROPBOX_OAUTH_APP_KEY = "dropbox_oauth_app_key";
	private static final String DROPBOX_OAUTH_APP_SECRET = "dropbox_oauth_app_secret";
	private static final String DROPBOX_OAUTH_CSRF = "dropbox_oauth_csrf";

	public void save(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		int loggedUser = request.getLoggedUserId();
		String language = request.getLanguage(); 

		String configurations = request.getString("configurations", "{}");
		
		List<ConfigurationsDTO> configs = new ArrayList<ConfigurationsDTO>();
		
		try {
			JSONObject json = new JSONObject(configurations);

			Iterator<String> it = json.keys();
			while (it.hasNext()) {
				String key = it.next();
				String value = json.getString(key);

				if (key.equals("text.main.logged_in") || key.equals("text.main.logged_out")) {
					Translations.addOrReplaceSingleTranslation(schema, language, key, value, loggedUser);
				} else {
					configs.add(new ConfigurationsDTO(key, value));
				}
			}
		} catch (JSONException e) {
			this.setMessage(ActionResult.WARNING, "error.invalid_json");
			return;
		}

		if (configs.size() == 0) {
			return;
		}
		
		try {
			configs = Configurations.validate(schema, configs);
		} catch (ValidationException e) {
			this.setMessage(e);
			return;
		}
		
		try {
			boolean multiSchemaBefore = Schemas.isMultipleSchemasEnabled();
			Configurations.save(schema, configs, loggedUser);
			boolean multiSchemaAfter = Schemas.isMultipleSchemasEnabled();

			this.setMessage(ActionResult.SUCCESS, "administration.configurations.save.success");
			this.json.put("reload", multiSchemaBefore != multiSchemaAfter);
		} catch (Exception e) {
			this.setMessage(ActionResult.WARNING, "administration.configurations.error.save");
			return;
		}
	}
	
	public void ignoreUpdate(ExtendedRequest request, ExtendedResponse response) {
		request.getSession().removeAttribute(request.getSchema() + ".system_warning_new_version");
	}

	public void googleDriveAuthUrl(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		String clientId = StringUtils.trimToEmpty(request.getString("client_id"));
		String clientSecret = StringUtils.trimToEmpty(request.getString("client_secret"));

		OAuthClientCredentials credentials;
		try {
			credentials = GoogleDriveService.resolveOAuthClientCredentials(schema, clientId, clientSecret);
		} catch (Exception e) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.google_drive.oauth.error.missing_credentials");
			return;
		}

		if (!credentials.isComplete()) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.google_drive.oauth.error.missing_credentials");
			return;
		}

		String redirectUri = this.buildRedirectUri(request);
		String state = UUID.randomUUID().toString();

		request.setSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_STATE, state);
		request.setSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_REDIRECT_URI, redirectUri);
		request.setSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_CLIENT_ID, credentials.getClientId());
		request.setSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_CLIENT_SECRET, credentials.getClientSecret());

		String authorizationUrl = GoogleDriveService.createAuthorizationUrl(credentials, redirectUri, state);

		try {
			this.json.put("success", true);
			this.json.put("authorization_url", authorizationUrl);
			this.json.put("state", state);
			this.json.put("client_id", credentials.getClientId());
		} catch (JSONException e) {}
	}

	public void googleDriveExchangeCode(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		int loggedUser = request.getLoggedUserId();
		String code = StringUtils.trimToEmpty(request.getString("code"));
		String state = StringUtils.trimToEmpty(request.getString("state"));
		String clientId = StringUtils.trimToEmpty(request.getString("client_id"));
		String clientSecret = StringUtils.trimToEmpty(request.getString("client_secret"));

		if (code.isEmpty()) {
			this.setMessage(ActionResult.WARNING, "error.invalid_parameters");
			return;
		}

		Object expectedStateObject = request.getSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_STATE);
		Object redirectUriObject = request.getSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_REDIRECT_URI);
		Object sessionClientIdObject = request.getSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_CLIENT_ID);
		Object sessionClientSecretObject = request.getSessionAttribute(schema, GOOGLE_DRIVE_OAUTH_CLIENT_SECRET);

		request.getSession().removeAttribute(schema + "." + GOOGLE_DRIVE_OAUTH_STATE);
		request.getSession().removeAttribute(schema + "." + GOOGLE_DRIVE_OAUTH_REDIRECT_URI);
		request.getSession().removeAttribute(schema + "." + GOOGLE_DRIVE_OAUTH_CLIENT_ID);
		request.getSession().removeAttribute(schema + "." + GOOGLE_DRIVE_OAUTH_CLIENT_SECRET);

		String expectedState = expectedStateObject == null ? "" : expectedStateObject.toString();
		String redirectUri = redirectUriObject == null ? this.buildRedirectUri(request) : redirectUriObject.toString();

		if (expectedState.isEmpty() || !expectedState.equals(state)) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.google_drive.oauth.error.invalid_state");
			return;
		}

		if (clientId.isEmpty() && sessionClientIdObject != null) {
			clientId = StringUtils.trimToEmpty(sessionClientIdObject.toString());
		}
		if (clientSecret.isEmpty() && sessionClientSecretObject != null) {
			clientSecret = StringUtils.trimToEmpty(sessionClientSecretObject.toString());
		}

		OAuthClientCredentials credentials;
		try {
			credentials = GoogleDriveService.resolveOAuthClientCredentials(schema, clientId, clientSecret);
		} catch (Exception e) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.google_drive.oauth.error.missing_credentials");
			return;
		}

		if (!credentials.isComplete()) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.google_drive.oauth.error.missing_credentials");
			return;
		}

		try {
			com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse tokenResponse =
					GoogleDriveService.exchangeAuthorizationCode(credentials, code, redirectUri);
			String refreshToken = StringUtils.trimToEmpty(tokenResponse.getRefreshToken());

			if (refreshToken.isEmpty()) {
				this.setMessage(ActionResult.WARNING, "administration.configuration.google_drive.oauth.error.missing_refresh_token");
				return;
			}

			String accountEmail = "";
			try {
				accountEmail = StringUtils.trimToEmpty(GoogleDriveService.getAccountEmailFromAccessToken(tokenResponse.getAccessToken()));
			} catch (Exception e) {
				accountEmail = "";
			}

			List<ConfigurationsDTO> configs = new ArrayList<ConfigurationsDTO>();
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED, "true"));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_ID, credentials.getClientId()));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_SECRET, credentials.getClientSecret()));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN, refreshToken));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ACCOUNT_EMAIL, accountEmail));
			Configurations.save(schema, configs, loggedUser);

			this.json.put("success", true);
			this.json.put("refresh_token", refreshToken);
			this.json.put("account_email", accountEmail);
		} catch (Exception e) {
			this.setMessage(ActionResult.ERROR, "administration.configuration.google_drive.oauth.error.exchange_failed");
		}
	}

	public void dropboxAuthUrl(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		String appKey = StringUtils.trimToEmpty(request.getString("app_key"));
		String appSecret = StringUtils.trimToEmpty(request.getString("app_secret"));

		DropboxService.OAuthClientCredentials credentials = DropboxService.resolveOAuthClientCredentials(schema, appKey, appSecret);
		if (!credentials.isComplete()) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.dropbox.oauth.error.missing_credentials");
			return;
		}

		String redirectUri = this.resolveOAuthRedirectUri(request);
		String state = UUID.randomUUID().toString();
		DbxSessionStore csrfStore = new ServletSessionDbxSessionStore(request.getSession(), schema + "." + DROPBOX_OAUTH_CSRF);

		request.setSessionAttribute(schema, DROPBOX_OAUTH_STATE, state);
		request.setSessionAttribute(schema, DROPBOX_OAUTH_REDIRECT_URI, redirectUri);
		request.setSessionAttribute(schema, DROPBOX_OAUTH_APP_KEY, credentials.getAppKey());
		request.setSessionAttribute(schema, DROPBOX_OAUTH_APP_SECRET, credentials.getAppSecret());

		try {
			String authorizationUrl = DropboxService.createAuthorizationUrl(credentials, redirectUri, csrfStore, state);
			this.json.put("success", true);
			this.json.put("authorization_url", authorizationUrl);
			this.json.put("redirect_uri", redirectUri);
			this.json.put("state", state);
			this.json.put("app_key", credentials.getAppKey());
		} catch (Exception e) {
			this.logDropboxOAuthError("auth_url", "Could not create Dropbox OAuth authorization URL.", e);
			this.setMessage(ActionResult.ERROR, "administration.configuration.dropbox.oauth.error.auth_url_failed");
		}
	}

	public void dropboxExchangeCode(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		int loggedUser = request.getLoggedUserId();
		String code = StringUtils.trimToEmpty(request.getString("code"));
		String state = StringUtils.trimToEmpty(request.getString("state"));
		String appKey = StringUtils.trimToEmpty(request.getString("app_key"));
		String appSecret = StringUtils.trimToEmpty(request.getString("app_secret"));

		if (code.isEmpty() || state.isEmpty()) {
			this.setMessage(ActionResult.WARNING, "error.invalid_parameters");
			return;
		}

		Object expectedStateObject = request.getSessionAttribute(schema, DROPBOX_OAUTH_STATE);
		Object redirectUriObject = request.getSessionAttribute(schema, DROPBOX_OAUTH_REDIRECT_URI);
		Object sessionAppKeyObject = request.getSessionAttribute(schema, DROPBOX_OAUTH_APP_KEY);
		Object sessionAppSecretObject = request.getSessionAttribute(schema, DROPBOX_OAUTH_APP_SECRET);

		String expectedState = expectedStateObject == null ? "" : expectedStateObject.toString();
		String redirectUri = redirectUriObject == null ? this.buildRedirectUri(request) : redirectUriObject.toString();

		if (appKey.isEmpty() && sessionAppKeyObject != null) {
			appKey = StringUtils.trimToEmpty(sessionAppKeyObject.toString());
		}
		if (appSecret.isEmpty() && sessionAppSecretObject != null) {
			appSecret = StringUtils.trimToEmpty(sessionAppSecretObject.toString());
		}

		DropboxService.OAuthClientCredentials credentials = DropboxService.resolveOAuthClientCredentials(schema, appKey, appSecret);
		if (!credentials.isComplete()) {
			this.setMessage(ActionResult.WARNING, "administration.configuration.dropbox.oauth.error.missing_credentials");
			return;
		}

		try {
			DbxSessionStore csrfStore = new ServletSessionDbxSessionStore(request.getSession(), schema + "." + DROPBOX_OAUTH_CSRF);
			DbxAuthFinish authFinish = DropboxService.exchangeAuthorizationCode(credentials, redirectUri, csrfStore, code, state);
			String returnedState = StringUtils.trimToEmpty(authFinish.getUrlState());
			if (expectedState.isEmpty() || !expectedState.equals(returnedState)) {
				this.setMessage(ActionResult.WARNING, "administration.configuration.dropbox.oauth.error.invalid_state");
				return;
			}

			String accessToken = StringUtils.trimToEmpty(authFinish.getAccessToken());
			String refreshToken = StringUtils.trimToEmpty(authFinish.getRefreshToken());
			if (refreshToken.isEmpty()) {
				this.setMessage(ActionResult.WARNING, "administration.configuration.dropbox.oauth.error.missing_refresh_token");
				return;
			}

			String accountEmail = "";
			try {
				accountEmail = StringUtils.trimToEmpty(DropboxService.getAccountEmailFromAccessToken(accessToken));
			} catch (Exception e) {
				accountEmail = "";
			}

			List<ConfigurationsDTO> configs = new ArrayList<ConfigurationsDTO>();
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_ENABLED, "true"));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_APP_KEY, credentials.getAppKey()));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_APP_SECRET, credentials.getAppSecret()));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN, accessToken));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_REFRESH_TOKEN, refreshToken));
			String expiresAt = authFinish.getExpiresAt() == null ? "" : String.valueOf(authFinish.getExpiresAt());
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN_EXPIRES_AT, expiresAt));
			configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_ACCOUNT_EMAIL, accountEmail));
			Configurations.save(schema, configs, loggedUser);

			request.getSession().removeAttribute(schema + "." + DROPBOX_OAUTH_STATE);
			request.getSession().removeAttribute(schema + "." + DROPBOX_OAUTH_REDIRECT_URI);
			request.getSession().removeAttribute(schema + "." + DROPBOX_OAUTH_APP_KEY);
			request.getSession().removeAttribute(schema + "." + DROPBOX_OAUTH_APP_SECRET);

			this.json.put("success", true);
			this.json.put("access_token", accessToken);
			this.json.put("refresh_token", refreshToken);
			this.json.put("access_token_expires_at", expiresAt);
			this.json.put("account_email", accountEmail);
		} catch (Exception e) {
			this.logDropboxOAuthError("exchange_code", "Could not finish Dropbox OAuth authorization.", e);
			this.setMessage(ActionResult.ERROR, "administration.configuration.dropbox.oauth.error.exchange_failed");
		}
	}

	private void logDropboxOAuthError(String operation, String message, Throwable error) {
		String fullMessage = "[Biblivre][DropboxOAuth][" + operation + "] " + message;
		LOGGER.error(fullMessage, error);
		System.err.println(fullMessage);
		if (error != null) {
			error.printStackTrace(System.err);
		}
	}

	private String buildRedirectUri(ExtendedRequest request) {
		StringBuilder url = new StringBuilder();
		url.append(request.getScheme()).append("://").append(request.getServerName());

		int port = request.getServerPort();
		boolean isDefaultHttp = "http".equalsIgnoreCase(request.getScheme()) && port == 80;
		boolean isDefaultHttps = "https".equalsIgnoreCase(request.getScheme()) && port == 443;
		if (!isDefaultHttp && !isDefaultHttps) {
			url.append(":").append(port);
		}

		url.append(request.getRequestURI());
		return url.toString();
	}

	private String resolveOAuthRedirectUri(ExtendedRequest request) {
		String redirectUri = this.stripQueryAndFragment(StringUtils.trimToEmpty(request.getString("redirect_uri")));
		if (StringUtils.isNotBlank(redirectUri) && this.isSameRequestUri(request, redirectUri)) {
			return this.normalizeRedirectUri(redirectUri);
		}

		return this.normalizeRedirectUri(this.buildRedirectUri(request));
	}

	private String stripQueryAndFragment(String url) {
		int queryIndex = url.indexOf('?');
		int fragmentIndex = url.indexOf('#');
		int endIndex = -1;

		if (queryIndex >= 0 && fragmentIndex >= 0) {
			endIndex = Math.min(queryIndex, fragmentIndex);
		} else if (queryIndex >= 0) {
			endIndex = queryIndex;
		} else if (fragmentIndex >= 0) {
			endIndex = fragmentIndex;
		}

		return endIndex >= 0 ? url.substring(0, endIndex) : url;
	}

	private boolean isSameRequestUri(ExtendedRequest request, String redirectUri) {
		try {
			URI uri = new URI(redirectUri);
			if (!uri.isAbsolute() || StringUtils.isBlank(uri.getHost())) {
				return false;
			}

			if (!StringUtils.equalsIgnoreCase(request.getScheme(), uri.getScheme())) {
				return false;
			}
			if (!StringUtils.equalsIgnoreCase(request.getServerName(), uri.getHost())) {
				return false;
			}
			if (this.normalizePort(request.getScheme(), request.getServerPort()) != this.normalizePort(uri.getScheme(), uri.getPort())) {
				return false;
			}

			return StringUtils.equals(this.normalizeRedirectPath(request.getRequestURI()), this.normalizeRedirectPath(uri.getRawPath()));
		} catch (URISyntaxException e) {
			return false;
		}
	}

	private String normalizeRedirectPath(String path) {
		String normalizedPath = StringUtils.defaultString(path);
		if (normalizedPath.length() > 1 && normalizedPath.endsWith("/")) {
			return normalizedPath.substring(0, normalizedPath.length() - 1);
		}

		return normalizedPath;
	}

	private String normalizeRedirectUri(String redirectUri) {
		try {
			URI uri = new URI(redirectUri);
			String path = StringUtils.defaultString(uri.getRawPath());
			if (path.length() > 1 && redirectUri.endsWith("/")) {
				return redirectUri.substring(0, redirectUri.length() - 1);
			}
		} catch (URISyntaxException e) {
			return redirectUri;
		}

		return redirectUri;
	}

	private int normalizePort(String scheme, int port) {
		if (port != -1) {
			return port;
		}
		if ("http".equalsIgnoreCase(scheme)) {
			return 80;
		}
		if ("https".equalsIgnoreCase(scheme)) {
			return 443;
		}
		return port;
	}

	private static class ServletSessionDbxSessionStore implements DbxSessionStore {
		private final HttpSession session;
		private final String key;

		private ServletSessionDbxSessionStore(HttpSession session, String key) {
			this.session = session;
			this.key = key;
		}

		@Override
		public String get() {
			Object value = this.session.getAttribute(this.key);
			return value == null ? null : value.toString();
		}

		@Override
		public void set(String value) {
			this.session.setAttribute(this.key, value);
		}

		@Override
		public void clear() {
			this.session.removeAttribute(this.key);
		}
	}
}
