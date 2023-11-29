package br.com.fiap.api.service;

import br.com.fiap.api.exception.MensagemNotFoundException;
import br.com.fiap.api.model.Mensagem;
import br.com.fiap.api.repository.MensagemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static br.com.fiap.api.utils.MensagemHelper.gerarMensagem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MensagemServiceTest {

    private MensagemService mensagemService;

    @Mock
    private MensagemRepository mensagemRepository;

    AutoCloseable mock;

    @BeforeEach
    void setUp() {
        mock = MockitoAnnotations.openMocks(this);
        mensagemService = new MensagemServiceImpl(mensagemRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    @Test
    void devePermitirRegistrarMensagem() {
        //Arrange
        var mensagem = gerarMensagem();

        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(i -> i.getArgument(0));

        //Act
        var mensagemRegistrada = mensagemService.registrarMensagem(mensagem);

        //Assert
        assertThat(mensagemRegistrada).isInstanceOf(Mensagem.class).isNotNull();
        assertThat(mensagemRegistrada.getConteudo()).isEqualTo(mensagem.getConteudo());
        assertThat(mensagemRegistrada.getUsuario()).isEqualTo(mensagem.getUsuario());
        assertThat(mensagemRegistrada.getId()).isNotNull();
        verify(mensagemRepository, times(1)).save(mensagem);
    }

    @Test
    void devePermitirBuscarMensagem() {

        //Arrange
        var id = UUID.fromString("b8079c96-f9a7-4f36-8112-cbaf54495f4d");
        var mensagem = gerarMensagem();
        mensagem.setId(id);

        when(mensagemRepository.findById(id)).thenReturn(Optional.of(mensagem));

        //Act
        var mensagemOprional = mensagemService.buscarMensagem(id);

        //Assert
        assertThat(mensagemOprional).isEqualTo(mensagem);
        verify(mensagemRepository, times(1)).findById(id);
    }

    @Test
    void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExiste() {

        //Arrange
        var id = UUID.fromString("bd04f514-599c-47fa-9f80-aea2979c580c");

        when(mensagemRepository.findById(id)).thenReturn(Optional.empty());

        //Act

        //Assert
        assertThatThrownBy(() -> mensagemService.buscarMensagem(id))
                .isInstanceOf(MensagemNotFoundException.class)
                .hasMessage("Mensagem não encontrada");
        verify(mensagemRepository, times(1)).findById(id);
    }

    @Test
    void devePermitirAlterarMensagem() {
        //Arrange
        var id = UUID.fromString("d4e7c5a4-168d-4784-bedf-2ac92e018462");

        var mensagemAntiga = gerarMensagem();
        mensagemAntiga.setId(id);

        when(mensagemRepository.findById(id)).thenReturn(Optional.of(mensagemAntiga));

        var mensagemNova = new Mensagem();
        mensagemNova.setId(mensagemAntiga.getId());
        mensagemNova.setUsuario(mensagemAntiga.getUsuario());
        mensagemNova.setConteudo("ABCD 12345");

        when(mensagemRepository.save(any(Mensagem.class)))
                .thenAnswer(i -> i.getArgument(0));
        //Act
        var mensagemObtida = mensagemService.alterarMensagem(id, mensagemNova);

        //Assert
        assertThat(mensagemObtida).isInstanceOf(Mensagem.class).isNotNull();
        assertThat(mensagemObtida.getId()).isEqualTo(mensagemNova.getId());
        assertThat(mensagemObtida.getConteudo()).isEqualTo(mensagemNova.getConteudo());
        verify(mensagemRepository, times(1)).findById(id);
        verify(mensagemRepository, times(1)).save(mensagemAntiga);
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdNaoExiste() {

        //Arrange
        var id = UUID.fromString("9a092684-62f4-4c7e-8372-dfd784581711");

        var mensagem = gerarMensagem();
        mensagem.setId(id);

        when(mensagemRepository.findById(id)).thenReturn(Optional.empty());

        //Act

        //Assert
        assertThatThrownBy(() -> mensagemService.alterarMensagem(id, mensagem))
                .isInstanceOf(MensagemNotFoundException.class)
                .hasMessage("Mensagem não encontrada");
        verify(mensagemRepository, times(1)).findById(id);
        verify(mensagemRepository, never()).save(mensagem);
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdNaoSaoOsMesmos() {

        //Arrange
        var id = UUID.fromString("3d958fa3-c5cd-4165-90c0-4dcbe2dd2067");

        var mensagemAntiga = gerarMensagem();
        mensagemAntiga.setId(id);

        when(mensagemRepository.findById(id)).thenReturn(Optional.of(mensagemAntiga));

        var idMensagemAlterda = UUID.fromString("fea47f06-ec28-4cc5-84c0-4f19d224a025");
        var mensagemNova = new Mensagem();
        mensagemNova.setId(idMensagemAlterda);
        mensagemNova.setUsuario(mensagemAntiga.getUsuario());
        mensagemNova.setConteudo("ABCD 12345");

        //Act

        //Assert
        assertThatThrownBy(() -> mensagemService.alterarMensagem(id, mensagemNova))
                .isInstanceOf(MensagemNotFoundException.class)
                .hasMessage("Mensagem atualizada não apresenta o ID correto");
        verify(mensagemRepository, times(1)).findById(id);
        verify(mensagemRepository, never()).save(mensagemNova);
    }

    @Test
    void devePermitirRemoverMensagem() {
        //Arrange
        var id = UUID.fromString("3b6fb9c7-c629-4c1a-a059-13a79c5b6809");
        var mensagem = gerarMensagem();
        mensagem.setId(id);

        when(mensagemRepository.findById(id)).thenReturn(Optional.of(mensagem));
        doNothing().when(mensagemRepository).deleteById(id);

        //Act
        var mensagemFoirRemovida = mensagemService.removerMensagem(id);

        //Assert
        assertThat(mensagemFoirRemovida).isTrue();
        verify(mensagemRepository, times(1)).findById(id);
        verify(mensagemRepository, times(1)).deleteById(id);
    }

    @Test
    void deveGerarExcecao_QuandoRemoverMensagem_IdNaoExiste() {
        //Arrange
        var id = UUID.fromString("6967393c-2255-4d69-b405-e3946d073283");

        when(mensagemRepository.findById(id)).thenReturn(Optional.empty());

        //Act

        //Assert
        assertThatThrownBy(() -> mensagemService.removerMensagem(id))
                .isInstanceOf(MensagemNotFoundException.class)
                .hasMessage("Mensagem não encontrada");
        verify(mensagemRepository, times(1)).findById(id);
        verify(mensagemRepository, never()).deleteById(id);
    }

    @Test
    void devePermitirListarMensagens() {

        //Arrange
        Page<Mensagem> listaMensagens = new PageImpl<>(Arrays.asList(
                gerarMensagem(),
                gerarMensagem()
        ));
        when(mensagemRepository.listarMensagens(any(Pageable.class)))
                .thenReturn(listaMensagens);

        //Act
        var resultadoObtido = mensagemService.listarMensagem(Pageable.unpaged());

        //Assert
        assertThat(resultadoObtido).hasSize(2);
        assertThat(resultadoObtido.getContent())
                .asList()
                .allSatisfy(mensagem -> {
                    assertThat(mensagem)
                            .isNotNull()
                            .isInstanceOf(Mensagem.class);
                });
        verify(mensagemRepository, times(1)).listarMensagens(any(Pageable.class));
    }
}