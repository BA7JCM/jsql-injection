package com.test.vendor.mysql;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.util.ArrayList;

class MergeUndo extends JEditorPane {
    private final JButton btnUndo=new JButton("Undo");
    private final JButton btnRedo=new JButton("Redo");
    private final UndoManager undoManager=new UndoManager();
 
    public static void main(String[] args) {
        JFrame frame = new JFrame("Merge undoable actions in one group");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final MergeUndo app = new MergeUndo();
        JScrollPane scroll = new JScrollPane(app);
        frame.getContentPane().add(scroll);
 
        JToolBar tb=new JToolBar();
        app.btnUndo.addActionListener(e -> app.undoManager.undo());
        tb.add(app.btnUndo);
        app.btnRedo.addActionListener(e -> app.undoManager.redo());
        tb.add(app.btnRedo);
        frame.getContentPane().add(tb, BorderLayout.NORTH);
 
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
 
    public MergeUndo() {
        super();
        this.setEditorKit(new StyledEditorKit());
        this.getDocument().addUndoableEditListener(this.undoManager);
 
        this.undoManager.refreshControls();
    }
 
    static class MyCompoundEdit extends CompoundEdit {
        private boolean isUnDone=false;
        public int getLength() {
            return this.edits.size();
        }
 
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            this.isUnDone=true;
        }
        @Override
        public void redo() throws CannotUndoException {
            super.redo();
            this.isUnDone=false;
        }
        @Override
        public boolean canUndo() {
            return !this.edits.isEmpty() && !this.isUnDone;
        }

        @Override
        public boolean canRedo() {
            return !this.edits.isEmpty() && this.isUnDone;
        }
    }
    private class UndoManager extends AbstractUndoableEdit implements UndoableEditListener {
        private String lastEditName=null;
        private final ArrayList<MyCompoundEdit> edits=new ArrayList<>();
        private MyCompoundEdit current;
        private int pointer=-1;
 
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            UndoableEdit edit=e.getEdit();
            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
                try {
                    AbstractDocument.DefaultDocumentEvent event=(AbstractDocument.DefaultDocumentEvent)edit;
                    int start=event.getOffset();
                    int len=event.getLength();
                    String text=event.getDocument().getText(start, len);
                    boolean isNeedStart = this.current == null
                        || text.contains("\n")
                        || this.lastEditName == null
                        || !this.lastEditName.equals(edit.getPresentationName());

                    while (this.pointer<this.edits.size()-1) {
                        this.edits.remove(this.edits.size()-1);
                        isNeedStart=true;
                    }
                    if (isNeedStart) {
                        this.createCompoundEdit();
                    }
 
                    this.current.addEdit(edit);
                    this.lastEditName=edit.getPresentationName();
 
                    this.refreshControls();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }
 
        public void createCompoundEdit() {
            if (this.current==null || this.current.getLength()>0) {
                this.current= new MyCompoundEdit();
            }
 
            this.edits.add(this.current);
            this.pointer++;
        }
 
        @Override
        public void undo() throws CannotUndoException {
            if (!this.canUndo()) {
                throw new CannotUndoException();
            }
 
            MyCompoundEdit u=this.edits.get(this.pointer);
            u.undo();
            this.pointer--;
 
            this.refreshControls();
        }
 
        @Override
        public void redo() throws CannotUndoException {
            if (!this.canRedo()) {
                throw new CannotUndoException();
            }
 
            this.pointer++;
            MyCompoundEdit u=this.edits.get(this.pointer);
            u.redo();
 
            this.refreshControls();
        }
 
        @Override
        public boolean canUndo() {
            return this.pointer>=0;
        }

        @Override
        public boolean canRedo() {
            return !this.edits.isEmpty() && this.pointer<this.edits.size()-1;
        }
 
        public void refreshControls() {
            MergeUndo.this.btnUndo.setEnabled(this.canUndo());
            MergeUndo.this.btnRedo.setEnabled(this.canRedo());
        }
    }
}