package org.academiadecodigo.splicegirls;

import java.net.InetAddress;

public class Player {

    // Properties
    private String ID;
    private int index;
    private String nickName;
    private InetAddress inetAddress;
    private int playerPort;     // new properties
    private String answer;              // new properties

    // Constructor
    public Player(String ID, InetAddress inetAddress) {
        this.ID = ID;
        this.inetAddress = inetAddress;
    }

    // Setters
    public void setNickName (String playerName) {
        this.nickName = playerName;
    }
    public void setPort(int port) {
        this.playerPort = port;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public void setAnswer(String answer) {                          // New setter
        this.answer = answer;
    }

    // Getters
    public String getNickName() {
        return nickName;
    }
    public String getIP() {
        return inetAddress.getHostAddress();
    }
    public  int getPlayerPort() {
        return playerPort;
    }

    public int getIndex() {
        return index;
    }

    public String getAnswer() {
        return answer;
    }


}
