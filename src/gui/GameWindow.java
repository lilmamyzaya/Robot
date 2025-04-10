package gui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;
    public GameWindow(GameVisualizer visualizer)
    {
        super("Игровое поле", true, true, true, true);
        //m_visualizer = new GameVisualizer();
        this.m_visualizer = visualizer;
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }
    public void shutdown() {
        m_visualizer.shutdown();
    }

}
