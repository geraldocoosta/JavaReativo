# Java Reativo com Spring

## O problema da responsividade

Suponde que uma requição leva em média 200 ms (5 RPS) e temos um servidor tomcat com 300 threads no pool, podemos concluir que suportamos 1500 requisições por segundo

O que acontece quando recebemos 10000 requisições em um segundo? (Por exemplo)

O servidor vai ficar indisponivel

O sistema perde sua responsividade, primeiro a latencia aumenta, depois vem a indiposnibilidade

**E se aumentarmos a quantidade de threads?**

Um servidor convencional (tomcat) funciona da seguinte forma

O tomcat tem uma thread para buscar conexões de entrada (a cada req que recebe), e depois manda isso para uma thread.

O problema é que o tomcat se limita a esse número de threads (que tem no pool), e se o número de threads chega ao limite, ele não consegue mais conectar. E tem que esperar outras requisições terminarem. Isso torna "a fila de espera" muito grande.

Com a fila de espera grande, começam a ocorrer timeouts, eventualmente, todos tomam timeout, o tomcat não consegue gerenciar mais isso, e ai vem a indisponibilidade.

**E se a gente aumenta o numero de tomcats?**

Ai é problema que não estamos usando muito bem os recursos.

**Precisamos de elasticidade então**

Como?

- Com lightweight threads (go routines por exemplo)
- Reatividade a nível inter-service communication (mensageria)
- Reatividade a nível de service (Programação Reativa)

## Fundamentos da Programação Reativa

Primeiramente, não é um paradigma de programação

Reactive Programming != Reactive Systems (manifesto) (Manifesto reativo é um buzzword pra facilitar algumas caracteristicas dos sistemas de hoje em dia como, fall tolerence, async, non blocking,  pull vs push, distributed, etc)

É um modelo de programação assíncrono onde o fluxo é composto por fontes observáveis e reações a eventos sem sacrificio.

- É dividido em dois mundos:
  - O event-loop (I/O reactor, Netty, AsyncHttp)
  - A implementação do reactive-extensions (RxJava, Reactor, Mutiny) Especificação criada em cima da programação reativa (Pesquisar essa especificação)

## Caracteristicas da programação Reativa

Tudo é com base em publisher (fonte observavel que publica eventos, pai de todos) e subscriber

- Publisher
  - Multi (0..N ou erro) - Flux, Mono -> Publica 0~mais eventos ou erro
  - Single (0..1 ou erro) - Observable, Single -> Publica 0~1 evento ou erro
- Lazy evaluation
  - Nada acontece até que alguém se inscreva no Publisher -> Gerador de confusão
- Hot vs Cold publishers
  - Hot -> o publisher continua publicando e não volta por quem perdeu eventos
  - Cold -> o publiser é para o subscriver, então ele pode voltar quando perder eventos
- Schedulers ao invés de (low level threading)
  - Abstração para facilitar o que é low level threading
- Implementa o observer pattern
  - Tem uma semelhança com o iterable-iterator do Java
- Pull vs Push (+Backpressure)
  - BackPressure -> O subscriver precisa dar um pull pra dar feedback para o publisher de quantos eventos/mensagens ele (subscriber) pode processar
  - O subscriber pode ficar acumulando mensagens ou dropar as mensagens, caso o subscriber produza mais mensagens que o subscriber consiga consumir

Na programação reativa, o publisher empurra um valor quando tiver pronto, não é o subscriber que busca o valor

## Beneficios

Abstrai complexidade de lidar com thread, concorrencia, lock, latches, semafaros etc.

Abstrai o boilerplate tornando o código mais simples e permitindo focar no negocio, as bibliotecas são implementadas com base em programação funcional. A programação funcional também pode ser feita como no JS, com callbacks, porém isso pode gerar callback hell.

Melhor latência e throughput comparado ao convencional thread-per-request
