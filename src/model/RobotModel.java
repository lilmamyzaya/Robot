package model;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

public class RobotModel extends Observable {
    private double x = 100;
    private double y = 100;
    private double direction = 0;

    private double targetX = 150;
    private double targetY = 100;

    private final Timer timer = new Timer("robot-movement", true);

    public RobotModel() {
        // Запуск обновлений каждые 10 мс
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updatePosition(0.01); // 10 мс = 0.01 сек
            }
        }, 0, 10);
    }
    private static final double MAX_VELOCITY = 10;            // было 0.1
    private static final double MAX_ANGULAR_VELOCITY = 0.05;   // было 0.001

    public void updatePosition(double duration) {
        // здесь логика расчета движения, исправленная

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.hypot(dx, dy);
        double angleToTarget = Math.atan2(dy, dx);
        double angleDiff = normalizeAngle(angleToTarget - direction);

        if (distance < 0.5 && Math.abs(angleDiff) < 0.05) {
            return;
        }
        /// Параметры движения
        //double velocity = 100.0; // такая же как maxVelocity
        /*
        double velocity = applyLimits(100.0, 0, 100.0);
        double maxAngularVelocity = 0.5;*/
        //double angularVelocity = Math.signum(angleDiff) * MAX_ANGULAR_VELOCITY;
        double velocity = MAX_VELOCITY;
        double angularVelocity = Math.signum(angleDiff) * MAX_ANGULAR_VELOCITY;


        moveRobot(velocity, angularVelocity, duration);

        setChanged();
        notifyObservers();
    }

    private void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);


        double newX = x + velocity / angularVelocity *
                (Math.sin(direction + angularVelocity * duration) - Math.sin(direction));
        if (!Double.isFinite(newX)) {
            newX = x + velocity * duration * Math.cos(direction);
        }

        double newY = y - velocity / angularVelocity *
                (Math.cos(direction + angularVelocity * duration) - Math.cos(direction));
        if (!Double.isFinite(newY)) {
            newY = y + velocity * duration * Math.sin(direction);
        }

        x = newX;
        y = newY;
        direction = normalizeAngle(direction + angularVelocity * duration);
    }

    private double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private double normalizeAngle(double angle) {
        while (angle < -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
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

    public void shutdown() {
        timer.cancel();
    }
}

