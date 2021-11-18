package com.paulsen.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

public class PUIRotaryControl extends PUIElement {

    protected float valueLength = 0.5f;
    protected float rotationArea = 270; // in degrees
    protected float valueThickness = 20; // in degrees
    protected float mouseMultiplicator = 0.005f;
    private ArrayList<Runnable> valueUpdateAction = new ArrayList<Runnable>();
    private float value = .5f;
    private Color valueColor = Color.GRAY;
    private Color backgroundColor = Color.LIGHT_GRAY;
    private ElementAlignment alignment = ElementAlignment.VERTICAL;
    private boolean useMouseWheel = true;

    // tempVars
    private int mousePosX, mousePosY;
    private boolean isDragged = false;

    public PUIRotaryControl(PUIFrame f) {
        super(f);
        init();
    }

    public PUIRotaryControl(PUIFrame f, int layer) {
        super(f);
        init();
        setLayer(layer);
    }

    private void init() {
        paint = new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                if (w < 0)
                    w = -w;
                if (h < 0)
                    h = -h;

                // BG
                if (PUIElement.darkUIMode && backgroundColor == Color.LIGHT_GRAY)
                    g.setColor(PUIElement.darkBG_1);
                else
                    g.setColor(backgroundColor);
                g.fillOval(x, y, w, h);

                // value-visual
                if (PUIElement.darkUIMode && valueColor == Color.GRAY)
                    g.setColor(PUIElement.darkSelected);
                else
                    g.setColor(valueColor);

                // Value-Visual
                g.fillArc(x, y, w, h, (int) (360 - ((rotationArea * value + (360 - rotationArea) / 2 + 90) + valueThickness / 2)), (int) valueThickness);

                // Overpaint part of ^ , to visualize valueLength
                if (PUIElement.darkUIMode && backgroundColor == Color.LIGHT_GRAY)
                    g.setColor(PUIElement.darkBG_1);
                else
                    g.setColor(backgroundColor);

                g.fillOval((int) (x + (1.0f - valueLength) * (w / 2)), (int) (y + (1.0f - valueLength) * (h / 2)), (int) (w * (valueLength)), (int) (h * (valueLength)));

                // Outline
                if (PUIElement.darkUIMode)
                    g.setColor(PUIElement.darkOutline);
                else
                    g.setColor(Color.black);
                g.drawOval(x, y, w, h);

            }
        };
        hoverOverlay = new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(new Color(100, 100, 100, 100));
                g.fillOval(x, y, w, h);
            }
        };
        pressOverlay = new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(new Color(100, 100, 100, 200));
                g.fillOval(x, y, w, h);
            }
        };
        mouseMotionListeners.add(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                isDragged = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isHovered()) {
                    if (!isDragged) {
                        isDragged = true;
                        mousePosX = e.getX();
                        mousePosY = e.getY();
                    }
                }
                if (isDragged && isCurrentlyPressing()) {
                    if (alignment == ElementAlignment.VERTICAL) {
                        setValue(getValue() - (e.getY() - mousePosY) * mouseMultiplicator);
                    } else if (alignment == ElementAlignment.HORIZONTAL) {
                        setValue(getValue() - (e.getX() - mousePosX) * mouseMultiplicator);
                    }
                    mousePosX = e.getX();
                    mousePosY = e.getY();
                }
            }
        });
        mouseWheelListeners.add(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (useMouseWheel && isHovered()) {
                    setValue((float) (e.getWheelRotation() * 0.1 + getValue()));
                }
            }
        });
    }

    /**
     * only allows events in a oval area.
     */
    @Override
    public boolean contains(Point p) {
        return new Point(x + w / 2, y + h / 2).distanceSq(p.x, p.y) <= (w / 2) * (w / 2);
    }

    @Override
    public synchronized void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, w);
    }

    public void setBounds(int x, int y, int s) {
        if (s > 0)
            setBounds(x, y, s, s);
    }

    @Override
    public void setBounds(int w, int h) {
        setBounds(x, y, w, w);
    }

    public void setSize(int s) {
        setBounds(s, s);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        runAllValueUpdateActions();
        this.value = (value > 1 ? 1 : (value < 0 ? 0 : value));
    }

    public float getValueLength() {
        return valueLength;
    }

    public void setValueLength(float valueLength) {
        this.valueLength = valueLength;
    }

    public float getRotationArea() {
        return rotationArea;
    }

    public void setRotationArea(float rotationArea) {
        this.rotationArea = rotationArea;
    }

    public float getValueThickness() {
        return valueThickness;
    }

    public void setValueThickness(float valueThickness) {
        this.valueThickness = valueThickness;
    }

    public Color getValueColor() {
        return valueColor;
    }

    public void setValueColor(Color valueColor) {
        this.valueColor = valueColor;
    }

    public boolean isMouseWheelUsed() {
        return useMouseWheel;
    }

    public void setUseMouseWheel(boolean useMouseWheel) {
        this.useMouseWheel = useMouseWheel;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public float getMouseMultiplicator() {
        return mouseMultiplicator;
    }

    public void setMouseMultiplicator(float mouseMultiplicator) {
        this.mouseMultiplicator = mouseMultiplicator;
    }

    public ElementAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(ElementAlignment alignment) {
        this.alignment = alignment;
    }

    public void runAllValueUpdateActions() {
        if (repaintFrameOnEvent && frame != null) {
            frame.repaint();
        }

        if (valueUpdateAction != null)
            for (Runnable r : valueUpdateAction)
                if (r != null)
                    r.run();
    }

    public void addValueUpdateAction(Runnable r) {
        valueUpdateAction.add(r);
    }

    public void removeValueUpdateAction(Runnable r) {
        valueUpdateAction.remove(r);
    }

    public ArrayList<Runnable> getValueUpdateActions() {
        return valueUpdateAction;
    }

}
