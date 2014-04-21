import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parabot.core.ui.components.LogArea;
import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.api.events.MessageEvent;
import org.rev317.api.events.listeners.MessageListener;
import org.rev317.api.methods.Camera;
import org.rev317.api.methods.Interfaces;
import org.rev317.api.methods.Inventory;
import org.rev317.api.methods.Menu;
import org.rev317.api.methods.Npcs;
import org.rev317.api.methods.Players;
import org.rev317.api.methods.SceneObjects;
import org.rev317.api.methods.Skill;
import org.rev317.api.wrappers.hud.Item;
import org.rev317.api.wrappers.interactive.Npc;
import org.rev317.api.wrappers.scene.Area;
import org.rev317.api.wrappers.scene.SceneObject;
import org.rev317.api.wrappers.scene.Tile;
import org.rev317.api.wrappers.walking.TilePath;

@ScriptManifest(author = "BigShot", category = Category.MINING, description = "Mines ores on UltimateScape 2", name = "USMiner", servers = { "UltimateScape" }, version = 1.0)
public class USMiner extends Script implements Paintable, MessageListener {

	private final ArrayList<Strategy> strategies = new ArrayList<Strategy>();
	private final Tile[] PATH = { new Tile(2384, 4721, 0), new Tile(2384, 4715,0), new Tile(2388, 4710 ,0), new Tile(2393, 4713, 0), new Tile(2397, 4714, 0) };
	public static Area bank = new Area (new Tile(2395, 4719, 0), new Tile(2401, 4719, 0), new Tile(2401, 4712, 0), new Tile(2395, 4712, 0));
	public static Area mine = new Area (new Tile(2378, 4727, 0), new Tile(2378, 4715, 0), new Tile(2389, 4715, 0), new Tile(2389, 4727, 0));
	public int oreID;
	public int lvl;
	public int oreCount;
	public int cashMade;
	public int currentLVL;
	public int oreChoice = 1;
	public int[] oreIDs = {436, 438, 440, 453, 444, 449, 447, 451};
	public boolean guiWait = true;
	Gui g = new Gui();
	private final Color color1 = new Color(255, 255, 255);
	private final Font font1 = new Font("Arial", 0, 20);
	private final Font font2 = new Font("Arial", 0, 14);
	private final Color color2 = new Color(225, 50, 55);
	private final Timer RUNTIME = new Timer();

	@Override
	public boolean onExecute() {
		g.setVisible(true);
		while (guiWait) {
			sleep(500);
		}
		
		strategies.add(new Walk());
		strategies.add(new Mine());
		strategies.add(new BankOre());
		provide(strategies);
		return true;
	}

	private boolean atMine() {
		if (mine.contains(Players.getLocal().getLocation())) {
			return true;
		}
		return false;
	}

	private boolean atBank() {
		if (bank.contains(Players.getLocal().getLocation())) {
			return true;
		}
		return false;
	}

	@Override
	public void paint(Graphics arg0) {

		Graphics2D g = (Graphics2D) arg0;
		g.setColor(new Color(0f, 0f, 0f, .5f));
		g.fillRect(4, 23, 150, 65);
		g.setColor(color2);
		g.setFont(font1);
		g.drawString("USMiner", 6, 43);
		g.setFont(font2);
		g.setColor(color1);
		g.drawString("Ores Mined: " + oreCount, 6, 57);
		g.drawString("Cash Made: " + cashMade, 6, 70);
		g.drawString("Runtime: " + RUNTIME, 6, 83);
	}

	public void onFinish() {

	}

	public class Walk implements Strategy {

		@Override
		public boolean activate() {
			if (!Inventory.isFull() && !mine.contains(Players.getLocal().getLocation()))
				return true;
			else if (Inventory.isFull() &&!bank.contains(Players.getLocal().getLocation()))
				return true;
			else 
				return false;
		}

		@Override
		public void execute() {
			if (!Inventory.isFull() && !mine.contains(Players.getLocal().getLocation())) {
				TilePath path = new TilePath(PATH).reverse();
				if (path != null) {
					path.traverse();
					Time.sleep(1000);
				}
			}
			if (Inventory.isFull() && !bank.contains(Players.getLocal().getLocation())) {
				TilePath path = new TilePath(PATH);
				if (path != null) {
					path.traverse();
					Time.sleep(1000);
				}
			}
		}

	}

	public class Mine implements Strategy {

		@Override
		public boolean activate() {
			final SceneObject Ores[] = SceneObjects.getNearest(oreID);
			final SceneObject Ore = Ores[0];
			return !Inventory.isFull() 
					&& atMine()
					&& Ore != null;
		}

	/*	@Override
		public void execute() {

			final SceneObject[] Ores = SceneObjects.getNearest(oreID);
			final SceneObject Ore = Ores[0];
			if (!Inventory.isFull() 
					&& Ore != null 
					&& Players.getLocal().getAnimation() == -1 
					&& !Players.getLocal().isWalking()) {
				Time.sleep(500);
				if (!Inventory.isFull() && Ore.isOnScreen() && Players.getLocal().getAnimation() == -1) {
					Ore.interact("");
					Time.sleep(2000);
				} else {
					Tile Rock = Ore.getLocation();
					Rock.clickMM();
					Time.sleep(500);
				}
			}
		}*/
		@Override
		public void execute() {
			final SceneObject Ores[] = SceneObjects.getNearest(oreID);
			final SceneObject Ore = Ores[0];
			Time.sleep(500);
			if (Ore.isOnScreen() && Players.getLocal().getAnimation() == -1 && !Players.getLocal().isWalking()) {
				Ore.interact("Mine");
				Time.sleep(2000);
			} else  if (!Ore.isOnScreen() && Players.getLocal().getAnimation() != -1){
				Tile Rock = Ore.getLocation();
				Rock.clickMM();
				Time.sleep(200);
			}
			while (Players.getLocal().getAnimation() != -1) {
				Time.sleep(200);

			}
		}
	}

	public class BankOre implements Strategy {

		@Override
		public boolean activate() {
			final Npc Booths[] = Npcs.getNearest(953);
			final Npc Banker = Booths[0];
			return (Inventory.isFull() 
					&& bank.contains(Players.getLocal().getLocation()));
		}

		@Override
		public void execute() {

			final Npc Booths[] = Npcs.getNearest(953);
			final Npc Banker = Booths[0];

			if (Interfaces.getOpenInterfaceId() != 5292 && Banker != null) {
				Npcs.getNearest(953)[0].interact("");
				Time.sleep(200);
			} else if (Interfaces.getOpenInterfaceId() == 5292) {
				//Menu.interact("Deposit carried tems", new Point(400, 300));
				Time.sleep(500);
				for (Item i : Inventory.getItems(oreIDs)) {
					i.interact("Store All");
				}
				Time.sleep(1000);
			} else if (!Banker.isOnScreen()) {
				Banker.getLocation().clickMM();
			}
			Time.sleep(100);
		}
	}

	@Override
	public void messageReceived(MessageEvent me) {
		if (me.getMessage().contains("You get some copper ore.")) {
			oreCount += 1;
			cashMade += 137;
		} else if (me.getMessage().contains("You get some iron ore.")) {
			oreCount += 1;
			cashMade += 137;
		} else if (me.getMessage().contains("You get some coal.")) {
			oreCount += 1;
			cashMade += 405;
		} else if (me.getMessage().contains("You get some mithril ore.")) {
			oreCount += 1;
			cashMade += 729;
		} else if (me.getMessage().contains("You get some adamantite ore.")) {
			oreCount += 1;
			cashMade += 769;
		} else if (me.getMessage().contains("You get some runite ore.")) {
			oreCount += 1;
			cashMade += 1012;
		}
	}
	
	public class Gui extends JFrame {

		private JPanel contentPane;

		public void main(String[] args) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						Gui frame = new Gui();
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		 public Gui() {
			initComponents();
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 150, 180);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);

			lblUSMiner = new JLabel("USMiner");
			lblUSMiner.setFont(new Font("Arial", Font.PLAIN, 20));
			lblUSMiner.setBounds(15, 11, 101, 16);
			contentPane.add(lblUSMiner);

			lblWhatOre = new JLabel("Choose Ore");
			lblWhatOre.setBounds(17, 49, 82, 14);
			contentPane.add(lblWhatOre);

			rockToMine = new JComboBox();
			rockToMine.setModel(new DefaultComboBoxModel(new String[] {"Copper", "Tin", "Iron", "Coal", "Gold", "Mith", "Addy", "Rune"}));
			rockToMine.setBounds(17, 75, 82, 20);
			contentPane.add(rockToMine);

			btnStart = new JButton("Start");
			btnStart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String chosen = rockToMine.getSelectedItem().toString();
					if(rockToMine.getSelectedItem().equals("Copper")) {
						oreID = 2090;
					} else if (rockToMine.getSelectedItem().equals("Tin")) {
						oreID = 2095;
					} else if (rockToMine.getSelectedItem().equals("Iron")) {
						oreID = 2093;
					} else if (rockToMine.getSelectedItem().equals("Coal")) {
						oreID = 2096;
					} else if (rockToMine.getSelectedItem().equals("Gold")) {
						oreID = 2108;
					} else if (rockToMine.getSelectedItem().equals("Mith")) {
						oreID = 2102;
					} else if (rockToMine.getSelectedItem().equals("Addy")) {
						oreID = 2104;
					} else if (rockToMine.getSelectedItem().equals("Rune")) {
						oreID = 2106;
					}
					guiWait= false;
					g.dispose();
				}
			});
			btnStart.setBounds(10, 112, 89, 23);
			contentPane.add(btnStart);
		 }
		 private void initComponents() {
			 lblUSMiner = new JLabel();
			 lblWhatOre = new JLabel();
			 rockToMine = new JComboBox();

		 }
		 private JLabel lblUSMiner;
		 private JButton btnStart;
		 private JComboBox rockToMine;
		 private JLabel lblWhatOre;

	}

}
