package model;

import java.util.Observable;

public class RobotModel extends Observable {
    private double x = 100;
    private double y = 100;
    private double direction = 0;

    private double targetX = 150;
    private double targetY = 100;

    public void updatePosition(double duration) {
        // здесь логика расчета движения, исправленная
        double distance = distanceToTarget();
        if (distance < 0.5) return;

        double angleToTarget = Math.atan2(targetY - y, targetX - x);
        double angleDiff = normalizeAngle(angleToTarget - direction);

        double angularVelocity = Math.signum(angleDiff) * Math.min(Math.abs(angleDiff), 0.001);
        double velocity = 0.1;

        direction = normalizeAngle(direction + angularVelocity * duration);
        x += velocity * duration * Math.cos(direction);
        y += velocity * duration * Math.sin(direction);

        setChanged();
        notifyObservers();
    }

    private double distanceToTarget() {
        return Math.hypot(targetX - x, targetY - y);
    }

    private double normalizeAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }

    public void setTarget(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }
}

