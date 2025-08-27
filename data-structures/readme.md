### Modelos de Dados no Redis

> Resumido e/ou Traduzido do livro.
> Obrigado GPT!!!


O uso de qualquer tipo de armazenamento de dados exige a tomada de decisões sobre como representar os dados dentro desse armazenamento. Esse modelo, por sua vez, controla como os dados são adicionados ao banco de dados e como são recuperados.

Os dados são armazenados no Redis usando chaves. As chaves podem ser praticamente qualquer coisa, pois são binárias e seguras. Por exemplo, você poderia usar uma imagem como chave. No entanto, a maioria das chaves são strings simples.

O Redis possui uma variedade de comandos para trabalhar com dados de diferentes tipos. Alguns comandos notáveis são abordados nesta seção, incluindo SET e GET. O comando SET cria ou altera um valor correspondente a uma determinada chave. O comando GET recupera o valor associado a uma determinada chave.

Os valores são sobrescritos com o comando SET. Isso significa que, se você chamar SET duas vezes para a mesma chave, o último valor será o que será armazenado e recuperado.

Os valores correspondentes a uma determinada chave podem ser formatados de várias maneiras para criar um modelo de dados específico para as necessidades da organização. Esta seção examina os principais modelos de dados no Redis.


#### **Strings e BitMaps**

O tipo de valor mais simples no Redis é uma *string*. Um valor pode ser adicionado à base de dados com o comando `SET`. Ao usar o comando `SET`, uma chave e um valor são os requisitos mínimos para criar a entrada. Por exemplo, para criar uma chave chamada `user` com o valor `steve`, basta executar este comando a partir da interface de linha de comando (CLI) do Redis:

```
> SET user "steve"
```

Embora aspas duplas tenham sido usadas para este valor de string, elas não são estritamente necessárias quando o valor é uma única palavra. Com esse comando, um valor simples de string `steve` foi armazenado na base de dados e pode ser recuperado com o comando `GET`:

```
> GET user
```

Este comando recupera o seguinte valor:

```
"steve"
```

Diversos outros comandos podem ser executados, e alguns fazem sentido num determinado contexto.
Por exemplo, uma forma comum de usar valores de string simples é como um contador.
Nestes casos, comandos como `INCR` (abreviatura de *increment*) podem ser usados. Considere este exemplo:

```
SET logincount 1
```

Neste comando, uma nova chave chamada `logincount` é criada e definida com o valor 1. Em seguida, chama-se `INCR` nessa chave:

```
INCR logincount
```

Quando `INCR` é executado, o novo valor é retornado imediatamente:

```
(integer) 2
```

Claro, pode sempre recuperar o valor com o comando `GET`:

```
GET logincount
```

Fazendo isso, retorna o seguinte:

```
"2"
```

Pode manipular muitos outros comandos e trabalhar com dados do tipo string no Redis, embora não possa usar comandos destinados a dados numéricos em dados de string.

Estreitamente relacionados com strings estão os *bitmaps*, que são uma forma de armazenamento de string.

Usando um bitmap, pode representar muitos elementos de dados que estão simplesmente ligados (1) ou desligados (0).

Isto é útil para operações onde só precisa de saber esses dois valores possíveis, como se um utilizador está ativo ou inativo.

Como só pode ser um de dois valores, pode representar esses dados de forma eficiente.

O tamanho máximo para um único valor de string é 512MB. Isto significa que pode armazenar 2³² valores possíveis dentro de um único valor de string no Redis.

Este limite de tamanho estará a aumentar e pode já ter aumentado no momento em que está a ler isto.

Consulte a documentação mais recente do Redis para ver o limite de tamanho atual para valores de string.

Existem comandos específicos para trabalhar com bitmaps disponíveis no Redis. 
Estes comandos incluem `SETBIT` e `GETBIT`, que são usados para criar/alterar um valor e recuperar um valor, respetivamente. Outros comandos incluem `BITOP` e `BITFIELD`.


#### **Lists**

As **listas** são uma forma de armazenar dados relacionados. Em alguns contextos, as listas são chamadas de *arrays*, mas no Redis, uma lista é uma *lista ligada* (*linked list*), o que significa que as operações de escrita na lista são muito rápidas. 

No entanto, dependendo da localização do item na lista, o seu desempenho não é tão rápido para operações de leitura. Embora nem sempre seja apropriado devido a valores repetidos, um *set* (conjunto, discutido mais adiante) pode por vezes ser usado quando a velocidade de leitura é crucial.

As listas usam uma chave que contém vários valores ordenados, e os valores são armazenados como *strings*. Pode adicionar valores à cabeça ou à cauda (chamados *left* e *right* no Redis) de uma lista e recuperar valores pelo seu índice.

Os valores dentro de uma lista podem repetir-se, o que significa que pode ter o mesmo valor em índices diferentes dentro da lista.

Pode adicionar um valor a uma lista com os comandos `LPUSH` e `RPUSH`, que colocam valores numa lista, seja à esquerda (ou cabeça) ou à direita (ou cauda) da lista. Por exemplo, criar uma lista com dois itens tem este aspeto:

```
LPUSH users steve bob
```

Para adicionar no final use:

```
RPUSH users claudio
```

A lista contém agora dois itens, indexados a partir do 0. Um item individual pode ser recuperado usando o comando `LINDEX`. Por exemplo, recuperar o primeiro item da lista tem este aspeto:

```
LINDEX users 0
```

Recuperar o segundo item tem este aspeto:

```
LINDEX users 1
```

Se tentar recuperar um índice que não existe, receberá `(nil)` como resultado.

Todos os itens ou apenas uma parte deles podem ser recuperados com o comando `LRANGE`. O comando `LRANGE` espera receber o primeiro e o último índice a recuperar, por número. Se quiser recuperar todos os itens da lista `users`, tem este aspeto:

```
LRANGE users 0 -1
```

Note o uso do `-1` como segundo valor. O `-1` significa "até ao final da lista".

O resultado do comando `LRANGE` para a lista `users` é o seguinte:

```
1) "bob"
2) "steve"
```

Além disso, é importante notar que, como foi usado `LPUSH`, o último item, `bob`, tornou-se o topo da lista, ou item 1 (índice 0). Se esta lista tivesse sido criada com `RPUSH`, então `bob` seria a cauda da lista, ou item 2 (índice 1).


#### **SET**

Do ponto de vista de uma aplicação, os sets são um pouco semelhantes às listas, pois você usa uma única chave para armazenar múltiplos valores. No entanto, ao contrário das listas, os sets não são recuperados por um número de índice e não são ordenados.

Em vez disso, você consulta para ver se um membro existe no sets. Também diferentemente das listas, os conjuntos não podem ter membros repetidos dentro da mesma chave.

O Redis gerencia o armazenamento interno dos conjuntos. O resultado é que você não trabalha com os valores do conjunto da mesma forma que com as listas. 

Por exemplo, você não pode fazer push e pop no início e no final de um conjunto como pode fazer com uma lista.

Você pode adicionar um valor a um conjunto com o comando `SADD`:

```
SADD frutas apple
```

Você pode listar todos os membros de um conjunto com o comando `SMEMBERS`:

```
SMEMBERS frutas
```

Dado que a chave chamada "frutas" existe, o comando retorna uma lista de todos os membros desse conjunto. Neste caso, o único item retornado é o seguinte:

```
1) "apple"
```

Você pode determinar se um determinado valor existe em um conjunto com o comando `SISMEMBER`. Por exemplo, para ver se um valor chamado "apple" existe na chave "frutas", você usaria o seguinte comando:

```
SISMEMBER frutas apple
```

Se o membro existir no conjunto, um inteiro `1` é retornado. Se o membro não existir, um inteiro `0` é retornado.


#### Hashes/Tabela hash

Os hashes são usados para armazenar coleções de pares chave/valor. Diferencie um hash de um tipo de dados string simples, onde há um valor correspondente a uma chave. 

Um hash possui uma chave, mas, dentro dessa estrutura, há mais campos e valores.

Você pode usar um hash para armazenar o estado atual de um objeto em uma aplicação. Por exemplo, ao armazenar informações sobre uma casa à venda, uma estrutura lógica poderia ser assim:

```
houseID: 5150
numBedrooms: 3
squareFeet: 2700
hvac: forced air
```

Representar essa estrutura com um hash do Redis tem a seguinte aparência:

```
HSET house:5150 numBedrooms 3 squareFeet 2700 hvac "forced air"
```

Campos individuais dentro do hash global `house:5150` são recuperados com o comando `HGET`. Para recuperar o valor do campo `numBedrooms`, use este comando:

```
HGET house:5150 numBedrooms
```

O resultado é o seguinte:

```
"3"
```


#### Sorted sets


Sets ordenados são usados para armazenar dados que precisam ser classificados, como uma tabela de líderes (leaderboard).

Como um hash, uma única chave armazena vários membros.

A pontuação (score) de cada membro é um número.

Por exemplo, se você estivesse rastreando o número de seguidores de um grupo de usuários, os dados poderiam ser assim:

```
Usuário Seguidores:
steve: 31
owen: 2
jakob: 13
```

Dentro do Redis, esses dados podem ser recriados como um conjunto ordenado com o seguinte comando:

```
ZADD userFollowers 31 steve 2 owen 13 jakob
```

O comando `ZRANGE` é usado para recuperar o conjunto ordenado resultante. Como o comando `LRANGE`, que é usado para recuperar valores de uma lista, o comando `ZRANGE` aceita os números inicial e final para a recuperação. Por exemplo, você pode recuperar todos os membros de um conjunto ordenado assim:

```
ZRANGE userFollowers 0 -1
```

Quando esse comando é executado, os membros são recuperados, mas não as pontuações correspondentes. Para recuperar tanto os nomes dos membros quanto suas pontuações, adicione o argumento `WITHSCORES` ao comando, assim:

```
ZRANGE userFollowers 0 -1 WITHSCORES
```

Quando esse comando é executado com o conjunto de dados inserido anteriormente, o resultado é:

```
1) "owen"
2) "2"
3) "jakob"
4) "13"
5) "steve"
6) "31"
```

Como você pode ver na saída do `ZRANGE`, os membros e suas pontuações são classificados pelo valor da pontuação, do menor para o maior. Você também pode recuperar os membros e suas pontuações em ordem inversa (ou seja, do maior para o menor) com o comando `ZREVRANGE`:

```
ZREVRANGE userFollowers 0 -1 WITHSCORES
```

A pontuação de um membro individual pode ser incrementada por qualquer número válido com o comando `ZINCRBY`. Por exemplo, para incrementar o nome de usuário "jakob" em 20, o comando seria o seguinte:

```
ZINCRBY userFollowers 20 jakob
```

A pontuação resultante é retornada, então, neste caso, o valor retornado representa os 13 seguidores originais mais 20:

```
"33"
```

O resultado do `ZRANGE` ou `ZREVRANGE` também refletirá a mudança no número de seguidores.

Outra forma de trabalhar com dados em um conjunto ordenado é usar o comando `ZRANK` para determinar onde, dentro do conjunto ordenado, um determinado membro reside.

#### HyperLogLog

O HyperLogLog é um tipo de dados especializado, mas muito útil, no Redis. Um HyperLogLog é usado para manter uma contagem estimada de itens únicos. Você pode usar o tipo de dados HyperLogLog para rastrear uma contagem geral de visitantes únicos em um site.

O tipo de dados HyperLogLog mantém um hash interno para determinar se já viu o valor anteriormente. Se já tiver visto, o valor não é inserido na base de dados.

O comando `PFADD` é usado tanto para criar uma chave quanto para adicionar itens a uma chave HyperLogLog:
```
PFADD visitantes 127.0.0.1
```

Se esta for a primeira vez que o valor `127.0.0.1` é visto na chave "visitantes", então um valor inteiro `1` é retornado para indicar uma adição bem-sucedida à base de dados. Um `0` é retornado se o valor já existir.

O comando `PFCOUNT` é usado para fornecer uma estimativa do número de itens únicos dentro de um HyperLogLog.