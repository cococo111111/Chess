package chessModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import chessModel.piece.Piece;
import chessViewController.SelectMode;
import util.Instantiator;

public class Game {
	private int currentSide;
	private int gameMode;
	private Board board;
	private Timer side1Timer;
	private Timer side2Timer;
	private Time player1TimeLeft;
	private Time player2TimeLeft;
	Player player1, player2;
	private boolean humanInputEnabled;

	// Game Modes
	public static int HUMAN_VS_AI = 0;
	public static int HUMAN_VS_HUMAN = 1;
	public static int AI_VS_AI = 2;

	public static final int DEFAULT_TIME = 60; // In Seconds, 3600 is one hour

	public Game() {
		humanInputEnabled = false;

		board = new Board();

		gameMode = SelectMode.selectMode();

		try {
			if (gameMode != 2) {
				player1 = new HumanPlayer("Human Player One", 0);
			} else {
				player1 = SelectMode.pickComputerPlayer(board, 0);
			}

			if (gameMode != 1) {
				player2 = SelectMode.pickComputerPlayer(board, 1);
			} else {
				player2 = new HumanPlayer("Human Player Two", 1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to load players");
			System.exit(0);
		}

		currentSide = 0;
		player1TimeLeft = new Time(DEFAULT_TIME);
		player2TimeLeft = new Time(DEFAULT_TIME);

		side1Timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player1TimeLeft.dec();
			}
		});
		side2Timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player2TimeLeft.dec();
			}
		});
		side1Timer.start();

		gameLoop();
	}

	public void gameLoop() {
		while (!isCheckMate()) {
			humanInputEnabled = false;
			if (getCurrentPlayer() instanceof HumanPlayer) {
				humanInputEnabled = true;
				return;
			} else {
				Integer[] move = ((ComputerPlayer) getCurrentPlayer()).getMove();
				move(move[0], move[1], move[2], move[3]);
			}
		}
		JOptionPane.showMessageDialog(null, "Checkmate");
	}

	public Player getCurrentPlayer() {
		if (currentSide == 0) {
			return player1;
		} else {
			return player2;
		}
	}

	public void move(int oldX, int oldY, int x, int y) {
		Piece tmp = board.getPiece(oldX, oldY);
		if (tmp == null) {
			JOptionPane.showMessageDialog(null, "AI submitted invalid move.", "INVALID", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if (tmp.getSide() == currentSide && board.move(oldX, oldY, x, y)) {
			if (currentSide == 0) {
				currentSide = 1;
				side1Timer.stop();
				side2Timer.start();
			} else if (currentSide == 1) {
				currentSide = 0;
				side2Timer.stop();
				side1Timer.start();
			}
		}
	}

	public Board getBoard() {
		return board;
	}

	public String getPlayer1Time() {
		return player1TimeLeft.getTime();
	}

	public String getPlayer2Time() {
		return player2TimeLeft.getTime();
	}

	public int getCurrentSide() {
		return currentSide;
	}

	public int getPlayer1Score() {
		return board.getWhiteScore();
	}

	public int getPlayer2Score() {
		return board.getBlackScore();
	}

	/**
	 * @param side
	 *            The side that is being checked for checkmate
	 * @return
	 */
	public boolean isCheckMate() {
		if (!board.isInCheck(currentSide)) { // Can't be in checkmate if not in
												// check
			return false;
		}
		for (Integer[] move : board.getAllMoves(0)) {
			Piece p = board.getPiece(move[0], move[1]);
			if (board.resolvesCheck(p, move[2], move[3])) {
				return false; // If there is a move that resolves check, it is
								// not checkmate
			}
		}
		return true;
	}

	/**
	 * @param side
	 *            The side that is being checked for a draw
	 * @return
	 */
	public boolean isDraw() {
		return (board.getAllMoves(currentSide).size() == 0);
	}

	public boolean isHumanInputEnabled() {
		return humanInputEnabled;
	}
}
