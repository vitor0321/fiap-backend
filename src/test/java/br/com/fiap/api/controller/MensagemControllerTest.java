package br.com.fiap.api.controller;

import br.com.fiap.api.exception.MensagemNotFoundException;
import br.com.fiap.api.model.Mensagem;
import br.com.fiap.api.service.MensagemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static br.com.fiap.api.utils.MensagemHelper.gerarMensagem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MensagemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MensagemService mensagemService;

    AutoCloseable mock;

    @BeforeEach
    void setup() {
        mock = MockitoAnnotations.openMocks(this);
        MensagemController mensagemController = new MensagemController(mensagemService);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(mensagemController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    @Nested
    class RegistrarMensagem {

        @Test
        void devePermitirRegistrarMensagem() throws Exception {
            //Arrange
            var mensagem = gerarMensagem();
            when(mensagemService.registrarMensagem(any(Mensagem.class))).thenAnswer(i -> i.getArgument(0));

            //Act
            mockMvc.perform(
                    post("/mensagens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(mensagem))
            ).andExpect(status().isCreated());

            //Assert
            verify(mensagemService, times(1)).registrarMensagem(any(Mensagem.class));
        }

        @Test
        void deveGerarExcecao_QuandoRegistrarMensagem_PayloadXML() throws Exception {
            //Arrange
            String xmlPayload = "<mensagem><usuario>Ana</usuario>Mensagem do Conteudo<conteudo></conteudo></mensagem>";

            //Act
            mockMvc.perform(
                    post("/mensagens")
                            .contentType(MediaType.APPLICATION_XML)
                            .content(xmlPayload)
            ).andExpect(status().isUnsupportedMediaType());

            //Assert
            verify(mensagemService, never()).registrarMensagem(any(Mensagem.class));
        }
    }

    @Nested
    class BuscarMensagem {
        @Test
        void devePermitirBuscarMensagem() throws Exception {

            //Arrange
            var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bb");
            var mensagem = gerarMensagem();
            when(mensagemService.buscarMensagem(any(UUID.class))).thenReturn(mensagem);

            //Act
            mockMvc.perform(
                    get("/mensagens/{id}", id)
            ).andExpect(status().isOk());

            //Assert
            verify(mensagemService, times(1)).buscarMensagem(any(UUID.class));
        }

        @Test
        void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExiste() throws Exception {

            //Arrange
            var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bc");
            when(mensagemService.buscarMensagem(id)).thenThrow(MensagemNotFoundException.class);

            //Assert
            mockMvc.perform(
                    get("/mensagens/{id}", id)
            ).andExpect(status().isBadRequest());
            verify(mensagemService, times(1)).buscarMensagem(id);
        }
    }

    @Nested
    class AlterarMensagem {
        @Test
        void devePermitirAlterarMensagem() throws Exception {
            //Arrange
            var id = UUID.fromString("870c5a73-d080-44e4-8cd8-32a6ddba2efb");

            var mensagemAtualizada = gerarMensagem();
            mensagemAtualizada.setId(id);
            when(mensagemService.alterarMensagem(id, mensagemAtualizada))
                    .thenAnswer(i -> i.getArgument(1));

            //Act
            mockMvc.perform(put("/mensagens/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(mensagemAtualizada)))
                    .andExpect(status().isAccepted());
            //Assert
            verify(mensagemService, times(1)).alterarMensagem(id, mensagemAtualizada);
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

        @Test
        void deveGerarExcecao_QuandoAlterarMensagem_PayloadXML() throws Exception {
            //Arrange
            var id = UUID.fromString("cacd563e-34ab-41da-b34a-0d9a191b91f4");

            String xmlPayload = "<mensagem><id>"+ id +"</id><usuario>Ana</usuario>Mensagem do Conteudo<conteudo></conteudo></mensagem>";

            //Act
            mockMvc.perform(
                    put("/mensagens/{id}", id)
                            .contentType(MediaType.APPLICATION_XML)
                            .content(xmlPayload)
            ).andExpect(status().isUnsupportedMediaType());

            //Assert
            verify(mensagemService, never()).alterarMensagem(any(UUID.class), any(Mensagem.class));
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

    public static String asJsonString(final Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        return objectMapper.writeValueAsString(object);
    }
}