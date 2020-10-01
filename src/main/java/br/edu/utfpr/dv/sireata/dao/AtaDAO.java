package br.edu.utfpr.dv.sireata.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.dv.sireata.model.Ata;
import br.edu.utfpr.dv.sireata.model.Ata.TipoAta;
import br.edu.utfpr.dv.sireata.util.DateUtils;

public class AtaDAO {

	Connection conn = null;
	PreparedStatement sstmt = null;
	Statement stmt = null;
	ResultSet rs = null;
	
	public Ata buscarPorId(int id) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			sstmt = conn.prepareStatement(
						"SELECT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
						"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
						"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
						"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
						"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
						"WHERE idAta = ?");
			
			sstmt.setInt(1, id);
			
			rs = sstmt.executeQuery();
			
			if(rs.next()){
				return this.carregarObjeto(rs);
			}else{
				return null;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((sstmt != null) && !sstmt.isClosed())
				sstmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public Ata buscarPorNumero(int idOrgao, TipoAta tipo, int numero, int ano) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			sstmt = conn.prepareStatement(
						"SELECT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
						"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
						"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
						"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
						"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
						"WHERE atas.publicada = 1 AND atas.idOrgao = ? AND atas.tipo = ? AND atas.numero = ? AND YEAR(atas.data) = ?");

			sstmt.setInt(1, idOrgao);
			sstmt.setInt(2, tipo.getValue());
			sstmt.setInt(3, numero);
			sstmt.setInt(4, ano);
			
			rs = sstmt.executeQuery();
			
			if(rs.next()){
				return this.carregarObjeto(rs);
			}else{
				return null;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((sstmt != null) && !sstmt.isClosed())
				sstmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public Ata buscarPorPauta(int idPauta) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			sstmt = conn.prepareStatement(
				"SELECT DISTINCT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"INNER JOIN pautas ON pautas.idAta=atas.idAta " +
				"WHERE pautas.idPauta = ?");

			sstmt.setInt(1, idPauta);
			
			rs = sstmt.executeQuery();
			
			if(rs.next()){
				return this.carregarObjeto(rs);
			}else{
				return null;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((sstmt != null) && !sstmt.isClosed())
				sstmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public int buscarProximoNumeroAta(int idOrgao, int ano, TipoAta tipo) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			sstmt = conn.prepareStatement(
				"SELECT MAX(numero) AS numero FROM atas WHERE idOrgao = ? AND YEAR(data) = ? AND tipo = ?");

			sstmt.setInt(1, idOrgao);
			sstmt.setInt(2, ano);
			sstmt.setInt(3, tipo.getValue());
			
			rs = sstmt.executeQuery();
			
			if(rs.next()){
				return rs.getInt("numero") + 1;
			}else{
				return 1;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((sstmt != null) && !sstmt.isClosed())
				sstmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listar(int idUsuario, int idCampus, int idDepartamento, int idOrgao, boolean publicadas) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = stmt.executeQuery("SELECT DISTINCT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
					"FROM atas INNER JOIN ataparticipantes ON ataparticipantes.idAta=atas.idAta " +
					"INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
					"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
					"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
					"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
					"WHERE ataparticipantes.idUsuario = " + String.valueOf(idUsuario) +
					" AND atas.publicada = " + (publicadas ? "1 " : "0 ") +
					(idCampus > 0 ? " AND departamentos.idCampus = " + String.valueOf(idCampus) : "") +
					(idDepartamento > 0 ? " AND departamentos.idDepartamento = " + String.valueOf(idDepartamento) : "") +
					(idOrgao > 0 ? " AND atas.idOrgao = " + String.valueOf(idOrgao) : "") +
					"ORDER BY atas.data DESC");
			
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPublicadas() throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"WHERE atas.publicada=1 ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPorOrgao(int idOrgao) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"WHERE atas.publicada=1 AND atas.idOrgao=" + String.valueOf(idOrgao) + " ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPorDepartamento(int idDepartamento) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"WHERE atas.publicada=1 AND Orgaos.idDepartamento=" + String.valueOf(idDepartamento) + " ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPorCampus(int idCampus) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"WHERE atas.publicada=1 AND departamentos.idCampus=" + String.valueOf(idCampus) + " ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarNaoPublicadas(int idUsuario) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT DISTINCT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"INNER JOIN ataparticipantes ON ataparticipantes.idAta=atas.idAta " +
				"WHERE atas.publicada=0 AND ataparticipantes.idUsuario=" + String.valueOf(idUsuario) +" ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPorOrgao(int idOrgao, int idUsuario) throws SQLException{
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT DISTINCT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"INNER JOIN ataparticipantes ON ataparticipantes.idAta=atas.idAta " +
				"WHERE atas.publicada=0 AND ataparticipantes.idUsuario=" + String.valueOf(idUsuario) + " AND atas.idOrgao=" + String.valueOf(idOrgao) + " ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPorDepartamento(int idDepartamento, int idUsuario) throws SQLException{
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT DISTINCT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"INNER JOIN ataparticipantes ON ataparticipantes.idAta=atas.idAta " +
				"WHERE atas.publicada=0 AND ataparticipantes.idUsuario=" + String.valueOf(idUsuario) + " AND Orgaos.idDepartamento=" + String.valueOf(idDepartamento) + " ORDER BY atas.data DESC");
			
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public List<Ata> listarPorCampus(int idCampus, int idUsuario) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT DISTINCT atas.*, orgaos.nome AS orgao, p.nome AS presidente, s.nome AS secretario " +
				"FROM atas INNER JOIN orgaos ON orgaos.idOrgao=atas.idOrgao " +
				"INNER JOIN departamentos ON departamentos.idDepartamento=orgaos.idDepartamento " +
				"INNER JOIN usuarios p ON p.idUsuario=atas.idPresidente " +
				"INNER JOIN usuarios s ON s.idUsuario=atas.idSecretario " +
				"INNER JOIN ataparticipantes ON ataparticipantes.idAta=atas.idAta " +
				"WHERE atas.publicada=0 AND ataparticipantes.idUsuario=" + String.valueOf(idUsuario) + " AND departamentos.idCampus=" + String.valueOf(idCampus) + " ORDER BY atas.data DESC");
		
			List<Ata> list = new ArrayList<Ata>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public int salvar(Ata ata) throws SQLException{
		boolean insert = (ata.getIdAta() == 0);
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			
			if(insert){
				sstmt = conn.prepareStatement("INSERT INTO atas(idOrgao, idPresidente, idSecretario, tipo, numero, data, local, localCompleto, dataLimiteComentarios, consideracoesIniciais, audio, documento, publicada, dataPublicacao, aceitarComentarios) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, 0, NULL, 0)", Statement.RETURN_GENERATED_KEYS);
			}else{
				sstmt = conn.prepareStatement("UPDATE atas SET idOrgao=?, idPresidente=?, idSecretario=?, tipo=?, numero=?, data=?, local=?, localCompleto=?, dataLimiteComentarios=?, consideracoesIniciais=?, audio=? WHERE idAta=?");
			}

			sstmt.setInt(1, ata.getOrgao().getIdOrgao());
			sstmt.setInt(2, ata.getPresidente().getIdUsuario());
			sstmt.setInt(3, ata.getSecretario().getIdUsuario());
			sstmt.setInt(4, ata.getTipo().getValue());
			sstmt.setInt(5, ata.getNumero());
			sstmt.setTimestamp(6, new java.sql.Timestamp(ata.getData().getTime()));
			sstmt.setString(7, ata.getLocal());
			sstmt.setString(8, ata.getLocalCompleto());
			sstmt.setDate(9, new java.sql.Date(ata.getDataLimiteComentarios().getTime()));
			sstmt.setString(10, ata.getConsideracoesIniciais());
			if(ata.getAudio() == null){
				sstmt.setNull(11, Types.BINARY);
			}else{
				sstmt.setBytes(11, ata.getAudio());
			}
			
			if(!insert){
				sstmt.setInt(12, ata.getIdAta());
			}

			sstmt.execute();
			
			if(insert){
				rs = sstmt.getGeneratedKeys();
				
				if(rs.next()){
					ata.setIdAta(rs.getInt(1));
				}
			}
			
			return ata.getIdAta();
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((sstmt != null) && !sstmt.isClosed())
				sstmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public void publicar(int idAta, byte[] documento) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			sstmt = conn.prepareStatement("UPDATE atas SET documento=?, dataPublicacao=?, publicada=1, aceitarComentarios=0 WHERE publicada=0 AND idAta=?");

			sstmt.setBytes(1, documento);
			sstmt.setTimestamp(2, new java.sql.Timestamp(DateUtils.getNow().getTime().getTime()));
			sstmt.setInt(3, idAta);

			sstmt.execute();
		}finally{
			if((sstmt != null) && !sstmt.isClosed())
				sstmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public void liberarComentarios(int idAta) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			stmt.execute("UPDATE atas SET aceitarComentarios=1 WHERE publicada=0 AND idAta=" + String.valueOf(idAta));
		}finally{
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public void bloquearComentarios(int idAta) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			stmt.execute("UPDATE atas SET aceitarComentarios=0 WHERE idAta=" + String.valueOf(idAta));
		}finally{
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	private Ata carregarObjeto(ResultSet rs) throws SQLException{
		Ata ata = new Ata();
		
		ata.setIdAta(rs.getInt("idAta"));
		ata.getOrgao().setIdOrgao(rs.getInt("idOrgao"));
		ata.getOrgao().setNome(rs.getString("orgao"));
		ata.getPresidente().setIdUsuario(rs.getInt("idPresidente"));
		ata.getPresidente().setNome(rs.getString("presidente"));
		ata.getSecretario().setIdUsuario(rs.getInt("idSecretario"));
		ata.getSecretario().setNome(rs.getString("secretario"));
		ata.setTipo(TipoAta.valueOf(rs.getInt("tipo")));
		ata.setNumero(rs.getInt("numero"));
		ata.setData(rs.getTimestamp("data"));
		ata.setLocal(rs.getString("local"));
		ata.setLocalCompleto(rs.getString("localCompleto"));
		ata.setDataLimiteComentarios(rs.getDate("dataLimiteComentarios"));
		ata.setConsideracoesIniciais(rs.getString("consideracoesIniciais"));
		ata.setAudio(rs.getBytes("audio"));
		ata.setPublicada(rs.getInt("publicada") == 1);
		ata.setAceitarComentarios(rs.getInt("aceitarComentarios") == 1);
		ata.setDataPublicacao(rs.getTimestamp("dataPublicacao"));
		ata.setDocumento(rs.getBytes("documento"));
		
		return ata;
	}
	
	public boolean temComentarios(int idAta) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = sstmt.executeQuery("SELECT COUNT(comentarios.idComentario) AS qtde FROM comentarios " +
				"INNER JOIN pautas ON pautas.idPauta=comentarios.idPauta " + 
				"WHERE pautas.idAta=" + String.valueOf(idAta));
		
			if(rs.next()){
				return (rs.getInt("qtde") > 0);
			}else{
				return false;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public boolean isPresidenteOuSecretario(int idUsuario, int idAta) throws SQLException{
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = stmt.executeQuery("SELECT atas.idAta FROM atas " +
				"WHERE idAta=" + String.valueOf(idAta) + " AND (idPresidente=" + String.valueOf(idUsuario) + " OR idSecretario=" + String.valueOf(idUsuario) + ")");
		
			return rs.next();
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public boolean isPresidente(int idUsuario, int idAta) throws SQLException{
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = stmt.executeQuery("SELECT atas.idAta FROM atas " +
				"WHERE idAta=" + String.valueOf(idAta) + " AND idPresidente=" + String.valueOf(idUsuario));
		
			return rs.next();
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public boolean isPublicada(int idAta) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = stmt.executeQuery("SELECT atas.publicada FROM atas " +
				"WHERE idAta=" + String.valueOf(idAta));
		
			if(rs.next()) {
				return rs.getInt("publicada") == 1;
			} else {
				return false;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	public boolean excluir(int idAta) throws SQLException{		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			
			stmt.execute("DELETE FROM comentarios WHERE idPauta IN (SELECT idPauta FROM pautas WHERE idAta=" + String.valueOf(idAta) + ")");
			stmt.execute("DELETE FROM pautas WHERE idAta=" + String.valueOf(idAta));
			stmt.execute("DELETE FROM ataparticipantes WHERE idAta=" + String.valueOf(idAta));
			stmt.execute("DELETE FROM anexos WHERE idAta=" + String.valueOf(idAta));
			boolean ret = stmt.execute("DELETE FROM atas WHERE idAta=" + String.valueOf(idAta));
			
			conn.commit();
			
			return ret;
		}catch(SQLException ex) {
			conn.rollback();
			throw ex;
		}finally{
			conn.setAutoCommit(true);
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}

}
