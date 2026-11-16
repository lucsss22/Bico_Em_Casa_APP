package br.pucpr.trabalho;

import br.pucpr.trabalho.controllers.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class CadastroApp  extends  Application{

    @Override  
    public  void start(Stage stage)throws Exception { 
        MainController controller = new MainController(stage);
        controller.mostrar();
    }

    public static void main(String[] args){
        launch(args);
    }
    
}
