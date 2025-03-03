package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class App extends Application {
    private Stage primaryStage;
    private double height;
    private double width;
    private File[] files;//size 3
    private Text[] texts_file;//size 3 too
    private Button[] file_buttons;//again !
    private String[] textPDFs;//you guessed it
    private ImageView[] file_icons;//you're still wondering ?
    private boolean[] goodPDFs;//i hope you're not that would be embarassing
    private List<String>[] parsedPDF;//you're not ? good
    private Text textSwitchButton;
    private SwitchButton switchButton;
    private Stage settingsStage;
    private String responseAI;
    private VBox scrollContent;
    private List<VBox> textsPrinted;//nope not this time unfortunately
    private List<String> responseParser;
    private Thread apiThread;

    private Properties properties;

    private String[] remove(String[] arr, int in) {
        if (arr == null || in < 0 || in >= arr.length) {
            return arr;
        }

        String[] arr2 = new String[arr.length - 1];

        for (int i = 0, k = 0; i < arr.length; i++) {
            if (i == in) continue;
            arr2[k++] = arr[i];
        }

        return arr2;
    }

    private boolean compareNames(String name1, String name2) {
        //comparer aussi les noms quand ils sont en entier
        String[] parts1 = name1.toLowerCase().trim().split(" ");
        String[] parts2 = name2.toLowerCase().trim().split(" ");

        if (parts1.length != parts2.length) {
            return false;
        }

        if (name1.equals(name2)) {
            return true;
        }

        for (int i = 0; i < 2; i++) {//on verifie deja si les deux ont les meme initiales
            if (parts1[i].charAt(0) != parts2[i].charAt(0)) {
                return false;//si ce n'est pas le cas on peut deja s'arreter
            }
        }

        if (parts1[0].length() == 1 && parts1[1].length() == 1) {//donc si name1 est des initiales
            return true;//car on a deja verifie les initiales
        }
        else if (parts1[0].length() == 1 && parts1[1].length() == 1) {//idem name2 = init
            return true;//idem verification plus haut
        }

        return false;
    }

    private boolean compareDates(String date1, String date2) {
        String[] parts1 = date1.split("/");
        String[] parts2 = date2.split("/");

        String year1 = "";
        String year2 = "";
        int placeYear1 = 0;
        int placeYear2 = 0;

        for (int i = 0; i < Math.min(parts1.length,parts2.length); i++) {
            if (parts1[i].length() == 4) {
                year1 = parts1[i];
                placeYear1 = i;
            }
            if (parts2[i].length() == 4) {
                year2 = parts2[i];
                placeYear2 = i;
            }
        }

        if (year1 != "" && year2 != "") {//si les deux on les annees
            if (placeYear1 == placeYear2) {//si meme notation des dates
                return date1.equals(date2);
            }
            else {//donc notation inversé des dates
                for (int i = 0; i < 3; i++) {
                    if (!parts1[i].equals(parts2[2-i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        else {//Si l'un a l'annee mais pas l'autre
            if (year1 != "") {
                parts1 = remove(parts1,placeYear1);
            }
            else if (year2 != "") {
                parts2 = remove(parts2,placeYear2);
            }
            return parts1[0].equals(parts2[0]) && parts1[1].equals(parts2[1]);
        }
    }

    private boolean isInResponse(String name, String role, String date, String otherRole) {
        String supposedOut = (name+" a le role : "+role+" et le role : "+otherRole+" le "+date);
        System.out.println("start = "+supposedOut);
        for (int i = 0; i < this.responseParser.size(); i++) {
            System.out.println(this.responseParser.get(i));
            if (supposedOut.equals(this.responseParser.get(i))) {
                return true;
            }
        }
        return false;
    }

    private String checkHasSmthAlrd(String name, String role, String date) {
        for (int i = 0; i < 3; i++) {
            if (this.goodPDFs[i]) {
                for (int j = 0; j < this.parsedPDF[i].size(); j++) {
                    String[] response = this.parsedPDF[i].get(j).split("_");
                    //si le nom est le meme et la date est la meme mais que ce n'est pas le meme role
                    if (compareNames(name,response[2]) && compareDates(response[0],date) && !response[1].equals(role)) {
                        if (!isInResponse(name,role,date,response[1])) {
                            return response[1];
                        }
                    }
                }
            }
        }
        return null;
    }

    private void compareFiles() {
        for (int i = 0; i < 3; i++) {
            if (this.goodPDFs[i]) {
                for (int j = 0; j < this.parsedPDF[i].size(); j++) {
                    String[] response = this.parsedPDF[i].get(j).split("_");
                    //something on the form : 22/2_president_timoth´┐¢e binder
                    //or 22/2_lecteur_nicolas espanet
                    String otherRole = checkHasSmthAlrd(response[2],response[1],response[0]);
                    if (otherRole != null) {
                        this.responseParser.add(response[2]+" a le role : "+response[1]+" et le role : "+otherRole+" le "+response[0]);
                    }
                }
                
            }
        }
    }

    private void putSize(Button button, ImageView img, double w, double h) {
        button.setMaxHeight(h);
        button.setMinHeight(h);
        button.setMaxWidth(w);
        button.setMinWidth(w);
        button.setPrefWidth(w);
        button.setPrefHeight(h);

        img.setFitWidth(w);
        img.setFitHeight(h);
    }

    private Button createButton(String filePath, double widthBtn, double heightBtn, double xCoord, double yCoord, ImageView icon) {
        Image img = new Image(getClass().getResourceAsStream(filePath));
        
        icon.setImage(img);

        Button button = new Button("");

        button.setGraphic(icon);
        putSize(button,icon,widthBtn,heightBtn);
        button.setLayoutX(xCoord);
        button.setLayoutY(yCoord);

        buttonAnimations(button);

        return button;
    }

    private void buttonAnimations(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.2);
        scaleIn.setToY(1.2);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.playFromStart());
        button.setOnMouseExited(e -> scaleOut.playFromStart());
    }

    private void adjustTextWrapping(Text textNode, int x) {
        double maxHeight = (22.5 / 100) * this.height;
        double increment = (0.5 / 100) * this.height;

        textNode.setBoundsType(TextBoundsType.VISUAL);
        textNode.setLayoutY(maxHeight);

        textNode.applyCss();

        Platform.runLater(() -> {
            Bounds bounds = textNode.getBoundsInLocal();
            double buttonY = this.file_buttons[x].localToScene(this.file_buttons[x].getBoundsInLocal()).getMinY();

            while (textNode.localToScene(bounds).getMaxY() > buttonY) {
                textNode.setLayoutY(textNode.getLayoutY() - increment);
                textNode.applyCss();
                bounds = textNode.getBoundsInLocal();
            }
        });
    }

    private boolean selectFile(int x) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez un fichier");

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            if (x == 0 && !selectedFile.getName().toLowerCase().contains("vcm") && !selectedFile.getName().toLowerCase().contains("cahier")) {
                fillText(true, "VEUILLEZ ENTRER LE CAHIER DE VIE ET MINISTERE (VCM) SUR LE BON EMPLACEMENT !");
                return false;
            }
            if (x == 1 && !selectedFile.getName().toLowerCase().contains("discours") && !selectedFile.getName().toLowerCase().contains("week-end")) {
                fillText(true, "VEUILLEZ ENTRER LE PROGRAMME DES DISCOURS SUR LE BON EMPLACEMENT !");
                return false;
            }
            else if (x == 2 && !selectedFile.getName().toLowerCase().contains("services") && !selectedFile.getName().toLowerCase().contains("semaine")) {
                fillText(true, "VEUILLEZ ENTRER LE PROGRAMME DES SERVICES DE LA SEMAINE SUR LE BON EMPLACEMENT !");
                return false;
            }
            this.files[x] = selectedFile;
            this.texts_file[x].setText(selectedFile.getName());
            adjustTextWrapping(this.texts_file[x],x);
            return true;
        }
        return false;
    }

    private void openSettingsWindow() {
        //prompt AI ???
        //changer version AI ???
        //exceptions personnes = prisoxException
        //mettre les roles des programmes
        //faire une update de maj auto
        Settings settings = new Settings(this.width, this.height);
        settings.show();
    }

    private void createText(double size, int x) {
        String[] texts = {"Programme : Cahier Vie et Ministère","Programme : Discours Week-End","Programme : Services Semaine"};
        this.texts_file[x] = new Text(texts[x]);
        this.texts_file[x].setId("text");
        this.texts_file[x].setLayoutX(width*((40.0+10.0*x)/100.0));
        this.texts_file[x].setLayoutY(height*(24.0/100.0));
        this.texts_file[x].setWrappingWidth(size);
    }

    private void fillText(boolean ai, String textToPrint) {
        for (int i = 0; i < this.textsPrinted.size(); i++) {//remove every text from the pane
            this.scrollContent.getChildren().remove(this.textsPrinted.get(i));
        }
        this.textsPrinted.clear();//remove every text from the list

        if (textToPrint != null) {
            Text text = new Text(textToPrint);
            text.setId("text");
            text.setWrappingWidth((40.0/100)*this.width);
            VBox textContainer = new VBox(text);
            textContainer.setPadding(new Insets(8));
            this.scrollContent.getChildren().add(textContainer);
            this.textsPrinted.add(textContainer);
            return;
        }

        if (ai) {
            this.responseAI = this.responseAI.replace("\\n","");
            String[] responseAITab = this.responseAI.split("-");
            for (int i = 1; i < responseAITab.length; i++) {
                Text text = new Text(responseAITab[i]);
                text.setId("text");
                text.setWrappingWidth((40.0/100)*this.width);
                VBox textContainer = new VBox(text);
                textContainer.setPadding(new Insets(8));
                this.scrollContent.getChildren().add(textContainer);
                this.textsPrinted.add(textContainer);
            }
        }
        else {
            for (int i = 0; i < this.responseParser.size(); i++) {
                Text text = new Text(this.responseParser.get(i));
                text.setId("text");
                text.setWrappingWidth((40.0/100)*this.width);
                VBox textContainer = new VBox(text);
                textContainer.setPadding(new Insets(8));
                this.scrollContent.getChildren().add(textContainer);
                this.textsPrinted.add(textContainer);
            }
            if (this.responseParser.size() == 0) {
                Text text = new Text("Aucune personne n'a deux rôles le même jour.");
                text.setId("text");
                text.setWrappingWidth((40.0/100)*this.width);
                VBox textContainer = new VBox(text);
                textContainer.setPadding(new Insets(8));
                this.scrollContent.getChildren().add(textContainer);
                this.textsPrinted.add(textContainer);
            }
        }
    }

    private void loadPDF() {
        for (int i = 0; i < 3; i++) {
            this.goodPDFs[i] = false;//on reste tout avant de tout load de nouveau
        }
        for (int i = 0; i < this.files.length; i++) {
            File pdfFile = this.files[i];
            if (pdfFile != null && pdfFile.exists()) {
                try (PDDocument document = PDDocument.load(pdfFile)) {
                    if (document.isEncrypted()) {
                        fillText(true, ("FICHIER PDF ENCRYPTEE : "+pdfFile.getName()));
                        continue;
                    }
                    PDFTextStripper pdfStripper = new PDFTextStripper();
                    String txt = pdfStripper.getText(document);

                    byte[] bytes = txt.getBytes(StandardCharsets.UTF_8);
                    this.textPDFs[i] = new String(bytes, StandardCharsets.UTF_8);
                    this.goodPDFs[i] = true;
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier PDF : " + pdfFile.getName());
                    e.printStackTrace();
                    this.textPDFs[i] = "Erreur de chargement";
                    this.goodPDFs[i] = false;
                }
            } else {
                System.err.println("Fichier PDF non valide : " + pdfFile);
                this.textPDFs[i] = "Fichier non valide";
                this.goodPDFs[i] = false;
            }
        }
    }

    private void compare() {
        if (!this.switchButton.isSelected()) {
            fillText(this.switchButton.isSelected(), "Chargement ...");
            loadPDF();
            for(int i = 0; i < 3; i++) {
                if (this.goodPDFs[i]) {
                    this.parsedPDF[i] = Parser.parseAll(this.textPDFs[i],i);
                }
            }
            this.compareFiles();
            fillText(false, null);//important de mettre false en brut car si le switch est active entre temps
            //cela ne doit pas changer l'affichage du texte
            return;
        }
        int count = 0;
        String prompt = properties.getProperty("prompt");

        for (int i = 0; i < 3; i ++) {
            if (this.files[i] != null) {
                String name = this.files[i].getName();
                if (name.substring(name.length()-4,name.length()).equals(".pdf")) {
                    count += 1;
                }
                else {
                    fillText(true, ("UNIQUEMENT DES FICHIERS PDF !"));
                    return;
                }
            }
        }

        if (count < 2) {
            fillText(true, ("VEUILLEZ RENSEIGNER AU MINIMUM 2 FICHIERS !"));
            return;
        }

        loadPDF();
        try {
            Platform.runLater(() -> fillText(this.switchButton.isSelected(), "Chargement ..."));

            this.apiThread = new Thread(() -> {
                try {
                    String response = GeminiAPIRequest.geminiRequest(prompt, this.files, this.goodPDFs);
                    Platform.runLater(() -> {
                        this.responseAI = response;
                        if (this.responseAI.equals("Vous n'avez plus de points")) {
                            fillText(true, "Vous n'avez plus assez de jetons pour l'instant");
                            return;
                        }
                        else if (this.responseAI.equals("Timeout")) {
                            fillText(true, "Problème de connexion au serveur...");
                            return;
                        }
                        else if (this.responseAI.equals("La requête a été interrompue")) {
                            fillText(true, "La requête a été interrompue");
                            return;
                        }
                        else if (this.responseAI.equals("Problem")) {
                            fillText(true, "Problème lors de la réponse de l'IA");
                            return;
                        }
                        else if (this.responseAI.equals("Requete trop longue")) {
                            fillText(true, "Votre requete est trop longue par rapport a votre nombre de jetons restants");
                            return;
                        }
                        fillText(true, null);//important de mettre true en brut car si le switch est active entre temps
                        //cela ne doit pas changer l'affichage du texte
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        fillText(true, "Erreur lors de la requête API");
                        e.printStackTrace();
                    });
                }
            });
            this.apiThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() { 
            @Override
            public void handle(WindowEvent event) {
                if (apiThread != null && apiThread.isAlive()) {
                    apiThread.interrupt();
                }
                Platform.exit();
            }
        });
        this.files = new File[3];
        this.texts_file = new Text[3];
        this.file_buttons = new Button[3];
        this.textPDFs = new String[3];
        this.file_icons = new ImageView[3];
        this.textsPrinted = new ArrayList<VBox>();
        this.goodPDFs = new boolean[3];
        for (int i = 0; i < 3; i++) {
            this.goodPDFs[i] = false;
        }
        this.parsedPDF = new List[3];
        for (int i = 0; i < 3; i++) {
            this.parsedPDF[i] = new ArrayList<String>();
        }
        this.responseParser = new ArrayList<String>();
        this.properties = Settings.loadProperties();

        this.primaryStage = primaryStage;

        Dimension tailleMoniteur = Toolkit.getDefaultToolkit().getScreenSize();
        this.width = (double) tailleMoniteur.width;//1536.0 de base sur mon ecran
        this.height = (double) tailleMoniteur.height;//864.0 de base sur mon ecran

        primaryStage.setTitle("Inter-prog");
        primaryStage.getIcons().add(new Image("title.png"));

        Pane content = new Pane();
        content.setId("mainPane");
        content.setPrefSize(width, height);
        content.getStylesheets().add("style.css");

        // SwitchButton IA Generation
        this.textSwitchButton = new Text("Génération par IA : ON");
        this.textSwitchButton.setId("text");
        this.textSwitchButton.setLayoutX((20.0/100)*this.width);
        this.textSwitchButton.setLayoutY((15.0/100)*this.height);
        this.textSwitchButton.setWrappingWidth((5.5/100)*this.width);

        this.switchButton = new SwitchButton(this.textSwitchButton, "Génération par IA : ON", "Génération par IA : OFF");
        this.switchButton.setId("switch");
        this.switchButton.setLayoutX((20.0/100)*this.width);
        this.switchButton.setLayoutY((20.0/100)*this.height);

        // Boutons SVG pour sélectionner les fichiers
        for (int i = 0; i <= 2; i++) {
            ImageView icon = new ImageView();
            Button btn = createButton("/dropFile.png",(5.0/100)*this.width,(6.5/100)*this.height, width*((40.0+10.0*i)/100.0), height*(30.0/100.0), icon);
            btn.setId("buttonFiles");
            this.file_buttons[i] = btn;
            this.file_icons[i] = icon;
            createText((5.5/100)*this.width,i);
            content.getChildren().addAll(btn,texts_file[i]);
            int x = i;
            this.file_buttons[i].setOnAction(value ->  {
                boolean b = selectFile(x);
                if (b) {
                    this.file_buttons[x].setId("file_button_loaded");
                    this.texts_file[x].setId("file-text-loaded");
                    this.file_icons[x].setId("image_view_loaded");
                }
            });
        }

        // Bouton Paramètres
        ImageView icon = new ImageView();
        Button settingsButton = createButton("/settings.png",(5.0/100)*this.width,(6.0/100)*this.height, width*(93/100.0), height*(3.0/100.0), icon);
        content.getChildren().add(settingsButton);
        settingsButton.setOnAction(e -> openSettingsWindow());
        HBox topBar = new HBox(settingsButton);
        settingsButton.setStyle("-fx-alignment: top-right; -fx-background-color: TRANSPARENT;");

        Button compareButton = new Button("Comparer");
        compareButton.setId("button");
        compareButton.setLayoutX(width*(70.0/100.0));
        compareButton.setLayoutY(height*(15.0/100.0));
        compareButton.setPrefWidth((7.0/100)*this.width);
        compareButton.setPrefHeight((5.0/100)*this.height);
        compareButton.setOnAction(e -> compare());
        buttonAnimations(compareButton);

        // Création du rectangle sous les boutons de sélection de fichiers
        this.scrollContent = new VBox(0);
        scrollContent.setPrefWidth(width * (54.0/100));

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setPrefSize(width * (54.0/100), height * (53.0/100));
        scrollPane.setLayoutX(width * (25.0/100));
        scrollPane.setLayoutY(height * (37.5/100));
        scrollPane.setStyle("-fx-background: transparent;"+
            "-fx-border-color: transparent;" + 
            "-fx-border-radius: 20px;" +
            "-fx-background-radius: 25px;" +
            "-fx-background-color: #1F2A36;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);"
        );
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        content.getChildren().addAll(settingsButton, switchButton, textSwitchButton, compareButton, scrollPane);
        Scene scene = new Scene(content, width, height);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
