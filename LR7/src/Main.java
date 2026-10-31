import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/** Геометричні обчислення для завдання. */
class TriangleGeometry {
    static final double EPS = 1e-6;
    static final String[] SIDE_NAMES = {"AB", "BC", "CA"};
    static final String[] VERTEX_NAMES = {"A", "B", "C"};

    /** Результат: на яких сторонах лежить точка та текст відповіді. */
    static class Result {
        final boolean[] onSide;
        final String answer;

        Result(boolean[] onSide, String answer) {
            this.onSide = onSide;
            this.answer = answer;
        }

        boolean onAnySide() {
            return onSide[0] || onSide[1] || onSide[2];
        }
    }

    /** Векторний добуток (a − o) × (b − o). */
    static double cross(Point2D o, Point2D a, Point2D b) {
        return (a.getX() - o.getX()) * (b.getY() - o.getY())
                - (a.getY() - o.getY()) * (b.getX() - o.getX());
    }

    static boolean onSegment(Point2D p, Point2D s1, Point2D s2) {
        return Line2D.ptSegDist(s1.getX(), s1.getY(), s2.getX(), s2.getY(),
                p.getX(), p.getY()) < EPS;
    }

    static Result locate(Point2D a, Point2D b, Point2D c, Point2D p) {
        double area = cross(a, b, c);
        if (Math.abs(area) < EPS) {
            return new Result(new boolean[3],
                    "точки A, B і C лежать на одній прямій — трикутник не існує");
        }

        boolean[] on = {
                onSegment(p, a, b),
                onSegment(p, b, c),
                onSegment(p, c, a)
        };

        // Точка на двох сторонах одночасно — це вершина
        for (int i = 0; i < 3; i++) {
            int next = (i + 1) % 3;
            if (on[i] && on[next]) {
                return new Result(on, "точка P збігається з вершиною " + VERTEX_NAMES[next]
                        + " (лежить на сторонах " + SIDE_NAMES[i] + " і "
                        + SIDE_NAMES[next] + ")");
            }
        }

        for (int i = 0; i < 3; i++) {
            if (on[i]) {
                return new Result(on, "точка P лежить на стороні " + SIDE_NAMES[i]);
            }
        }

        double d1 = cross(a, b, p);
        double d2 = cross(b, c, p);
        double d3 = cross(c, a, p);
        boolean inside = (d1 > 0 && d2 > 0 && d3 > 0) || (d1 < 0 && d2 < 0 && d3 < 0);
        return new Result(on, "точка P не лежить на жодній стороні — вона "
                + (inside ? "всередині трикутника" : "поза трикутником"));
    }
}

/** Панель, на якій виконується креслення. */
class DrawingPanel extends JPanel {
    private Point2D a, b, c, p;
    private TriangleGeometry.Result result;

    private double scale, offX, offY;

    DrawingPanel() {
        setBackground(Color.WHITE);
    }

    void setData(Point2D a, Point2D b, Point2D c, Point2D p) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.p = p;
        this.result = TriangleGeometry.locate(a, b, c, p);
        repaint();
    }

    // Перетворення математичних координат в екранні
    private int sx(double x) {
        return (int) Math.round(offX + x * scale);
    }

    private int sy(double y) {
        return (int) Math.round(offY - y * scale);
    }

    private static double min(double... values) {
        double m = values[0];
        for (double v : values) m = Math.min(m, v);
        return m;
    }

    private static double max(double... values) {
        double m = values[0];
        for (double v : values) m = Math.max(m, v);
        return m;
    }

    /** «Гарний» крок сітки: 1, 2, 5, 10, 20, 50... */
    private static double niceStep(double raw) {
        double pow = Math.pow(10, Math.floor(Math.log10(raw)));
        for (int m : new int[]{1, 2, 5, 10}) {
            if (m * pow >= raw) {
                return m * pow;
            }
        }
        return 10 * pow;
    }

    private static String fmt(double v) {
        if (v == Math.rint(v)) {
            return String.valueOf((long) v);
        }
        return String.format("%.2f", v);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTaskText(g2);
        if (a == null) {
            return;
        }

        // Межі області креслення (з початком координат і запасом в 1 одиницю)
        double minX = min(0, a.getX(), b.getX(), c.getX(), p.getX()) - 1;
        double maxX = max(0, a.getX(), b.getX(), c.getX(), p.getX()) + 1;
        double minY = min(0, a.getY(), b.getY(), c.getY(), p.getY()) - 1;
        double maxY = max(0, a.getY(), b.getY(), c.getY(), p.getY()) + 1;

        int top = 70, bottom = 60, side = 40;
        int w = getWidth() - 2 * side;
        int h = getHeight() - top - bottom;
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        scale = Math.min(w / rangeX, h / rangeY);
        offX = side + (w - rangeX * scale) / 2 - minX * scale;
        offY = top + (h - rangeY * scale) / 2 + maxY * scale;

        drawGrid(g2, minX, maxX, minY, maxY);
        drawAxes(g2, minX, maxX, minY, maxY);
        drawTriangle(g2);
        drawPoint(g2);
        drawAnswer(g2);
    }

    private void drawTaskText(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Завдання (варіант 26): задано координати вершин трикутника "
                + "і координати точки.", 20, 25);
        g2.drawString("Визначте, на якій стороні трикутника розташована ця точка.", 20, 45);
    }

    private void drawGrid(Graphics2D g2, double minX, double maxX, double minY, double maxY) {
        double step = niceStep(40 / scale);  // не менше ~40 пікселів між лініями
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

        for (long k = (long) Math.ceil(minX / step); k * step <= maxX; k++) {
            double x = k * step;
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(sx(x), sy(minY), sx(x), sy(maxY));
            if (k != 0) {
                g2.setColor(Color.GRAY);
                g2.drawString(fmt(x), sx(x) - 6, sy(0) + 15);
            }
        }
        for (long k = (long) Math.ceil(minY / step); k * step <= maxY; k++) {
            double y = k * step;
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(sx(minX), sy(y), sx(maxX), sy(y));
            if (k != 0) {
                g2.setColor(Color.GRAY);
                g2.drawString(fmt(y), sx(0) + 5, sy(y) + 4);
            }
        }
    }

    private void drawAxes(Graphics2D g2, double minX, double maxX, double minY, double maxY) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Вісь X зі стрілкою
        int xEnd = sx(maxX), y0 = sy(0);
        g2.drawLine(sx(minX), y0, xEnd, y0);
        g2.fillPolygon(new int[]{xEnd, xEnd - 10, xEnd - 10}, new int[]{y0, y0 - 5, y0 + 5}, 3);
        g2.drawString("X", xEnd - 12, y0 - 10);

        // Вісь Y зі стрілкою
        int yEnd = sy(maxY), x0 = sx(0);
        g2.drawLine(x0, sy(minY), x0, yEnd);
        g2.fillPolygon(new int[]{x0, x0 - 5, x0 + 5}, new int[]{yEnd, yEnd + 10, yEnd + 10}, 3);
        g2.drawString("Y", x0 + 10, yEnd + 12);

        g2.drawString("0", x0 - 12, y0 + 15);
    }

    private void drawTriangle(Graphics2D g2) {
        Point2D[] pts = {a, b, c};

        Path2D.Double path = new Path2D.Double();
        path.moveTo(sx(a.getX()), sy(a.getY()));
        path.lineTo(sx(b.getX()), sy(b.getY()));
        path.lineTo(sx(c.getX()), sy(c.getY()));
        path.closePath();

        g2.setColor(new Color(100, 149, 237, 60));
        g2.fill(path);
        g2.setColor(new Color(30, 90, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(path);

        // Виділення сторони, на якій лежить точка
        g2.setColor(new Color(220, 20, 60));
        g2.setStroke(new BasicStroke(4f));
        for (int i = 0; i < 3; i++) {
            if (result.onSide[i]) {
                Point2D s1 = pts[i], s2 = pts[(i + 1) % 3];
                g2.drawLine(sx(s1.getX()), sy(s1.getY()), sx(s2.getX()), sy(s2.getY()));
            }
        }

        // Вершини та підписи
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (int i = 0; i < 3; i++) {
            int x = sx(pts[i].getX()), y = sy(pts[i].getY());
            g2.setColor(Color.BLACK);
            g2.fillOval(x - 3, y - 3, 6, 6);
            g2.setColor(new Color(30, 90, 200));
            g2.drawString(TriangleGeometry.VERTEX_NAMES[i] + "(" + fmt(pts[i].getX()) + "; "
                    + fmt(pts[i].getY()) + ")", x + 6, y - 6);
        }
    }

    private void drawPoint(Graphics2D g2) {
        int x = sx(p.getX()), y = sy(p.getY());
        g2.setColor(new Color(220, 20, 60));
        g2.fillOval(x - 5, y - 5, 10, 10);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("P(" + fmt(p.getX()) + "; " + fmt(p.getY()) + ")", x + 8, y + 16);
    }

    private void drawAnswer(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(result.onAnySide() ? new Color(0, 120, 0) : new Color(0, 70, 160));
        g2.drawString("Відповідь: " + result.answer, 20, getHeight() - 25);
    }
}

/** Головне вікно з полями введення координат. */
class TriangleFrame extends JFrame {
    private static final String[] LABELS = {"Ax", "Ay", "Bx", "By", "Cx", "Cy", "Px", "Py"};
    private static final String[] DEFAULTS = {"-4", "-2", "5", "-2", "1", "5", "2", "-2"};

    private final JTextField[] fields = new JTextField[8];
    private final DrawingPanel panel = new DrawingPanel();

    TriangleFrame() {
        super("ЛР7. Варіант 26 — точка на стороні трикутника");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (int i = 0; i < fields.length; i++) {
            controls.add(new JLabel(LABELS[i] + ":"));
            fields[i] = new JTextField(DEFAULTS[i], 4);
            controls.add(fields[i]);
        }
        JButton calcButton = new JButton("Обчислити");
        calcButton.addActionListener(e -> calculate());
        controls.add(calcButton);

        add(controls, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        setSize(950, 750);
        setLocationRelativeTo(null);
        calculate();
    }

    private void calculate() {
        try {
            double[] v = new double[8];
            for (int i = 0; i < v.length; i++) {
                v[i] = Double.parseDouble(fields[i].getText().trim().replace(',', '.'));
            }
            panel.setData(
                    new Point2D.Double(v[0], v[1]),
                    new Point2D.Double(v[2], v[3]),
                    new Point2D.Double(v[4], v[5]),
                    new Point2D.Double(v[6], v[7]));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Усі координати мають бути числами.",
                    "Помилка введення", JOptionPane.ERROR_MESSAGE);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TriangleFrame().setVisible(true));
    }
}