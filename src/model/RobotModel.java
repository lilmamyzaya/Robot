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

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.hypot(dx, dy);
        //double distance = distanceToTarget();
        if (distance < 0.5) return;

        double angleToTarget = Math.atan2(dy, dx);
        double angleDiff = normalizeAngle(angleToTarget - direction);

        // Увеличим допустимую скорость поворота
        double maxAngularVelocity = 0.05;
        double angularVelocity = Math.signum(angleDiff) * Math.min(Math.abs(angleDiff), maxAngularVelocity);

        // Ехать только если повёрнут не слишком криво
        double forwardSpeed = Math.abs(angleDiff) < Math.PI / 2 ? 1.0 : 0.0;
       // double angularVelocity = Math.signum(angleDiff) * Math.min(Math.abs(angleDiff), 0.1);
        //double velocity = 1;

        direction = normalizeAngle(direction + angularVelocity * duration);
        x += forwardSpeed * Math.cos(direction) * duration * 100;
        y += forwardSpeed * Math.sin(direction) * duration * 100;

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

    public int getTargetX() {
        return (int) targetX;
    }

    public int getTargetY() {
        return (int) targetY;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }

    public void setTarget(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }
}

