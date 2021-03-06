/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tiaorockeiro.dao;

import br.com.tiaorockeiro.modelo.Pedido;
import java.util.List;

/**
 *
 * @author Wendel
 */
public interface PedidoDAO extends DAO<Pedido, Long> {

    public Pedido obterAbertoPorMesa(Integer mesa);

    public List<Pedido> obterAbertos();
}
