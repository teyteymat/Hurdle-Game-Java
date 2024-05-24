import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class HurdleGame extends JPanel implements ActionListener, KeyListener {

    private int personX = 50;
    private int personY = 240;
    private int hurdleX = 1000;
    private int hurdleY = 280;
    private int speed = 25;
    private boolean jumping = false;
    private int jumpHeight = 150;
    private int jumpSpeed = 20;
    private int jumpY = 0;

    private Image person;
    private Image hurdle;
    private Image background;

    private Timer timer;
    private int hurdleGap = 50; // Fixed gap between hurdles to ensure they are closer
    private int score = 0; // Score variable
    private int highestScore = getHighestScore(); // Retrieve the highest score

    private String playerName; // Player's name

    // File path for storing highest score
    private static final String HIGHEST_SCORE_FILE = "highest_score.txt";

    public HurdleGame(String playerName) {
        this.playerName = playerName;

        ImageIcon personIcon = new ImageIcon("person1.png");
        person = personIcon.getImage().getScaledInstance(70, 90, Image.SCALE_SMOOTH);

        ImageIcon hurdleIcon = new ImageIcon("hurdle.png");
        hurdle = hurdleIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);

        ImageIcon backgroundIcon = new ImageIcon("trackAndField.jpg");
        background = backgroundIcon.getImage().getScaledInstance(1000, 400, Image.SCALE_SMOOTH);

        timer = new Timer(50, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background image
        g2d.drawImage(background, 0, 0, this);

        // Draw person and hurdle
        g2d.drawImage(person, personX, personY - jumpY, this);
        g2d.drawImage(hurdle, hurdleX, hurdleY, this);

        // Draw score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        String scoreText = "Score: " + score;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);
        int x = (getWidth() - textWidth) / 2;
        g2d.drawString(scoreText, x, 30);

        // player's name at the center below the scoreboard
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String playerNameText = "Player: " + playerName;
        int nameTextWidth = fm.stringWidth(playerNameText);
        int nameX = (getWidth() - nameTextWidth) / 2;
        g2d.drawString(playerNameText, nameX, 70);

        // highest score
        String highestScoreText = "Highest Score from the Previous Games: " + highestScore;
        int highestScoreTextWidth = fm.stringWidth(highestScoreText);
        int highestScoreX = (getWidth() - highestScoreTextWidth) / 2;
        g2d.drawString(highestScoreText, highestScoreX, 100);

        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        hurdleX -= speed;

        if (hurdleX < -hurdle.getWidth(this)) {
            hurdleX = 1000 + hurdleGap;
            score += 100; // Increase score by 100 for each successful jump
        }

        if (jumping) {
            if (jumpY < jumpHeight) {
                jumpY += jumpSpeed;
            } else {
                jumping = false;
                jumpY = 0;
            }
        }

        if (checkCollision()) {
            timer.stop();
            updateHighestScore(); // Update the highest score if necessary

            int option = JOptionPane.showOptionDialog(this,
                    "Final Score: " + score + "\nHighest Score from the Previous Games: " + highestScore,
                    "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, new String[] { "Retry", "Quit" }, "Retry");

            if (option == JOptionPane.YES_OPTION) {
                resetGame(); // Retry
            } else {
                System.exit(0); // Quit
            }
        }

        repaint();
    }

    private void resetGame() {
        // Reset variables and start a new game
        hurdleX = 1000;
        jumpY = 0;
        score = 0;
        timer.start();
    }

    private int getHighestScore() {
        // Retrieve the highest score from the file
        try {
            File file = new File(HIGHEST_SCORE_FILE);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                if (line != null) {
                    return Integer.parseInt(line);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateHighestScore() {
        if (score > highestScore) {
            // Update the highest score in the file
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(HIGHEST_SCORE_FILE));
                bw.write(String.valueOf(score));
                bw.close();
                highestScore = score;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCollision() {
        Rectangle personRect = new Rectangle(personX, personY - jumpY, person.getWidth(this), person.getHeight(this));
        Rectangle hurdleRect = new Rectangle(hurdleX + 10, hurdleY, hurdle.getWidth(this) - 10, hurdle.getHeight(this));

        return personRect.intersects(hurdleRect);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !jumping) {
            jumping = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hurdle Game");

        // initial popup dialog
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Hurdle Game", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionsLabel = new JLabel(
                "<html><center>Instructions: Press SPACE BAR to jump over hurdles.<br/>Avoid collision with hurdles to continue.</center></html>",
                JLabel.CENTER);
        instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        instructionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(instructionsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JLabel("Enter your name: ", JLabel.CENTER));
        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(startButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JDialog dialog = new JDialog(frame, "Welcome to the Hurdle Game!", true);
        dialog.getContentPane().add(panel);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText().trim();
                if (!playerName.isEmpty()) {
                    dialog.dispose();
                    HurdleGame game = new HurdleGame(playerName);
                    frame.add(game);
                    frame.setSize(1000, 400);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please enter your name to start the game.", "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }
}
