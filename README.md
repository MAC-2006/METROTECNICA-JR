# Metrotécnica — Sistema de Gestão e Calibração

Sistema web para gestão de instrumentos de medição e suas calibrações, com
emissão de certificados em PDF, relatórios, controle de padrões metrológicos
e suporte a múltiplas empresas (multi-tenant) em uma única instalação.

Migrado de um sistema legado em Python/Flask para **Java + Spring Boot** no
backend e **Angular** no frontend.

## Sumário

- [Sobre o projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Funcionalidades](#funcionalidades)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Pré-requisitos](#pré-requisitos)
- [Configuração e execução](#configuração-e-execução)
- [Perfis de usuário e permissões](#perfis-de-usuário-e-permissões)
- [Principais endpoints da API](#principais-endpoints-da-api)
- [Licença](#licença)

## Sobre o projeto

O Metrotécnica centraliza o controle de calibração de instrumentos de
medição para empresas que precisam manter conformidade metrológica: cadastro
de instrumentos, histórico de calibrações, emissão de certificados com
assinatura digital e QR Code de validação pública, geração de relatórios
(PDF/Excel) e cópia digital dos certificados dos padrões utilizados nas
calibrações.

## Arquitetura

O sistema é **multi-tenant**: uma única instalação atende várias empresas
("tenants"), cada uma com seus próprios instrumentos, setores, locais de uso
e usuários — completamente isolados uns dos outros.

- Um **super-admin** (usuário sem empresa vinculada) acessa o Painel Global,
  onde cadastra empresas, cria os usuários de cada empresa e pode "acessar"
  (impersonar) qualquer empresa para dar suporte.
- Cada empresa tem seus próprios usuários com papel `admin` (cadastra e
  edita instrumentos, setores, locais de uso) ou `user` (acesso somente
  leitura + geração de relatórios).
- Autenticação stateless via **JWT**, com o `tenant_id` e o `role`
  embutidos no token — é isso que isola os dados de cada empresa.

## Tecnologias

**Backend** (`api/`)
- Java 17 · Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security
- MySQL + Flyway (versionamento de schema)
- JWT (`jjwt`) para autenticação stateless
- Thymeleaf (templates de certificado/relatório em PDF)
- OpenHTMLtoPDF (geração de PDF), JFreeChart (gráficos), ZXing (QR Code)
- Apache POI (Excel), MapStruct + Lombok

**Frontend** (`metrotecnica-web/`)
- Angular 22 (standalone components, signals, SSR)
- Tailwind CSS
- RxJS

## Funcionalidades

- Cadastro e edição de instrumentos, com histórico de calibrações
- Emissão de certificado de calibração em PDF, com assinatura digital e
  validação pública via QR Code / hash do documento
- Relatórios em PDF e Excel filtráveis por período, setor, status etc.
- Cadastro de setores e locais de uso por empresa
- Cópia digital dos padrões metrológicos (upload em lote via ZIP)
- Migração de dados do sistema legado (FoxPro/DBF)
- Painel administrativo multi-tenant: cadastro de empresas, gestão de
  usuários por empresa e acesso "modo suporte" (impersonate)

## Estrutura do repositório

```
Metrotecnica_Java/
├── api/                    # Backend Spring Boot
│   └── src/main/java/com/metrotecnica/api/
│       ├── controller/      # Endpoints REST
│       ├── service/         # Regras de negócio
│       ├── model/           # Entidades JPA
│       ├── repository/      # Spring Data JPA
│       ├── dto/              # DTOs de entrada/saída
│       └── security/         # JWT, filtros, UserDetails
│   └── src/main/resources/
│       ├── application.yml
│       ├── db/migration/     # Scripts Flyway
│       └── templates/        # Templates Thymeleaf (PDF)
└── metrotecnica-web/        # Frontend Angular
    └── src/app/
        ├── core/              # Services, guards, models
        └── features/          # Telas (login, dashboard, instrumentos...)
```

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Node.js 20+ e npm
- MySQL 8+

## Configuração e execução

### 1. Banco de dados

Crie o schema no MySQL:

```sql
CREATE DATABASE metrotecnica CHARACTER SET utf8mb4;
```

As tabelas são criadas automaticamente na primeira execução, via Flyway
(`api/src/main/resources/db/migration`).

### 2. Backend

Defina as variáveis de ambiente exigidas em `application.yml`:

| Variável       | Descrição                                   |
|----------------|-----------------------------------------------|
| `DB_USERNAME`  | Usuário do MySQL (padrão: `root`)             |
| `DB_PASSWORD`  | Senha do MySQL                                |
| `JWT_SECRET`   | Chave secreta para assinatura dos tokens JWT  |

```bash
cd api
export DB_PASSWORD=sua_senha
export JWT_SECRET=uma_chave_bem_grande_e_aleatoria
mvn clean install
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

### 3. Frontend

```bash
cd metrotecnica-web
npm install
npm start
```

A aplicação sobe em `http://localhost:4200` e consome a API configurada em
`src/environments/environment.ts`.

## Perfis de usuário e permissões

| Papel               | Escopo             | Pode fazer                                                       |
|---------------------|---------------------|--------------------------------------------------------------------|
| **super-admin**      | Global (sem tenant) | Cadastrar empresas, criar usuários de cada empresa, acessar (impersonar) qualquer empresa, importar dados legados, gerenciar padrões metrológicos globais |
| **admin** (da empresa) | Uma empresa         | Cadastrar/editar instrumentos, setores e locais de uso, gerar relatórios |
| **user** (da empresa)  | Uma empresa         | Somente visualizar instrumentos e gerar relatórios (PDF/Excel); se marcado como assinante, também pode assinar certificados |

Todas as regras de escrita são aplicadas tanto na API (`@PreAuthorize`)
quanto na interface, para que a experiência do usuário reflita o que ele
realmente tem permissão de fazer.

## Principais endpoints da API

| Método | Rota                                  | Descrição                                  |
|--------|-----------------------------------------|-----------------------------------------------|
| POST   | `/api/login`                           | Autenticação                                  |
| GET/POST/PUT | `/api/instrumentos`              | CRUD de instrumentos (escrita exige admin)     |
| GET    | `/api/relatorio/pdf` \| `/excel`       | Geração de relatórios                          |
| GET/POST/PUT/DELETE | `/api/setores`, `/api/locais-uso` | Cadastros auxiliares (escrita exige admin) |
| GET    | `/api/certificado/{id}/pdf`            | Emissão do certificado de calibração           |
| GET    | `/api/validar/{hash}`                  | Validação pública de certificado               |
| GET/POST/PUT/DELETE | `/api/tenants`                | Gestão de empresas (super-admin)               |
| GET/POST/PUT/DELETE | `/api/tenants/{id}/users`     | Gestão de usuários por empresa (super-admin)   |
| POST   | `/api/admin/upload-migracao`           | Importação de dados legados FoxPro (super-admin) |
| POST   | `/api/admin/upload-padroes`            | Upload em lote de padrões metrológicos (super-admin) |

## Licença

**Proprietário — Todos os direitos reservados.**

Copyright © 2026 JOSE RUBENS CARDOSO DA COSTA JUNIOR. Este é um projeto de software
desenvolvido sob encomenda (freelance). O código-fonte, sua estrutura,
documentação e todos os artefatos deste repositório são de propriedade do
cliente contratante, conforme os termos do contrato de prestação de serviços
firmado entre as partes.

É vedado copiar, modificar, distribuir, sublicenciar ou utilizar este
código, no todo ou em parte, para qualquer finalidade, sem autorização
prévia e por escrito do titular dos direitos.

> Este repositório **não** está sob uma licença open source (MIT, Apache,
> GPL etc.). Se em algum momento decidirem abrir parte do código, é
> necessário criar uma licença específica para isso e revisar o contrato
> vigente antes.
