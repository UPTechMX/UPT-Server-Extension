package org.oskari.upt;

import java.util.ArrayList;


public class Directories {
    public String label;
    public String data;
    public String icon;
    public String expandedIcon;
    public String collapsedIcon;
    public ArrayList<Directories> children;
    public String type;
    public boolean expanded;
    public String key;

    public Directories() {
        expandedIcon="pi pi-folder-open";
        collapsedIcon="pi pi-folder";
        icon="fas fa-layergroup";
        type="directory";
        expanded=true;
    }
    
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        this.key = data;
    }
    public String getExpandedIcon() {
        return expandedIcon;
    }

    public void setExpandedIcon(String expandedIcon) {
        this.expandedIcon = expandedIcon;
    }
    public String getCollapsedIcon() {
        return collapsedIcon;
    }

    public void setCollapsedIcon(String collapsedIcon) {
        this.collapsedIcon = collapsedIcon;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public ArrayList<Directories> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Directories> children) {
        this.children = children;
    }
}


