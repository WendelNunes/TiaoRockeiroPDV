/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tiaorockeiro.dao;

import br.com.tiaorockeiro.modelo.ObservacaoProduto;
import java.util.List;

/**
 *
 * @author INLOC01
 */
public interface ObservacaoProdutoDAO extends DAO<ObservacaoProduto, Long> {

    public List<ObservacaoProduto> obterPorIdItemPedido(Long idItemPedido);
}
