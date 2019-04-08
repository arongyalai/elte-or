package musicbox.command;

import musicbox.net.action.Change;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "change", sortOptions = false,
	header = {"", ""},
	mixinStandardHelpOptions = true,
	descriptionHeading = "@|bold %nDescription|@:%n", description = {"", "Changes the playing music's tempo and or transposition",},
	optionListHeading = "@|bold %nOptions|@:%n", footer = {"", "Author: AlexAegis"})
public class ChangeCommand implements Runnable {

	@ParentCommand
	private ClientCommands parent;

	@CommandLine.Parameters(paramLabel = "<no>", index = "0", arity = "0", description = "The playing songs number. If empty, then all the songs playing towards the client is affected.")
	private Integer no;
	@CommandLine.Option(names = {"-tr", "--transpose"}, paramLabel = "<transpose>", description = "The transposition you with to change the song's playback to.")
	private Integer transpose;
	@CommandLine.Option(names = {"-t", "--tempo"}, paramLabel = "<tempo>", description = "The tempo you wish to change the song's playback to.")
	private Integer tempo;

	@Override
	public void run() {
		// Verify the instructions rules: REP cant got further back than notes are behind it. Also, should be one note and one number.
		new Change(parent.getClient().getConnectionSubject().getValue(), no, tempo, transpose).blockingSubscribe();
		parent.getOut().println("Changing...");
		parent.getOut().flush();
	}

}