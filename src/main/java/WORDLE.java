import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;
import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.image.*;

public class WORDLE extends JFrame {

    final static int ROWS = 6;
    final static int COLUMNS = 5;
    final static int FRAME_WIDTH = 400;
    final static int FRAME_HEIGHT = ROWS * 98;
    final static String ALPHABET = "qwertyuiopasdfghjklzxcvbnm";
    public int CURRENT_ROW = 0;
    public int CURRENT_INDEX = 0;
    public String ANSWER;
    public boolean KEYBOARD_EXISTS = false;
    public boolean ERROR_EXISTS = false;

    Font FONT = new Font("Arial", Font.BOLD, 50);

    Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    final static Color GRAY = new Color(50, 50, 50);
    final static Color LIGHT_GRAY = new Color(129, 131, 132);
    final static Color YELLOW = new Color(181, 159, 59);
    final static Color GREEN = new Color(83, 141, 78);
    final static Color BLACK = new Color(20, 20, 20);
    final static Color WHITE = new Color(230, 230, 230);

    JTextField[][] TEXT_FIELDS = new JTextField[ROWS][COLUMNS];
    ArrayList<String> FIVE_LETTER_WORDS = new ArrayList<String>();
    ArrayList<RoundedButton> KEYBOARD_KEYS = new ArrayList<RoundedButton>();
    ArrayList<JDialog> ERROR_DIALOGS = new ArrayList<JDialog>();

    public WORDLE() {
        super("WORDLE");
        generateAnswer();
        System.out.print(ANSWER);
        adjustFrame();
        generateHeader();
        generateTextGrid();
        generateKeyboardAdder();
        generateVirtualKeyboard();
        TEXT_FIELDS[0][0].requestFocus();
    }

    class GuessChecker implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean validGuess = true;
            boolean fiveLetterCheck = true;
            String guess = "";

            for (JTextField field : TEXT_FIELDS[CURRENT_ROW]) {
                String text = field.getText();
                if (text.isEmpty()) {
                    validGuess = false;
                    fiveLetterCheck = false;
                    break;
                } else {
                    guess += text;
                }
            }

            guess = guess.toLowerCase();
            if (validGuess == false || FIVE_LETTER_WORDS.indexOf(guess) == -1) {
                validGuess = false;
            }

            if (validGuess == true) {
                String tempAnswer = ANSWER;
                CURRENT_ROW += 1;

                for (int i = 0; i < COLUMNS; i++) {
                    char guessChar = guess.charAt(i);
                    char answerChar = ANSWER.charAt(i);
                    JTextField previousField = TEXT_FIELDS[CURRENT_ROW - 1][i];

                    if (guessChar == answerChar) {
                        previousField.setBackground(GREEN);
                        tempAnswer = tempAnswer.replaceFirst(String.valueOf(guessChar), "");
                        int letterIndex = ALPHABET.indexOf(guessChar);
                        KEYBOARD_KEYS.get(letterIndex).setBackground(GREEN);

                    } else if (tempAnswer.indexOf(guessChar) != -1 && ANSWER
                            .charAt(ANSWER.lastIndexOf(guessChar)) != guess.charAt(ANSWER.lastIndexOf(guessChar))) {
                        previousField.setBackground(YELLOW);
                        tempAnswer = tempAnswer.replaceFirst(String.valueOf(guessChar), "");
                        int letterIndex = ALPHABET.indexOf(guessChar);
                        if (KEYBOARD_KEYS.get(letterIndex).getBackground() != GREEN) {
                            KEYBOARD_KEYS.get(letterIndex).setBackground(YELLOW);
                        }

                    } else {
                        previousField.setBackground(GRAY);
                        if (ANSWER.indexOf(guessChar) == -1) {
                            int letterIndex = ALPHABET.indexOf(guessChar);
                            KEYBOARD_KEYS.get(letterIndex).setBackground(GRAY);
                        }
                    }
                    if (!guess.equals(ANSWER) && CURRENT_ROW != ROWS) {
                        TEXT_FIELDS[CURRENT_ROW][i].setEditable(true);
                    }
                    previousField.setForeground(WHITE);
                    previousField.setEditable(false);
                    previousField.removeKeyListener(new CursorMover());

                }

                if (guess.equals(ANSWER)) {
                    JTextField endTextField = new JTextField("CONGRATS!");
                    gameOver(WORDLE.this, endTextField, true);
                } else if (CURRENT_ROW == ROWS) {
                    JTextField endTextField = new JTextField("THE ANSWER WAS:\n" + ANSWER.toUpperCase());
                    gameOver(WORDLE.this, endTextField, false);
                } else {
                    TEXT_FIELDS[CURRENT_ROW][0].requestFocus();
                }

            } else if (fiveLetterCheck == true && !ERROR_EXISTS) {
                invalidInputDisplay(WORDLE.this, "NOT IN WORD LIST");
            } else if (!ERROR_EXISTS) {
                invalidInputDisplay(WORDLE.this, "NOT ENOUGH LETTERS");
            } else if (ERROR_EXISTS && fiveLetterCheck == true) {
                ERROR_DIALOGS.get(0).dispose();
                ERROR_DIALOGS.remove(0);
                invalidInputDisplay(WORDLE.this, "NOT IN WORD LIST");
            }
        }
    }

    class CursorMover implements KeyListener {

        public void keyTyped(KeyEvent e) {
            String keyChar = String.valueOf(e.getKeyChar());

            if (CURRENT_INDEX != 4 && keyChar.matches("[a-zA-Z]")) {
                TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX + 1].requestFocus();
            } else if (CURRENT_INDEX == 4 && keyChar.matches("[a-zA-Z]")) {
                TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX].requestFocus();
            }
        }

        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_RIGHT && CURRENT_INDEX != 4) {
                TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX + 1].requestFocus();
            } else if (keyCode == KeyEvent.VK_LEFT && CURRENT_INDEX != 0) {
                TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX - 1].requestFocus();
            } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                if (CURRENT_INDEX != 0 && TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX].getText().equals("")) {
                    TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX - 1].requestFocus();
                    TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX - 1].setText("");
                } else {
                    TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX].requestFocus();
                    TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX].setText("");
                }
            }
        }

        public void keyReleased(KeyEvent e) {
        };
    }

    class TextFilter extends DocumentFilter {
        public void replace(FilterBypass fb, int offs, int length,
                String str, AttributeSet a) throws BadLocationException {
            AbstractDocument doc = (AbstractDocument) fb.getDocument();

            String text = doc.getText(0, doc.getLength());
            text += str;
            if (str == "") {
                super.replace(fb, offs, length, "", a);
            } else if ((doc.getLength() + str.length()) <= 1
                    && text.matches("[a-zA-Z]+")) {
                super.insertString(fb, offs, str.toUpperCase(), a);
            } else if ((doc.getLength() + str.length()) == 2
                    && text.matches("[a-zA-Z]+")) {
                doc.remove(0, doc.getLength());
                super.insertString(fb, 0, str.toUpperCase(), a);
            }
        }

        public void insertString(FilterBypass fb, int offs, String str,
                AttributeSet a) throws BadLocationException {
            AbstractDocument doc = (AbstractDocument) fb.getDocument();

            String text = doc.getText(0, doc.getLength());
            text += str;
            if (str == "") {
                super.replace(fb, offs, 1, "", a);
            } else if ((doc.getLength() + str.length()) <= 1
                    && text.matches("[a-zA-Z]+")) {
                super.insertString(fb, offs, str.toUpperCase(), a);
            } else if ((doc.getLength() + str.length()) == 2
                    && text.matches("[a-zA-Z]+")) {
                doc.remove(0, doc.getLength());
                super.insertString(fb, 0, str.toUpperCase(), a);
            }
        }
    }

    class ButtonActionListener implements ActionListener {
        RoundedButton key;

        public ButtonActionListener(RoundedButton key) {
            this.key = key;
        }

        public void actionPerformed(ActionEvent e) {
            int id = KeyEvent.KEY_TYPED;

            if (key.getText() == "ENTER") {
                ActionEvent enterAction = new ActionEvent(this, 0, "", 0L, 0);
                new GuessChecker().actionPerformed(enterAction);
            } else if (key.getText() == "\u232b") {
                KeyEvent backspaceEvent = new KeyEvent(TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX], 0,
                        System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, 'a');
                new CursorMover().keyPressed(backspaceEvent);
            } else {
                char keyChar = key.getText().charAt(0);
                int keyCode = KeyEvent.VK_UNDEFINED;
                int modifiers = 0;
                KeyEvent keyEvent = new KeyEvent(TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX], id,
                        System.currentTimeMillis(), modifiers, keyCode, keyChar);
                new CursorMover().keyTyped(keyEvent);

                String keyString = String.valueOf(keyChar);
                TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX].setText(keyString);
            }
        }
    }

    class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            RoundRectangle2D roundedRect = new RoundRectangle2D.Float(5, 0, getWidth() - 10, getHeight() - 1, 10, 10);
            ((Graphics2D) g).fill(roundedRect);

            super.paintComponent(g);
        }
    }

    static class KeyUI extends BasicButtonUI {
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            g.setColor(new Color(80, 80, 80));
            RoundRectangle2D roundedRect = new RoundRectangle2D.Float(5, 0, b.getWidth() - 10, b.getHeight() - 1, 10,
                    10);
            ((Graphics2D) g).fill(roundedRect);
        }
    }

    public void generateAnswer() {

        try (InputStream inputStream = WORDLE.class.getResourceAsStream("/WORDS_FILE.txt");
        Scanner scanner = new Scanner(inputStream)){
            while (scanner.hasNextLine()) {
                String tempWord = scanner.nextLine().strip();
                FIVE_LETTER_WORDS.add(tempWord);
            }
        } catch (Exception e) {
            FIVE_LETTER_WORDS.add("DAVID");
            invalidInputDisplay(WORDLE.this, "PLEASE CHANGE FILEPATH FOR WORDS_FILE AT TOP OF CODE");
        }

        Random rand = new Random();
        int wordIndex = rand.nextInt(FIVE_LETTER_WORDS.size());
        ANSWER = FIVE_LETTER_WORDS.get(wordIndex).toLowerCase();
    }

    public void adjustFrame() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        setVisible(true);

        int x = (SCREEN_SIZE.width - getWidth()) / 2;
        int y = (SCREEN_SIZE.height - getHeight()) / 2;
        setLocation(x, y);
    }

    public void generateHeader() {

        JPanel titlePanel = new JPanel(new FlowLayout());
        titlePanel.setPreferredSize(new Dimension(400, 80));
        titlePanel.setBackground(BLACK);

        JTextField title = new JTextField("Wordle");
        title.setFont(FONT);
        title.setEditable(false);
        title.setBorder(null);
        title.setBackground(BLACK);
        title.setForeground(WHITE);

        titlePanel.add(title);
        Border paddingBorder = BorderFactory.createEmptyBorder(10, 0, 10, 0);
        title.setBorder(BorderFactory.createCompoundBorder(title.getBorder(), paddingBorder));
        getContentPane().add(titlePanel, BorderLayout.NORTH);
    }

    public void generateTextGrid() {

        GridLayout gridLayout = new GridLayout(ROWS, COLUMNS);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);

        JPanel gridPanel = new JPanel(gridLayout);
        gridPanel.setBorder(new EmptyBorder(0, 5, 8, 5));
        gridPanel.setBackground(BLACK);
        gridPanel.setForeground(WHITE);

        getContentPane().add(gridPanel, BorderLayout.CENTER);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                JTextField tempField = generateField(j);
                if (i == 0) {
                    tempField.setEditable(true);
                }
                TEXT_FIELDS[i][j] = tempField;
                gridPanel.add(tempField);
            }
        }
    }

    public void generateKeyboardAdder() {
        JPanel keyboardAdderPanel = new JPanel(new BorderLayout());
        keyboardAdderPanel.setBackground(BLACK);

        RoundedButton keyboardAdderButton = new RoundedButton("VIRTUAL KEYBOARD");
        keySetup(keyboardAdderButton, 15);
        ActionListener[] listener = keyboardAdderButton.getActionListeners();
        keyboardAdderButton.removeActionListener(listener[0]);
        keyboardAdderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!KEYBOARD_EXISTS) {
                    generateVirtualKeyboard();
                }
            }
        });
        keyboardAdderPanel.add(keyboardAdderButton, BorderLayout.CENTER);

        getContentPane().add(keyboardAdderPanel, BorderLayout.SOUTH);
    }

    public void generateVirtualKeyboard() {
        JFrame virtualKeyboard = new JFrame("Virtual Keyboard");
        virtualKeyboard.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                KEYBOARD_EXISTS = true;
            }

            public void windowClosing(WindowEvent e) {
                KEYBOARD_EXISTS = false;
            }
        });
        virtualKeyboard.setLayout(new BoxLayout(virtualKeyboard.getContentPane(), BoxLayout.Y_AXIS));
        int width = 525, height = 250;
        virtualKeyboard.setSize(width, height);

        JPanel firstLayer = generateKeyLayer(10, 0, 0, 0, 10, "QWERTYUIOP", 25);
        virtualKeyboard.add(firstLayer);

        JPanel secondLayer = generateKeyLayer(8, 25, 8, 25, 9, "ASDFGHJKL", 25);
        virtualKeyboard.add(secondLayer);

        JPanel thirdLayerOuter = new JPanel();
        thirdLayerOuter.setBackground(BLACK);
        thirdLayerOuter.setLayout(new FlowLayout(FlowLayout.LEFT));
        RoundedButton enterKey = new RoundedButton("ENTER");
        keySetup(enterKey, 12);
        enterKey.setPreferredSize(new Dimension(75, 50));
        thirdLayerOuter.add(enterKey);

        JPanel thirdLayer = generateKeyLayer(0, 0, 0, 0, 7, "ZXCVBNM", 25);
        thirdLayer.setPreferredSize(new Dimension(350, 50));
        thirdLayerOuter.add(thirdLayer);

        RoundedButton backspaceKey = new RoundedButton("\u232b");
        keySetup(backspaceKey, 20);
        backspaceKey.setPreferredSize(new Dimension(65, 50));
        thirdLayerOuter.add(backspaceKey);

        virtualKeyboard.add(thirdLayerOuter);

        int x = (SCREEN_SIZE.width + getWidth()) / 2 + 10;
        int y = (SCREEN_SIZE.height - height) / 2;
        virtualKeyboard.setLocation(x, y);
        virtualKeyboard.setVisible(true);

    }

    public void gameOver(JFrame frame, JTextField endTextField, boolean win) {
        String title;
        if (win == true) {
            title = "YOU WIN";
        } else {
            title = "YOU LOSE";
        }
        JDialog dialog = new JDialog(frame, title, true);

        int dialogHeight = 150;
        int dialogWidth = endTextField.getText().length() * 25;
        dialog.setSize(dialogWidth, dialogHeight);
        int x = (SCREEN_SIZE.width - dialogWidth) / 2;
        int y = (SCREEN_SIZE.height - frame.getHeight()) / 2 + 32;
        dialog.setLocation(x, y);
        dialog.setBackground(BLACK);

        endTextField.setFont(new Font("Arial", Font.BOLD, 30));
        endTextField.setEditable(false);
        endTextField.setBorder(null);
        endTextField.setHorizontalAlignment(JTextField.CENTER);
        endTextField.setBackground(BLACK);
        endTextField.setForeground(WHITE);

        dialog.add(endTextField);
        dialog.setVisible(true);
    }

    public void invalidInputDisplay(JFrame frame, String invalidWordText) {
        JDialog dialog = new JDialog(frame, "ERROR", false);
        int dialogHeight = 150;
        int dialogWidth = invalidWordText.length() * 21;
        dialog.setSize(dialogWidth, dialogHeight);
        int x = (SCREEN_SIZE.width - dialogWidth) / 2;
        int y = (SCREEN_SIZE.height - frame.getHeight()) / 2 + 32;
        dialog.setLocation(x, y);
        dialog.setBackground(BLACK);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                ERROR_EXISTS = true;
                ERROR_DIALOGS.add(dialog);
            }

            public void windowClosing(WindowEvent e) {
                ERROR_EXISTS = false;
                ERROR_DIALOGS.remove(dialog);
                TEXT_FIELDS[CURRENT_ROW][CURRENT_INDEX].requestFocus();
            }
        });

        JTextField invalidTextField = new JTextField(invalidWordText);

        invalidTextField.setFont(new Font("Arial", Font.BOLD, 30));
        invalidTextField.setEditable(false);
        invalidTextField.setBorder(null);
        invalidTextField.setHorizontalAlignment(JTextField.CENTER);
        invalidTextField.setBackground(BLACK);
        invalidTextField.setForeground(WHITE);

        dialog.add(invalidTextField);
        dialog.setVisible(true);
    }

    public JTextField generateField(int index) {
        JTextField field = new JTextField(1);
        field.setFont(FONT);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setEditable(false);
        field.setBackground(BLACK);
        field.setForeground(WHITE);

        field.addActionListener(new GuessChecker());
        field.addKeyListener(new CursorMover());
        field.addFocusListener(new FocusListener() {
            int fieldIndex = index;

            public void focusGained(FocusEvent e) {
                CURRENT_INDEX = fieldIndex;
            }

            public void focusLost(FocusEvent e) {
            }
        });

        AbstractDocument document = (AbstractDocument) field.getDocument();
        document.setDocumentFilter(new TextFilter());

        return field;
    }

    public JPanel generateKeyLayer(int borderTop, int borderLeft, int borderBottom, int borderRight, int gridCols,
            String keys, int fontSize) {
        JPanel keyLayer = new JPanel();
        keyLayer.setBackground(BLACK);
        keyLayer.setBorder(new EmptyBorder(borderTop, borderLeft, borderBottom, borderRight));
        keyLayer.setLayout(new GridLayout(1, gridCols));

        for (int i = 0; i < keys.length(); i++) {
            RoundedButton key = new RoundedButton(String.valueOf(keys.charAt(i)));
            keySetup(key, fontSize);
            keyLayer.add(key);
            KEYBOARD_KEYS.add(key);
        }
        return keyLayer;
    }

    public void keySetup(RoundedButton key, int fontSize) {
        key.setUI(new KeyUI());
        key.setFont(new Font("Arial Unicode MS", Font.BOLD, fontSize));
        key.setBackground(LIGHT_GRAY);
        key.setForeground(WHITE);
        key.setBorder(null);
        key.setFocusable(false);
        key.addActionListener(new ButtonActionListener(key));
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WORDLE();
            }
        });
    }
}
