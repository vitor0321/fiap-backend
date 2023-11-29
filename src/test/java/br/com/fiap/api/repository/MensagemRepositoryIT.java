package br.com.fiap.api.repository;

import br.com.fiap.api.model.Mensagem;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import static br.com.fiap.api.utils.MensagemHelper.gerarMensagem;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class MensagemRepositoryIT {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Test
    void devePermitirCriarTabela() {
        var tatalDeRegistro = mensagemRepository.count();
        assertThat(tatalDeRegistro).isGreaterThan(0);
    }

    @Test
    void devePermitirRegistrarMensagem() {
        //Arrange
        var id = UUID.randomUUID();
        var mensagem = gerarMensagem();
        mensagem.setId(id);

        //Act
        var mensagemRecebida = registrarMensagem(mensagem);

        //Assert
        assertThat(mensagemRecebida)
                .isInstanceOf(Mensagem.class)
                .isNotNull();
        assertThat(mensagemRecebida).isEqualTo(mensagem);
    }

    @Test
    void devePermitirBuscarMensagem() {
        //Arrange
        var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bb");

        //Act
        var mensagemBuscada = mensagemRepository.findById(id);

        //Assert
        assertThat(mensagemBuscada).isPresent();
        mensagemBuscada.ifPresent(mensagemNotNull -> {
            assertThat(mensagemNotNull.getId()).isEqualTo(id);
        });

    }

    @Test
    void devePermitirRemoverMensagem() {
        //Arrange
        var id = UUID.fromString("870c5a73-d080-44e4-8cd8-32a6ddba2efb");

        //Act
        mensagemRepository.deleteById(id);
        var mensagemBuscada = mensagemRepository.findById(id);

        //Assert
        assertThat(mensagemBuscada).isNotPresent();
    }

    @Test
    void devePermitirListarMensagens() {
        //Arrange
        var resultadoObetidos= mensagemRepository.findAll();

        //Assert
        assertThat(resultadoObetidos).hasSize(3);
    }

    private Mensagem registrarMensagem (Mensagem mensagem){
       return mensagemRepository.save(mensagem);
    }
}
