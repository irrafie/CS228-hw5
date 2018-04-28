package edu.iastate.cs228.hw5;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import edu.iastate.cs228.hw5.Edge;
import edu.iastate.cs228.hw5.Graph;
import edu.iastate.cs228.hw5.Vertex;
import edu.iastate.cs228.hw5.Visitable;


/**
 * A graphical interface for showing graphs.
 *
 * @author Mike Petersen
 */
public
class
Visualizer
{
  private
  Graph
  graph;

  private
  DrawnVertex[]
  verts;

  private
  JFrame
  frame;

  private
  Ticker
  ticker;

  private
  Config
  config;


  public
  Visualizer()
  {
    this(768, 432);
  }

  public
  Visualizer(int w, int h)
  {
    this.config = new Config();

    initGUI(w, h);

    this.frame.setVisible(true);
    this.frame.pack();

    new Thread(this.ticker).start();
  }


  public
  void
  useGraph(Graph graph)
  {
    this.graph = graph;

    if (null != graph && (null == verts || config.resetVertsOnNewGraph))
    {
      int vertCount = graph.getVertices().length;
      int radius = Math.max(frame.getWidth(), frame.getHeight()) / 6;

      Vector rot = Vector.newFromPolar(1, 2 * Math.PI / vertCount);
      Vector pos = new Vector(radius, 0);
      Vector center = new Vector(frame.getWidth() / 2, frame.getHeight() / 2);

      DrawnVertex[] newVerts = new DrawnVertex[vertCount];
      for (int i = 0; i < newVerts.length; ++i)
      {
        newVerts[i] = new DrawnVertex(pos.add(center), i);
        pos = pos.multiplyComplex(rot);
      }
      verts = newVerts;
    }
  }

  private
  boolean
  hasGraph()
  {
    return null != graph && null != verts;
  }

  private
  void
  initGUI(int w, int h)
  {
    frame = new JFrame("PHT Graph Visualizer");
    JPanel panel = new VisPanel();

    panel.setPreferredSize(new Dimension(w, h));
    MouseAdapter mouser = new VisMouseAdapter();
    panel.addMouseListener(mouser);
    panel.addMouseMotionListener(mouser);

    frame.getContentPane().add(panel, BorderLayout.CENTER);

    JButton pause = new JButton("Toggle main thread (currently running)");
    pause.addActionListener(new ActionListener()
      {
        private
        boolean
        suspended
          = false;

        @SuppressWarnings("deprecation") // because c'mon
        @Override
        public
        void
        actionPerformed(ActionEvent e)
        {
          boolean found = false;
          // assume within the first 6 threads
          Thread[] threads = new Thread[6];
          Thread.enumerate(threads);
          for (Thread t : threads)
          {
            if (null != t && "main".equals(t.getName()))
            {
              found = true;

              if (suspended)
              {
                t.resume();
                pause.setText("Toggle main thread (currently running)");
              }
              else
              {
                t.suspend();
                pause.setText("Toggle main thread (currently suspended)");
              }

              suspended = !suspended;
              break;
            }
          }

          if (!found)
          {
            pause.setText("main thread has terminated");
          }
        }
      });
    frame.getContentPane().add(pause, BorderLayout.SOUTH);

    frame.addWindowListener(new WindowAdapter()
      {
        @Override
        public
        void
        windowClosed(WindowEvent e)
        {
          ticker.stop();
        }
      });

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


    JMenuBar menuBar = new JMenuBar();

    JMenu configMenu = new JMenu("Config");
    JMenuItem configMenuItem = new JMenuItem("Open config");
    configMenuItem.addActionListener(new ActionListener()
      {
        @Override
        public
        void
        actionPerformed(ActionEvent e)
        {
          editConfig();
        }
      });
    configMenu.add(configMenuItem);

    JMenuItem defaultConfigMenuItem = new JMenuItem("Restore default config");
    defaultConfigMenuItem.addActionListener(new ActionListener()
      {
        @Override
        public
        void
        actionPerformed(ActionEvent e)
        {
          config = new Config();
        }
      });
    configMenu.add(defaultConfigMenuItem);

    menuBar.add(configMenu);

    frame.setJMenuBar(menuBar);


    ticker = new Ticker(panel::repaint, 50);
  }

  private
  JDialog
  configWindow
    = null;

  private
  void
  editConfig()
  {
    if (null == configWindow)
    {
      configWindow = new JDialog(frame, "Configuration");

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      configWindow.getContentPane().add(panel, BorderLayout.CENTER);


      Supplier<Color> background
        = colorField(panel, "Background color:", config.background);

      Supplier<Color> visited
        = colorField(panel, "Visited color:", config.visited);

      Supplier<Color> unvisited
        = colorField(panel, "Unvisited color:", config.unvisited);

      Supplier<Color> highlight
        = colorField(panel, "Self-loop color:", config.selfLoop);

      IntSupplier vertexRadius
        = spinner(panel, "Vertex radius:", config.vertexRadius, 0, 128);

      IntSupplier edgeOffset
        = spinner(panel, "Edge offset:", config.edgeOffset, 0, 128);

      BooleanSupplier resetVerts
        = checkbox(panel, "Reset positions on new graph",
                   config.resetVertsOnNewGraph);

      JButton apply = new JButton("Apply");
      apply.addActionListener(new ActionListener()
        {
          @Override
          public
          void
          actionPerformed(ActionEvent e)
          {
            Config newConfig
              = new Config(background.get(), visited.get(), unvisited.get(),
                           highlight.get(), vertexRadius.getAsInt(),
                           edgeOffset.getAsInt(), resetVerts.getAsBoolean());

            if (newConfig.edgeOffset > newConfig.vertexRadius)
            {
              System.out.println("don't do that (edge offset > vert radius)");

              newConfig.edgeOffset = newConfig.vertexRadius;
            }

            config = newConfig;
          }
        });
      panel.add(apply);
      JButton accept = new JButton("Accept");
      accept.addActionListener(new ActionListener()
        {
          @Override
          public
          void
          actionPerformed(ActionEvent e)
          {
            Config newConfig
              = new Config(background.get(), visited.get(), unvisited.get(),
                           highlight.get(), vertexRadius.getAsInt(),
                           edgeOffset.getAsInt(), resetVerts.getAsBoolean());

            if (newConfig.edgeOffset > newConfig.vertexRadius)
            {
              System.out.println("don't do that (edge offset > vert radius)");

              newConfig.edgeOffset = newConfig.vertexRadius;
            }

            config = newConfig;

            configWindow.dispose();
          }
        });
      panel.add(accept);
    }

    configWindow.setVisible(true);
    configWindow.pack();
  }

  private
  Supplier<Color>
  colorField(JPanel addTo, String labelText, Color defaultValue)
  {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    JLabel label = new JLabel(labelText);
    panel.add(label);
    JTextField field = new JTextField(6);
    field.setText(colorToStr(defaultValue));

    panel.add(field);
    addTo.add(panel);

    return () ->
      {
        Color c = strToColor(field.getText());
        return (null == c) ? defaultValue : c;
      };
  }

  private
  BooleanSupplier
  checkbox(JPanel addTo, String labelText, boolean defaultValue)
  {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    JCheckBox check = new JCheckBox(labelText);
    panel.add(check);
    addTo.add(panel);

    return () -> check.isSelected();
  }

  private
  IntSupplier
  spinner(JPanel addTo, String labelText, int defaultValue, int min, int max)
  {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    JLabel label = new JLabel(labelText);
    panel.add(label);
    JSpinner spin
      = new JSpinner(new SpinnerNumberModel(defaultValue, min, max, 1));
    panel.add(spin);
    addTo.add(panel);

    return () -> (int) spin.getValue();
  }

  private
  Color
  strToColor(String s)
  {
    int rgb;
    try
    {
      rgb = Integer.parseInt(s, 16);
    }
    catch (NumberFormatException e)
    {
      return null;
    }

    return new Color(rgb);
  }

  private
  String
  colorToStr(Color c)
  {
    String str = Integer.toHexString(0xFFFFFF & c.getRGB());

    while (6 != str.length())
    {
      str = "0" + str;
    }

    return str;
  }


  private
  class
  Config
  {
    public
    Color
    background;

    public
    Color
    visited;

    public
    Color
    unvisited;

    public
    Color
    selfLoop;

    public
    int
    vertexRadius;

    public
    int
    edgeOffset;

    public
    boolean
    resetVertsOnNewGraph;


    public
    Config()
    {
      this(Color.WHITE, new Color(0xC04040), new Color(0x404040),
           new Color(0xFF8000), 12, 4, false);
    }

    public
    Config(Color background, Color visited, Color unvisited, Color highlight,
           int vertexRadius, int edgeOffset, boolean resetVertsOnNewGraph)
    {
      this.background = background;
      this.visited = visited;
      this.unvisited = unvisited;
      this.selfLoop = highlight;
      this.vertexRadius = vertexRadius;
      this.edgeOffset = edgeOffset;
      this.resetVertsOnNewGraph = resetVertsOnNewGraph;
    }


    public
    Color
    visitableColor(Visitable v)
    {
      return (v.isVisited()) ? visited : unvisited;
    }

    public
    int
    edgeAdjustment()
    {
      int r = vertexRadius;
      int d = edgeOffset;

      return r - (int) Math.round(Math.sqrt(r * r - d * d));
    }
  }

  private
  class
  Ticker
    implements Runnable
  {
    private
    final
    Runnable
    toRun;

    private
    final
    int
    millis;

    private
    volatile
    boolean
    running;


    public
    Ticker(Runnable toRun, int millis)
    {
      this.toRun = toRun;
      this.millis = millis;
      this.running = false;
    }


    public
    void
    stop()
    {
      running = false;
    }

    @Override
    public
    void
    run()
    {
      long m;
      int n;
      running = true;

      while (running)
      {
        long t = System.nanoTime();
        toRun.run();
        t = System.nanoTime() - t;

        t = millis * 1000000 - t;

        if (0 < t)
        {
          m = t / 1000000;
          n = (int) (t % 1000000);

          try
          {
            Thread.sleep(m, n);
          }
          catch (InterruptedException e)
          {
            // ignore
          }
        }
        else
        {
          System.err.printf("ticking took %dns but wanted less than %dns%n", t,
                            millis * 1000000);
        }
      }
    }
  }

  private
  class
  DrawnVertex
  {
    public
    Vector
    location;

    public
    int
    index;


    public
    DrawnVertex(Vector location, int index)
    {
      this.location = location;
      this.index = index;
    }


    public
    void
    draw(Graphics g)
    {
      int x = Math.round(location.getX());
      int y = Math.round(location.getY());

      Color origColor = g.getColor();

      g.fillOval(x - config.vertexRadius, y - config.vertexRadius,
                 2 * config.vertexRadius + 1, 2 * config.vertexRadius + 1);

      g.setColor(new Color(~g.getColor().getRGB()));
      g.drawString(String.valueOf(index), x - 6, y);

      g.setColor(origColor);
    }
  }

  private
  class
  VisMouseAdapter
    extends MouseAdapter
  {
    private
    int
    movingIdx
      = -1;

    private
    Vector
    lastPos
      = null;


    @Override
    public
    void
    mousePressed(MouseEvent e)
    {
      if (hasGraph())
      {
        lastPos = new Vector(e.getX(), e.getY());
        int radSq = config.vertexRadius * config.vertexRadius;

        for (int i = 0; i < verts.length; ++i)
        {
          Vector diff = lastPos.subtract(verts[i].location);
          if (diff.magnitudeSquared() <= radSq)
          {
            movingIdx = i;
            break;
          }
        }
      }
    }

    @Override
    public
    void
    mouseReleased(MouseEvent e)
    {
      movingIdx = -1;
    }

    @Override
    public
    void
    mouseDragged(MouseEvent e)
    {
      if (!hasGraph())
      {
        return;
      }

      if (0 <= movingIdx)
      {
        verts[movingIdx].location.setX(e.getX());
        verts[movingIdx].location.setY(e.getY());
      }
      else if (e.isShiftDown())
      {
        Vector pos = new Vector(e.getX(), e.getY());
        Vector diff = pos.subtract(lastPos);
        for (DrawnVertex v : verts)
        {
          v.location = v.location.add(diff);
        }
        lastPos = pos;
      }
    }
  }

  private
  class
  VisPanel
    extends JPanel
  {
    @Override
    public
    void
    paintComponent(Graphics g)
    {
      g.setColor(config.background);
      g.fillRect(0, 0, getWidth(), getHeight());

      if (!hasGraph())
      {
        return;
      }

      for (DrawnVertex v : verts)
      {
        Vertex vert = graph.getVertices()[v.index];
        boolean selfLoop = false;

        for (Edge out : vert.edges())
        {
          if (out.getFrom() == out.getTo())
          {
            selfLoop = true;
          }

          g.setColor(config.visitableColor(out));
          drawEdge(g, out);
        }

        if (!selfLoop)
        {
          g.setColor(config.visitableColor(vert));
        }
        else
        {
          g.setColor(config.selfLoop);
        }
        v.draw(g);
      }
    }

    private
    void
    drawEdge(Graphics g, Edge edge)
    {
      Vector textPos;

      if (edge.getFrom() == edge.getTo())
      {
        DrawnVertex fromDrawn = verts[edge.getFrom().index()];
        textPos = fromDrawn.location.add(new Vector(0, -config.vertexRadius));
      }
      else
      {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();

        DrawnVertex fromDrawn = verts[from.index()];
        DrawnVertex toDrawn = verts[to.index()];
        Vector fromVect = fromDrawn.location;
        Vector toVect = toDrawn.location;

        Vector direction = toVect.subtract(fromVect).unit();
        Vector off = direction.orthogNeg().multiply(config.edgeOffset);
        int adjustedRadius = config.vertexRadius - config.edgeAdjustment();

        Vector start
          = fromVect.add(direction.multiply(adjustedRadius)).add(off);

        Vector end
          = toVect.add(direction.multiply(-adjustedRadius)).add(off);

        drawArrow(g, start, end);

        Vector midpt = direction.multiply(end.subtract(start).magnitude() / 2);
        textPos = midpt.add(start).subtract(off);
      }

      g.drawString(edge.data(),
                   Math.round(textPos.getX() - 4 * edge.data().length()),
                   Math.round(textPos.getY()));
    }

    private
    void
    drawArrow(Graphics g, Vector from, Vector to)
    {
      Vector arrowHead = to.subtract(from).unit().multiply(5);

      g.drawLine(Math.round(from.getX()), Math.round(from.getY()),
                 Math.round(to.getX()), Math.round(to.getY()));

      Vector tmp = to.subtract(arrowHead.rotate(Math.PI / 6));
      g.drawLine(Math.round(to.getX()), Math.round(to.getY()),
                 Math.round(tmp.getX()), Math.round(tmp.getY()));

      tmp = to.subtract(arrowHead.rotate(-Math.PI / 6));
      g.drawLine(Math.round(to.getX()), Math.round(to.getY()),
                 Math.round(tmp.getX()), Math.round(tmp.getY()));
    }
  }
}

class
Vector
{
  private
  float
  x;

  private
  float
  y;


  public
  Vector(float x, float y)
  {
    this.x = x;
    this.y = y;
  }

  public
  static
  Vector
  newFromPolar(float r, double theta)
  {
    return new Vector(r * (float) Math.cos(theta),
                      r * (float) Math.sin(theta));
  }


  public
  float
  getX()
  {
    return x;
  }

  public
  void
  setX(float x)
  {
    this.x = x;
  }

  public
  float
  getY()
  {
    return y;
  }

  public
  void
  setY(float y)
  {
    this.y = y;
  }

  public
  Vector
  unit()
  {
    return multiply(1 / magnitude());
  }

  public
  float
  magnitude()
  {
    return (float) Math.sqrt(magnitudeSquared());
  }

  public
  float
  magnitudeSquared()
  {
    return this.dot(this);
  }

  public
  float
  dot(Vector other)
  {
    return this.x * other.x + this.y * other.y;
  }

  public
  Vector
  projectionOn(Vector onto)
  {
    return onto.multiply(this.dot(onto) / onto.magnitudeSquared());
  }

  public
  double
  angleBetween(Vector other)
  {
    double mag = Math.sqrt(this.magnitudeSquared() * other.magnitudeSquared());

    return Math.acos(this.dot(other) / mag);
  }

  public
  float
  cross(Vector right)
  {
    return this.x * right.y - this.y * right.x;
  }

  public
  Vector
  negate()
  {
    return new Vector(-x, -y);
  }

  public
  Vector
  add(Vector other)
  {
    return new Vector(this.x + other.x, this.y + other.y);
  }

  public
  Vector
  subtract(Vector right)
  {
    return new Vector(this.x - right.x, this.y - right.y);
  }

  public
  Vector
  multiply(float scalar)
  {
    return new Vector(scalar * x, scalar * y);
  }

  public
  Vector
  multiplyComplex(Vector other)
  {
    return new Vector(this.x * other.x - this.y * other.y,
                      this.x * other.y + this.y * other.x);
  }

  public
  Vector
  orthogPos()
  {
    return new Vector(-y, x);
  }

  public
  Vector
  orthogNeg()
  {
    return new Vector(y, -x);
  }

  public
  Vector
  rotate(double angle)
  {
    return multiplyComplex(newFromPolar(1, angle));
  }

  public
  Vector
  rotateAround(double angle, Vector pole)
  {
    return this.subtract(pole).rotate(angle).add(pole);
  }

  @Override
  public
  boolean
  equals(Object obj)
  {
    if (null == obj || !this.getClass().equals(obj.getClass()))
    {
      return false;
    }

    Vector o = (Vector) obj;

    return this.x == o.x && this.y == o.y;
  }

  @Override
  public
  int
  hashCode()
  {
    return Float.hashCode(x) ^ Integer.rotateLeft(Float.hashCode(y), 16);
  }

  @Override
  public
  String
  toString()
  {
    return "<" + x + ", " + y + ">";
  }
}

