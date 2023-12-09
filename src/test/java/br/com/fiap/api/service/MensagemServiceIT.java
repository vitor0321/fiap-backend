package br.com.fiap.api.service;

import br.com.fiap.api.exception.MensagemNotFoundException;
import br.com.fiap.api.model.Mensagem;
import br.com.fiap.api.repository.MensagemRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static br.com.fiap.api.utils.MensagemHelper.gerarMensagem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
class MensagemServiceIT {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private MensagemService mensagemService;

    @Nested
    class RegistrarMensagem {

        @Test
        void devePermitirRegistrarMensagem() {
            //Arrange
            var mensagem = gerarMensagem();

            //Act
            var mensagemRegistrada = mensagemService.registrarMensagem(mensagem);

            //Assert
            assertThat(mensagemRegistrada).isInstanceOf(Mensagem.class).isNotNull();
            assertThat(mensagemRegistrada.getConteudo()).isEqualTo(mensagem.getConteudo());
            assertThat(mensagemRegistrada.getUsuario()).isEqualTo(mensagem.getUsuario());
            assertThat(mensagemRegistrada.getId()).isNotNull();
            assertThat(mensagemRegistrada.getDataCriacao()).isNotNull();
            assertThat(mensagemRegistrada.getGostei()).isZero();
        }
    }

    @Nested
    class BuscarMensagem {
        @Test
        void devePermitirBuscarMensagem() {

            //Arrange
            var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bb");

            //Act
            var mensagemOprional = mensagemService.buscarMensagem(id);

            //Assert
            assertThat(mensagemOprional).isNotNull().isInstanceOf(Mensagem.class);
            assertThat(mensagemOprional.getId()).isNotNull().isEqualTo(id);
            assertThat(mensagemOprional.getUsuario()).isNotNull().isEqualTo("Adam");
            assertThat(mensagemOprional.getConteudo()).isNotNull().isEqualTo("Conteudo da mensagem 1");
            assertThat(mensagemOprional.getDataCriacao()).isNotNull();
            assertThat(mensagemOprional.getGostei()).isZero();
        }

        @Test
        void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExiste() {

            //Arrange
            var id = UUID.fromString("bd04f514-599c-47fa-9f80-aea2979c580c");

            //Assert
            assertThatThrownBy(() -> mensagemService.buscarMensagem(id))
                    .isInstanceOf(MensagemNotFoundException.class)
                    .hasMessage("Mensagem não encontrada");
        }
    }

    @Nested
    class AlterarMensagem {
        @Test
        void devePermitirAlterarMensagem() {
            //Arrange
            var id = UUID.fromString("870c5a73-d080-44e4-8cd8-32a6ddba2efb");

            var mensagemAtualizada = gerarMensagem();
            mensagemAtualizada.setId(id);

            //Act
            var mensagemObtida = mensagemService.alterarMensagem(id, mensagemAtualizada);

            //Assert
            assertThat(mensagemObtida).isInstanceOf(Mensagem.class).isNotNull();
            assertThat(mensagemObtida.getId()).isEqualTo(mensagemAtualizada.getId());
            assertThat(mensagemObtida.getConteudo()).isEqualTo(mensagemAtualizada.getConteudo());
        }

        @Test
        void deveGerarExcecao_QuandoAlterarMensagem_IdNaoExiste() {

            //Arrange
            var id = UUID.fromString("9a092684-62f4-4c7e-8372-dfd784581711");
            var mensagem = gerarMensagem();
            mensagem.setId(id);

            //Act

            //Assert
            assertThatThrownBy(() -> mensagemService.alterarMensagem(id, mensagem))
                    .isInstanceOf(MensagemNotFoundException.class)
                    .hasMessage("Mensagem não encontrada");
        }

        @Test
        void deveGerarExcecao_QuandoAlterarMensagem_IdNaoSaoOsMesmos() {

            //Arrange
            var id = UUID.fromString("cacd563e-34ab-41da-b34a-0d9a191b91f5");

            var mensagemAlterada = gerarMensagem();
            var idMensagemAlterda = UUID.fromString("fea47f06-ec28-4cc5-84c0-4f19d224a025");
            mensagemAlterada.setId(idMensagemAlterda);

            //Act

            //Assert
            assertThatThrownBy(() -> mensagemService.alterarMensagem(id, mensagemAlterada))
                    .isInstanceOf(MensagemNotFoundException.class)
                    .hasMessage("Mensagem atualizada não apresenta o ID correto");
        }
    }

    @Nested
    class RemoverMensagem {
        @Test
        void devePermitirRemoverMensagem() {
            //Arrange
            var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bb");

            //Act
            var mensagemFoirRemovida = mensagemService.removerMensagem(id);

            //Assert
            assertThat(mensagemFoirRemovida).isTrue();
        }

        @Test
        void deveGerarExcecao_QuandoRemoverMensagem_IdNaoExiste() {
            //Arrange
            var id = UUID.fromString("6967393c-2255-4d69-b405-e3946d073283");

            //Act

            //Assert
            assertThatThrownBy(() -> mensagemService.removerMensagem(id))
                    .isInstanceOf(MensagemNotFoundException.class)
                    .hasMessage("Mensagem não encontrada");
        }
    }

    @Nested
    class ListarMensagens {
        @Test
        void devePermitirListarMensagens() {

            //Arrange

            //Act
            var resultadoObtido = mensagemService.listarMensagem(Pageable.unpaged());

            //Assert
            assertThat(resultadoObtido).hasSize(3);
            assertThat(resultadoObtido.getContent())
                    .asList()
                    .allSatisfy(mensagem -> {
                        assertThat(mensagem)
                                .isNotNull()
                                .isInstanceOf(Mensagem.class);
                    });
        }
    }
}
