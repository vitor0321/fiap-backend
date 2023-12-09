# language: pt

Funcionalidade: Mensagem

  @smoke @high
  Cenario: Registrar Mensagem
    Quando registrar uma nova mensagem
    Então a mensagem é registrada com sucesso
    E deve ser apresentada

  @smoke
  Cenario: Buscar Mensagem
    Dado que uma mensagem já foi publicada
    Quando efetuar a busca da mensagem
    Então a mensagem é exibida com sucesso

  @low
  Cenario: Alterar mensagem
    Dado que uma mensagem já foi publicada
    Quando efetuar a requisição par aalterar mensagem
    Então a mensegem é a atualizada com sucesso
    E deve ser apresentada

  @high @slow
  Cenario: Remover mensagem
    Dado que uma mensagem já foi publicada
    Quando requisitar a remoção da mensagem
    Então a mensagem é removida com sucesso