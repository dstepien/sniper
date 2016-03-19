package pl.dawidstepien.sniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main implements AuctionEventListener {

  public static final String STATUS_JOINING = "Joining";

  public static final String STATUS_LOST = "Lost";

  public static final String STATUS_BIDDING = "Bidding";

  public static final String SNIPER_STATUS_NAME = "SNIPER_STATUS_NAME";

  public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Event: JOIN;";

  public static final String BID_COMMAND_FORMAT = "";

  private MainWindow ui;

  private static final int ARG_HOSTNAME = 0;

  private static final int ARG_USERNAME = 1;

  private static final int ARG_PASSWORD = 2;

  private static final int ARG_ITEM_ID = 3;

  public static final String AUCTION_RESOURCE = "Auction";

  public static final String ITEM_ID_AS_LOGIN = "auction-%s";

  public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

  @SuppressWarnings("unused")
  private Chat notToBeGCd;

  public Main() throws Exception {
    startUserInterface();
  }

  private void startUserInterface() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        ui = new MainWindow();
      }
    });
  }

  public static void main(String... args) throws Exception {
    Main main = new Main();
    main.joinAuction(connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]), args[ARG_ITEM_ID]);
  }

  private void joinAuction(XMPPConnection connection, String itemId) throws XMPPException {
    disconnectWhenUICloses(connection);
    Chat chat = connection.getChatManager().createChat(
      auctionId(itemId, connection),
      new AuctionMessageTranslator(this)
    );
    this.notToBeGCd = chat;
    chat.sendMessage(JOIN_COMMAND_FORMAT);
  }

  private void disconnectWhenUICloses(XMPPConnection connection) {
    ui.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        connection.disconnect();
      }
    });
  }

  private static XMPPConnection connection(String hostname, String username, String password) throws XMPPException {
    XMPPConnection connection = new XMPPConnection(hostname);
    connection.connect();
    connection.login(username, password, AUCTION_RESOURCE);
    return connection;
  }

  private static String auctionId(String itemId, XMPPConnection connection) {
    return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }

  @Override
  public void auctionClosed() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        ui.showsStatus(Main.STATUS_LOST);
      }
    });
  }

  @Override
  public void currentPrice(int price, int increment) {

  }
}
