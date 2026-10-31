create extension if not exists pgcrypto;

create table usuarios (
    id uuid primary key default gen_random_uuid(),
    nome varchar(150) not null,
    username varchar(100) not null,
    senha varchar(255) not null,
    tipo varchar(20) not null,
    ativo boolean not null default true,
    data_cadastro timestamp not null default now(),
    constraint uk_usuarios_username unique (username),
    constraint ck_usuarios_tipo check (tipo in ('CLIENTE', 'EMPRESA', 'BANCO'))
);

create table clientes (
    id uuid primary key references usuarios(id) on delete cascade,
    rg varchar(20) not null,
    cpf varchar(14) not null,
    profissao varchar(100),
    logradouro varchar(150),
    numero varchar(20),
    complemento varchar(100),
    bairro varchar(100),
    cidade varchar(100),
    uf varchar(2),
    cep varchar(10),
    constraint uk_clientes_cpf unique (cpf)
);

create table agentes (
    id uuid primary key references usuarios(id) on delete cascade,
    cnpj varchar(18) not null,
    razao_social varchar(200) not null,
    constraint uk_agentes_cnpj unique (cnpj)
);

create table empresas (
    id uuid primary key references agentes(id) on delete cascade
);

create table bancos (
    id uuid primary key references agentes(id) on delete cascade,
    codigo_banco varchar(10) not null,
    constraint uk_bancos_codigo unique (codigo_banco)
);

create table rendimentos (
    id uuid primary key default gen_random_uuid(),
    cliente_id uuid not null references clientes(id) on delete cascade,
    entidade_empregadora varchar(150) not null,
    valor numeric(15,2) not null,
    constraint ck_rendimentos_valor check (valor > 0)
);

create index ix_rendimentos_cliente on rendimentos(cliente_id);

create table automoveis (
    id uuid primary key default gen_random_uuid(),
    matricula varchar(30) not null,
    ano integer not null,
    marca varchar(80) not null,
    modelo varchar(80) not null,
    placa varchar(10) not null,
    proprietario_id uuid not null references usuarios(id) on delete restrict,
    constraint uk_automoveis_matricula unique (matricula),
    constraint uk_automoveis_placa unique (placa),
    constraint ck_automoveis_ano check (ano between 1900 and 2100)
);

create index ix_automoveis_proprietario on automoveis(proprietario_id);

create table pedidos_aluguel (
    id uuid primary key default gen_random_uuid(),
    cliente_id uuid not null references clientes(id) on delete restrict,
    automovel_id uuid not null references automoveis(id) on delete restrict,
    data_inicio date not null,
    data_fim date not null,
    status varchar(30) not null default 'PENDENTE',
    observacoes text,
    data_criacao timestamp not null default now(),
    data_atualizacao timestamp not null default now(),
    constraint ck_pedidos_status check (
        status in ('PENDENTE', 'EM_ANALISE', 'APROVADO',
                   'REPROVADO', 'CANCELADO', 'CONTRATO_EXECUTADO')
    ),
    constraint ck_pedidos_datas check (data_fim >= data_inicio)
);

create index ix_pedidos_cliente on pedidos_aluguel(cliente_id);
create index ix_pedidos_automovel on pedidos_aluguel(automovel_id);
create index ix_pedidos_status on pedidos_aluguel(status);

create table contratos_credito (
    id uuid primary key default gen_random_uuid(),
    pedido_id uuid not null references pedidos_aluguel(id) on delete restrict,
    banco_id uuid not null references bancos(id) on delete restrict,
    valor numeric(15,2) not null,
    taxa_juros numeric(5,2) not null,
    prazo_meses integer not null,
    data_concessao date not null default current_date,
    constraint uk_contratos_pedido unique (pedido_id),
    constraint ck_contratos_valor check (valor > 0),
    constraint ck_contratos_taxa check (taxa_juros >= 0),
    constraint ck_contratos_prazo check (prazo_meses > 0)
);

create index ix_contratos_banco on contratos_credito(banco_id);

alter table public.usuarios enable row level security;
alter table public.clientes enable row level security;
alter table public.agentes enable row level security;
alter table public.empresas enable row level security;
alter table public.bancos enable row level security;
alter table public.rendimentos enable row level security;
alter table public.automoveis enable row level security;
alter table public.pedidos_aluguel enable row level security;
alter table public.contratos_credito enable row level security;
