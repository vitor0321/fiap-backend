package br.com.fiap.api.controller;

import br.com.fiap.api.exception.MensagemNotFoundException;
import br.com.fiap.api.model.Mensagem;
import br.com.fiap.api.service.MensagemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static br.com.fiap.api.utils.MensagemHelper.gerarMensagem;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                })
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
        void deveGerarExcecao_QuandoAlterarMensagem_IdNaoExiste() throws Exception {

            //Arrange
            var id = UUID.fromString("9a092684-62f4-4c7e-8372-dfd784581711");
            var mensagem = gerarMensagem();
            mensagem.setId(id);
            var conteudoDaExcecao = "Mensagem não encontrada";
            when(mensagemService.alterarMensagem(id, mensagem))
                    .thenThrow(new MensagemNotFoundException(conteudoDaExcecao));
            //Assert
            mockMvc.perform(put("/mensagens/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(mensagem)))
//                    .andDo(print())
                    .andExpect(status().isBadRequest());
//                    .andExpect(content().string(conteudoDaExcecao));

            verify(mensagemService, times(1))
                    .alterarMensagem(any(UUID.class), any(Mensagem.class));
        }

        @Test
        @Description("valida o cenário de excecao quando alterar mensagem")
        void deveGerarExcecao_QuandoAlterarMensagem_IdNaoSaoOsMesmos() throws Exception {

            //Arrange
            var id = UUID.fromString("9a092684-62f4-4c7e-8372-dfd784581711");
            var mensagem = gerarMensagem();
            mensagem.setId(UUID.fromString("9a092684-62f4-4c7e-3372-dfd784581712"));
            var conteudoDaExcecao = "Mensagem atualizada não apresenta o ID correto";
            when(mensagemService.alterarMensagem(id, mensagem))
                    .thenThrow(new MensagemNotFoundException(conteudoDaExcecao));
            //Assert
            mockMvc.perform(put("/mensagens/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(mensagem)))
//                    .andDo(print())
                    .andExpect(status().isBadRequest());
//                    .andExpect(content().string(conteudoDaExcecao));

            verify(mensagemService, times(1))
                    .alterarMensagem(any(UUID.class), any(Mensagem.class));
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        void deveGerarExcecao_QuandoAlterarMensagem_PayloadXML() throws Exception {
            //Arrange
            var id = UUID.fromString("cacd563e-34ab-41da-b34a-0d9a191b91f4");

            String xmlPayload = "<mensagem><id>" + id + "</id><usuario>Ana</usuario>Mensagem do Conteudo<conteudo></conteudo></mensagem>";

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
        void devePermitirRemoverMensagem() throws Exception {
            //Arrange
            var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bb");

            when(mensagemService.removerMensagem(id)).thenReturn(true);
            //Act
            mockMvc.perform(delete("/mensagens/{id}", id))
                    .andExpect(status().isOk());
//                    .andExpect(content().string("mensagem removida"));

            //Assert
            verify(mensagemService, times(1)).removerMensagem(id);
        }

        @Test
        void deveGerarExcecao_QuandoRemoverMensagem_IdNaoExiste() throws Exception {
            //Arrange
            var id = UUID.fromString("5874303c-c837-4354-bc69-10805f1eb4bd");

            when(mensagemService.removerMensagem(id)).thenThrow(new MensagemNotFoundException("mensagem nao encontrada"));

            //Act
            mockMvc.perform(delete("/mensagens/{id}", id))
                    .andExpect(status().isBadRequest());
//                    .andExpect(content().string("mensagem nao encontrada"));

            //Assert
            verify(mensagemService, times(1)).removerMensagem(id);
        }
    }

    @Nested
    class ListarMensagens {
        @Test
        void devePermitirListarMensagens() throws Exception {

            //Arrange
            var mensagem = gerarMensagem();
            var page = new PageImpl<>(Collections.singletonList(mensagem));

            when(mensagemService.listarMensagem(any(Pageable.class))).thenReturn(page);

            //Act


            //Assert
            mockMvc.perform(get("/mensagens")
                            .param("page", "0")
                            .param("size", "10"))
//                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", not(empty())))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));

        }

        @Test
        void devePermitirListarMensagens_QuandoNaoInformadoPaginacao() throws Exception {

            //Arrange
            var mensagem = gerarMensagem();
            var page = new PageImpl<>(Collections.singletonList(mensagem));

            when(mensagemService.listarMensagem(any(Pageable.class))).thenReturn(page);

            //Assert
            mockMvc.perform(get("/mensagens"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", not(empty())))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));

        }
    }

    public static String asJsonString(final Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        return objectMapper.writeValueAsString(object);
    }
}