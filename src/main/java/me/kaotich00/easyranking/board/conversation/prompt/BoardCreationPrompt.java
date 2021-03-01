package me.kaotich00.easyranking.board.conversation.prompt;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class BoardCreationPrompt implements ConversationAbandonedListener {

    private ConversationFactory conversationFactory;

    public BoardCreationPrompt(Easyranking plugin) {
        this.conversationFactory = new ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(new BoardIdPrompt())
                .withEscapeSequence("cancel")
                .withTimeout(60)
                .thatExcludesNonPlayersWithMessage("Only players can run this conversation")
                .addConversationAbandonedListener(this);
    }

    public void startConversationForPlayer(Player player) {
        conversationFactory.buildConversation(player).begin();
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.gracefulExit()) {
            abandonedEvent.getContext().getForWhom().sendRawMessage(ChatFormatter.formatSuccessMessage("Setup completed!"));
        } else {
            abandonedEvent.getContext().getForWhom().sendRawMessage(ChatFormatter.formatErrorMessage("Setup canceled!"));
        }
    }

    private class BoardIdPrompt extends FixedSetPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String promptMessage = ChatFormatter.formatSuccessMessage(ChatColor.GRAY + "Board creation initialized. Follow the instructions below or type 'cancel' at any moment to abandon the setup");
            promptMessage = promptMessage.concat( "\n" + ChatFormatter.formatErrorMessage(ChatColor.GRAY + "Please choose an ID for the board. It will be the board identifiers for the commands (must not contain white spaces), example: mobKilled") );
            return promptMessage;
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            BoardService boardService = ERBoardService.getInstance();
            return !boardService.getBoardById(input).isPresent() && !input.contains(" ");
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            context.setSessionData("boardId", input);
            return new BoardNamePrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return "Make sure a board with that name doesn't already exists! Also make sure the ID doesn't contain any white space.";
        }
    }

    private class BoardNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String promptMessage = ChatFormatter.formatSuccessMessage(ChatColor.GRAY + "Please choose a readable name for the Board that will be displayed to users, example: Killed Mobs");
            return promptMessage;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("boardName", input);
            return new BoardDescriptionPrompt();
        }

    }

    private class BoardDescriptionPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String promptMessage = ChatFormatter.formatSuccessMessage(ChatColor.GRAY + "Please choose an extensive description for the board. Example: This board tracks the amount of Hostile mobs that were killed.");
            return promptMessage;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("boardDescription", input);
            return new BoardMaxShownPlayersPrompt();
        }

    }

    private class BoardMaxShownPlayersPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String promptMessage = ChatFormatter.formatSuccessMessage(ChatColor.GRAY + "Please choose how many players should be shown on the leaderboard. Please note that this does not limit the amount of player on the board but just the amount of SHOWN players.");
            return promptMessage;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            context.setSessionData("boardMaxShownPlayer", input);
            return new BoardUserScoreNamePrompt();
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.intValue() >= 3;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return "Input number must be equal or greater than 3";
        }
    }

    private class BoardUserScoreNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String promptMessage = ChatFormatter.formatSuccessMessage(ChatColor.GRAY + "Please choose a suffix for the score. For example, if you are creating a money board it could be '$'");
            return promptMessage;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("boardUserScoreName", input);
            return new CreateBoardPrompt();
        }

    }

    private class CreateBoardPrompt extends MessagePrompt {

        public String getPromptText(ConversationContext context) {
            String boardId = (String) context.getSessionData("boardId");
            String boardName = (String) context.getSessionData("boardName");
            String boardDescription = (String) context.getSessionData("boardDescription");
            int maxShownPlayers = (int) context.getSessionData("boardMaxShownPlayer");
            String userScoreName = (String) context.getSessionData("boardUserScoreName");

            BoardService boardService = ERBoardService.getInstance();
            boardService.createBoard(boardId,boardName,boardDescription,maxShownPlayers,userScoreName, false, true);

            Player player = (Player) context.getForWhom();
            player.playSound( player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1 );

            return ChatFormatter.formatSuccessMessage("Board " + ChatColor.GOLD + boardId + ChatColor.GREEN + " successfully created!");
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }

    }

}

