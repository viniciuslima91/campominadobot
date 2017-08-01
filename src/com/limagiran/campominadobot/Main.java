package com.limagiran.campominadobot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 *
 * @author Vinicius Silva
 */
public class Main {

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
            + "    Clique em <b>Iniciar</b> com a janela do campo minado já em exibição "
            + "    e com o jogo reiniciado. Iniciar o bot com o jogo já iniciado pode não dar certo."
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
            + "    Atualmente o programa só calcula uma lógica simples de quantidade "
            + "    de campos/bandeiras ao redor. Em muitos casos é possível saber, por dedução/exclusão, "
            + "    qual é a casa que obrigatoriamente vai ter bomba, clicando nos campos seguros "
            + "    para proseguir, porém o programa ainda não tem essa lógica implementada."
            + "  </p>"
            + "  <p>"
            + "    Melhores tempos nos testes: Principiante: <b>1s</b> - Intermediário: <b>3s</b> - Especialista: <b>8s</b>"
            + "  </p>"
            + "</div>";

    /**
     * Texto do botão enquanto o bot não está em execução
     */
    private static String BUTTON_TEXT_START = "Iniciar";

    /**
     * Texto do botão enquanto o bot está em execução
     */
    private static String BUTTON_TEXT_STARTED = "Jogando... (Parar)";

    /**
     * Área de texto que exibirá mensagens ao usuário
     */
    private static JTextArea JTEXT_AREA_LOG;

    static {
        //<editor-fold defaultstate="collapsed" desc="Apply Nimbus">
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIDefaults ui = UIManager.getLookAndFeelDefaults();
                    ui.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
                    ui.put("TextPane.contentMargins", new Insets(0, 3, 0, 3));
                    ui.put("Button.contentMargins", new Insets(5, 8, 5, 8));
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException |
                InstantiationException | IllegalAccessException e) {
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

            //botão que inicia e encerra o bot
            JButton buttonStart = new JButton(BUTTON_TEXT_START);
            buttonStart.setFont(new Font("Arial", Font.BOLD, 16));
            buttonStart.setFocusable(false);
            buttonStart.addActionListener(evt -> buttonAction(evt));

            //label que contém as informações do programa
            JLabel label = new JLabel(LABEL_TEXT, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            //área para exibir mensagens ao usuário
            JTEXT_AREA_LOG = new JTextArea("LOG", 4, 48);
            JTEXT_AREA_LOG.setEditable(false);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(buttonStart);

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
            frame.setVisible(true);
        });
    }

    /**
     * Ação do botão principal. Executa o método de iniciar ou parar, de acordo
     * com o texto sendo exibido no botão.
     *
     * @param evt evento do botão
     */
    private static void buttonAction(ActionEvent evt) {
        final JButton button = (JButton) evt.getSource();
        if (button.getText().equals(BUTTON_TEXT_START)) {
            start(button);
        } else {
            stop(button);
        }
    }

    /**
     * Inicia o bot
     *
     * @param button jbutton que realizou a ação
     */
    private static void start(JButton button) {
        button.setText(BUTTON_TEXT_STARTED);
        new Thread(() -> {
            Utils.sleep(500);
            notification("Jogo iniciado.", true);
            CampoMinadoBot.start();
            SwingUtilities.invokeLater(() -> button.setText("Iniciar"));
        }).start();
    }

    /**
     * Força o encerramento do bot
     *
     * @param button jbutton que realizou a ação
     */
    private static void stop(JButton button) {
        button.setText(BUTTON_TEXT_START);
        CampoMinadoBot.stop();
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
        SwingUtilities.invokeLater(() -> JTEXT_AREA_LOG.setText((!clear ? JTEXT_AREA_LOG.getText() + '\n' : "") + message));
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
