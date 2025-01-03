package biblivre.update.v5_1_31;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biblivre.core.exceptions.DAOException;
import biblivre.core.translations.Translations;
import biblivre.update.UpdateService;
import biblivre.z3950.Z3950AddressDTO;
import biblivre.z3950.Z3950DAO;

public class Update implements UpdateService {

	public void doUpdate(Connection connection) throws SQLException {
		_addTranslations();
		//insertNewsZ3950Addresses(connection);
	}

	@Override
	public String getVersion() {
		return "5.1.31";
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
		put("administration.setup.restore.form", new HashMap() {{
			put("pt-BR", "Restaurar Formulário Catalográfico");
			put("es", "Restaurar Formulario de Catalogación");
			put("en-US", "Restore Cataloging Form");
		}});
		
		put("administration.setup.restore.form.description", new HashMap() {{
			put("pt-BR", "O formulário catalográfico será restaurado ao seu estado original, portanto, caso haja customização do formulário, ela será substituída.");
			put("es", "El formulario del catálogo será restaurado a su estado original, por lo que si hay alguna personalización del formulario, será reemplazado.");
			put("en-US", "The cataloging form will be restored to its original state, so if there is any customization of the form, it will be replaced.");
		}});
		
		put("administration.setup.restore.form.button", new HashMap() {{
			put("pt-BR", "Restaurar todos os campos de formulário");
			put("es", "Restaurar todos los campos del formulario");
			put("en-US", "Restore all form fields");
		}});
		
		put("administration.setup.restore.form.confirm", new HashMap() {{
			put("pt-BR", "Deseja restaurar formulário catalográfico ?");
			put("es", "¿Quieres restaurar el formulario del catálogo?");
			put("en-US", "Do you want to restore catalog form ?");
		}});
		
		put("menu.help_manual4", new HashMap() {{
			put("pt-BR", "Manual do Biblivre 4");
			put("es", "Manual del Biblivre 4");
			put("en-US", "Biblivre Manual 4");
		}});
		
		put("menu.help_manual5", new HashMap() {{
			put("pt-BR", "Manual do Biblivre 5");
			put("es", "Manual del Biblivre 5");
			put("en-US", "Biblivre Manual 5");
		}});		
		
		put("multi_schema.manage.new_schema.button.back", new HashMap() {{
			put("pt-BR", "Voltar");
			put("es", "Para volver");
			put("en-US", "To go back");
		}});
		
		
		
	}};
		
	
	public void insertNewsZ3950Addresses(Connection con) throws SQLException{
		
			List<String> zlist= addZ3950();//Recebe a lista de novos servidores Z3950
			List<Z3950AddressDTO> addressesList = new Z3950DAO().getZ3950Servers(con);//Pega os servidores z3950 da tabela	
			
			if(!addressesList.isEmpty()) {
				new Z3950DAO().deleteAllz3950(con);
			}
			
			for(String newZ3950server: zlist) {//Novos servidores z3950
			
					try (PreparedStatement pstz3950 = con.prepareStatement(newZ3950server)) {
						pstz3950.executeUpdate();
						con.commit();
						
					} catch (SQLException e) {						
						e.printStackTrace();
					}			
			}	
			
		  new Z3950DAO().insertDTOList(con,addressesList);//servidores z3950 que já estavam na tabela


	}
	
	

	
	private List<String> addZ3950() {
				
			List<String> z3950 = new ArrayList<String>();
			/* Aguardando voltar a funcionar
			z3950.add("INSERT INTO single.z3950_addresses (id, name, url, port, collection) "
					+ "VALUES ('1','3 - BIBLIOTECA NACIONAL (BIBLIOGRÁFICO)','152.70.215.55','9998','bib');");
			z3950.add("INSERT INTO single.z3950_addresses (id, name, url, port, collection) "
					  + "VALUES ('2','4 - BIBLIOTECA NACIONAL (AUTORIDADE)','152.70.215.55','9998','aut');"); */
			z3950.add("INSERT INTO single.z3950_addresses (id, name, url, port, collection) "
					  + "VALUES ('3','1 - Universidade Federal de Santa Catarina (UFSC)','z3950.ufsc.br','210','Default');");
			z3950.add("INSERT INTO single.z3950_addresses (id, name, url, port, collection) "
					  + "VALUES ('4','2 - Biblioteca do Senado','biblioteca2.senado.gov.br','9991','sen01');");			
			
			
	  return z3950;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/*
		private void _updateZ3950Address(Connection connection, String name, String url)
		throws SQLException {

		String sql = "UPDATE z3950_addresses SET url = ? WHERE name = ?;";

		try (PreparedStatement pst = connection.prepareStatement(sql)) {
			PreparedStatementUtil.setAllParameters(pst, url, name);

			pst.execute();
		}
	} * 
	 		try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
			statement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 */
	
	
	
	
	
	
	
	
}
