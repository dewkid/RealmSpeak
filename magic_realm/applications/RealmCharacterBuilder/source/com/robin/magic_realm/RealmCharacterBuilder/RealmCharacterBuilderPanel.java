/* 
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.io.FileManager;
import com.robin.general.swing.*;
import com.robin.general.util.StringBufferedList;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.swing.RelationshipTable;
import com.robin.magic_realm.components.utility.BattleUtility;
import com.robin.magic_realm.components.utility.Constants;

public class RealmCharacterBuilderPanel extends JPanel {
	
	private RealmCharacterBuilderModel model;
	
	private JFrame parent;
	private JLabel mainIcon;
	private JLabel fairnessLabel;
	private JLabel moveFairnessReasonLabel;
	private JLabel fightFairnessReasonLabel;
	
	private JButton editSymbolButton;
	private JButton loadPictureButton;
	private JButton viewCardButton;
	private JButton viewGearButton;
	private JButton extraNotesButton;
	private JButton evaluateButton;
	
	private JPanel chitGrid;
	private JPanel leftPanel;
	private JPanel topPanel;
	
	private CharacterLevelPanel[] levelPanel;
	
	private RelationshipTable relationshipTable;
	
	// Creator
	private JTextField creatorField;
	private JTextField artCreditField;
	
	// Symbol Meaning
	private JTextField symbolMeaningField;
	
	// Starting Locations
	private JCheckBox startInnChoice; // will be disabled
	private JCheckBox startGuardChoice;
	private JCheckBox startChapelChoice;
	private JCheckBox startHouseChoice;
	private JCheckBox startGhostsChoice;
	
	// Vulnerability
	private JRadioButton vulnerabilityLight;
	private JRadioButton vulnerabilityMedium;
	private JRadioButton vulnerabilityHeavy;
	private JRadioButton vulnerabilityTremendous;
	
	// Class
	private JRadioButton fighterClassOption;
	private JRadioButton mageClassOption;
	
	// Gender
	private JRadioButton genderMale;
	private JRadioButton genderFemale;
	
	// Class variables
//	private File lastSymbolPath;
	private GameData magicRealmData;
	private FileManager graphicsManager;
	private String[] ACCEPT_EXT = {".png",".gif",".jpg"};
	private FileFilter loadFileFilter = new FileFilter() {
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			else if (f.isFile()) {
				for (int i=0;i<ACCEPT_EXT.length;i++) {
					if (f.getPath().toLowerCase().endsWith(ACCEPT_EXT[i])) {
						return true;
					}
				}
			}
			return false;
		}

		public String getDescription() {
			return "Graphics Files (*.png,*.gif,*.jpg)";
		}
	};
	
	public RealmCharacterBuilderPanel(JFrame frame,RealmCharacterBuilderModel model,GameData magicRealmData) {
		this.parent = frame;
		this.model = model;
		this.magicRealmData = magicRealmData;
		graphicsManager = new FileManager(frame,"JPG Files (*.jpg)","jpg");
		graphicsManager.setLoadFileFilter(loadFileFilter);
		initComponents();
	}
	public FileManager getGraphicsManager() {
		return graphicsManager;
	}
	public void saveCardTo(File file,boolean includePicture) {
		CharacterInfoCard card = model.getCard();
		RealmCharacterBuilderModel.exportImage(file,card.getImageIcon(includePicture),"JPG");
	}
	public void saveStuffTo(File file) {
		RealmCharacterBuilderModel.exportImage(file,getGearImage(),"JPG");
	}
	private ImageIcon getGearImage() {
		int width = 650;
		int height = 600;
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,width,height);
		
		g.setColor(Color.black);
		g.setFont(new Font("Dialog",Font.BOLD,36));
		g.drawString(model.getCharacter().getCharacterLevelName(4),5,45);
		
		int b = 5;
		int x = b;
		int y = 60;
		int n = 0;
		CharacterChitComponent token = model.getCharacterToken();
		CharacterActionChitComponent attention = BattleUtility.getAttentionMarker(model.getCharacter());
		g.drawImage(token.getImage(),x,y,null);
		g.drawImage(token.getFlipSideImage(),x+105,y,null);
		g.drawImage(attention.getImage(),x+258,y+62,null);
		y += 120;
		for (GameObject go:model.getAllChits()) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)RealmComponent.getRealmComponent(go);
			ImageIcon icon = chit.getIcon();
			g.drawImage(icon.getImage(),x,y,null);
			g.drawImage(attention.getImage(),x+200,y,null);
			x += icon.getIconWidth()+b;
			n++;
			if (n>2) {
				n = 0;
				x = b;
				y += icon.getIconHeight()+b;
			}
		}
		y+=10;
		for (GameObject go:model.getAllUniqueWeapons()) {
			WeaponChitComponent weapon = (WeaponChitComponent)RealmComponent.getRealmComponent(go);
			ImageIcon icon = weapon.getIcon();
			g.drawImage(icon.getImage(),x,y,null);
			g.drawImage(weapon.getFlipSideImage(),x,y+icon.getIconHeight()+b,null);
			x += icon.getIconWidth()+b;
		}
		for (GameObject go:model.getAllUniqueArmor(magicRealmData)) {
			ArmorChitComponent armor = new ArmorChitComponent(go);
			ImageIcon icon = armor.getIcon();
			g.drawImage(icon.getImage(),x,y,null);
			g.drawImage(armor.getFlipSideImage(),x,y+icon.getIconHeight()+b,null);
			x += icon.getIconWidth()+b;
		}
		y = 60;
		x = 400;
		for (GameObject go:model.getAllCompanions()) {
			ChitComponent companion = (ChitComponent)RealmComponent.getRealmComponent(go);
			ImageIcon icon = companion.getIcon();
			g.drawImage(icon.getImage(),x,y,null);
			g.drawImage(companion.getFlipSideImage(),x+icon.getIconWidth()+b,y,null);
			y += icon.getIconHeight()+b;
		}
		for (GameObject go:model.getAllExtraChits()) {
			CharacterActionChitComponent chit = new CharacterActionChitComponent(go);
			ImageIcon icon = chit.getIcon();
			g.drawImage(icon.getImage(),x,y,null);
			g.drawImage(chit.getFlipSideImage(),x+icon.getIconWidth()+b,y,null);
			y += icon.getIconHeight()+b;
		}
		for (GameObject go:model.getAllExtraInventory()) {
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			ImageIcon icon = rc.getIcon();
			g.drawImage(icon.getImage(),x,y,null);
			if (rc.isHorse()) {
				SteedChitComponent horse = (SteedChitComponent)rc;
				g.drawImage(horse.getFlipSideImage(),x+icon.getIconWidth()+b,y,null);
			}
			y += icon.getIconHeight()+b;
		}
		RealmComponent.reset();
		return new ImageIcon(bi);
	}
	private void initComponents() {
		setLayout(new BorderLayout());
		
		chitGrid = new JPanel(new GridLayout(4,1));
		levelPanel = new CharacterLevelPanel[4];
		for (int i=1;i<=4;i++) {
			levelPanel[i-1] = new CharacterLevelPanel(i);
			chitGrid.add(levelPanel[i-1]);
		}
		add(chitGrid,"East");
		
		leftPanel = new JPanel(new BorderLayout());
			topPanel = new JPanel(new BorderLayout());
			Box box = Box.createHorizontalBox();
			mainIcon = new JLabel(model.getCharacter().getGameObject().getName());
			mainIcon.setIcon(model.getCharacterToken().getIcon());
			mainIcon.setFont(new Font("Dialog", Font.BOLD, 36));
			mainIcon.setIconTextGap(10);
			box.add(mainIcon);
			box.add(Box.createHorizontalGlue());
			JPanel buttons = new JPanel(new GridLayout(6,1));
			editSymbolButton = new JButton("Edit Symbol");
			editSymbolButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					editSymbol();
				}
			});
			buttons.add(editSymbolButton);
			loadPictureButton = new JButton("Load Picture");
			loadPictureButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					loadPicture();
				}
			});
			buttons.add(loadPictureButton);
			extraNotesButton = new JButton("Edit Notes...");
			extraNotesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					editNotes();
				}
			});
			buttons.add(extraNotesButton);
			viewCardButton = new JButton("View Card");
			viewCardButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					CharacterInfoCard card = model.getCard();
					JOptionPane.showMessageDialog(RealmCharacterBuilderPanel.this,new JLabel(card.getImageIcon(true)));
				}
			});
			buttons.add(viewCardButton);
			viewGearButton = new JButton("View Gear");
			viewGearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					ImageIcon gear = getGearImage();
					JOptionPane.showMessageDialog(RealmCharacterBuilderPanel.this,new JLabel(gear));
				}
			});
			buttons.add(viewGearButton);
			evaluateButton = new JButton("Evaluate");
			evaluateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					evaluateNow();
				}
			});
			buttons.add(evaluateButton);
			ComponentTools.lockComponentSize(buttons,100,120);
			box.add(buttons);
			topPanel.add(box,"North");
			int boxHeight = 200;
			box = Box.createHorizontalBox();
				String[] startingLoc = model.getCharacter().getStartingLocations(false);
				ArrayList<String> list = new ArrayList(Arrays.asList(startingLoc));
				JPanel locationControls = new JPanel(new GridLayout(5,1));
				startInnChoice = new JCheckBox("Inn (ALWAYS)",true); // ALWAYS true
				startInnChoice.setEnabled(false); // ALWAYS disabled (MUST have INN as a choice)
				locationControls.add(startInnChoice);
				
				startGuardChoice = new JCheckBox("Guard",list.contains("Guard"));
				startGuardChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateStartingLocation("Guard",startGuardChoice.isSelected());
					}
				});
				locationControls.add(startGuardChoice);
				startChapelChoice = new JCheckBox("Chapel",list.contains("Chapel"));
				startChapelChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateStartingLocation("Chapel",startChapelChoice.isSelected());
					}
				});
				locationControls.add(startChapelChoice);
				startHouseChoice = new JCheckBox("House",list.contains("House"));
				startHouseChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateStartingLocation("House",startHouseChoice.isSelected());
					}
				});
				locationControls.add(startHouseChoice);
				startGhostsChoice = new JCheckBox("Ghosts",list.contains("Ghost"));
				startGhostsChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateStartingLocation("Ghost",startGhostsChoice.isSelected());
					}
				});
				locationControls.add(startGhostsChoice);
				locationControls.setBorder(BorderFactory.createTitledBorder("Starting Locations"));
				ComponentTools.lockComponentSize(locationControls,120,boxHeight);
			box.add(locationControls);
				Box vulAndType = Box.createVerticalBox();
					ButtonGroup vulGroup = new ButtonGroup();
					JPanel vulnerabilityControls = new JPanel(new GridLayout(4,1));
					Strength vul = model.getCharacter().getVulnerability();
					vulnerabilityLight = new JRadioButton("Light","L".equals(vul.toString()));
					vulnerabilityLight.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							model.getCharacter().getGameObject().setThisAttribute("vulnerability","L");
							updateAllLevels();
						}
					});
					vulGroup.add(vulnerabilityLight);
					vulnerabilityControls.add(vulnerabilityLight);
					vulnerabilityMedium = new JRadioButton("Medium","M".equals(vul.toString()));
					vulnerabilityMedium.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							model.getCharacter().getGameObject().setThisAttribute("vulnerability","M");
							updateAllLevels();
						}
					});
					vulGroup.add(vulnerabilityMedium);
					vulnerabilityControls.add(vulnerabilityMedium);
					vulnerabilityHeavy = new JRadioButton("Heavy","H".equals(vul.toString()));
					vulnerabilityHeavy.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							model.getCharacter().getGameObject().setThisAttribute("vulnerability","H");
							updateAllLevels();
						}
					});
					vulGroup.add(vulnerabilityHeavy);
					vulnerabilityControls.add(vulnerabilityHeavy);
					vulnerabilityTremendous = new JRadioButton("Tremendous","T".equals(vul.toString()));
					vulnerabilityTremendous.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							model.getCharacter().getGameObject().setThisAttribute("vulnerability","T");
							updateAllLevels();
						}
					});
					vulGroup.add(vulnerabilityTremendous);
					vulnerabilityControls.add(vulnerabilityTremendous);
		
					vulnerabilityControls.setBorder(BorderFactory.createTitledBorder("Vulnerability"));
					ComponentTools.lockComponentSize(vulnerabilityControls,120,boxHeight-80);
				vulAndType.add(vulnerabilityControls);
					ButtonGroup classGroup = new ButtonGroup();
					JPanel classControls = new JPanel(new GridLayout(2,1));
					fighterClassOption = new JRadioButton("Fighter",model.getCharacter().getGameObject().hasThisAttribute("fighter"));
					fighterClassOption.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							model.getCharacter().getGameObject().setThisAttribute("fighter");
							model.getCharacter().getGameObject().removeThisAttribute("magicuser");
						}
					});
					classGroup.add(fighterClassOption);
					classControls.add(fighterClassOption);
					mageClassOption = new JRadioButton("Magic User",model.getCharacter().getGameObject().hasThisAttribute("magicuser"));
					mageClassOption.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							model.getCharacter().getGameObject().removeThisAttribute("fighter");
							model.getCharacter().getGameObject().setThisAttribute("magicuser");
						}
					});
					classGroup.add(mageClassOption);
					classControls.add(mageClassOption);
					
					classControls.setBorder(BorderFactory.createTitledBorder("Class"));
					ComponentTools.lockComponentSize(classControls,120,80);
				vulAndType.add(classControls);
			box.add(vulAndType);
				Box extraInfo = Box.createVerticalBox();
					JPanel infoControls = new JPanel(new GridLayout(3,1));
					Box line = Box.createHorizontalBox();
						line.add(Box.createHorizontalGlue());
						line.add(new JLabel("Creator:"));
						creatorField = new JTextField(model.getCharacter().getGameObject().getThisAttribute("creator"));
						creatorField.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("creator",creatorField.getText());
							}
						});
						creatorField.addFocusListener(new FocusAdapter() {
							public void focusGained(FocusEvent ev) {
								creatorField.selectAll();
							}
							public void focusLost(FocusEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("creator",creatorField.getText());
							}
						});
						ComponentTools.lockComponentSize(creatorField,100,20);
						line.add(creatorField);
					infoControls.add(line);
					line = Box.createHorizontalBox();
						line.add(Box.createHorizontalGlue());
						line.add(new JLabel("Art Credit:"));
						artCreditField = new JTextField(model.getCharacter().getGameObject().getThisAttribute("artcredit"));
						artCreditField.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("artcredit",artCreditField.getText());
							}
						});
						artCreditField.addFocusListener(new FocusAdapter() {
							public void focusGained(FocusEvent ev) {
								artCreditField.selectAll();
							}
							public void focusLost(FocusEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("artcredit",artCreditField.getText());
							}
						});
						ComponentTools.lockComponentSize(artCreditField,100,20);
						line.add(artCreditField);
					infoControls.add(line);
					line = Box.createHorizontalBox();
						line.add(Box.createHorizontalGlue());
						line.add(new JLabel("Symbol Meaning:"));
						symbolMeaningField = new JTextField(model.getCharacter().getGameObject().getThisAttribute("meaning"));
						symbolMeaningField.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("meaning",symbolMeaningField.getText());
							}
						});
						symbolMeaningField.addFocusListener(new FocusAdapter() {
							public void focusGained(FocusEvent ev) {
								symbolMeaningField.selectAll();
							}
							public void focusLost(FocusEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("meaning",symbolMeaningField.getText());
							}
						});
						ComponentTools.lockComponentSize(symbolMeaningField,100,20);
						line.add(symbolMeaningField);
					infoControls.add(line);
					infoControls.setBorder(BorderFactory.createTitledBorder("Other"));
					ComponentTools.lockComponentSize(infoControls,200,boxHeight-80);
				extraInfo.add(infoControls);
					ButtonGroup genderGroup = new ButtonGroup();
					JPanel genderControls = new JPanel(new GridLayout(2,1));
						String pronoun = model.getCharacter().getGameObject().getThisAttribute("pronoun");
						genderMale = new JRadioButton("Male","he".equals(pronoun));
						genderMale.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("pronoun","he");
							}
						});
						genderGroup.add(genderMale);
					genderControls.add(genderMale);
						genderFemale = new JRadioButton("Female","she".equals(pronoun));
						genderFemale.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								model.getCharacter().getGameObject().setThisAttribute("pronoun","she");
							}
						});
						genderGroup.add(genderFemale);
					genderControls.add(genderFemale);
					
					genderControls.setBorder(BorderFactory.createTitledBorder("Gender"));
					ComponentTools.lockComponentSize(genderControls,200,80);
				extraInfo.add(genderControls);
			box.add(extraInfo);
			box.add(Box.createHorizontalGlue());
			topPanel.add(box,"Center");
			JPanel fairnessPanel = new JPanel(new BorderLayout());
				fairnessLabel = new JLabel("",JLabel.CENTER);
				fairnessLabel.setFont(new Font("Dialog",Font.BOLD|Font.ITALIC,16));
				fairnessLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			fairnessPanel.add(fairnessLabel,BorderLayout.CENTER);
				JPanel mfReasonPanel = new JPanel(new GridLayout());
				moveFairnessReasonLabel = new JLabel("",JLabel.CENTER);
				moveFairnessReasonLabel.setFont(new Font("Dialog",Font.BOLD,14));
				moveFairnessReasonLabel.setForeground(Color.blue);
				mfReasonPanel.add(moveFairnessReasonLabel);
				fightFairnessReasonLabel = new JLabel("",JLabel.CENTER);
				fightFairnessReasonLabel.setFont(new Font("Dialog",Font.BOLD,14));
				fightFairnessReasonLabel.setForeground(Color.blue);
				mfReasonPanel.add(fightFairnessReasonLabel);
			fairnessPanel.add(mfReasonPanel,BorderLayout.SOUTH);
			topPanel.add(fairnessPanel,"South");
		leftPanel.add(topPanel,"North");
			ArrayList<String[]> relationships = new ArrayList<String[]>();
			for (int i=0;i<RealmCharacterConstants.DEFAULT_RELATIONSHIPS.length;i++) {
				relationships.add(RealmCharacterConstants.DEFAULT_RELATIONSHIPS[i]);
			}
			relationshipTable = new RelationshipTable(relationships,model.getCharacter());
			relationshipTable.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent ev) {
					updateRelationshipMouse(ev);
				}
			});
			relationshipTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent ev) {
					updateRelationshipMouse(ev);
				}
			});
		leftPanel.add(new JScrollPane(relationshipTable),"Center");
		add(leftPanel,"Center");
		updateAllWeaponIcons();
		updateAllLevels();
	}
	private void updateRelationshipMouse(MouseEvent ev) {
		Point p = ev.getPoint();
		int row = relationshipTable.rowAtPoint(p);
		int col = relationshipTable.columnAtPoint(p);
		if (row>=0 && row<relationshipTable.getRowCount() && col>0 && col<relationshipTable.getColumnCount()) {
			int rel = col-3;
			String name = RealmCharacterConstants.DEFAULT_RELATIONSHIPS[row][1].substring(1);
			model.setRelationship(name,rel);
			repaint();
		}
	}
	private void evaluateNow() {
		TreeSet<String> notes = new TreeSet<String>();
		
		boolean mediumArmor = false;
		boolean mediumMoves = false;
		boolean heavyArmor = false;
		boolean heavyMoves = false;
		
		boolean heavyChits = false;
		boolean tremendousChits = false;
		
		boolean heavyFight = false;
		boolean tremendousFight = false;
		
		boolean lightCharacter = vulnerabilityLight.isSelected(); 
		boolean mediumCharacter = vulnerabilityMedium.isSelected(); 
		
		int maxSpellSlots = 0;
		for (int i=0;i<levelPanel.length;i++) {
			if (levelPanel[i].hasInvalidChits()) {
				notes.add("ERROR - There are invalid chits at level "+(i+1));
			}
			maxSpellSlots = Math.max(maxSpellSlots,levelPanel[i].startingSpellCount());
			if (!mediumMoves) mediumMoves = levelPanel[i].hasMediumMoves();
			if (!heavyMoves) heavyMoves = levelPanel[i].hasHeavyMoves();
			if (!heavyChits) heavyChits = levelPanel[i].hasHeavyChits();
			if (!tremendousChits) tremendousChits = levelPanel[i].hasTremendousChits();
			if (!heavyFight) heavyFight = levelPanel[i].hasHeavyFight();
			if (!tremendousFight) tremendousFight = levelPanel[i].hasTremendousFight();
			mediumArmor = levelPanel[i].hasMediumArmor();
			heavyArmor = levelPanel[i].hasHeavyArmor();
			Strength weaponStrength = levelPanel[i].getWeaponStrength();
			if (weaponStrength!=null) {
				if (maxSpellSlots>2) {
					notes.add("WARNING - Spellcasters with more than two spell slots shouldn't start with a weapon.");
				}
				if ("M".equals(weaponStrength.getChar()) && !heavyFight) {
					notes.add("WARNING - Characters should only start with medium weapons if they have heavy FIGHT chits.");
				}
				else if ("H".equals(weaponStrength.getChar()) && !tremendousFight) {
					notes.add("WARNING - Characters should only start with heavy weapons if they have tremendous FIGHT chits.");
				}
			}
			
			if (mediumArmor || heavyArmor) {
				if (maxSpellSlots>1) {
					notes.add("WARNING - Spellcasters with more than one spell slot shouldn't start with armor.");
				}
				if (mediumArmor && !mediumMoves) {
					notes.add("WARNING - Medium armor cannot be carried at level "+(i+1)+", without at least one medium MOVE.");  
				}
				if (heavyArmor && !heavyMoves) {
					notes.add("WARNING - Heavy armor cannot be carried at level "+(i+1)+", without at least one heavy MOVE.");  
				}
			}
			
			if (levelPanel[i].maxChitFairnessOffset()>1) {
				notes.add("WARNING - Chits at level "+(i+1)+" have an advantage/disadvantage greater than one.");
			}
			
			if (i>0) {
				int spellGain = levelPanel[i].startingSpellCount() - levelPanel[i-1].startingSpellCount();
				if (spellGain>1) {
					notes.add("WARNING - Gain of "+spellGain+" spell slots between level "+i+" and "+(i+1)+" is unusual.");
				}
				else if (spellGain<0) {
					notes.add("ERROR - Loss of "+Math.abs(spellGain)+" spell slots between level "+i+" and "+(i+1)+" looks wrong.");
				}
			}
		}
		if (lightCharacter) {
			if (heavyChits) {
				notes.add("WARNING - Light characters shouldn't have HEAVY chits");
			}
			if (tremendousChits) {
				notes.add("WARNING - Light characters shouldn't have TREMENDOUS chits");
			}
		}
		else if (mediumCharacter) {
			if (tremendousChits) {
				notes.add("WARNING - Medium characters shouldn't have TREMENDOUS chits");
			}
		}
		
		if (notes.size()==0) {
			notes.add("Congratulations, character meets all tested requirements.");
		}
		
		// Finish
		StringBuffer sb = new StringBuffer();
		for (String note:notes) {
			sb.append(note);
			sb.append("\n");
		}
		JScrollPane sp = new JScrollPane(new JTextArea(sb.toString()));
		ComponentTools.lockComponentSize(sp,640,480);
		JOptionPane.showMessageDialog(this,sp,"Character Evaluation",JOptionPane.INFORMATION_MESSAGE);
		
	}
	private void editSymbol() {
		SymbolEditDialog sed = new SymbolEditDialog(parent,model,graphicsManager);
		sed.setVisible(true);
		mainIcon.setIcon(model.getCharacterToken().getIcon());
		repaint();
	}
	private void loadPicture() {
		File file = graphicsManager.getLoadPath();
		if (file!=null) {
			ImageIcon icon = IconFactory.findIcon(file.getAbsolutePath());
			if (icon!=null) {
				model.setPictureIcon(resizeImage(icon,CharacterInfoCard.PICTURE_WIDTH,CharacterInfoCard.PICTURE_HEIGHT));
				repaint();
			}
		}
	}
	private void editNotes() {
		String text = model.getCharacter().getGameObject().getThisAttribute("extra_notes");
		JTextArea editField = new JTextArea();
		editField.setLineWrap(true);
		editField.setWrapStyleWord(true);
		if (text!=null) {
			editField.setText(text);
		}
		JScrollPane sp = new JScrollPane(editField);
		ComponentTools.lockComponentSize(sp,400,300);
		JOptionPane.showMessageDialog(RealmCharacterBuilderPanel.this,sp,"Character Notes:",JOptionPane.PLAIN_MESSAGE);
		model.getCharacter().getGameObject().setThisAttribute("extra_notes",editField.getText());
	}
	private ImageIcon resizeImage(ImageIcon image,int width,int height) {
		if (image.getIconWidth()==width && image.getIconHeight()==height) {
			// no resizing necessary
			return image;
		}
		return new ImageIcon(image.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH));
	}
	private void updateStartingLocation(String location,boolean selected) {
		String[] startingLoc = model.getCharacter().getStartingLocations(false);
		ArrayList<String> list = new ArrayList(Arrays.asList(startingLoc));
		if (selected) {
			// add it
			list.add(location);
		}
		else {
			// remove it
			while(list.contains(location)) { // guarantees all instance of it are removed!!
				list.remove(location);
			}
		}
		StringBufferedList sb = new StringBufferedList(",","");
		sb.appendAll(list);
		model.getCharacter().getGameObject().setThisAttribute("start",sb.toString());
	}
	private void updateAllLevels() {
		double totalScore = 0;
		int moveCount = 0;
		int fightCount = 0;
		for (int i=0;i<levelPanel.length;i++) {
			levelPanel[i].updateControls();
			totalScore += levelPanel[i].getTotalFairnessScore();
			moveCount += levelPanel[i].getMoveCount();
			fightCount += levelPanel[i].getFightCount();
		}
		
		// No MOVE chits?  -1
		if (moveCount==0) {
			totalScore--;
			moveFairnessReasonLabel.setText("No MOVE chits: -1.0");
		}
		else moveFairnessReasonLabel.setText("");
			
		// No FIGHT chits? -1
		if (fightCount==0) {
			totalScore--;
			fightFairnessReasonLabel.setText("No FIGHT chits: -1.0");
		}
		else fightFairnessReasonLabel.setText("");
		
		StringBuffer sb = new StringBuffer();
		sb.append("Chit Fairness ");
		sb.append(totalScore>0?"Advantage of ":"Disadvantage of ");
		sb.append(Math.abs(totalScore));
		fairnessLabel.setText(totalScore==0?"":sb.toString());
	}
	private void updateAllWeaponIcons() {
		for (int i=0;i<levelPanel.length;i++) {
			levelPanel[i].updateWeaponIcon();
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////
	// INNER CLASS
	///////////////////////////////////////////////////////////////////////
	private class CharacterLevelPanel extends JPanel {
		
		private int level;
		private String levelKey;
		
		private JTextField levelNameField;
		private CharacterActionChitComponent chit[];
		private JLabel chitFairness[];
		private double totalFairnessScore;
		
		// Starting Equipment
		private JCheckBox helmet;
		private JCheckBox breastplate;
		private JCheckBox shield;
		private JCheckBox armor;
		
		private JCheckBox cap;
		private JCheckBox cuirass;
		private JCheckBox buckler;
		
		private JLabel weaponNameLabel;
		private JLabel weaponIcon;
		private JButton editWeaponButton;
		
		// Starting Spells
		private JComboBox startingSpellCount;
		
		// Special Advantages
		private AdvantagePanel advantagePanel;
		
		public CharacterLevelPanel(int level) {
			this.level = level;
			this.levelKey = "level_"+level;
			initComponents();
		}
		private void updateLevelName() {
			String levelKey = "level_"+level;
			model.getCharacter().getGameObject().setAttribute(levelKey,"name",levelNameField.getText());
			if (level==4) {
				model.getCharacter().getGameObject().setName(levelNameField.getText());
				mainIcon.setText(levelNameField.getText());
				revalidate();
				repaint();
			}
			mainIcon.grabFocus();
		}
		private void initComponents() {
			setLayout(new GridLayout(1,3));
			
			add(getLeftPanel());
			add(getMiddlePanel());
			add(getRightPanel());
			setBorder(BorderFactory.createTitledBorder("Level "+level));
			updateControls();
		}
		private JPanel getLeftPanel() {
			JPanel left = new JPanel(new BorderLayout());
			levelNameField = new JTextField(model.getCharacter().getCharacterLevelName(level));
			levelNameField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateLevelName();
				}
			});
			levelNameField.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent ev) {
					levelNameField.selectAll();
				}
				public void focusLost(FocusEvent ev) {
					updateLevelName();
				}
			});
			left.add(levelNameField,"North");
			
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			
			chit = new CharacterActionChitComponent[3];
			chitFairness = new JLabel[3];
			int n=0;
			for (GameObject go:model.getChits(level)) {
				chit[n] = (CharacterActionChitComponent)RealmComponent.getRealmComponent(go);
				chit[n].addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent ev) {
						editChit((CharacterActionChitComponent)ev.getSource());
						updateAllLevels();
					}
				});
				chitFairness[n] = new JLabel("0",JLabel.CENTER);
				chitFairness[n].setFont(new Font("Dialog", Font.BOLD, 12));
				chitFairness[n].setBorder(BorderFactory.createLoweredBevelBorder());
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(Box.createVerticalStrut(5),"North");
				panel.add(chit[n],"Center");
				panel.add(chitFairness[n],"South");
				box.add(panel);
				n++;
			}
			left.add(box,"Center");
			return left;
		}
		private JPanel getMiddlePanel() {
			UniformLabelGroup group = new UniformLabelGroup();
			JPanel middle = new JPanel(new GridLayout(1,2));
				JPanel middleLeft = new JPanel(new BorderLayout());
				middleLeft.add(getWeaponPanel(),"Center");
					Box line = group.createLabelLine("# Spells");
					startingSpellCount = new JComboBox(RealmCharacterConstants.SPELL_COUNT);
					startingSpellCount.setSelectedItem(String.valueOf(model.getCharacter().getGameObject().getInt(levelKey,"spellcount")));
					startingSpellCount.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							updateStartingSpells();
						}
					});
					ComponentTools.lockComponentSize(startingSpellCount,40,20);
					line.add(startingSpellCount);
					line.add(Box.createHorizontalGlue());
				middleLeft.add(line,"North");
			middle.add(middleLeft);
			middle.add(getArmorPanel());
			return middle;
		}
		private JPanel getArmorPanel() {
			String armorList = model.getCharacter().getGameObject().getAttribute(levelKey,"armor");
			if (armorList==null) armorList = "";
			JPanel armorPanel = new JPanel(new GridLayout(7,1));
			//Box armorPanel = Box.createVerticalBox();
			cap = new JCheckBox("Cap",armorList.contains("Cap"));
			cap.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Cap",cap.isSelected());
					if (helmet.isSelected()) {
						updateArmor("Helmet",false);
						helmet.setSelected(false);
					}
				}
			});
			armorPanel.add(cap);
			cuirass = new JCheckBox("Cuirass",armorList.contains("Cuirass"));
			cuirass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Cuirass",cuirass.isSelected());
					if (breastplate.isSelected()) {
						updateArmor("Breastplate",false);
						breastplate.setSelected(false);
					}
				}
			});
			armorPanel.add(cuirass);
			buckler = new JCheckBox("Buckler",armorList.contains("Buckler"));
			buckler.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Buckler",buckler.isSelected());
					if (shield.isSelected()) {
						updateArmor("Shield",false);
						shield.setSelected(false);
					}
				}
			});
			armorPanel.add(buckler);
			helmet = new JCheckBox("Helmet",armorList.contains("Helmet"));
			helmet.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Helmet",helmet.isSelected());
					if (cap.isSelected()) {
						updateArmor("Cap",false);
						cap.setSelected(false);
					}
				}
			});
			armorPanel.add(helmet);
			breastplate = new JCheckBox("Breastplate",armorList.contains("Breastplate"));
			breastplate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Breastplate",breastplate.isSelected());
					if (cuirass.isSelected()) {
						updateArmor("Cuirass",false);
						cuirass.setSelected(false);
					}
				}
			});
			armorPanel.add(breastplate);
			shield = new JCheckBox("Shield",armorList.contains("Shield"));
			shield.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Shield",shield.isSelected());
					if (buckler.isSelected()) {
						updateArmor("Buckler",false);
						buckler.setSelected(false);
					}
				}
			});
			armorPanel.add(shield);
			armor = new JCheckBox("Armor",armorList.contains("Armor"));
			armor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateArmor("Armor",armor.isSelected());
				}
			});
			armorPanel.add(armor);
			armorPanel.setBorder(BorderFactory.createTitledBorder("Armor"));
			return armorPanel;
		}
		private JPanel getWeaponPanel() {
			JPanel weaponPanel = new JPanel(new BorderLayout());
			editWeaponButton = new JButton("Edit");
			editWeaponButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					String weaponName = model.getCharacter().getGameObject().getAttribute(levelKey,"weapon");
					WeaponEditDialog wed = new WeaponEditDialog(parent,model,graphicsManager,weaponName);
					wed.setVisible(true);
					weaponName = wed.getWeaponName();
					if (weaponName==null) {
						model.getCharacter().getGameObject().removeAttribute(levelKey,"weapon");
					}
					else {
						model.getCharacter().getGameObject().setAttribute(levelKey,"weapon",wed.getWeaponName());
					}
					model.updateWeaponUsage();
					updateAllWeaponIcons();
				}
			});
			weaponPanel.add(editWeaponButton,"South");
			weaponNameLabel = new JLabel();
			weaponNameLabel.setHorizontalAlignment(JLabel.CENTER);
			weaponIcon = new JLabel();
			weaponIcon.setHorizontalAlignment(JLabel.CENTER);
			weaponPanel.add(weaponNameLabel,"North");
			weaponPanel.add(weaponIcon,"Center");
			weaponPanel.setBorder(BorderFactory.createTitledBorder("Weapon"));
			return weaponPanel;
		}
		private AdvantagePanel getRightPanel() {
			advantagePanel = new AdvantagePanel(levelKey);
			advantagePanel.setBorder(BorderFactory.createTitledBorder("Advantages"));
			advantagePanel.updateAdvantage();
			return advantagePanel;
		}
		public void updateWeaponIcon() {
			String weaponName = model.getCharacter().getGameObject().getAttribute(levelKey,"weapon");
			if (weaponName==null) {
				weaponNameLabel.setText("");
				weaponIcon.setIcon(null);
			}
			else {
				weaponNameLabel.setText(weaponName);
				GameObject weapon = model.getWeapon(weaponName);
				weaponIcon.setIcon(RealmComponent.getRealmComponent(weapon).getMediumIcon());
			}
		}
		private void editChit(CharacterActionChitComponent eChit) {
			ChitEditDialog ced = new ChitEditDialog(parent,eChit);
			ced.setLocationRelativeTo(eChit);
			ced.setVisible(true);
			repaint();
		}
		private ArrayList<String> getMagicTypes() {
			ArrayList<String> magicTypes = new ArrayList<String>();
			ArrayList<CharacterActionChitComponent> list = model.getCharacter().getAllActionChitsSorted(level);
			for (CharacterActionChitComponent cc:list) {
				if (cc.isMagic()) {
					String type = cc.getMagicType();
					if (!magicTypes.contains(type)) {
						magicTypes.add(type);
					}
				}
			}
			return magicTypes;
		}
		public double getTotalFairnessScore() {
			return totalFairnessScore;
		}
		public int getMoveCount() {
			int count=0;
			for (int i=0;i<3;i++) {
				if (chit[i].isMove()) count++;
			}
			return count;
		}
		public int getFightCount() {
			int count=0;
			for (int i=0;i<3;i++) {
				if (chit[i].isFight()) count++;
			}
			return count;
		}
		public boolean hasMediumMoves() {
			for (int i=0;i<3;i++) {
				if (chit[i].isMove() && chit[i].getStrength().strongerOrEqualTo(new Strength("M"))) {
					return true;
				}
			}
			return false;
		}
		public boolean hasHeavyMoves() {
			for (int i=0;i<3;i++) {
				if (chit[i].isMove() && chit[i].getStrength().strongerOrEqualTo(new Strength("H"))) {
					return true;
				}
			}
			return false;
		}
		public boolean hasHeavyFight() {
			for (int i=0;i<3;i++) {
				if (chit[i].isFight() && chit[i].getStrength().strongerOrEqualTo(new Strength("H"))) {
					return true;
				}
			}
			return false;
		}
		public boolean hasTremendousFight() {
			for (int i=0;i<3;i++) {
				if (chit[i].isFight() && chit[i].getStrength().strongerOrEqualTo(new Strength("T"))) {
					return true;
				}
			}
			return false;
		}
		public void updateControls() {
			// Update chit fairness
			totalFairnessScore = 0;
			for (int i=0;i<3;i++) {
				if (validChit(chit[i])) {
					double score = fairnessScore(chit[i]);
					totalFairnessScore += score;
					Color color = Color.black;
					if (score<0) {
						color = Color.red;
					}
					else if (score>0) {
						color = Color.blue;
					}
					chitFairness[i].setText(String.valueOf(score));
					chitFairness[i].setForeground(color);
					if (chit[i].getGameObject().hasThisAttribute(Constants.UNPLAYABLE)) {
						chit[i].getGameObject().removeThisAttribute(Constants.UNPLAYABLE);
						chit[i].repaint();
					}
				}
				else {
					chitFairness[i].setText("INVALID");
					chitFairness[i].setForeground(Color.red);
					if (!chit[i].getGameObject().hasThisAttribute(Constants.UNPLAYABLE)) {
						chit[i].getGameObject().setThisAttribute(Constants.UNPLAYABLE);
						chit[i].repaint();
					}
				}
			}
			
			ArrayList<String> magicTypes = getMagicTypes();
			startingSpellCount.setEnabled(!magicTypes.isEmpty());
			if (magicTypes.isEmpty()) {
				startingSpellCount.setSelectedIndex(0);
			}
			updateStartingSpells();
		}
		public int startingSpellCount() {
			return Integer.valueOf(startingSpellCount.getSelectedItem().toString());
		}
		private void updateStartingSpells() {
			int count = Integer.valueOf(startingSpellCount.getSelectedItem().toString());
			if (count==0) {
				model.getCharacter().getGameObject().removeAttribute(levelKey,"spellcount");
				model.getCharacter().getGameObject().removeAttribute(levelKey,"spelltypes");
			}
			else {
				model.getCharacter().getGameObject().setAttribute(levelKey,"spellcount",count);
				model.getCharacter().getGameObject().setAttributeList(levelKey,"spelltypes",getMagicTypes());
			}
		}
		public boolean hasMediumArmor() {
			return helmet.isSelected() || breastplate.isSelected() || shield.isSelected();
		}
		public boolean hasHeavyArmor() {
			return armor.isSelected();
		}
		public Strength getWeaponStrength() {
			String weaponName = model.getCharacter().getGameObject().getAttribute(levelKey,"weapon");
			if (weaponName!=null) {
				GameObject weapon = model.getWeapon(weaponName);
				if (weapon!=null) {
					WeaponChitComponent wc = (WeaponChitComponent)RealmComponent.getRealmComponent(weapon);
					return wc.getStrength();
				}
			}
			return null;
		}
		private void updateArmor(String armor,boolean val) {
			String armorList = model.getCharacter().getGameObject().getAttribute(levelKey,"armor");
			if (armorList==null) armorList = "";
			ArrayList<String> list = StringUtilities.stringToCollection(armorList,",");
			if (val) {
				if (!list.contains(armor)) {
					list.add(armor);
				}
			}
			else {
				list.remove(armor);
			}
			model.getCharacter().getGameObject().setAttribute(levelKey,"armor",StringUtilities.collectionToString(list,","));
		}
		public boolean hasHeavyChits() {
			for (int i=0;i<chit.length;i++) {
				if ("H".equals(chit[i].getStrength().getChar())) {
					return true;
				}
			}
			return false;
		}
		public boolean hasTremendousChits() {
			for (int i=0;i<chit.length;i++) {
				if ("T".equals(chit[i].getStrength().getChar())) {
					return true;
				}
			}
			return false;
		}
		public double maxChitFairnessOffset() {
			double max = 0;
			for (int i=0;i<chit.length;i++) {
				if (chit[i].isMagic() || chit[i].isMove() || chit[i].isFight()) {
					max = Math.max(max,fairnessScore(chit[i]));
				}
			}
			return max;
		}
		public boolean hasInvalidChits() {
			for (int i=0;i<chit.length;i++) {
				if (!validChit(chit[i])) {
					return true;
				}
			}
			return false;
		}
		private boolean validChit(CharacterActionChitComponent eChit) {
			if (eChit.isMove()) {
				Strength chitStrength = eChit.getStrength();
				Strength vulnerability = model.getCharacter().getVulnerability();
				if (!chitStrength.strongerOrEqualTo(vulnerability)) {
					return false;
				}
			}
			else if (eChit.isMagic() || eChit.isFly() || eChit.isFightAlert()) {
				if (eChit.getEffortAsterisks()<1) {
					return false;
				}
			}
			return true;
		}
		/**
		 * @return		Fairness score.  If positive, the chit has an advantage.  If negative, the chit has a disadvantage.
		 */
		private double fairnessScore(CharacterActionChitComponent eChit) {
			double score = 0;
			int effort = eChit.getEffortAsterisks();
			if (eChit.isMagic()) {
				// One effort is standard
				score = (1-effort)/2.0;
				
				// Speed adjustment
				int speed = eChit.getSpeed().getNum();
				score += (4.0-speed)/2.0;
			}
			else {
				int speed = eChit.getSpeed().getNum();
				int strength = eChit.getStrength().getLevels(); // 1 is L, 4 is T
				
				// Compare to M5 base chit
				score = strength-2; // 2 is M
				score += 5-speed; // 5 speed
				score -= effort; // subtract effort
			}
			if (eChit.isFly()) score++; // Fly chits have an advantage
			if (eChit.isFightAlert()) score++; // Fight/Alert chits have an advantage
			if (eChit.isMoveLock() || eChit.isFightLock()) score--; // Lock chits are a base disadvantage
			return score;
		}
	}
	private class AdvantagePanel extends JPanel {
		private Advantage advantage = null;
		private Box box;
		private JLabel badgeIcon;
		private JLabel nameField;
		private JTextArea descriptionField;
		private JButton editButton;
		private String levelKey;
		public AdvantagePanel(String levelKey) {
			this.levelKey = levelKey;
			setLayout(new BorderLayout());
			
			box = Box.createVerticalBox();
			
			Box line = Box.createHorizontalBox();
			badgeIcon = new JLabel();
			ComponentTools.lockComponentSize(badgeIcon,38,38);
			badgeIcon.setBorder(BorderFactory.createEtchedBorder());
			line.add(badgeIcon);
			line.add(Box.createHorizontalStrut(10));
			nameField = new JLabel();
			line.add(nameField);
			line.add(Box.createHorizontalGlue());
			box.add(line);
			descriptionField = new JTextArea();
			descriptionField.setFont(new Font("Dialog",Font.PLAIN,10));
			descriptionField.setEditable(false);
			descriptionField.setOpaque(false);
			descriptionField.setLineWrap(true);
			descriptionField.setWrapStyleWord(true);
			box.add(new JScrollPane(descriptionField));
			editButton = new JButton("Edit");
			editButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					editAdvantage();
					updateAdvantage();
				}
			});
			box.add(Box.createVerticalGlue());
			add(box,"Center");
			add(editButton,"South");
		}
		public void updateAdvantage() {
			advantage = Advantage.createFromCharacter(model.getCharacter().getGameObject(),levelKey);
			if (advantage!=null) {
				badgeIcon.setIcon(advantage.getBadge());
				nameField.setText(advantage.getName());
				descriptionField.setText(advantage.getDescription());
				box.setVisible(true);
				editButton.setText("Edit");
			}
			else {
				badgeIcon.setIcon(null);
				nameField.setText("");
				descriptionField.setText("");
				box.setVisible(false);
				editButton.setText("New");
			}
		}
		
		public void editAdvantage() {
			AdvantageEditDialog edit = new AdvantageEditDialog(parent,model,graphicsManager,levelKey,advantage,magicRealmData);
			edit.setVisible(true);
			updateAdvantage();
		}
	}
}