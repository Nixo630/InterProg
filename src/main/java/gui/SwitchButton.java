package gui;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;

class SwitchButton extends Pane {
    private boolean selected = true;
    private final Rectangle background;
    private final Circle toggle;

    private String strDisactivate;
    private String strActivate;
    private Text text;

    public SwitchButton(Text text, String s1, String s2) {
        this.strDisactivate = s1;
        this.strActivate = s2;
        this.text = text;

        setWidth(50);
        setHeight(25);
        
        background = new Rectangle(50, 25, Color.web("#E74C3C"));
        background.setArcWidth(25);
        background.setArcHeight(25);
        
        toggle = new Circle(12); 
        toggle.setFill(Color.WHITE);
        toggle.setTranslateX(38);
        toggle.setTranslateY(12.5);
        
        getChildren().addAll(background, toggle);

        setOnMouseClicked(this::toggle);
    }

    private void toggle(MouseEvent event) {
        double startX = selected ? 38 : 12;
        double endX = selected ? 12 : 38;

        // Animation fluide du toggle
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), toggle);
        transition.setFromX(startX);
        transition.setToX(endX);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.play();

        if (selected) {
            background.setFill(Color.web("#117A65"));
        } else {
            background.setFill(Color.web("#E74C3C"));
        }

        // Retarde le changement de texte et de couleur
        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(e -> {
            if (selected) {
                text.setText(strActivate);
            } else {
                text.setText(strDisactivate);
            }
            this.selected = !this.selected;
        });
        delay.play();
    }
    
    public boolean isSelected() {
        return this.selected;
    }
}