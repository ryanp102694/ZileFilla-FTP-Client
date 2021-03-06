package com.pelletier.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

/**
 * Created by ryanb on 3/31/2016.
 */
public class DirectoryViewManager {

    public TreeView<String> treeView;
    public TitledPane titledPane;
    public StringProperty currentFilePath = new SimpleStringProperty();
    public FileItemProvider fileItemProvider;

    public DirectoryViewManager(TitledPane titledPane, TreeView<String> treeView, String currentFilePath, FileItemProvider fileItemProvider){
        this.titledPane = titledPane;
        this.titledPane.textProperty().bind(this.currentFilePath);
        this.treeView = treeView;
        this.currentFilePath.setValue(currentFilePath);
        this.fileItemProvider = fileItemProvider;
    }

    public void populateDirectoryView(){
        TreeItem<String> root = new TreeItem<>(currentFilePath.get(), new ImageView(new Image(getClass().getResourceAsStream("/images/folder.PNG"))));

        treeView.getSelectionModel().selectedItemProperty().addListener((treeItem, oldValue, newValue) -> {
            currentFilePath.setValue(buildCurrentFilePathFromTreeItem((TreeItem<String>) treeItem.getValue()));   //it doesn't seem like this is updating the title pane
            if(fileItemProvider.children(currentFilePath.get()) != null){
                addTreeItems(treeItem.getValue(), currentFilePath.get());
            }
        });


        treeView.setRoot(root);
        addTreeItems(root, currentFilePath.get());
        root.setExpanded(false);
    }

    private void addTreeItems(TreeItem<String> treeItem, String filePath){
        treeItem.getChildren().remove(0, treeItem.getChildren().size());
        List<String> childrenAbsolutePaths = fileItemProvider.children(currentFilePath.get());
        if(childrenAbsolutePaths != null){
            for(String absoluteFilePath: childrenAbsolutePaths){
                if(fileItemProvider.isDirectory(absoluteFilePath)){

                    TreeItem<String> directoryTreeItem = new TreeItem<>(fileItemProvider.getName(absoluteFilePath), new ImageView(new Image(getClass().getResourceAsStream("/images/folder.PNG"))));
                    directoryTreeItem.expandedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            BooleanProperty booleanProperty = (BooleanProperty) observable;
                            TreeItem<String> t = (TreeItem<String>) booleanProperty.getBean();
                            if(!newValue){
                                t.getChildren().remove(0, t.getChildren().size());
                                t.getChildren().add(new TreeItem<String>(""));
                            }else{
                                currentFilePath.setValue(buildCurrentFilePathFromTreeItem(t));  //it doesn't seem like this is updating the title pane
                                addTreeItems(t, currentFilePath.get());
                            }
                        }
                    });
                    directoryTreeItem.getChildren().add(new TreeItem<>(""));
                    treeItem.getChildren().add(directoryTreeItem);
                }else{
                    treeItem.getChildren().add(new TreeItem<>(fileItemProvider.getName(absoluteFilePath), new ImageView(new Image(getClass().getResourceAsStream("/images/file.png")))));
                }
            }
        }
    }

    public String buildCurrentFilePathFromTreeItem(TreeItem<String> treeItem){
        if(treeItem == null){
            return "";
        }
        if(treeItem.getParent() == null){
            return treeItem.getValue();
        }
        if(treeItem.getParent().getValue().equals("C:/") || treeItem.getParent().getValue().equals("/")){
            return buildCurrentFilePathFromTreeItem(treeItem.getParent()) + treeItem.getValue();
        }
        return buildCurrentFilePathFromTreeItem(treeItem.getParent()) + "/" + treeItem.getValue();
    }

    public void setFileItemProvider(FileItemProvider fileItemProvider) {
        this.fileItemProvider = fileItemProvider;
    }

    public String getCurrentFilePath() {
        return currentFilePath.get();
    }

    public StringProperty currentFilePathProperty() {
        return currentFilePath;
    }
}
