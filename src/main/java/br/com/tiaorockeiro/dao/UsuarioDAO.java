/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tiaorockeiro.dao;

import br.com.tiaorockeiro.modelo.Usuario;

/**
 *
 * @author Wendel
 */
public interface UsuarioDAO extends DAO<Usuario, Long> {

    public Usuario procurarPorDescricao(String descricao);
}
