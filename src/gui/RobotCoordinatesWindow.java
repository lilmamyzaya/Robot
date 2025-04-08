package gui;

import model.RobotModel;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class RobotCoordinatesWindow extends JDialog implements Observer {
    private final JLabel coordsLabel;

    public RobotCoordinatesWindow(JFrame owner, RobotModel model) {
        super(owner, "Координаты робота", false);
        coordsLabel = new JLabel();
        add(coordsLabel);
        model.addObserver(this);
        setSize(200, 100);
        setLocation(100, 100);
        setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof RobotModel model) {
            coordsLabel.setText(String.format("x: %.2f, y: %.2f", model.getX(), model.getY()));
        }
    }
}
