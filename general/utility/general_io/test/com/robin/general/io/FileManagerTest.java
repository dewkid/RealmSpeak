/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2016 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */

package com.robin.general.io;

import com.robin.general.util.AbstractTest;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Unit tests for {@link FileManager}.
 * <p>
 * NOTE: Since this involves user interaction via dialog boxes, we are not
 * using JUnit to run the tests. Rather, a main method has been provided
 * to instantiate a JFrame with buttons, from which the tests can be
 * invoked.
 */
public class FileManagerTest extends AbstractFileTest {

    private static final String LOAD_PATH_NO_TITLE = "loadPathNoTitle";
    private static final String LOAD_PATH_SOME_TITLE = "loadPathSomeTitle";

    private static final String DESC = "Some Description";
    private static final String TITLE = "Some Title";

    private JFrame frame;
    private JPanel panel;
    private ActionListener actionListener = new InternalButtonListener();
    private FileManager fm;

    /**
     * Instantiates the file manager test JFrame.
     */
    public FileManagerTest() {
        createPanel();
        createFrame();
        frame.setVisible(true);
    }

    private void createFrame() {
        frame = new JFrame("Test FileManager");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(300, 500);
        frame.setLocation(200, 200);
        frame.getContentPane().add(panel);
    }

    private void createPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(220, 240, 255));

        addButton(LOAD_PATH_NO_TITLE);
        addButton(LOAD_PATH_SOME_TITLE);
    }

    private void addButton(String text) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        panel.add(button);
    }


    // psuedo tests

    public void loadPathNoTitle() {
        fm = new FileManager(frame, DESC, null);
        File loadPath = fm.getLoadPath();
        print("Load path: <%s>", loadPath);
    }

    public void loadPathSomeTitle() {
        fm = new FileManager(frame, DESC, null);
        File loadPath = fm.getLoadPath(TITLE);
        print("Load path: <%s>", loadPath);
    }

    // TODO: needs better coverage of the FileManager class.


    /**
     * Invokes the tests from button presses.
     */
    private class InternalButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case LOAD_PATH_NO_TITLE:
                    loadPathNoTitle();
                    break;

                case LOAD_PATH_SOME_TITLE:
                    loadPathSomeTitle();
                    break;

                default:
                    print("(unknown action: %s)", e.getActionCommand());
                    break;
            }
        }
    }


    /**
     * Since we are messing with JFrames and dialogs, it is tricky to unit
     * test with JUnit, so providing this as a manual way of verifying
     * behavior instead.
     *
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        new FileManagerTest();
    }
}




