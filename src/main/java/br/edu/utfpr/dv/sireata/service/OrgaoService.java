package br.edu.utfpr.dv.sireata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.edu.utfpr.dv.sireata.bo.OrgaoBO;
import br.edu.utfpr.dv.sireata.model.Orgao;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Path("/orgao")
@Controller
public class OrgaoService {

	@GetMapping("/listar/{departamento}")
	@ResponseBody
	public Response listar(@RequestParam("departamento") int idDepartamento) {
		try {
			List<Orgao> list = new OrgaoBO().listarPorDepartamento(idDepartamento);
			List<OrgaoJson> ret = new ArrayList<OrgaoJson>();
			
			for(Orgao o : list) {
				OrgaoJson orgao = new OrgaoJson();
				
				orgao.setCodigo(o.getIdOrgao());
				orgao.setNome(o.getNome());
				
				ret.add(orgao);
			}
			
			return Response.ok(ret).build();
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);

			return Response.status(Status.INTERNAL_SERVER_ERROR.ordinal(), e.getMessage()).build();
		}
	}
	
	public class OrgaoJson {
		
		private int codigo;
		private String nome;
		
		public OrgaoJson() {
			this.setCodigo(0);
			this.setNome("");
		}
		
		public int getCodigo() {
			return codigo;
		}
		public void setCodigo(int codigo) {
			this.codigo = codigo;
		}
		public String getNome() {
			return nome;
		}
		public void setNome(String nome) {
			this.nome = nome;
		}
		
	}

}
