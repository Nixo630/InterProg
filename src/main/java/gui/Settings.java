package gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;

public class Settings {
    private Stage stage;
    private double width;
    private double height;
    private TextArea promptField;
    private TextField aiVersionField;
    private TextField exceptionsInputField;
    private ListView<String> exceptionsListView;
    private ObservableList<String> exceptionsList;
    
    // Rôles pour les trois programmes
    private TabPane rolesTabPane;
    private ObservableList<String> roles1List;
    private ObservableList<String> roles2List;
    private ObservableList<String> roles3List;
    private ListView<String> roles1ListView;
    private ListView<String> roles2ListView;
    private ListView<String> roles3ListView;
    private TextField role1InputField;
    private TextField role2InputField;
    private TextField role3InputField;
    
    private Properties properties;
    private static final String CONFIG_FILE = "config.properties";
    public static final String EXCEPTIONS_SEPARATOR = ";";
    public static final String ROLES_SEPARATOR = ";";

    public Settings(double width, double height) {
        this.width = width;
        this.height = height;
        this.stage = new Stage();
        this.properties = Settings.loadProperties();
        initializeWindow();
    }

    public static Properties loadProperties() {
        Properties props = new Properties();
        try {
            if (Files.exists(Paths.get(CONFIG_FILE))) {
                props.load(Files.newInputStream(Paths.get(CONFIG_FILE)));
            } else {
                // Valeurs par défaut
                props.setProperty("prompt", "Ton role va etre d'etre mon correcteur, j'ai fait 3 pdfs, pour 3 programmes differents pour les meme evenements, chaque semaine le jeudi/mardi et le samedi, et j'ai mis le nom/initiales de chaque personne qui a un role/responsabilite ce jour la. Tu vas donc regarder chaque pdf et me dire toutes les personnes qui ont 2 roles/responsabilites le meme jour, que ce soit dans le meme programme ou non. Pour la comprehension des fichiers voici comment tu vas ty' prendre : Les roles possibles dans le programme des services sont : Date Accueil Hall Accueil Salle Accueil Zoom Micros Audio Video Scene et ils sont categorises au debut car le fichier de programme des services est un tableau, dont les colonnes sont les roles et les lignes les dates. Pour le programme des discours c'est egalement un tableau dont les roles sont Orateur President Lecteur Pr et ils sont egalement ecrits au debut car ce sont les colonnes. Les lignes sont les dates. Quant au Cahier de vie et ministere (VCM) ce n'est pas un tableau mais une sorte de liste, ou il est ecrit une date puis toutes les lignes qui suivent seront de cette date avec chaque role en debut de ligne puis le ou les noms qui remplissent ce role et a chaque nouvelle date c'est une nouvelle semaine. De plus verifie bien avant de m'envoyer tes resultats que les personnes en question ont bien un role et un service les dates en question et qu'ils y apparaissent bien. Attention il y a parfois des evenements tel que des assemblees de Circonscription ou regionale, ces jours ci il n'y aura personne en double. Tu trouveras les programmes avec leur intitule comme decrit precedemment en piece jointe. Ta reponse sera en francais sous la forme - (personne en double) pour (role1) et (role2) le (date et jour). Et ce pour chaque personne en double. Et tu ne rajouteras aucun autre texte avant ou apres, tu me donneras uniquement les differences des fichiers sous la forme precedemment dicte.");
                props.setProperty("aiVersion", "gemini-2.0-flash");
                props.setProperty("exceptions", "Priso Priso;");
                props.setProperty("roles1", "Le programme VCM n'a pas de quadrillage et n'est pas un tableau !");
                props.setProperty("roles2", "Orateur;President;Lecteur;Pr");
                props.setProperty("roles3", "Date;Accueil Hall;Accueil Salle;Accueil Zoom;Micros;Audio;Video;Scene");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des propriétés: " + e.getMessage());
        }
        return props;
    }

    private void initializeWindow() {
        stage.setTitle("Paramètres");
        
        // Création du conteneur principal avec le style de fond
        GridPane grid = new GridPane();
        grid.setId("mainPane");
        grid.setPadding(new Insets(30));
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        // Titre
        Text titleText = new Text("Configuration de l'application");
        titleText.setId("text");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        grid.add(titleText, 0, 0, 2, 1);
        GridPane.setMargin(titleText, new Insets(0, 0, 20, 0));
        titleText.requestFocus();

        // Prompt AI
        Text promptLabel = createStyledLabel("Message AI :");
        promptField = new TextArea(properties.getProperty("prompt"));
        promptField.setPrefWidth(width/2);
        promptField.setPrefHeight(height/5);
        promptField.setWrapText(true);// Activer le retour à la ligne automatique
        promptField.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; -fx-border-color: #7F8C8D; "
                        + "-fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px; "
                        + "-fx-control-inner-background: #34495E;");
        grid.add(promptLabel, 0, 1);
        grid.add(promptField, 1, 1);

        // Version AI
        Text versionLabel = createStyledLabel("Version AI :");
        aiVersionField = createStyledTextField(properties.getProperty("aiVersion"));
        grid.add(versionLabel, 0, 2);
        grid.add(aiVersionField, 1, 2);

        // Section exceptions des noms composés
        Text exceptionsTitle = createStyledLabel("Exceptions des noms composés :");
        exceptionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        grid.add(exceptionsTitle, 0, 3, 2, 1);
        
        // Liste des exceptions
        initializeExceptionsList();
        exceptionsListView = new ListView<>(exceptionsList);
        styleListView(exceptionsListView);
        exceptionsListView.setPrefHeight(150);
        grid.add(exceptionsListView, 0, 4, 2, 1);
        
        // Champ et boutons pour ajouter/supprimer des exceptions
        HBox exceptionsControls = new HBox(10);
        exceptionsControls.setAlignment(Pos.CENTER_LEFT);
        
        exceptionsInputField = createStyledTextField("");
        exceptionsInputField.setPromptText("Entrez une exception (ex: Jean-Pierre)");
        exceptionsInputField.setPrefWidth(300);
        
        Button addExceptionButton = createStyledButton("Ajouter");
        addExceptionButton.setPrefWidth(120);
        addExceptionButton.setOnAction(e -> addException());
        
        Button removeExceptionButton = createStyledButton("Supprimer");
        removeExceptionButton.setPrefWidth(120);
        removeExceptionButton.setOnAction(e -> removeException());
        
        exceptionsControls.getChildren().addAll(exceptionsInputField, addExceptionButton, removeExceptionButton);
        grid.add(exceptionsControls, 0, 5, 2, 1);

        // Section des rôles des programmes
        Text rolesTitle = createStyledLabel("Attributions des programmes :");
        rolesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        grid.add(rolesTitle, 0, 6, 2, 1);
        
        // Initialisation et création du TabPane pour les rôles
        initializeRolesLists();
        createRolesTabPane();
        grid.add(rolesTabPane, 0, 7, 2, 1);

        // Boutons
        Button saveButton = createStyledButton("Sauvegarder");
        Button cancelButton = createStyledButton("Annuler");
        
        // Configuration des boutons dans une HBox pour l'alignement
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttons, 1, 8);

        // Event handlers
        saveButton.setOnAction(e -> saveSettings());
        cancelButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(grid, width/1.3, height/1.1);
        
        // Appliquer le CSS
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        Platform.runLater(() -> {
            grid.requestFocus(); // Mettre le focus sur le grid plutôt que sur un TextField
        });
    }

    private void initializeExceptionsList() {
        String exceptionsStr = properties.getProperty("exceptions", "");
        List<String> exceptions = new ArrayList<>();
        
        if (!exceptionsStr.isEmpty()) {
            exceptions = Arrays.asList(exceptionsStr.split(EXCEPTIONS_SEPARATOR));
        }
        
        exceptionsList = FXCollections.observableArrayList(exceptions);
    }

    private void initializeRolesLists() {
        // Initialisation des listes de rôles pour les 3 programmes
        String roles1Str = properties.getProperty("roles1", "");
        String roles2Str = properties.getProperty("roles2", "");
        String roles3Str = properties.getProperty("roles3", "");
        
        List<String> roles1 = new ArrayList<>();
        List<String> roles2 = new ArrayList<>();
        List<String> roles3 = new ArrayList<>();
        
        if (!roles1Str.isEmpty()) {
            roles1 = Arrays.asList(roles1Str.split(ROLES_SEPARATOR));
        }
        
        if (!roles2Str.isEmpty()) {
            roles2 = Arrays.asList(roles2Str.split(ROLES_SEPARATOR));
        }
        
        if (!roles3Str.isEmpty()) {
            roles3 = Arrays.asList(roles3Str.split(ROLES_SEPARATOR));
        }
        
        roles1List = FXCollections.observableArrayList(roles1);
        roles2List = FXCollections.observableArrayList(roles2);
        roles3List = FXCollections.observableArrayList(roles3);
    }

    private void createRolesTabPane() {
        rolesTabPane = new TabPane();
        rolesTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        rolesTabPane.setStyle("-fx-background-color: #34495E;-fx-tab-background-color: #34495E; -fx-tab-spacing: 5;");
        
        // Création des trois onglets pour les programmes
        Tab program1Tab = createProgramTab("Programme VCM", roles1List, 
                                          listView -> roles1ListView = listView,
                                          textField -> role1InputField = textField);
        
        Tab program2Tab = createProgramTab("Programme des discours", roles2List,
                                          listView -> roles2ListView = listView,
                                          textField -> role2InputField = textField);
        
        Tab program3Tab = createProgramTab("Programme des services", roles3List,
                                          listView -> roles3ListView = listView,
                                          textField -> role3InputField = textField);
        
        rolesTabPane.getTabs().addAll(program1Tab, program2Tab, program3Tab);
        rolesTabPane.setPrefHeight(350);
    }

    private Tab createProgramTab(String programName, ObservableList<String> rolesList,
                                java.util.function.Consumer<ListView<String>> listViewConsumer,
                                java.util.function.Consumer<TextField> textFieldConsumer) {
        Tab tab = new Tab(programName);
        
        VBox tabContent = new VBox(15);
        tabContent.setId("mainPane");
        tabContent.setPadding(new Insets(15));
        
        // Label d'instructions
        Label instructionsLabel = new Label("Liste des attributions pour " + programName + " :");
        instructionsLabel.setTextFill(Color.web("#ECF0F1"));
        
        // ListView pour afficher les rôles
        ListView<String> rolesListView = new ListView<>(rolesList);
        styleListView(rolesListView);
        rolesListView.setPrefHeight(200);
        listViewConsumer.accept(rolesListView);
        
        // Contrôles pour ajouter/modifier/supprimer des rôles
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField roleInputField = createStyledTextField("");
        roleInputField.setPromptText("Entrez une attribution");
        roleInputField.setPrefWidth(250);
        textFieldConsumer.accept(roleInputField);
        
        Button addRoleButton = createStyledButton("Ajouter");
        addRoleButton.setPrefWidth(100);
        addRoleButton.setOnAction(e -> addRole(rolesList, roleInputField, rolesListView,programName));
        
        Button editRoleButton = createStyledButton("Modifier");
        editRoleButton.setPrefWidth(100);
        editRoleButton.setOnAction(e -> editRole(rolesList, roleInputField, rolesListView,programName));
        
        Button removeRoleButton = createStyledButton("Supprimer");
        removeRoleButton.setPrefWidth(100);
        removeRoleButton.setOnAction(e -> removeRole(rolesList, rolesListView,programName));
        
        controlsBox.getChildren().addAll(roleInputField, addRoleButton, editRoleButton, removeRoleButton);
        
        tabContent.getChildren().addAll(instructionsLabel, rolesListView, controlsBox);
        tab.setContent(tabContent);
        
        return tab;
    }

    private void styleListView(ListView<String> listView) {
        listView.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; -fx-control-inner-background: #34495E; -fx-border-color: #7F8C8D; -fx-border-width: 1px;");
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void addException() {
        String newException = exceptionsInputField.getText().trim();
        
        if (!newException.isEmpty() && !exceptionsList.contains(newException)) {
            exceptionsList.add(newException);
            exceptionsInputField.clear();
        } else if (exceptionsList.contains(newException)) {
            showErrorMessage("Cette exception existe déjà dans la liste.");
        }
    }

    private void removeException() {
        int selectedIndex = exceptionsListView.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex >= 1) {
            exceptionsList.remove(selectedIndex);
        }
        else if (selectedIndex == 0) {
            showErrorMessage("Impossible de supprimer car c'est une valeur par défaut !");
        }
        else {
            showErrorMessage("Veuillez sélectionner une exception à supprimer.");
        }
    }

    private void addRole(ObservableList<String> rolesList, TextField inputField, ListView<String> listView, String programName) {
        String newRole = inputField.getText().trim();
        
        if (programName.equals("Programme VCM")) {
            showErrorMessage("Programme VCM n'est pas modifiable !");
        }
        else if (!newRole.isEmpty() && !rolesList.contains(newRole)) {
            rolesList.add(newRole);
            inputField.clear();
        } else if (rolesList.contains(newRole)) {
            showErrorMessage("Ce rôle existe déjà dans la liste.");
        }
    }

    private void editRole(ObservableList<String> rolesList, TextField inputField, ListView<String> listView, String programName) {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        String newRole = inputField.getText().trim();
        
        if (programName.equals("Programme VCM")) {
            showErrorMessage("Programme VCM n'est pas modifiable !");
        }
        else if (selectedIndex >= 0 && !newRole.isEmpty()) {
            // Vérifier que le nouveau nom n'existe pas déjà ailleurs dans la liste
            if (!rolesList.contains(newRole) || rolesList.indexOf(newRole) == selectedIndex) {
                rolesList.set(selectedIndex, newRole);
                inputField.clear();
            } else {
                showErrorMessage("Ce rôle existe déjà dans la liste.");
            }
        } else if (selectedIndex < 0) {
            showErrorMessage("Veuillez sélectionner un rôle à modifier.");
        }
    }

    private void removeRole(ObservableList<String> rolesList, ListView<String> listView, String programName) {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        
        if (programName.equals("Programme VCM")) {
            showErrorMessage("Programme VCM n'est pas modifiable !");
        }
        else if (selectedIndex >= 0) {
            rolesList.remove(selectedIndex);
        } else {
            showErrorMessage("Veuillez sélectionner un rôle à supprimer.");
        }
    }

    private Text createStyledLabel(String labelText) {
        Text label = new Text(labelText);
        label.setId("text");
        return label;
    }

    private TextField createStyledTextField(String initialValue) {
        TextField textField = new TextField(initialValue);
        textField.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; -fx-border-color: #7F8C8D; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        return textField;
    }

    private Button createStyledButton(String buttonText) {
        Button button = new Button(buttonText);
        button.setId("button");
        button.setPrefWidth(150);
        button.setMinHeight(40);
        
        // Animation sur clic
        button.setOnMousePressed(e -> {
            button.setStyle("-fx-scale-x: 0.95; -fx-scale-y: 0.95;");
        });
        
        button.setOnMouseReleased(e -> {
            button.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
        
        return button;
    }

    private void saveSettings() {
        try {
            properties.setProperty("prompt", promptField.getText());
            properties.setProperty("aiVersion", aiVersionField.getText());
            
            // Sauvegarde des exceptions
            String exceptionsStr = String.join(EXCEPTIONS_SEPARATOR, exceptionsList);
            properties.setProperty("exceptions", exceptionsStr);
            
            // Sauvegarde des rôles pour les 3 programmes
            String roles1Str = String.join(ROLES_SEPARATOR, roles1List);
            String roles2Str = String.join(ROLES_SEPARATOR, roles2List);
            String roles3Str = String.join(ROLES_SEPARATOR, roles3List);
            
            properties.setProperty("roles1", roles1Str);
            properties.setProperty("roles2", roles2Str);
            properties.setProperty("roles3", roles3Str);
            
            FileWriter writer = new FileWriter(CONFIG_FILE);
            properties.store(writer, "Application Configuration");
            writer.close();
            
            // Afficher un message de confirmation
            showConfirmationMessage("Paramètres sauvegardés avec succès!");
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des paramètres: " + e.getMessage());
            // Afficher un message d'erreur
            showErrorMessage("Erreur lors de la sauvegarde des paramètres: " + e.getMessage());
        }
    }

    private void showConfirmationMessage(String message) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Confirmation");
        
        VBox vbox = new VBox(20);
        vbox.setId("mainPane");
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(30));
        
        Text text = new Text(message);
        text.setId("text");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Button okButton = createStyledButton("OK");
        okButton.setOnAction(e -> popupStage.close());
        
        vbox.getChildren().addAll(text, okButton);
        
        Scene scene = new Scene(vbox, 350, 150);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        popupStage.setScene(scene);
        popupStage.show();
    }
    
    private void showErrorMessage(String message) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Erreur");
        
        VBox vbox = new VBox(20);
        vbox.setId("mainPane");
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(30));
        
        Text text = new Text(message);
        text.setId("text");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setFill(Color.web("#E74C3C"));
        
        Button okButton = createStyledButton("OK");
        okButton.setOnAction(e -> popupStage.close());
        
        vbox.getChildren().addAll(text, okButton);
        
        Scene scene = new Scene(vbox, 400, 150);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        popupStage.setScene(scene);
        popupStage.show();
    }

    public void show() {
        stage.show();
    }
}