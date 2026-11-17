package br.pucpr.trabalho.controllers;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainController {
    private Stage stagePrincipal; 
    private Scene cena; 

    public MainController(Stage stagePrincipal){
        this.stagePrincipal = stagePrincipal;
        this.criarComponentes();
    }

    public void mostrar(){
        this.stagePrincipal.setTitle("Menu");
        this.stagePrincipal.setScene(this.cena);
        this.stagePrincipal.show();
    }

    private void criarComponentes(){
        Label lblTitulo = new Label("Usuários e Perfis");
        lblTitulo.setFont(new Font("Arial" , 22));

        Button btnIncluirUsuario = new Button("Incluir Usuario");
        btnIncluirUsuario.setOnAction(event ->{
            UsuarioController controller = new UsuarioController(this.stagePrincipal, this);
            controller.mostrar();
        });


        Button btnConsultarUsuario = new Button("Consultar Usuarios");
        btnConsultarUsuario.setOnAction(event -> {
            UsuarioConsultaController controller = new UsuarioConsultaController(this.stagePrincipal, this);
            controller.mostrar();
        });

        Button btnIncluirPerfil = new Button("Incluir Perfil");
        btnIncluirPerfil.setOnAction(event -> {
            PerfilController controller = new PerfilController(this.stagePrincipal, this);
            controller.mostrar();
        });

        Button btnConsultarPerfil = new Button("Consultar Perfis");
        btnConsultarPerfil.setOnAction(event -> {
            PerfilConsultaController controller = new PerfilConsultaController(this.stagePrincipal, this);
            controller.mostrar();
        });

        // Comentários e Pagamentos (Tayllor)
        Button btnComentarioPagamento = new Button("Comentários e Pagamentos");
        btnComentarioPagamento.setOnAction(event -> new ComentarioPagamentoController().mostrar());

        Button btnAvaliacoes = new Button("Avaliações");
        btnAvaliacoes.setOnAction(event -> new AvaliacaoController().mostrar());

        Button btnDenuncias = new Button("Denúncias");
        btnDenuncias.setOnAction(event -> new DenunciaController().mostrar());

        Button btnPortfolio = new Button("Portfólio");
        btnPortfolio.setOnAction(event -> new PortfolioController(this.stagePrincipal).mostrar());

        Button btnServicos = new Button("Serviços realizados");
        btnServicos.setOnAction(event -> new ServicoController(this.stagePrincipal).mostrar());

        Button btnSair = new Button("Sair");
        btnSair.setOnAction(event->this.stagePrincipal.close());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding:20; -fx-alignment: center;");
        layout.getChildren().addAll(lblTitulo, btnIncluirUsuario, btnConsultarUsuario, btnIncluirPerfil,
                btnConsultarPerfil, btnComentarioPagamento, btnAvaliacoes, btnDenuncias, btnPortfolio,
                btnServicos, btnSair);
        this.cena = new Scene(layout, 350, 610);
    }





    }

