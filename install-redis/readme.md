> Eu,
> Nessa pratica nós sempre usaremos o docker
> Mas pode fazer com o redis cloud e conectar com a extenção [redis-vscode](https://redis.io/docs/latest/develop/tools/#redis-vscode-extension) ou [redis-insight](https://redis.io/docs/latest/develop/tools/#redis-insight).
> Vamos usar o redis-for-vscode ou o redis-cli. Use:
```bash
sudo docker run --name my-redis -p 6379:6379 redis 
```

<img src="redis-insight.png" alt="redis-insight image gui" width="256px" /><img src="redis-vscode.png" alt="redis-vscode extension image use tab" width="256px"/>


# Implantando o Redis

> Resumido do livro.
> Obrigado GPT!!!

Existem três formas principais de instalar o Redis:

## ☁️ 1. Redis Enterprise Cloud (Nuvem)
- **Plano gratuito**: 30MB disponível
- **Onde**: AWS, Google Cloud ou Microsoft Azure
- **Como**: 
  1. Acesse https://redislabs.com/try-free
  2. Escolha a nuvem e região
  3. Selecione a opção gratuita de 30MB
  4. Configure parâmetros como nome, segurança e módulos

**Exemplo**: Ideal para quem quer testar rapidamente sem instalar nada localmente.

## 💻 2. Instalação Manual (Código Fonte)
**Para Linux/Ubuntu:**

```bash
# Baixe e descompacte
wget https://download.redis.io/redis-stable.tar.gz
tar -xzf redis-stable.tar.gz
cd redis-stable

# Compile
make

# Teste
make test

# Instale (opcional)
sudo make install

# Execute
./src/redis-server
```

**Observação**: Compilar para 32 bits economiza memória, mas limita para 4GB de RAM.

## 🐳 3. Usando Docker
**Comandos básicos:**

```bash
# Baixe a imagem
docker pull redis

# Execute o container
docker run --name meu-redis -p 6379:6379 -d redis

# Exemplo prático: rodar com persistência
docker run --name meu-redis -p 6379:6379 -v ./dados:/data -d redis
```

**Vantagem**: Rápido, isolado e fácil de remover depois.

---

### 💡 Dica Inicial
Para começar rapidamente:
- Use a **nuvem gratuita** para testes simples
- Use **Docker** para desenvolvimento local
- **Compile manualmente** apenas se precisar de recursos específicos

O Redis estará pronto na porta 6379 após qualquer uma dessas instalações!