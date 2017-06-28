/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tiaorockeiro.controller;

import br.com.tiaorockeiro.modelo.FormaPagamento;
import br.com.tiaorockeiro.modelo.ItemPedido;
import br.com.tiaorockeiro.modelo.ItemVenda;
import br.com.tiaorockeiro.modelo.Pagamento;
import br.com.tiaorockeiro.modelo.Pedido;
import br.com.tiaorockeiro.modelo.Venda;
import br.com.tiaorockeiro.negocio.AberturaCaixaNegocio;
import br.com.tiaorockeiro.negocio.AdicionalProdutoNegocio;
import br.com.tiaorockeiro.negocio.ItemPedidoNegocio;
import br.com.tiaorockeiro.negocio.ObservacaoProdutoNegocio;
import br.com.tiaorockeiro.negocio.PedidoNegocio;
import br.com.tiaorockeiro.negocio.VendaNegocio;
import static br.com.tiaorockeiro.util.MensagemUtil.enviarMensagemConfirmacao;
import static br.com.tiaorockeiro.util.MensagemUtil.enviarMensagemErro;
import static br.com.tiaorockeiro.util.MensagemUtil.enviarMensagemInformacao;
import static br.com.tiaorockeiro.util.MoedaUtil.formataMoeda;
import static br.com.tiaorockeiro.util.MoedaUtil.parseMoeda;
import static br.com.tiaorockeiro.util.QuantidadeUtil.formataQuantidade;
import br.com.tiaorockeiro.util.SessaoUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Wendel
 */
public class TelaFinalizarVendaController implements Initializable {

    @FXML
    private Button btnAcaoVoltar;
    @FXML
    private ListView<ItemPedido> listViewItens;
    @FXML
    private TextField textFieldQuantidadeItens;
    @FXML
    private TextField textFieldValorTotal;

    private Venda venda;

    public TelaFinalizarVendaController() {
        this.venda = new Venda();
        this.venda.setItens(new ArrayList<>());
        this.venda.setPagamentos(new ArrayList<>());
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.btnAcaoVoltar.setOnAction((ActionEvent event) -> {
            acaoVoltar(event);
        });
    }

    private void ajustaTabelaItens() {
        this.venda.getPedido().getItens().sort((o1, o2) -> o1.getId().compareTo(o2.getId()));
        this.listViewItens.setItems(FXCollections.observableList(this.venda.getPedido().getItens().stream()
                .filter(i -> i.getDataHoraCancelamento() == null).collect(Collectors.toList())));
        this.listViewItens.setCellFactory(param -> new ListCell<ItemPedido>() {
            @Override
            protected void updateItem(ItemPedido item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    try {
                        ListCellProduto cellProduto = new ListCellProduto(item.getProduto().getDescricao(), item.getValor(), item.getQuantidade(), item.getValorTotalAdicionais(), item.getValorTotal());
                        setGraphic(cellProduto.getCell());
                    } catch (Exception e) {
                        enviarMensagemErro(e.getMessage());
                        setText(null);
                        setGraphic(null);
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
    }

    private void atualizaTotalizadores() {
        this.textFieldQuantidadeItens.setText(formataQuantidade(this.listViewItens.getItems().stream().map(i -> i.getQuantidade()).reduce(BigDecimal.ZERO, BigDecimal::add)));
        BigDecimal total = this.listViewItens.getItems().stream().map(i -> i.getValorTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.textFieldValorTotal.setText(formataMoeda(total));
        BigDecimal desconto = new BigDecimal(this.textFieldDesconto.getText().replaceAll("\\.", "").replaceAll(",", "."));
        BigDecimal comissao = BigDecimal.ZERO;
        if (this.buttonCalcularComissao.isSelected()
                && SessaoUtil.getConfiguracao() != null
                && SessaoUtil.getConfiguracao().getPercentualComissao() != null
                && SessaoUtil.getConfiguracao().getPercentualComissao().compareTo(BigDecimal.ZERO) > 0) {
            comissao = total.multiply(SessaoUtil.getConfiguracao().getPercentualComissao()
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_DOWN)).setScale(2, RoundingMode.HALF_DOWN);
            this.textFieldComissao.setText(formataMoeda(comissao));
        } else {
            this.textFieldComissao.setText(formataMoeda(comissao));
        }
        BigDecimal totalGeral = total.add(comissao).subtract(desconto);
        this.textFieldTotalGeral.setText(formataMoeda(totalGeral));
        BigDecimal dinheiro = new BigDecimal(this.textFieldDinhieiro.getText().replaceAll("\\.", "").replaceAll(",", "."));
        BigDecimal cartaoCredito = new BigDecimal(this.textFieldCartaoCredito.getText().replaceAll("\\.", "").replaceAll(",", "."));
        BigDecimal cartaoDebito = new BigDecimal(this.textFieldCartaoDebito.getText().replaceAll("\\.", "").replaceAll(",", "."));
        BigDecimal outros = new BigDecimal(this.textFieldOutros.getText().replaceAll("\\.", "").replaceAll(",", "."));
        BigDecimal totalPagamento = dinheiro.add(cartaoCredito).add(cartaoDebito).add(outros);
        this.textFieldTroco.setText(formataMoeda(totalPagamento.compareTo(totalGeral) > 0
                ? totalPagamento.subtract(totalGeral) : BigDecimal.ZERO));
    }

    @FXML
    public void acaoCancelarProduto(ActionEvent event) {
        try {
            int index = this.listViewItens.getSelectionModel().getSelectedIndex();
            if (index != -1 && enviarMensagemConfirmacao("Deseja realmente cancelar o item selecionado?")) {
                ItemPedido item = this.listViewItens.getItems().get(index);
                item.setDataHoraCancelamento(new Date());
                item.setUsuarioCancelamento(SessaoUtil.getUsuario());
                new ItemPedidoNegocio().salvar(item);
                this.listViewItens.getItems().remove(index);
                this.atualizaTotalizadores();
            }
        } catch (Exception e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    @FXML
    public void acaoCalcularComissao(ActionEvent event) {
        try {
            this.atualizaTotalizadores();
        } catch (Exception e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    @FXML
    public void acaoBotao1(ActionEvent event) {
        this.adicionaNumero(1);
    }

    @FXML
    public void acaoBotao2(ActionEvent event) {
        this.adicionaNumero(2);
    }

    @FXML
    public void acaoBotao3(ActionEvent event) {
        this.adicionaNumero(3);
    }

    @FXML
    public void acaoBotao4(ActionEvent event) {
        this.adicionaNumero(4);
    }

    @FXML
    public void acaoBotao5(ActionEvent event) {
        this.adicionaNumero(5);
    }

    @FXML
    public void acaoBotao6(ActionEvent event) {
        this.adicionaNumero(6);
    }

    @FXML
    public void acaoBotao7(ActionEvent event) {
        this.adicionaNumero(7);
    }

    @FXML
    public void acaoBotao8(ActionEvent event) {
        this.adicionaNumero(8);
    }

    @FXML
    public void acaoBotao9(ActionEvent event) {
        this.adicionaNumero(9);
    }

    @FXML
    public void acaoBotao0(ActionEvent event) {
        this.adicionaNumero(0);
    }

    @FXML
    public void acaoBotaoLimpar(ActionEvent event) {
        try {
            if (this.grupoBotaoTeclado.getSelectedToggle() != null) {
                ((TextField) this.grupoBotaoTeclado.getSelectedToggle().getUserData()).setText(formataMoeda(BigDecimal.ZERO));
            }
            this.atualizaTotalizadores();
        } catch (Exception e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    private void adicionaNumero(Integer numero) {
        try {
            if (this.grupoBotaoTeclado.getSelectedToggle() != null) {
                TextField textField = (TextField) this.grupoBotaoTeclado.getSelectedToggle().getUserData();
                StringBuilder texto = new StringBuilder(parseMoeda(textField.getText()).toString().replaceAll("\\.", ""));
                texto.append(numero);
                texto.insert(texto.length() - 2, ".");
                textField.setText(formataMoeda(new BigDecimal(texto.toString())));
                this.atualizaTotalizadores();
            }
        } catch (NumberFormatException | ParseException e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    @FXML
    public void acaoFinalizarVenda(ActionEvent event) {
        try {
            BigDecimal dinheiro = new BigDecimal(this.textFieldDinhieiro.getText().replaceAll("\\.", "").replaceAll(",", "."));
            BigDecimal cartaoCredito = new BigDecimal(this.textFieldCartaoCredito.getText().replaceAll("\\.", "").replaceAll(",", "."));
            BigDecimal cartaoDebito = new BigDecimal(this.textFieldCartaoDebito.getText().replaceAll("\\.", "").replaceAll(",", "."));
            BigDecimal outros = new BigDecimal(this.textFieldOutros.getText().replaceAll("\\.", "").replaceAll(",", "."));
            BigDecimal pagamentos = dinheiro.add(cartaoCredito).add(cartaoDebito).add(outros);
            BigDecimal desconto = new BigDecimal(this.textFieldDesconto.getText().replaceAll("\\.", "").replaceAll(",", "."));
            BigDecimal comissao = new BigDecimal(this.textFieldComissao.getText().replaceAll("\\.", "").replaceAll(",", "."));
            BigDecimal totalItens = this.listViewItens.getItems().stream().map(i -> i.getValorTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalGeral = totalItens.add(comissao).subtract(desconto);
            // VERIFICA SE TOTAL DE PAGAMENTOS E MAIOR DO QUE O TOTAL GERAL
            if (pagamentos.compareTo(totalGeral) < 0) {
                enviarMensagemInformacao("O valor total dos pagamentos é menor que o valor total a pagar!");
            } else {
                this.venda.setAberturaCaixa(new AberturaCaixaNegocio()
                        .obterAbertoPorCaixa(SessaoUtil.getUsuario().getConfiguracao().getCaixaSelecionado()));
                this.venda.setUsuario(SessaoUtil.getUsuario());
                this.venda.setDataHora(new Date());
                this.venda.setValorComissao(comissao);
                this.venda.setValorDesconto(desconto);
                // ITENS
                this.venda.getPedido().getItens().forEach(i -> {
                    ItemVenda item = new ItemVenda();
                    item.setVenda(this.venda);
                    item.setProduto(i.getProduto());
                    item.setQuantidade(i.getQuantidade());
                    item.setValorUnitario(i.getValor());
                    this.venda.getItens().add(item);
                    i.getAdicionais().forEach(a -> {
                        ItemVenda itemAdicional = new ItemVenda();
                        itemAdicional.setVenda(this.venda);
                        itemAdicional.setProduto(a.getProduto());
                        itemAdicional.setQuantidade(a.getQuantidade());
                        itemAdicional.setValorUnitario(a.getValor());
                        this.venda.getItens().add(itemAdicional);
                    });
                });
                // FORMA PAGAMENTO
                if (dinheiro.compareTo(BigDecimal.ZERO) > 0) {
                    Pagamento pagamento = new Pagamento();
                    pagamento.setVenda(this.venda);
                    pagamento.setFormaPagamento(FormaPagamento.DINHEIRO);
                    pagamento.setValor(dinheiro);
                    this.venda.getPagamentos().add(pagamento);
                }
                if (cartaoCredito.compareTo(BigDecimal.ZERO) > 0) {
                    Pagamento pagamento = new Pagamento();
                    pagamento.setVenda(this.venda);
                    pagamento.setFormaPagamento(FormaPagamento.CARTAO_CREDITO);
                    pagamento.setValor(cartaoCredito);
                    this.venda.getPagamentos().add(pagamento);
                }
                if (cartaoDebito.compareTo(BigDecimal.ZERO) > 0) {
                    Pagamento pagamento = new Pagamento();
                    pagamento.setVenda(this.venda);
                    pagamento.setFormaPagamento(FormaPagamento.CARTAO_DEBITO);
                    pagamento.setValor(cartaoDebito);
                    this.venda.getPagamentos().add(pagamento);
                }
                if (outros.compareTo(BigDecimal.ZERO) > 0) {
                    Pagamento pagamento = new Pagamento();
                    pagamento.setVenda(this.venda);
                    pagamento.setFormaPagamento(FormaPagamento.OUTROS);
                    pagamento.setValor(outros);
                    this.venda.getPagamentos().add(pagamento);
                }
                this.venda = new VendaNegocio().salvar(this.venda);
                if (this.venda.getId() == null) {
                    throw new Exception("Erro ao finalizar a venda, tente novamente!");
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TelaMesas.fxml"));
                AnchorPane telaMesas = loader.load();
                TelaPrincipalController.getInstance().mudaTela(telaMesas, "Mesas");
                enviarMensagemInformacao("Venda finalizada com sucesso!");
            }
        } catch (Exception e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    @FXML
    public void acaoCancelarVenda(ActionEvent event) {
        try {
            if (enviarMensagemConfirmacao("Deseja realmente cancelar a venda?")) {
                this.venda.getPedido().setDataHoraCancelamento(new Date());
                this.venda.getPedido().setUsuarioCancelamento(SessaoUtil.getUsuario());
                new PedidoNegocio().salvar(this.venda.getPedido());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TelaMesas.fxml"));
                AnchorPane telaMesas = loader.load();
                TelaPrincipalController.getInstance().mudaTela(telaMesas, "Mesas");
                enviarMensagemInformacao("Venda cancelada com sucesso!");
            }
        } catch (IOException e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    @FXML
    public void acaoVoltar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TelaPedido.fxml"));
            AnchorPane telaPedido = loader.load();
            TelaPedidoController telaPedidoController = loader.getController();
            telaPedidoController.inicializaDados(this.venda.getMesa());
            TelaPrincipalController.getInstance().mudaTela(telaPedido, "Pedido - Mesa " + this.venda.getMesa());
        } catch (Exception e) {
            enviarMensagemErro(e.getMessage());
        }
    }

    public void inicializaDados(Long idPedido) {
        this.venda.setPedido(new PedidoNegocio().obterPorId(Pedido.class, idPedido));
        ObservacaoProdutoNegocio observacaoProdutoNegocio = new ObservacaoProdutoNegocio();
        AdicionalProdutoNegocio adicionalProdutoNegocio = new AdicionalProdutoNegocio();
        this.venda.getPedido().getItens().stream().map((itemPedido) -> {
            return itemPedido;
        }).forEachOrdered((itemPedido) -> {
            itemPedido.setObservacoes(observacaoProdutoNegocio.obterPorIdItemPedido(itemPedido.getId()));
            itemPedido.setAdicionais(adicionalProdutoNegocio.obterPorIdItemPedido(itemPedido.getId()));
        });
        this.venda.setMesa(this.venda.getPedido().getMesa());
        this.ajustaTabelaItens();
        this.atualizaTotalizadores();
    }
}
