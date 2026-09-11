package biblivre.update.v5_1_32;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import biblivre.core.translations.Translations;
import biblivre.update.UpdateService;



public class Update implements UpdateService {

	public void doUpdate(Connection connection) throws SQLException {
		_addTranslations();
	}

	@Override
	public String getVersion() {
		return "5.1.32";
	}

	private void _addTranslations() throws SQLException {
		for (Map.Entry<String, Map<String, String>> entry: _TRANSLATIONS.entrySet()) {
			for (Map.Entry<String, String> entry2: entry.getValue().entrySet()) {
				String key = entry.getKey();
				String language = entry2.getKey();
				String translation = entry2.getValue();
				Translations.addOrReplaceSingleTranslation(language, key, translation);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private static final Map<String, Map<String, String>> _TRANSLATIONS = new HashMap() {{
		
		put("administration.maintenance.backup.cloud_upload.success", new HashMap() {{
			put("pt-BR", "Upload realizado com sucesso.");
			put("es", "¡Carga realizada con éxito!");
			put("en-US", "Upload completed successfully!");
		}});
		
		put("administration.maintenance.backup.cloud_upload.error", new HashMap() {{
			put("pt-BR", "Ocorreu erro no upload !");
			put("es", " ¡Ocurrió un error en la carga!");
			put("en-US", "An error occurred during the upload!");
		}});
		
		
		put("administration.configuration.title.administration.backup.email.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup por e-mail");
			put("es", "Habilitar backup por correo electrónico");
			put("en-US", "Enable backup by email");
		}});

		put("administration.configuration.description.administration.backup.email.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado por e-mail após a conclusão.");
			put("es", "Define si el backup será enviado por correo electrónico después de la finalización.");
			put("en-US", "Define if the backup will be sent by email after completion.");
		}});

		put("administration.configuration.title.administration.backup.email.host", new HashMap() {{
			put("pt-BR", "Servidor SMTP");
			put("es", "Servidor SMTP");
			put("en-US", "SMTP Server");
		}});

		put("administration.configuration.description.administration.backup.email.host", new HashMap() {{
			put("pt-BR", "Endereço do servidor SMTP para envio de e-mail.");
			put("es", "Dirección del servidor SMTP para envío de correo electrónico.");
			put("en-US", "SMTP server address for sending email.");
		}});

		put("administration.configuration.title.administration.backup.email.port", new HashMap() {{
			put("pt-BR", "Porta SMTP");
			put("es", "Puerto SMTP");
			put("en-US", "SMTP Port");
		}});

		put("administration.configuration.description.administration.backup.email.port", new HashMap() {{
			put("pt-BR", "Porta do servidor SMTP (ex: 587 para TLS, 465 para SSL).");
			put("es", "Puerto del servidor SMTP (ej: 587 para TLS, 465 para SSL).");
			put("en-US", "SMTP server port (e.g. 587 for TLS, 465 for SSL).");
		}});

		put("administration.configuration.title.administration.backup.email.user", new HashMap() {{
			put("pt-BR", "Usuário SMTP");
			put("es", "Usuario SMTP");
			put("en-US", "SMTP User");
		}});

		put("administration.configuration.description.administration.backup.email.user", new HashMap() {{
			put("pt-BR", "Usuário para autenticação no servidor SMTP.");
			put("es", "Usuario para autenticación en el servidor SMTP.");
			put("en-US", "User for SMTP server authentication.");
		}});

		put("administration.configuration.title.administration.backup.email.password", new HashMap() {{
			put("pt-BR", "Senha SMTP");
			put("es", "Contraseña SMTP");
			put("en-US", "SMTP Password");
		}});

		put("administration.configuration.description.administration.backup.email.password", new HashMap() {{
			put("pt-BR", "Senha para autenticação no servidor SMTP.");
			put("es", "Contraseña para autenticación en el servidor SMTP.");
			put("en-US", "Password for SMTP server authentication.");
		}});

		put("administration.configuration.title.administration.backup.email.from", new HashMap() {{
			put("pt-BR", "E-mail do remetente");
			put("es", "Correo electrónico del remitente");
			put("en-US", "Sender Email");
		}});

		put("administration.configuration.description.administration.backup.email.from", new HashMap() {{
			put("pt-BR", "Endereço de e-mail que aparecerá como remetente.");
			put("es", "Dirección de correo electrónico que aparecerá como remitente.");
			put("en-US", "Email address that will appear as sender.");
		}});

		put("administration.configuration.title.administration.backup.email.to", new HashMap() {{
			put("pt-BR", "E-mail do destinatário");
			put("es", "Correo electrónico del destinatario");
			put("en-US", "Recipient Email");
		}});

		put("administration.configuration.description.administration.backup.email.to", new HashMap() {{
			put("pt-BR", "Endereço de e-mail que receberá o backup.");
			put("es", "Dirección de correo electrónico que recibirá el backup.");
			put("en-US", "Email address that will receive the backup.");
		}});

		put("administration.configuration.title.administration.backup.email.ssl", new HashMap() {{
			put("pt-BR", "Usar SSL/TLS");
			put("es", "Usar SSL/TLS");
			put("en-US", "Use SSL/TLS");
		}});

		put("administration.configuration.description.administration.backup.email.ssl", new HashMap() {{
			put("pt-BR", "Define se a conexão com o servidor SMTP deve usar SSL/TLS.");
			put("es", "Define si la conexión con el servidor SMTP debe usar SSL/TLS.");
			put("en-US", "Define if the connection to the SMTP server should use SSL/TLS.");
		}});

		put("administration.configuration.title.administration.backup.google_drive.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no Google Drive");
			put("es", "Habilitar backup en Google Drive");
			put("en-US", "Enable backup to Google Drive");
		}});

		put("administration.configuration.description.administration.backup.google_drive.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o Google Drive.");
			put("es", "Define si el backup será enviado a Google Drive.");
			put("en-US", "Define if the backup will be sent to Google Drive.");
		}});

		put("administration.configuration.title.administration.backup.google_drive.client_id", new HashMap() {{
			put("pt-BR", "Google Drive Client ID");
			put("es", "Google Drive Client ID");
			put("en-US", "Google Drive Client ID");
		}});

		put("administration.configuration.description.administration.backup.google_drive.client_id", new HashMap() {{
			put("pt-BR", "Client ID obtido no Google Cloud Console.");
			put("es", "Client ID obtenido en Google Cloud Console.");
			put("en-US", "Client ID obtained from Google Cloud Console.");
		}});

		put("administration.configuration.title.administration.backup.google_drive.client_secret", new HashMap() {{
			put("pt-BR", "Google Drive Client Secret");
			put("es", "Google Drive Client Secret");
			put("en-US", "Google Drive Client Secret");
		}});

		put("administration.configuration.description.administration.backup.google_drive.client_secret", new HashMap() {{
			put("pt-BR", "Client Secret obtido no Google Cloud Console.");
			put("es", "Client Secret obtenido en Google Cloud Console.");
			put("en-US", "Client Secret obtained from Google Cloud Console.");
		}});

		put("administration.configuration.title.administration.backup.google_drive.refresh_token", new HashMap() {{
			put("pt-BR", "Google Drive Refresh Token");
			put("es", "Google Drive Refresh Token");
			put("en-US", "Google Drive Refresh Token");
		}});

		put("administration.configuration.description.administration.backup.google_drive.refresh_token", new HashMap() {{
			put("pt-BR", "Refresh Token para acesso offline ao Google Drive.");
			put("es", "Refresh Token para acceso offline a Google Drive.");
			put("en-US", "Refresh Token for offline access to Google Drive.");
		}});

		put("administration.configuration.title.administration.backup.google_drive.account_email", new HashMap() {{
			put("pt-BR", "Conta Google conectada");
			put("es", "Cuenta Google conectada");
			put("en-US", "Connected Google account");
		}});

		put("administration.configuration.description.administration.backup.google_drive.account_email", new HashMap() {{
			put("pt-BR", "E-mail da conta Google autorizada para receber os backups.");
			put("es", "Correo de la cuenta Google autorizada para recibir los backups.");
			put("en-US", "Email of the Google account authorized to receive backups.");
		}});

		put("administration.configuration.title.administration.backup.onedrive.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no OneDrive");
			put("es", "Habilitar backup en OneDrive");
			put("en-US", "Enable backup to OneDrive");
		}});

		put("administration.configuration.description.administration.backup.onedrive.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o OneDrive.");
			put("es", "Define si el backup será enviado a OneDrive.");
			put("en-US", "Define if the backup will be sent to OneDrive.");
		}});

		put("administration.configuration.title.administration.backup.onedrive.client_id", new HashMap() {{
			put("pt-BR", "OneDrive Client ID");
			put("es", "OneDrive Client ID");
			put("en-US", "OneDrive Client ID");
		}});

		put("administration.configuration.description.administration.backup.onedrive.client_id", new HashMap() {{
			put("pt-BR", "Client ID obtido no Azure Portal.");
			put("es", "Client ID obtenido en Azure Portal.");
			put("en-US", "Client ID obtained from Azure Portal.");
		}});

		put("administration.configuration.title.administration.backup.onedrive.client_secret", new HashMap() {{
			put("pt-BR", "OneDrive Client Secret");
			put("es", "OneDrive Client Secret");
			put("en-US", "OneDrive Client Secret");
		}});

		put("administration.configuration.description.administration.backup.onedrive.client_secret", new HashMap() {{
			put("pt-BR", "Client Secret obtido no Azure Portal.");
			put("es", "Client Secret obtenido en Azure Portal.");
			put("en-US", "Client Secret obtained from Azure Portal.");
		}});

		put("administration.configuration.title.administration.backup.onedrive.refresh_token", new HashMap() {{
			put("pt-BR", "OneDrive Refresh Token");
			put("es", "OneDrive Refresh Token");
			put("en-US", "OneDrive Refresh Token");
		}});

		put("administration.configuration.description.administration.backup.onedrive.refresh_token", new HashMap() {{
			put("pt-BR", "Refresh Token para acesso offline ao OneDrive.");
			put("es", "Refresh Token para acceso offline a OneDrive.");
			put("en-US", "Refresh Token for offline access to OneDrive.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no Dropbox");
			put("es", "Habilitar backup en Dropbox");
			put("en-US", "Enable backup to Dropbox");
		}});

		put("administration.configuration.description.administration.backup.dropbox.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o Dropbox.");
			put("es", "Define si el backup será enviado a Dropbox.");
			put("en-US", "Define if the backup will be sent to Dropbox.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.app_key", new HashMap() {{
			put("pt-BR", "Dropbox App Key");
			put("es", "Dropbox App Key");
			put("en-US", "Dropbox App Key");
		}});

		put("administration.configuration.description.administration.backup.dropbox.app_key", new HashMap() {{
			put("pt-BR", "App Key obtida no Dropbox App Console.");
			put("es", "App Key obtenida en Dropbox App Console.");
			put("en-US", "App Key obtained from Dropbox App Console.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.app_secret", new HashMap() {{
			put("pt-BR", "Dropbox Secret Key");
			put("es", "Dropbox Secret Key");
			put("en-US", "Dropbox Secret Key");
		}});

		put("administration.configuration.description.administration.backup.dropbox.app_secret", new HashMap() {{
			put("pt-BR", "Secret Key obtida no Dropbox App Console.");
			put("es", "Secret Key obtenida en Dropbox App Console.");
			put("en-US", "Secret Key obtained from Dropbox App Console.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.access_token", new HashMap() {{
			put("pt-BR", "Dropbox Access Token");
			put("es", "Dropbox Access Token");
			put("en-US", "Dropbox Access Token");
		}});

		put("administration.configuration.description.administration.backup.dropbox.access_token", new HashMap() {{
			put("pt-BR", "Access Token OAuth salvo automaticamente.");
			put("es", "Access Token OAuth guardado automÃ¡ticamente.");
			put("en-US", "OAuth Access Token saved automatically.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.refresh_token", new HashMap() {{
			put("pt-BR", "Dropbox Refresh Token");
			put("es", "Dropbox Refresh Token");
			put("en-US", "Dropbox Refresh Token");
		}});

		put("administration.configuration.description.administration.backup.dropbox.refresh_token", new HashMap() {{
			put("pt-BR", "Refresh Token OAuth usado para renovar o acesso ao Dropbox.");
			put("es", "Refresh Token OAuth usado para renovar el acceso a Dropbox.");
			put("en-US", "OAuth Refresh Token used to renew Dropbox access.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.access_token_expires_at", new HashMap() {{
			put("pt-BR", "Dropbox Access Token expira em");
			put("es", "Dropbox Access Token expira en");
			put("en-US", "Dropbox Access Token expires at");
		}});

		put("administration.configuration.description.administration.backup.dropbox.access_token_expires_at", new HashMap() {{
			put("pt-BR", "Timestamp de expiraÃ§Ã£o do Access Token salvo automaticamente.");
			put("es", "Timestamp de expiraciÃ³n del Access Token guardado automÃ¡ticamente.");
			put("en-US", "Access Token expiration timestamp saved automatically.");
		}});

		put("administration.configuration.title.administration.backup.dropbox.account_email", new HashMap() {{
			put("pt-BR", "Conta Dropbox conectada");
			put("es", "Cuenta Dropbox conectada");
			put("en-US", "Connected Dropbox account");
		}});

		put("administration.configuration.description.administration.backup.dropbox.account_email", new HashMap() {{
			put("pt-BR", "E-mail da conta Dropbox autorizada para receber os backups.");
			put("es", "Correo de la cuenta Dropbox autorizada para recibir los backups.");
			put("en-US", "Email of the Dropbox account authorized to receive backups.");
		}});

		put("administration.configuration.dropbox.oauth.status.opening", new HashMap() {{
			put("pt-BR", "Abrindo autenticaÃ§Ã£o do Dropbox...");
			put("es", "Abriendo autenticaciÃ³n de Dropbox...");
			put("en-US", "Opening Dropbox authentication...");
		}});

		put("administration.configuration.dropbox.oauth.status.waiting", new HashMap() {{
			put("pt-BR", "Aguardando autorizaÃ§Ã£o no Dropbox...");
			put("es", "Esperando autorizaciÃ³n en Dropbox...");
			put("en-US", "Waiting for authorization in Dropbox...");
		}});

		put("administration.configuration.dropbox.oauth.status.exchanging", new HashMap() {{
			put("pt-BR", "Finalizando conexÃ£o com Dropbox...");
			put("es", "Finalizando conexiÃ³n con Dropbox...");
			put("en-US", "Finishing Dropbox connection...");
		}});

		put("administration.configuration.dropbox.oauth.status.connected", new HashMap() {{
			put("pt-BR", "Conta Dropbox conectada com sucesso. Clique em Salvar para persistir outras alteraÃ§Ãµes da pÃ¡gina.");
			put("es", "Cuenta Dropbox conectada con Ã©xito. Haga clic en Guardar para persistir otros cambios de la pÃ¡gina.");
			put("en-US", "Dropbox account connected successfully. Click Save to persist other page changes.");
		}});

		put("administration.configuration.dropbox.oauth.status.cancelled", new HashMap() {{
			put("pt-BR", "A janela de autenticaÃ§Ã£o foi fechada antes de finalizar.");
			put("es", "La ventana de autenticaciÃ³n se cerrÃ³ antes de finalizar.");
			put("en-US", "The authentication window was closed before finishing.");
		}});

		put("administration.configuration.dropbox.oauth.status.error", new HashMap() {{
			put("pt-BR", "NÃ£o foi possÃ­vel concluir a autenticaÃ§Ã£o do Dropbox.");
			put("es", "No fue posible completar la autenticaciÃ³n de Dropbox.");
			put("en-US", "Could not complete Dropbox authentication.");
		}});

		put("administration.configuration.dropbox.oauth.error.popup_blocked", new HashMap() {{
			put("pt-BR", "O navegador bloqueou a janela de autenticaÃ§Ã£o. Libere pop-ups para continuar.");
			put("es", "El navegador bloqueÃ³ la ventana de autenticaciÃ³n. Permita pop-ups para continuar.");
			put("en-US", "The browser blocked the authentication window. Allow pop-ups to continue.");
		}});

		put("administration.configuration.dropbox.oauth.error.user_denied", new HashMap() {{
			put("pt-BR", "A autorizaÃ§Ã£o do Dropbox foi cancelada.");
			put("es", "La autorizaciÃ³n de Dropbox fue cancelada.");
			put("en-US", "Dropbox authorization was cancelled.");
		}});

		put("administration.configuration.dropbox.oauth.error.missing_credentials", new HashMap() {{
			put("pt-BR", "App Key e Secret Key sÃ£o obrigatÃ³rias para autenticar no Dropbox.");
			put("es", "App Key y Secret Key son obligatorias para autenticar en Dropbox.");
			put("en-US", "App Key and Secret Key are required to authenticate with Dropbox.");
		}});

		put("administration.configuration.dropbox.oauth.error.invalid_state", new HashMap() {{
			put("pt-BR", "SessÃ£o de autenticaÃ§Ã£o expirada ou invÃ¡lida. Tente novamente.");
			put("es", "SesiÃ³n de autenticaciÃ³n expirada o invÃ¡lida. Intente nuevamente.");
			put("en-US", "Authentication session expired or invalid. Please try again.");
		}});

		put("administration.configuration.dropbox.oauth.error.missing_refresh_token", new HashMap() {{
			put("pt-BR", "O Dropbox nÃ£o retornou Refresh Token. Conecte novamente solicitando acesso offline.");
			put("es", "Dropbox no retornÃ³ Refresh Token. Conecte nuevamente solicitando acceso offline.");
			put("en-US", "Dropbox did not return a Refresh Token. Connect again requesting offline access.");
		}});

		put("administration.configuration.dropbox.oauth.error.auth_url_failed", new HashMap() {{
			put("pt-BR", "NÃ£o foi possÃ­vel gerar a URL de autenticaÃ§Ã£o do Dropbox.");
			put("es", "No fue posible generar la URL de autenticaciÃ³n de Dropbox.");
			put("en-US", "Failed to generate Dropbox authentication URL.");
		}});

		put("administration.configuration.dropbox.oauth.error.exchange_failed", new HashMap() {{
			put("pt-BR", "Falha ao finalizar a autenticaÃ§Ã£o do Dropbox.");
			put("es", "Error al finalizar la autenticaciÃ³n de Dropbox.");
			put("en-US", "Failed to finish Dropbox authentication.");
		}});

		put("administration.configuration.title.administration.backup.box.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no Box");
			put("es", "Habilitar backup en Box");
			put("en-US", "Enable backup to Box");
		}});

		put("administration.configuration.description.administration.backup.box.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o Box.");
			put("es", "Define si el backup será enviado a Box.");
			put("en-US", "Define if the backup will be sent to Box.");
		}});

		put("administration.configuration.title.administration.backup.box.client_id", new HashMap() {{
			put("pt-BR", "Box Client ID");
			put("es", "Box Client ID");
			put("en-US", "Box Client ID");
		}});

		put("administration.configuration.description.administration.backup.box.client_id", new HashMap() {{
			put("pt-BR", "Client ID do aplicativo OAuth no Box Developer Console.");
			put("es", "Client ID de la aplicaciÃ³n OAuth en Box Developer Console.");
			put("en-US", "OAuth application Client ID from Box Developer Console.");
		}});

		put("administration.configuration.title.administration.backup.box.client_secret", new HashMap() {{
			put("pt-BR", "Box Client Secret");
			put("es", "Box Client Secret");
			put("en-US", "Box Client Secret");
		}});

		put("administration.configuration.description.administration.backup.box.client_secret", new HashMap() {{
			put("pt-BR", "Client Secret do aplicativo OAuth no Box Developer Console.");
			put("es", "Client Secret de la aplicaciÃ³n OAuth en Box Developer Console.");
			put("en-US", "OAuth application Client Secret from Box Developer Console.");
		}});

		put("administration.configuration.title.administration.backup.box.access_token", new HashMap() {{
			put("pt-BR", "Box Access Token / Developer Token");
			put("es", "Box Access Token / Developer Token");
			put("en-US", "Box Access Token / Developer Token");
		}});

		put("administration.configuration.description.administration.backup.box.access_token", new HashMap() {{
			put("pt-BR", "Access Token OAuth salvo automaticamente ou Developer Token temporÃ¡rio para testes.");
			put("es", "Access Token OAuth guardado automÃ¡ticamente o Developer Token temporal para pruebas.");
			put("en-US", "OAuth Access Token saved automatically or temporary Developer Token for tests.");
		}});

		put("administration.configuration.title.administration.backup.box.refresh_token", new HashMap() {{
			put("pt-BR", "Box Refresh Token");
			put("es", "Box Refresh Token");
			put("en-US", "Box Refresh Token");
		}});

		put("administration.configuration.description.administration.backup.box.refresh_token", new HashMap() {{
			put("pt-BR", "Refresh Token OAuth do Box usado para renovar o acesso.");
			put("es", "Refresh Token OAuth de Box usado para renovar el acceso.");
			put("en-US", "Box OAuth Refresh Token used to renew access.");
		}});

		put("administration.configuration.title.administration.backup.box.folder_id", new HashMap() {{
			put("pt-BR", "Box Folder ID");
			put("es", "Box Folder ID");
			put("en-US", "Box Folder ID");
		}});

		put("administration.configuration.description.administration.backup.box.folder_id", new HashMap() {{
			put("pt-BR", "ID da pasta de destino no Box (0 para raiz).");
			put("es", "ID de la carpeta de destino en Box (0 para raíz).");
			put("en-US", "Destination folder ID in Box (0 for root).");
		}});

		put("administration.configuration.title.administration.backup.pcloud.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no pCloud");
			put("es", "Habilitar backup en pCloud");
			put("en-US", "Enable backup to pCloud");
		}});

		put("administration.configuration.description.administration.backup.pcloud.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o pCloud.");
			put("es", "Define si el backup será enviado a pCloud.");
			put("en-US", "Define if the backup will be sent to pCloud.");
		}});

		put("administration.configuration.title.administration.backup.pcloud.auth_token", new HashMap() {{
			put("pt-BR", "pCloud Auth Token");
			put("es", "pCloud Auth Token");
			put("en-US", "pCloud Auth Token");
		}});

		put("administration.configuration.description.administration.backup.pcloud.auth_token", new HashMap() {{
			put("pt-BR", "Token de autenticação obtido no pCloud.");
			put("es", "Token de autenticación obtenido en pCloud.");
			put("en-US", "Authentication token obtained from pCloud.");
		}});

		put("administration.configuration.title.administration.backup.pcloud.path", new HashMap() {{
			put("pt-BR", "pCloud Caminho");
			put("es", "Ruta de pCloud");
			put("en-US", "pCloud Path");
		}});

		put("administration.configuration.description.administration.backup.pcloud.path", new HashMap() {{
			put("pt-BR", "Caminho remoto no pCloud (ex: /Backups).");
			put("es", "Ruta remota en pCloud (ej: /Backups).");
			put("en-US", "Remote path in pCloud (e.g. /Backups).");
		}});

		put("administration.configuration.title.administration.backup.mega.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no MEGA");
			put("es", "Habilitar backup en MEGA");
			put("en-US", "Enable backup to MEGA");
		}});

		put("administration.configuration.description.administration.backup.mega.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o MEGA.");
			put("es", "Define si el backup será enviado a MEGA.");
			put("en-US", "Define if the backup will be sent to MEGA.");
		}});

		put("administration.configuration.title.administration.backup.mega.cmd_path", new HashMap() {{
			put("pt-BR", "Caminho do MEGAcmd");
			put("es", "Ruta de MEGAcmd");
			put("en-US", "MEGAcmd Path");
		}});

		put("administration.configuration.description.administration.backup.mega.cmd_path", new HashMap() {{
			put("pt-BR", "Diretório dos comandos mega-login/mega-put/mega-get.");
			put("es", "Directorio de mega-login/mega-put/mega-get.");
			put("en-US", "Directory containing mega-login/mega-put/mega-get.");
		}});

		put("administration.configuration.title.administration.backup.mega.email", new HashMap() {{
			put("pt-BR", "MEGA E-mail");
			put("es", "MEGA E-mail");
			put("en-US", "MEGA Email");
		}});

		put("administration.configuration.description.administration.backup.mega.email", new HashMap() {{
			put("pt-BR", "E-mail de acesso ao MEGA.");
			put("es", "E-mail de acceso a MEGA.");
			put("en-US", "MEGA account email.");
		}});

		put("administration.configuration.title.administration.backup.mega.password", new HashMap() {{
			put("pt-BR", "MEGA Senha");
			put("es", "MEGA Contraseña");
			put("en-US", "MEGA Password");
		}});

		put("administration.configuration.description.administration.backup.mega.password", new HashMap() {{
			put("pt-BR", "Senha de acesso ao MEGA.");
			put("es", "Contraseña de acceso a MEGA.");
			put("en-US", "MEGA account password.");
		}});

		put("administration.configuration.title.administration.backup.mega.remote_path", new HashMap() {{
			put("pt-BR", "MEGA Caminho Remoto");
			put("es", "Ruta remota MEGA");
			put("en-US", "MEGA Remote Path");
		}});

		put("administration.configuration.description.administration.backup.mega.remote_path", new HashMap() {{
			put("pt-BR", "Caminho remoto no MEGA (ex: /Backups).");
			put("es", "Ruta remota en MEGA (ej: /Backups).");
			put("en-US", "Remote path in MEGA (e.g. /Backups).");
		}});

		put("administration.configuration.title.administration.backup.proton.enabled", new HashMap() {{
			put("pt-BR", "Habilitar backup no Proton Drive");
			put("es", "Habilitar backup en Proton Drive");
			put("en-US", "Enable backup to Proton Drive");
		}});

		put("administration.configuration.description.administration.backup.proton.enabled", new HashMap() {{
			put("pt-BR", "Define se o backup será enviado para o Proton Drive.");
			put("es", "Define si el backup será enviado a Proton Drive.");
			put("en-US", "Define if the backup will be sent to Proton Drive.");
		}});

		put("administration.configuration.title.administration.backup.proton.rclone_path", new HashMap() {{
			put("pt-BR", "Caminho do rclone");
			put("es", "Ruta del rclone");
			put("en-US", "rclone Path");
		}});

		put("administration.configuration.description.administration.backup.proton.rclone_path", new HashMap() {{
			put("pt-BR", "Caminho do executável rclone configurado com Proton Drive.");
			put("es", "Ruta del ejecutable rclone configurado con Proton Drive.");
			put("en-US", "Path to the rclone executable configured with Proton Drive.");
		}});

		put("administration.configuration.title.administration.backup.proton.rclone_remote", new HashMap() {{
			put("pt-BR", "rclone Remote");
			put("es", "rclone Remote");
			put("en-US", "rclone Remote");
		}});

		put("administration.configuration.description.administration.backup.proton.rclone_remote", new HashMap() {{
			put("pt-BR", "Nome do remote rclone (ex: proton:Backups).");
			put("es", "Nombre del remote rclone (ej: proton:Backups).");
			put("en-US", "rclone remote name (e.g. proton:Backups).");
		}});

		put("administration.configuration.cloud.selector.label", new HashMap() {{
			put("pt-BR", "Serviço de nuvem");
			put("es", "Servicio de nube");
			put("en-US", "Cloud service");
		}});

		put("administration.configuration.cloud.selector.placeholder", new HashMap() {{
			put("pt-BR", "Selecione um serviço");
			put("es", "Seleccione un servicio");
			put("en-US", "Select a service");
		}});

		put("administration.setup.biblivre4restore_cloud.description", new HashMap() {{
			put("pt-BR", "Você também pode restaurar um backup armazenado em nuvem.");
			put("es", "Usted también puede restaurar un backup almacenado en la nube.");
			put("en-US", "You can also restore a backup stored in the cloud.");
		}});

		put("administration.setup.biblivre4restore_cloud.field.service", new HashMap() {{
			put("pt-BR", "Serviço de nuvem");
			put("es", "Servicio de nube");
			put("en-US", "Cloud service");
		}});

		put("administration.setup.biblivre4restore_cloud.field.filename", new HashMap() {{
			put("pt-BR", "Nome do arquivo de backup");
			put("es", "Nombre del archivo de backup");
			put("en-US", "Backup file name");
		}});

		put("administration.setup.biblivre4restore_cloud.button", new HashMap() {{
			put("pt-BR", "Restaurar backup da nuvem");
			put("es", "Restaurar backup desde la nube");
			put("en-US", "Restore backup from cloud");
		}});

		put("administration.setup.biblivre4restore_cloud.select_file", new HashMap() {{
			put("pt-BR", "Informe o nome do arquivo de backup");
			put("es", "Informe el nombre del archivo de backup");
			put("en-US", "Enter the backup file name");
		}});

		put("administration.setup.biblivre4restore_cloud.error", new HashMap() {{
			put("pt-BR", "Erro ao restaurar backup da nuvem");
			put("es", "Error al restaurar backup desde la nube");
			put("en-US", "Cloud backup restore error");
		}});

		put("administration.setup.biblivre4restore_cloud.error.description", new HashMap() {{
			put("pt-BR", "Não foi possível baixar o backup da nuvem. Verifique as configurações do serviço e o nome do arquivo.");
			put("es", "No fue posible descargar el backup desde la nube. Verifique la configuración del servicio y el nombre del archivo.");
			put("en-US", "Could not download the backup from cloud. Check the service settings and file name.");
		}});

		put("administration.setup.biblivre4restore_cloud.confirm_title", new HashMap() {{
			put("pt-BR", "Restaurar Backup da Nuvem");
			put("es", "Restaurar Backup de la Nube");
			put("en-US", "Restore Cloud Backup");
		}});

		put("administration.setup.biblivre4restore_cloud.confirm_description", new HashMap() {{
			put("pt-BR", "Você realmente deseja restaurar este backup da nuvem?");
			put("es", "¿Usted realmente desea restaurar este backup de la nube?");
			put("en-US", "Do you really wish to restore this cloud backup?");
		}});

		put("administration.setup.biblivre4restore_cloud.select_placeholder", new HashMap() {{
			put("pt-BR", "Selecione um backup");
			put("es", "Seleccione un backup");
			put("en-US", "Select a backup");
		}});

		put("administration.setup.biblivre4restore_cloud.button.refresh_list", new HashMap() {{
			put("pt-BR", "Atualizar lista");
			put("es", "Actualizar lista");
			put("en-US", "Refresh list");
		}});

		put("administration.setup.biblivre4restore_cloud.button.prev", new HashMap() {{
			put("pt-BR", "Anterior");
			put("es", "Anterior");
			put("en-US", "Previous");
		}});

		put("administration.setup.biblivre4restore_cloud.button.next", new HashMap() {{
			put("pt-BR", "Próxima");
			put("es", "Siguiente");
			put("en-US", "Next");
		}});

		put("administration.setup.biblivre4restore_cloud.page", new HashMap() {{
			put("pt-BR", "Página");
			put("es", "Página");
			put("en-US", "Page");
		}});

		put("administration.configuration.title.general.document.format.pdf", new HashMap() {{
			put("pt-BR", "Configuração de formato de documento");
			put("es", "Configuración del formato del documento");
			put("en-US", "Document Format Settings");
		}});
		
		put("administration.configuration.description.general.document.format.pdf", new HashMap() {{
			put("pt-BR", "Escolha qual formato de documento será usado para impressão de carteirinhas e etiquetas.");
			put("es", "Elija qué formato de documento se utilizará para imprimir tarjetas de identificación y etiquetas.");
			put("en-US", "Choose which document format will be used for printing ID cards and labels.");
		}});
		
		put("cataloging.reservation.error.onhold", new HashMap() {{
			put("pt-BR", "Este exemplar já se encontra reservado.");
			put("es", "Este ejemplar ya está reservado.");
			put("en-US", "This item is already on hold.");
		}});	
		
		
	}};
		

	
}
	
	
