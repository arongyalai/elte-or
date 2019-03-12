package battleships.net.action;

import battleships.Client;
import battleships.Server;
import battleships.model.Admiral;
import battleships.net.Connection;
import battleships.net.result.ReadyResult;
import battleships.state.Mode;
import battleships.state.Phase;

import java.io.Serializable;
import java.util.Optional;
import java.util.logging.Logger;

public class Ready extends Request<ReadyResult> implements Serializable {

	private static final long serialVersionUID = -8873647819519180472L;
	private Boolean ready;
	private String who;

	public Ready(String requester, String who, Boolean ready) {
		super(requester);
		this.ready = ready;
		this.who = who;
	}

	public Boolean isReady() {
		return ready;
	}

	/**
	 * @return the who
	 */
	public String getWho() {
		return who;
	}

	@Override
	public Optional<ReadyResult> response(Connection connection, Optional<Server> answerFromServer,
			Optional<Client> answerFromClient) {
		System.out.println("RESPONDING TO A READY");
		if (answerFromServer.isPresent()) {
			return answerFromServer.map(server -> {
				var reqAdm = server.getTable().getAdmiral(getRequester());
				var whoAdm = server.getTable().getAdmiral(getWho());
				System.out.println(" READY REQQ reqAdm: " + reqAdm + " whoAdm: " + whoAdm + " isReady() " + isReady());
				whoAdm.setReady(isReady());

				// This guy is now ready, lets tell everyone else
				server.getEveryOtherConnectedAdmiralsExcept(reqAdm).forEach(conn -> {
					conn.send(new Ready(conn.getAdmiral().getName(), whoAdm.getName(), whoAdm.isReady()))
							.subscribe(ack -> {
								Logger.getGlobal().info("Nofified about readyness, here's the acknowledgement: " + ack);
							});
				});

				// State propagation logic. If there are at least 2 players and everyone is ready then notify them that the match started
				if (server.isAtLeastNPlayers(2) && server.isEveryOneOnTheSamePhase(Phase.READY)) {
					Logger.getGlobal().info("Everybody ready!");
					server.setPhase(Phase.GAME);
					server.getEveryConnectedAdmirals().forEach(conn -> {
						Admiral firstTurnAdmiral = server.getCurrentAdmiral();

						/* TODO: BATTLE ROYALE
						if(Mode.ROYALE.equals(server.getMode())) {
							firstTurnAdmiral = conn.getAdmiral(); // Free for all, bitches :)
						}*/
						conn.send(new Turn(conn.getAdmiral().getName(), firstTurnAdmiral.getName())).subscribe(ack -> {
							Logger.getGlobal().info("Sent turn data, got ack: " + ack);
						});
					});
				}


				return new ReadyResult(getRequester(), ready);
			});
		} else {
			return answerFromClient.map(client -> {
				System.out.println("GOT A GUY WHO IS READY OR NOT!!! req: " + getRequester() + " who: " + getWho()
						+ "isReady " + ready);
				client.getGame().getAdmiral().getKnowledge().get(getWho()).setReady(isReady());
				return new ReadyResult(getRequester(), isReady());
			});
		}

	}


}
