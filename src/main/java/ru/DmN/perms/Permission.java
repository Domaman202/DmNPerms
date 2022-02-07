package ru.DmN.perms;

import java.io.Serializable;
import java.util.ArrayList;

public class Permission implements Serializable {
    public final ArrayList<String> players, commands;
    public String name, parent;

    public Permission(String name, String parent) {
        this.name = name;
        this.parent = parent;
        this.players = new ArrayList<>();
        this.commands = new ArrayList<>();
    }
}