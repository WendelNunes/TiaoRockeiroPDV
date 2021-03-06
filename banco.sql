CREATE TABLE configuracao (
    id bigserial NOT NULL,
    qtde_mesas INTEGER NOT NULL DEFAULT 0,
    percentual_comissao numeric(14,4) DEFAULT 0,
    hora_maxima_virada_dia_caixa TIME WITHOUT TIME ZONE,
    PRIMARY KEY (id)
);

CREATE TABLE pessoa (
    id bigserial NOT NULL,
    descricao character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE usuario(
    id bigserial NOT NULL,
    descricao character varying NOT NULL,
    senha character varying NOT NULL,
    id_pessoa bigint NOT NULL,
    administrador boolean NOT NULL DEFAULT false,
    gerente boolean NOT NULL DEFAULT false,
    operador_caixa boolean NOT NULL DEFAULT false,
    vendedor boolean NOT NULL DEFAULT false,
    PRIMARY KEY (id),
    FOREIGN KEY (id_pessoa) REFERENCES pessoa (id) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE impressora (
    id bigserial NOT NULL,
    descricao character varying NOT NULL,
    url character varying NOT NULL,
    codigo_inicio_impressao character varying,
    codigo_corte character varying,
    quantidade_caracteres integer,
    PRIMARY KEY (id)
);

CREATE TABLE caixa (
    id bigserial NOT NULL,
    codigo character varying NOT NULL,
    descricao character varying NOT NULL,
    id_impressora bigint NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (codigo),
    FOREIGN KEY (id_impressora) REFERENCES impressora (id) ON DELETE SET NULL
);

CREATE TABLE abertura_caixa (
   id bigserial NOT NULL, 
   id_caixa bigint NOT NULL, 
   data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP, 
   saldo_inicial numeric(14,4) NOT NULL DEFAULT 0,
   id_usuario bigint NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_caixa) REFERENCES caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE fechamento_caixa (
   id bigserial NOT NULL, 
   id_abertura_caixa bigint NOT NULL, 
   data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP, 
   saldo_final numeric(14,4) NOT NULL DEFAULT 0, 
   id_usuario bigint NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_abertura_caixa) REFERENCES abertura_caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE sangria_caixa (
   id bigserial NOT NULL, 
   id_abertura_caixa bigint NOT NULL, 
   data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP, 
   valor_sangria numeric(14,4) NOT NULL DEFAULT 0, 
   id_usuario bigint NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_abertura_caixa) REFERENCES abertura_caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE suprimento_caixa (
   id bigserial NOT NULL, 
   id_abertura_caixa bigint NOT NULL, 
   data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP, 
   valor_suprimento numeric(14,4) NOT NULL DEFAULT 0, 
   id_usuario bigint NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_abertura_caixa) REFERENCES abertura_caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE configuracao_usuario (
   id bigserial NOT NULL,
   id_usuario bigint NOT NULL,
   id_caixa_selecionado bigint,
   PRIMARY KEY (id),
   FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
   FOREIGN KEY (id_caixa_selecionado) REFERENCES caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE unidade_produto (
    id bigserial NOT NULL,
    codigo character varying NOT NULL,
    descricao character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE categoria_produto (
    id bigserial NOT NULL,
    descricao character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE produto (
    id bigserial NOT NULL,
    codigo character varying NOT NULL,
    descricao character varying NOT NULL,
    id_categoria_produto bigint NOT NULL,
    id_unidade_produto bigint NOT NULL,
    valor numeric(14,4) NOT NULL DEFAULT 0,
    id_impressora_comanda bigint,
    adicional boolean NOT NULL DEFAULT FALSE,
    imagem bytea NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_categoria_produto) REFERENCES categoria_produto (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_unidade_produto) REFERENCES unidade_produto (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_impressora_comanda) REFERENCES impressora (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE pedido (
    id bigserial NOT NULL,
    id_usuario bigint NOT NULL,
    data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_cancelamento bigint,
    data_hora_cancelamento timestamp without time zone,
    mesa integer NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_usuario_cancelamento) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE item_pedido (
    id bigserial NOT NULL,
    id_pedido bigint NOT NULL,
    id_abertura_caixa bigint NOT NULL,
    id_usuario bigint NOT NULL,
    data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_cancelamento bigint,
    data_hora_cancelamento timestamp without time zone,
    id_produto bigint NOT NULL,
    quantidade numeric(14,4) NOT NULL DEFAULT 0,
    valor numeric(14,4) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (id_pedido) REFERENCES pedido (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (id_abertura_caixa) REFERENCES abertura_caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_usuario_cancelamento) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_produto) REFERENCES produto (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE venda (
    id bigserial NOT NULL,
    id_pedido bigint NOT NULL,
    id_usuario bigint NOT NULL,
    id_abertura_caixa bigint NOT NULL,
    data_hora timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_cancelamento bigint,
    data_hora_cancelamento timestamp without time zone,
    mesa integer NOT NULL,
    valor_comissao numeric(14,4) DEFAULT 0,
    valor_desconto numeric(14,4) DEFAULT 0,
    valor_total numeric(14,4) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_pedido) REFERENCES pedido (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_abertura_caixa) REFERENCES abertura_caixa (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_usuario_cancelamento) REFERENCES usuario (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE item_venda (
    id bigserial NOT NULL,
    id_venda bigint NOT NULL,
    id_produto bigint NOT NULL,
    quantidade numeric(14,4) NOT NULL DEFAULT 0,
    valor_unitario numeric(14,4) NOT NULL DEFAULT 0,
    valor_total numeric(14,4) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_venda) REFERENCES venda (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (id_produto) REFERENCES produto (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE pagamento (
    id bigserial NOT NULL,
    id_venda bigint NOT NULL,
    id_forma_pagamento character varying NOT NULL,
    valor numeric(14,4) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_venda) REFERENCES venda (id) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE observacao (
    id bigserial NOT NULL,
    descricao character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE prefixo_observacao (
    id bigserial NOT NULL,
    descricao character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE observacao_produto (
    id bigserial NOT NULL,
    id_item_pedido bigint NOT NULL,
    id_prefixo_observacao bigint NOT NULL,
    id_observacao bigint NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_item_pedido) REFERENCES item_pedido (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (id_prefixo_observacao) REFERENCES prefixo_observacao (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    FOREIGN KEY (id_observacao) REFERENCES observacao (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE adicional_produto (
    id bigserial NOT NULL,
    id_item_pedido bigint NOT NULL,
    id_produto bigint NOT NULL,
    quantidade numeric(14,4) NOT NULL DEFAULT 0,
    valor numeric(14,4) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (id_item_pedido) REFERENCES item_pedido (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (id_produto) REFERENCES produto (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE promocao (
    id bigserial NOT NULL,
    descricao CHARACTER VARYING NOT NULL,
    data_inicial DATE NOT NULL,
    data_final DATE,
    PRIMARY KEY (id)
);

CREATE TABLE promocao_dia (
    id bigserial NOT NULL,
    id_promocao bigint NOT NULL,
    dia integer NOT NULL,
    hora_inicial TIME WITHOUT TIME ZONE,
    hora_final TIME WITHOUT TIME ZONE,
    PRIMARY KEY (id),
    FOREIGN KEY (id_promocao) REFERENCES promocao (id) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE promocao_produto (
    id bigserial NOT NULL,
    id_promocao bigint NOT NULL,
    id_produto bigint NOT NULL,
    valor_produto NUMERIC(14,4) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id_promocao) REFERENCES promocao (id) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (id_produto) REFERENCES produto (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);