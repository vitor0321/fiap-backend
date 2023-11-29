package br.com.fiap.api.utils;

import br.com.fiap.api.model.Mensagem;

public abstract class MensagemHelper {

    public static Mensagem gerarMensagem() {
        return Mensagem.builder()
                .usuario("José")
                .conteudo("Conteudo da mensagem")
                .build();
    }
}
