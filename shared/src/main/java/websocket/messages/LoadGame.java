package websocket.messages;

import chess.ChessGame;

public class LoadGame extends ServerMessage {
    private ChessGame game;

    public LoadGame(ServerMessageType type) {
        super(type);
    }

    public ChessGame getGame() {
        return game;
    }
}
