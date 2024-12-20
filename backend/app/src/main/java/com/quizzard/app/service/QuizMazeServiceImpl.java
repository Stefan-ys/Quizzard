package com.quizzard.app.service;

import com.quizzard.app.common.QuizMazeGameConstants;
import com.quizzard.app.domain.dto.request.QuizMazeMoveRequestDTO;
import com.quizzard.app.domain.dto.response.*;
import com.quizzard.app.domain.entity.Question;
import com.quizzard.app.domain.enums.GameActionEnum;
import com.quizzard.app.domain.model.QuizMaze.Player;
import com.quizzard.app.domain.model.QuizMaze.QuizMazeGame;
import com.quizzard.app.repository.QuestionRepository;
import com.quizzard.app.repository.UserRepository;
import com.quizzard.app.tracker.GameTracker;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class QuizMazeServiceImpl implements QuizMazeService {

    private final GameTracker gameTracker;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;


    @Override
    public String startNewGame(long player1Id, long player2Id) {

        Player player1 = modelMapper.map(userRepository.findById(player1Id), Player.class);
        Player player2 = modelMapper.map(userRepository.findById(player2Id), Player.class);
        String gameId = QuizMazeGameConstants.TITLE + "->" + player1.getUsername() + "-vs-" + player2.getUsername();

        Random random = new Random();
        byte[][] field = QuizMazeGameConstants.GAME_BOARDS_2_PLAYERS[random.nextInt(QuizMazeGameConstants.GAME_BOARDS_2_PLAYERS.length)].clone();

        QuizMazeGame quizMazeGame = new QuizMazeGame(player1, player2);

        quizMazeGame.setId(gameId);
        quizMazeGame.setPlayer1(player1);
        quizMazeGame.setPlayer2(player2);
        quizMazeGame.setField(field);

        gameTracker.createGame(quizMazeGame);


        return quizMazeGame.getId();
    }

    @Override
    public QuizMazeMoveResponseDTO processMove(String gameId, QuizMazeMoveRequestDTO quizMazeRequestDTO) {
        QuizMazeGame game = (QuizMazeGame) gameTracker.getGame(gameId);
        game.playerAttemptMove(quizMazeRequestDTO.getRow(), quizMazeRequestDTO.getCol());
        QuizMazeMoveResponseDTO responseDTO = modelMapper.map(quizMazeRequestDTO, QuizMazeMoveResponseDTO.class);
        responseDTO.setActionType(GameActionEnum.MOVE.toString());
        return responseDTO;
    }

    @Override
    public QuestionResponseDTO getRandomQuestion(String gameId) {
        Question question = questionRepository.findRandomQuestion()
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        ((QuizMazeGame) gameTracker.getGame(gameId)).setCurentQuestion(question);

        return modelMapper.map(question, QuestionResponseDTO.class);
    }

    @Override
    public int checkAnswer(String gameId, String submittedAnswer, int timeLeft) {
        QuizMazeGame game = (QuizMazeGame) gameTracker.getGame(gameId);
        String correctAnswer = game.getCurentQuestion().getCorrectAnswer();
        if (correctAnswer.equals(submittedAnswer)) {
            int baseScore = 100;
            return baseScore * (timeLeft / 10);
        }
        return 0;
    }

    @Override
    public void questionAnswerCheck(String gameId, String response, byte x, byte y) {
        QuizMazeGame game = (QuizMazeGame) gameTracker.getGame(gameId);

        if (game.getCurentQuestion().getCorrectAnswer().equals(response)) {
            game.playerSuccessfulAnswer(x, y);
        }

        game.setCurentQuestion(null);
        game.switchPlayers();
    }

    @Override
    public QuizMazeGameResponseDTO joinGame(String gameId, long playerId) {
        QuizMazeGame game = (QuizMazeGame) gameTracker.getGame(gameId);
        if (game.getPlayer1().getId() == playerId) {
            game.setPlayer1Present(true);
        } else if (game.getPlayer2().getId() == playerId) {
            game.setPlayer2present(true);
        } else {
            throw new IllegalArgumentException("Invalid player id");
        }
        return getGameResponseDTO(game);
    }

    public void leaveGame(String gameId, long playerId) {
        QuizMazeGame game = (QuizMazeGame) gameTracker.getGame(gameId);
        if (game.getPlayer1().getId() == playerId) {
            game.setPlayer1Present(false);
        } else if (game.getPlayer2().getId() == playerId) {
            game.setPlayer2present(false);
        } else {
            throw new IllegalArgumentException("Invalid player id");
        }
    }

    private QuizMazeGameResponseDTO getGameResponseDTO(QuizMazeGame quizMazeGame) {
        QuizMazeGameResponseDTO quizMazeGameResponseDTO = new QuizMazeGameResponseDTO();

        quizMazeGameResponseDTO.setId(quizMazeGame.getId());
        quizMazeGameResponseDTO.setTitle(QuizMazeGameConstants.TITLE);
        quizMazeGameResponseDTO.setVersion(QuizMazeGameConstants.VERSION);
        quizMazeGameResponseDTO.setDescription(QuizMazeGameConstants.DESCRIPTION);
        quizMazeGameResponseDTO.setRules(QuizMazeGameConstants.RULES);
        quizMazeGameResponseDTO.setTimeToAnswer(QuizMazeGameConstants.TIME_FOR_ANSWER);
        quizMazeGameResponseDTO.setTotalMovesAllowed(QuizMazeGameConstants.TOTAL_MOVES);
        quizMazeGameResponseDTO.setMoves(quizMazeGame.getMoves());
        quizMazeGameResponseDTO.setField(quizMazeGame.getField());
        quizMazeGameResponseDTO.setPlayerTurnId(quizMazeGame.getPlayerTurnId());
        quizMazeGameResponseDTO.setPlayer1(modelMapper.map(quizMazeGame.getPlayer1(), QuizMazePlayerResponseDTO.class));
        quizMazeGameResponseDTO.setPlayer2(modelMapper.map(quizMazeGame.getPlayer2(), QuizMazePlayerResponseDTO.class));
        quizMazeGameResponseDTO.getPlayer1().setPerks(quizMazeGame.getPlayer1().getPerks().stream().map(perk -> modelMapper.map(perk, QuizMazePerkResponseDTO.class)).toList());
        quizMazeGameResponseDTO.getPlayer2().setPerks(quizMazeGame.getPlayer2().getPerks().stream().map(perk -> modelMapper.map(perk, QuizMazePerkResponseDTO.class)).toList());

        return quizMazeGameResponseDTO;
    }
}
