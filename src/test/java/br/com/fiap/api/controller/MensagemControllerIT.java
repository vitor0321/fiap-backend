package br.com.fiap.api.controller;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static br.com.fiap.api.utils.MensagemHelper.gerarMensagem;
import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase
public class MensagemControllerIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Nested
    class RegistrarMensagem {

        @Test
        void devePermitirRegistrarMensagem() {
            var mensagem = gerarMensagem();

            given()
                    .filters(new AllureRestAssured())
                    .contentType(MediaType.APPLICATION_JSON_VALUE).body(mensagem)
//                    .log().all()
                    .when().post("/mensagens")
                    .then().statusCode(HttpStatus.CREATED.value());
//                    .log().all();
        }

        @Test
        void deveGerarExcecao_QuandoRegistrarMensagem_PayloadXML() {
            //Arrange

            //Act


            //Assert
        }
    }

    @Nested
    class BuscarMensagem {
        @Test
        void devePermitirBuscarMensagem()  {

            //Arrange

            //Act


            //Assert
        }

        @Test
        void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExiste()  {

            //Arrange

            //Act


            //Assert
        }
    }

    @Nested
    class AlterarMensagem {
        @Test
        void devePermitirAlterarMensagem()  {
            //Arrange

            //Act


            //Assert
        }

        @Test
        void deveGerarExcecao_QuandoAlterarMensagem_IdNaoExiste() {

            //Arrange

            //Act


            //Assert
        }

        @Test
        void deveGerarExcecao_QuandoAlterarMensagem_IdNaoSaoOsMesmos()  {

            //Arrange

            //Act


            //Assert
        }

        @Test
        void deveGerarExcecao_QuandoAlterarMensagem_PayloadXML()  {
            //Arrange

            //Act


            //Assert
        }
    }

    @Nested
    class RemoverMensagem {
        @Test
        void devePermitirRemoverMensagem()  {
            //Arrange

            //Act


            //Assert
        }

        @Test
        void deveGerarExcecao_QuandoRemoverMensagem_IdNaoExiste()  {
            //Arrange

            //Act


            //Assert
        }
    }

    @Nested
    class ListarMensagens {
        @Test
        void devePermitirListarMensagens()  {

            //Arrange

            //Act


            //Assert


        }

        @Test
        void devePermitirListarMensagens_QuandoNaoInformadoPaginacao(){

            //Arrange

            //Act


            //Assert
        }
    }

}
