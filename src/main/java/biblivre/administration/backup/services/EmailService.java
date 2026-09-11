package biblivre.administration.backup.services;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//import org.apache.log4j.Logger;

import biblivre.core.configurations.Configurations;
import biblivre.core.utils.Constants;

public class EmailService {
	//private static final Logger logger = Logger.getLogger(EmailService.class);
	private static final long MAX_ATTACHMENT_SIZE = 25 * 1024 * 1024; // 25 MB
	private static final String IMPLICIT_SSL_PORT = "465";
	private static final String SMTP_TIMEOUT = "60000";

	public static void sendBackup(String schema, File backupFile) throws Exception {
		System.out.println("=== Iniciando envio de backup por e-mail para schema: " + schema);
		System.out.println("Arquivo: " + backupFile.getAbsolutePath() + " (tamanho: " + backupFile.length() + " bytes)");
		
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_EMAIL_ENABLED)) {
			System.out.println("Envio de e-mail DESABILITADO nas configurações.");
			return;
		}

		if (backupFile.length() > MAX_ATTACHMENT_SIZE) {
			String error = "Arquivo de backup muito grande para e-mail (max 25MB). Tamanho atual: " + (backupFile.length() / 1024 / 1024) + " MB";
			System.out.println(error);
			throw new Exception(error);
		}

		final String host = Configurations.getString(schema, Constants.CONFIG_BACKUP_EMAIL_HOST);
		final String port = trim(Configurations.getString(schema, Constants.CONFIG_BACKUP_EMAIL_PORT));
		final String user = Configurations.getString(schema, Constants.CONFIG_BACKUP_EMAIL_USER);
		final String password = Configurations.getString(schema, Constants.CONFIG_BACKUP_EMAIL_PASSWORD);
		final String from = Configurations.getString(schema, Constants.CONFIG_BACKUP_EMAIL_FROM);
		final String to = Configurations.getString(schema, Constants.CONFIG_BACKUP_EMAIL_TO);
		final boolean ssl = Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_EMAIL_SSL);

		// Log de todas as configurações (para depuração)
		System.out.println("=== Configurações carregadas:");
		System.out.println("  Host: " + host);
		System.out.println("  Porta: " + port);
		System.out.println("  Usuário: " + user);
		System.out.println("  De: " + from);
		System.out.println("  Para: " + to);
		System.out.println("  SSL habilitado: " + ssl);

		try {
			int smtpPort = Integer.parseInt(port);
			boolean implicitSsl = isImplicitSslPort(port);
			System.out.println("  SSL implícito (porta 465): " + implicitSsl);

			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.connectiontimeout", SMTP_TIMEOUT);
			props.put("mail.smtp.timeout", SMTP_TIMEOUT);
			props.put("mail.smtp.writetimeout", SMTP_TIMEOUT);

			if (implicitSsl) {
				System.out.println("  Usando SSL implícito");
				props.put("mail.smtp.ssl.enable", "true");
				props.put("mail.smtp.socketFactory.port", port);
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.put("mail.smtp.socketFactory.fallback", "false");
			} else if (ssl) {
				System.out.println("  Usando STARTTLS");
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.starttls.required", "true");
			} else {
				System.out.println("  Sem criptografia");
			}

			//System.out.println("Criando sessão SMTP com debug ativado...");
			Session session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			});
			session.setDebug(false); // true Ativa debug detalhado!
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("Biblivre Backup - " + backupFile.getName());

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText("Segue em anexo o backup do Biblivre.");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(backupFile);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(backupFile.getName());
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);
			message.saveChanges();
			System.out.println("Mensagem criada com sucesso!");

			Transport transport = null;
			try {
				System.out.println("Obtendo transporte SMTP...");
				transport = session.getTransport("smtp");
				System.out.println("Conectando ao servidor " + host + ":" + smtpPort + "...");
				transport.connect(host, smtpPort, user, password);
				System.out.println("Conectado! Enviando mensagem...");
				transport.sendMessage(message, message.getAllRecipients());
				System.out.println("=== E-MAIL ENVIADO COM SUCESSO! ===");
				//logger.info("Backup enviado por e-mail com sucesso: " + backupFile.getName());
				System.out.println("Backup enviado por e-mail com sucesso: " + backupFile.getName());
			} finally {
				closeTransport(transport);
			}
		} catch (Exception e) {
			String errorMsg = "ERRO ao enviar backup por e-mail usando servidor SMTP " + host + ":" + port
					+ " de " + from + " para " + to;
			System.out.println("\n" + errorMsg);
			System.out.println("Mensagem de erro: " + e.getMessage());
			System.out.println("Stack trace:");
			e.printStackTrace();
			//logger.error(errorMsg, e);
			System.out.println(errorMsg + e);
			throw e;
		}
	}

	private static boolean isImplicitSslPort(String port) {
		return IMPLICIT_SSL_PORT.equals(port != null ? port.trim() : "");
	}

	private static String trim(String value) {
		return value != null ? value.trim() : "";
	}

	private static void closeTransport(Transport transport) {
		if (transport == null) {
			return;
		}

		try {
			transport.close();
		} catch (Exception e) {
			System.out.println("Erro ao fechar transporte SMTP: " + e.getMessage());
			e.printStackTrace();
			//logger.error("Erro ao fechar transporte SMTP", e);
			System.out.println("Erro ao fechar transporte SMTP: " + e.getMessage());
		}
	}
}
