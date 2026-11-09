import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class TextBox {
    public int x, y, width, height;
    private StringBuilder text = new StringBuilder();
    public String placeholder = "";
    public boolean focused = false;
    public boolean hasError = false;
    public boolean isPassword = false;
    public boolean showPassword = false;
    /** When true the box draws only text/caret (host draws the container). */
    public boolean bare = false;
    /** Darker/opaque field for use on frosted glass. */
    public boolean onGlass = false;

    private int caretPos = 0;
    private float caretTimer = 0f;
    private boolean caretVisible = true;
    private int selStart = -1;
    private int selEnd = -1;

    public TextBox(String placeholder) {
        this.placeholder = placeholder;
    }

    public TextBox(String placeholder, boolean isPassword) {
        this.placeholder = placeholder;
        this.isPassword = isPassword;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getText() {
        return text.toString();
    }

    public void setText(String s) {
        text = new StringBuilder(s == null ? "" : s);
        caretPos = text.length();
        clearSelection();
    }

    public void update(float dt) {
        if (focused) {
            caretTimer += dt;
            if (caretTimer >= 0.5f) {
                caretTimer = 0f;
                caretVisible = !caretVisible;
            }
        } else {
            caretVisible = false;
        }
    }

    private String getRenderText() {
        if (isPassword && !showPassword) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                sb.append('•');
            }
            return sb.toString();
        }
        return text.toString();
    }

    public void paint(Graphics2D g) {
        int radius = Math.min(16, height / 2);
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, width, height, radius, radius);

        if (!bare) {
            // Frosted glass field
            g.setColor(onGlass ? Fx.glassField(focused) : (focused ? Fx.surfaceHi() : Fx.surface()));
            g.fill(shape);

            if (hasError) {
                g.setColor(Theme.ERROR_RED);
                g.setStroke(new BasicStroke(1.5f));
                g.draw(shape);
            } else if (focused) {
                g.setColor(Fx.a(Fx.PINK, 45));
                g.setStroke(new BasicStroke(4f));
                g.draw(new RoundRectangle2D.Float(x - 1.5f, y - 1.5f, width + 3, height + 3, radius + 3, radius + 3));
                Fx.gradientBorder(g, shape, 1.5f, 1f, 0f);
            } else {
                g.setColor(Fx.border());
                g.setStroke(new BasicStroke(1f));
                g.draw(shape);
            }
        }

        // Font and metrics
        g.setFont(Fx.body(15.5f));
        FontMetrics fm = g.getFontMetrics();
        int padX = bare ? 2 : 16;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();

        int eyeAreaW = isPassword ? 36 : 0;

        // Clip text rendering
        Shape oldClip = g.getClip();
        g.setClip(new Rectangle(x + padX - 2, y, width - padX * 2 - eyeAreaW + 4, height));

        String rText = getRenderText();

        if (text.length() == 0 && !focused) {
            g.setColor(Fx.text3());
            g.drawString(placeholder, x + padX, textY);
        } else {
            if (hasSelection()) {
                int start = Math.min(selStart, selEnd);
                int end = Math.max(selStart, selEnd);
                String before = rText.substring(0, start);
                String selected = rText.substring(start, end);
                int sx1 = x + padX + fm.stringWidth(before);
                int sw = fm.stringWidth(selected);

                g.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 70));
                g.fillRect(sx1, y + 8, sw, height - 16);
            }

            g.setColor(Fx.text());
            g.drawString(rText, x + padX, textY);

            if (focused && caretVisible) {
                String sub = rText.substring(0, Math.min(caretPos, rText.length()));
                int cx = x + padX + fm.stringWidth(sub);
                g.setColor(Theme.BRAND_PINK);
                g.setStroke(new BasicStroke(1.8f));
                g.drawLine(cx, y + 10, cx, y + height - 10);
            }
        }
        g.setClip(oldClip);

        // Password Show/Hide Eye Icon
        if (isPassword) {
            int eyeX = x + width - 30;
            int eyeY = y + (height - 16) / 2;
            paintEyeIcon(g, eyeX, eyeY, 18, showPassword);
        }
    }

    private void paintEyeIcon(Graphics2D g, int ex, int ey, int size, boolean open) {
        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);
        g2.setColor(Theme.textSecondary());
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Eye shape
        Path2D.Float p = new Path2D.Float();
        p.moveTo(ex, ey + size / 2f);
        p.quadTo(ex + size / 2f, ey, ex + size, ey + size / 2f);
        p.quadTo(ex + size / 2f, ey + size, ex, ey + size / 2f);
        g2.draw(p);

        // Pupil
        float r = size * 0.2f;
        g2.fill(new Ellipse2D.Float(ex + size / 2f - r, ey + size / 2f - r, r * 2, r * 2));

        if (!open) {
            // Slashed eye line
            g2.drawLine(ex - 1, ey + size + 1, ex + size + 1, ey - 1);
        }
        g2.dispose();
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public boolean onMouseDown(int mx, int my) {
        if (contains(mx, my)) {
            // Check if eye icon clicked
            if (isPassword && mx >= x + width - 36) {
                showPassword = !showPassword;
                return true;
            }
            focused = true;
            caretTimer = 0f;
            caretVisible = true;
            clearSelection();
            return true;
        } else {
            focused = false;
            clearSelection();
            return false;
        }
    }

    public boolean onKey(KeyEvent e) {
        if (!focused) return false;

        int code = e.getKeyCode();
        char ch = e.getKeyChar();

        if (e.isControlDown()) {
            if (code == KeyEvent.VK_A) {
                selStart = 0;
                selEnd = text.length();
                caretPos = text.length();
                return true;
            } else if (code == KeyEvent.VK_C) {
                if (hasSelection()) {
                    int start = Math.min(selStart, selEnd);
                    int end = Math.max(selStart, selEnd);
                    copyToClipboard(text.substring(start, end));
                }
                return true;
            } else if (code == KeyEvent.VK_V) {
                String clip = getClipboardString();
                if (clip != null && !clip.isEmpty()) {
                    deleteSelection();
                    clip = clip.replace("\r", "").replace("\n", "");
                    text.insert(caretPos, clip);
                    caretPos += clip.length();
                    resetCaret();
                }
                return true;
            }
            return false;
        }

        if (code == KeyEvent.VK_BACK_SPACE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (caretPos > 0) {
                text.deleteCharAt(caretPos - 1);
                caretPos--;
                resetCaret();
            }
            return true;
        } else if (code == KeyEvent.VK_DELETE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (caretPos < text.length()) {
                text.deleteCharAt(caretPos);
                resetCaret();
            }
            return true;
        } else if (code == KeyEvent.VK_LEFT) {
            clearSelection();
            if (caretPos > 0) caretPos--;
            resetCaret();
            return true;
        } else if (code == KeyEvent.VK_RIGHT) {
            clearSelection();
            if (caretPos < text.length()) caretPos++;
            resetCaret();
            return true;
        } else if (code == KeyEvent.VK_HOME) {
            clearSelection();
            caretPos = 0;
            resetCaret();
            return true;
        } else if (code == KeyEvent.VK_END) {
            clearSelection();
            caretPos = text.length();
            resetCaret();
            return true;
        } else if (code == KeyEvent.VK_ESCAPE) {
            focused = false;
            return true;
        }

        if (ch >= 32 && ch != 127) {
            deleteSelection();
            text.insert(caretPos, ch);
            caretPos++;
            resetCaret();
            return true;
        }

        return false;
    }

    private void resetCaret() {
        caretTimer = 0f;
        caretVisible = true;
        hasError = false;
    }

    private boolean hasSelection() {
        return selStart >= 0 && selEnd >= 0 && selStart != selEnd;
    }

    private void clearSelection() {
        selStart = -1;
        selEnd = -1;
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        int start = Math.min(selStart, selEnd);
        int end = Math.max(selStart, selEnd);
        text.delete(start, end);
        caretPos = start;
        clearSelection();
        resetCaret();
    }

    private String getClipboardString() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
            return null;
        }
    }

    private void copyToClipboard(String str) {
        try {
            StringSelection sel = new StringSelection(str);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        } catch (Exception ignored) {}
    }
}
