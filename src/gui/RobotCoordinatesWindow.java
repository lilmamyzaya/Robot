package gui;

import model.RobotModel;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class RobotCoordinatesWindow extends JInternalFrame implements Observer {
    private final JLabel coordsLabel;

    public RobotCoordinatesWindow(RobotModel model) {
        super("Координаты робота", true, true, true, true);
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

