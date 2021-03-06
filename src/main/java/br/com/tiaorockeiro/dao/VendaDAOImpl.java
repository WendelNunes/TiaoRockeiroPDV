/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tiaorockeiro.dao;

import br.com.tiaorockeiro.modelo.Venda;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Wendel
 */
public class VendaDAOImpl extends DAOImpl<Venda, Long> implements VendaDAO {
    
    private final EntityManager entityManager;
    
    public VendaDAOImpl(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }
    
    @Override
    public Integer quantidadeRegistroConsultaVenda(Date periodoInicial, Date periodoFinal, Long idUsuario, Long idCaixa, Integer mesa, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("    SELECT CAST(COUNT(v.id) AS INTEGER)\n");
        sql.append("      FROM venda v\n");
        sql.append("INNER JOIN abertura_caixa ac ON ac.id = v.id_abertura_caixa\n");
        sql.append("     WHERE CAST(v.data_hora AS DATE) BETWEEN :periodoInicial AND :periodoFinal\n");
        if (idUsuario != null) {
            sql.append("       AND v.id_usuario = :idUsuario");
        }
        if (idCaixa != null) {
            sql.append("       AND ac.id_caixa = :idCaixa");
        }
        if (mesa != null) {
            sql.append("       AND v.mesa = :mesa");
        }
        if (status != null) {
            sql.append("       AND v.data_hora_cancelamento ").append(status.equals("1") ? "ISNULL" : "NOTNULL");
        }
        Query query = this.entityManager.createNativeQuery(sql.toString());
        query.setParameter("periodoInicial", periodoInicial);
        query.setParameter("periodoFinal", periodoFinal);
        if (idUsuario != null) {
            query.setParameter("idUsuario", idUsuario);
        }
        if (idCaixa != null) {
            query.setParameter("idCaixa", idCaixa);
        }
        if (mesa != null) {
            query.setParameter("mesa", mesa);
        }
        return (Integer) query.getSingleResult();
    }
    
    @Override
    public List<Object[]> listaConsultaVenda(Date periodoInicial, Date periodoFinal, Long idUsuario, Long idCaixa, Integer mesa,
            String status, Integer qtdeRegistro, Integer pagina) {
        StringBuilder sql = new StringBuilder();
        sql.append("    SELECT v.id,\n");
        sql.append("           v.mesa,\n");
        sql.append("           v.data_hora,\n");
        sql.append("           u.descricao AS descricao_usuario,\n");
        sql.append("           c.descricao AS descricao_caixa,\n");
        sql.append("           v.data_hora_cancelamento\n");
        sql.append("      FROM venda v\n");
        sql.append("INNER JOIN usuario u ON u.id = v.id_usuario\n");
        sql.append("INNER JOIN abertura_caixa ac ON ac.id = v.id_abertura_caixa\n");
        sql.append("INNER JOIN caixa c ON c.id = ac.id_caixa\n");
        sql.append("     WHERE CAST(v.data_hora AS DATE) BETWEEN :periodoInicial AND :periodoFinal\n");
        if (idUsuario != null) {
            sql.append("       AND u.id = :idUsuario");
        }
        if (idCaixa != null) {
            sql.append("       AND c.id = :idCaixa");
        }
        if (mesa != null) {
            sql.append("       AND v.mesa = :mesa");
        }
        if (status != null) {
            sql.append("       AND v.data_hora_cancelamento ").append(status.equals("1") ? "ISNULL" : "NOTNULL");
        }
        sql.append("  ORDER BY v.data_hora\n");
        sql.append("     LIMIT :qtdeRegistro");
        sql.append("    OFFSET (:pagina-1) * :qtdeRegistro");
        Query query = this.entityManager.createNativeQuery(sql.toString());
        query.setParameter("periodoInicial", periodoInicial);
        query.setParameter("periodoFinal", periodoFinal);
        if (idUsuario != null) {
            query.setParameter("idUsuario", idUsuario);
        }
        if (idCaixa != null) {
            query.setParameter("idCaixa", idCaixa);
        }
        if (mesa != null) {
            query.setParameter("mesa", mesa);
        }
        query.setParameter("qtdeRegistro", qtdeRegistro);
        query.setParameter("pagina", pagina);
        return query.getResultList();
    }
}
