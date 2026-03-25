# LothusPay Backend

Backend de uma plataforma financeira construído em arquitetura de microserviços com Java e Spring.

Este repositório representa a versão final pública do projeto: o sistema está funcional, mas foi descontinuado e não terá novas funcionalidades.

## Status do projeto

- Funcional para os fluxos principais implementados.
- Encerrado oficialmente (sem roadmap futuro).
- Publicado como parte de portfólio técnico.

## Arquitetura

Monorepo Maven com os seguintes módulos:

- `gateway-api`: API Gateway (roteamento, validação JWT e tráfego interno).
- `auth-api`: autenticação, cadastro, KYC, 2FA e gestão de API keys.
- `wallet-api`: carteira, extrato, depósitos/saques e operações administrativas.
- `payments-api`: criação e acompanhamento de depósitos/saques e callbacks.
- `email-api`: gerenciamento de layouts e integração de envio de e-mails.
- `events-common`: biblioteca compartilhada de eventos entre serviços.

## Tecnologias principais

- Java 17
- Spring Boot 3
- Spring Cloud Gateway
- Spring Security + JWT
- Spring WebFlux (nos serviços reativos)
- MongoDB
- Apache Kafka
- Docker Compose

## Funcionalidades implementadas

- Autenticação com JWT.
- Fluxo de cadastro e perfil com suporte a KYC.
- Suporte a autenticação em dois fatores (2FA/TOTP).
- Gestão de carteiras, extrato e operações administrativas.
- Criação de depósitos e saques com tratamento de callbacks.
- Gateway central para exposição de rotas públicas e internas.
- Comunicação assíncrona entre serviços por eventos (Kafka).

## Rotas (visão geral)

No gateway:

- Públicas: `/v1/auth/**`, `/v1/wallet/**`, `/v1/payments/**`, `/v1/email/**`
- Internas: `/int/auth/internal/**`, `/int/wallet/internal/**`, `/int/payments/internal/**`

Exemplos de domínios de API por serviço:

- `auth-api`: `/auth/login`, `/auth/register`, `/auth/profile`, `/auth/profile/2fa/*`
- `wallet-api`: `/wallet/statement`, `/wallet/deposits/*`, `/wallet/withdrawals/*`, `/wallet/admin/*`
- `payments-api`: `/payments/deposit`, `/payments/withdraw`, `/payments/callback/*`, `/payments/admin/*`
- `email-api`: `/email/layouts/*`

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker + Docker Compose (recomendado para execução completa)
- Acesso a instância MongoDB

## Configuração de ambiente

Crie um arquivo `.env` na raiz para uso com `docker-compose.yml`:

```env
DATABASE_USERNAME=seu_usuario
DATABASE_PASSWORD=sua_senha
DATABASE_URL=seu_cluster.mongodb.net
INTERNAL_SECRET=um_segredo_interno_forte
RESEND_API_KEY=sua_chave_resend
CLOUDFLARE_TUNNEL_TOKEN=seu_token_cloudflare
```

Observações:

- Os serviços também usam `SPRING_KAFKA_BOOTSTRAP_SERVERS` (no `docker-compose` já configurado para `kafka:29092`).
- O gateway usa `AUTH_SERVICE_URL`, `WALLET_SERVICE_URL`, `PAYMENTS_SERVICE_URL` e `EMAIL_SERVICE_URL`.

## Como executar

### Opção 1 (recomendada): Docker Compose

```bash
docker compose up -d
```

Gateway exposto em: `http://localhost:4401`

### Opção 2: execução local com Maven

Na raiz:

```bash
mvn clean install
```

Executando módulos individualmente:

```bash
mvn -pl auth-api spring-boot:run
mvn -pl wallet-api spring-boot:run
mvn -pl payments-api spring-boot:run
mvn -pl email-api spring-boot:run
mvn -pl gateway-api spring-boot:run
```

## Notas importantes para versão open source

- Trate toda chave/token como segredo e use variáveis de ambiente.
- Antes de publicar oficialmente, revise credenciais presentes em arquivos de configuração e faça rotação de qualquer chave já exposta.
- Este repositório é disponibilizado para estudo de arquitetura, organização e implementação de um backend distribuído.

## Limitações conhecidas

- Projeto sem evolução futura (estado final).
- Pode exigir ajustes para ambientes de produção atuais.
- Não foi preparado como produto mantido em comunidade.

## Contribuições

Este projeto está arquivado. Issues e PRs podem ser abertos para discussão técnica, mas não há garantia de manutenção ativa.

## Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo `LICENSE`.

