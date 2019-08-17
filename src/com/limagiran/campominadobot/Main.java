package com.limagiran.campominadobot;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Vinicius Silva
 */
public class Main {

    volatile public static boolean PLAY_TO_WIN = false;
    volatile private static int PLAY_TO_WIN_REPEAT_COUNT = 0;

    /**
     * Texto do label principal
     */
    private static String LABEL_TEXT = "<html>"
            + "<style>"
            + "  div {"
            + "    width:550px;"
            + "    text-align:justify;"
            + "  }"
            + "  p {"
            + "    margin:4px;"
            + "  }"
            + "</style>"
            + "<h1>Campo Minado Bot</h1>"
            + "<div>"
            + "  <p>"
            + "    Clique em <b>Iniciar</b> com a janela do campo minado já em exibição na tela."
            + "  </p>"
            + "  <p>"
            + "    O programa está configurado para clicar em algum campo aleatoriamente "
            + "    quando não encontrar uma jogada lógica, dependendo então da sorte, "
            + "    o que faz com que em muitos casos clique em bombas e perca o jogo."
            + "  </p>"
            + "  <p>"
            + "    O programa se sai bem no nível <b>principiante</b> e <b>intermediário</b>, "
            + "    porém no nível <b>especialista</b> é difícil finalizar um jogo por conta "
            + "    da grande quantidade de jogadas que exigem sorte."
            + "  </p>"
            + "  <p>"
            + "    Melhores tempos nos testes: Principiante: <b>1s</b> - Intermediário: <b>1s</b> - Especialista: <b>1s</b>"
            + "  </p>"
            + "<br />"
            + "  <p>"
            + "    Obs.: Executar até vencer (a cada 10 tentativas, um delay de 2 segundos para cancelar [ESC])"
            + "  </p>"
            + "</div>";

    /**
     * Área de texto que exibirá mensagens ao usuário
     */
    private static JTextArea JTEXT_AREA_LOG;

    static {
        //<editor-fold defaultstate="collapsed" desc="SystemLookAndFeel">
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
        //</editor-fold>
    }

    /**
     * Método principal
     *
     * @param args ???
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Campo Minado Bot - by Lima Giran (github.com/limagiran)");

            //botão que inicia o bot
            JButton buttonStart = new JButton("Iniciar");
            buttonStart.setFont(new Font("Arial", Font.BOLD, 16));
            buttonStart.setFocusable(false);
            buttonStart.addActionListener(evt -> buttonAction(evt));

            JCheckBox chkbxPlayToWin = new JCheckBox("Executar até vencer");
            chkbxPlayToWin.addActionListener(evt -> PLAY_TO_WIN = chkbxPlayToWin.isSelected());
            chkbxPlayToWin.setFont(new Font("Arial", Font.PLAIN, 16));
            chkbxPlayToWin.setFocusable(false);

            //label que contém as informações do programa
            JLabel label = new JLabel(LABEL_TEXT, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            //área para exibir mensagens ao usuário
            JTEXT_AREA_LOG = new JTextArea("", 6, 48);
            JTEXT_AREA_LOG.setEditable(false);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 5));
            buttonPanel.add(buttonStart);
            buttonPanel.add(chkbxPlayToWin);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            panel.add(label, "First");
            panel.add(new JScrollPane(JTEXT_AREA_LOG), "Center");
            panel.add(buttonPanel, "Last");

            buttonPanel.setOpaque(false);
            panel.setOpaque(false);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.getContentPane().add(panel, "Center");
            frame.pack();
            frame.setLocationRelativeTo(null);

            InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            frame.getRootPane().setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "stop_play_to_win");
            frame.getRootPane().getActionMap().put("stop_play_to_win", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chkbxPlayToWin.setSelected(false);
                    PLAY_TO_WIN = false;
                }
            });

            frame.setVisible(true);
        });
        Utils.exportAndOpenWinmine();
    }

    /**
     * Ação do botão principal.<br>
     * Inicia o bot.
     *
     * @param evt evento do botão
     */
    private static void buttonAction(final ActionEvent evt) {
        PLAY_TO_WIN_REPEAT_COUNT = 0;
        ((JButton) evt.getSource()).setEnabled(false);
        notification("Jogo iniciado.", true);
        new Thread(() -> {
            CampoMinadoBot.start();
            SwingUtilities.invokeLater(() -> {
                ((JButton) evt.getSource()).setEnabled(true);
            });
        }).start();

    }

    /**
     * Exibe uma mensagem na área de texto para o usuário
     *
     * @param message mensagem a ser exibida
     * @param clear {@code true} para limpar as mensagens anteriores.
     * {@code false} para concatenar a mensagem com as mensagens já existentes.
     */
    public static void notification(String message, boolean clear) {
        if (JTEXT_AREA_LOG == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JTEXT_AREA_LOG.setText((!clear ? JTEXT_AREA_LOG.getText() + '\n' : "") + message);
        });
        checkPlayToWin(message);
    }

    /**
     * Verifica se a mensagem de notificação recebida contém a palavra
     * "Perdemos".<br>
     * Se sim, verifica se a opção "Executar até vencer" está ativada, para
     * iniciar uma nova tentativa automaticamente.
     *
     * @param message notificação recebida
     */
    public static void checkPlayToWin(String message) {
        if (!PLAY_TO_WIN || !message.contains("Perdemos")) {
            return;
        }
        PLAY_TO_WIN_REPEAT_COUNT++;
        final boolean sleep = PLAY_TO_WIN_REPEAT_COUNT % 10 == 0;
        final Point winmineLocation = MouseInfo.getPointerInfo().getLocation();
        Window w = SwingUtilities.getWindowAncestor(JTEXT_AREA_LOG);
        if (sleep && w != null) {
            notification("Pressione ESC para parar.", true);
            Rectangle bounds = w.getBounds();
            Utils.ROBOT.mouseMove((int) bounds.getCenterX(), (int) bounds.getCenterY());
            Utils.ROBOT.mousePress(InputEvent.BUTTON1_MASK);
            Utils.ROBOT.mouseRelease(InputEvent.BUTTON1_MASK);
        }
        new Thread(() -> {
            if (sleep) {
                Utils.sleep(2000);
            }
            if (PLAY_TO_WIN) {
                notification("Jogo iniciado.", true);
                notification("Executar até vencer - tentativas: " + (PLAY_TO_WIN_REPEAT_COUNT + 1));
                Utils.restartGame(winmineLocation.x, winmineLocation.y);
                CampoMinadoBot.start();
            }
        }).start();
    }

    /**
     * Exibe uma mensagem na área de texto para o usuário.
     *
     * @param message mensagem a ser exibida
     *
     * @see Main#notification(java.lang.String, boolean)
     * Main.notification(message, clear)
     */
    public static void notification(String message) {
        notification(message, false);
    }
}
